package gq.kirmanak.mealient.ui.recipes.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gq.kirmanak.mealient.data.recipes.RecipeRepo
import gq.kirmanak.mealient.data.recipes.impl.RecipeImageUrlProvider
import gq.kirmanak.mealient.data.screen.KeepScreenOnStorage
import gq.kirmanak.mealient.database.recipe.entity.RecipeIngredientEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeInstructionEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeWithSummaryAndIngredientsAndInstructions
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.ui.navArgs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecipeInfoViewModel @Inject constructor(
    private val recipeRepo: RecipeRepo,
    private val logger: Logger,
    private val recipeImageUrlProvider: RecipeImageUrlProvider,
    private val keepScreenOnStorage: KeepScreenOnStorage,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.navArgs<RecipeScreenArgs>()

    private val _uiState = flow {
        logger.v { "Initializing UI state with args = $args" }
        val recipeInfo = recipeRepo.loadRecipeInfo(args.recipeId)
        logger.v { "Loaded recipe info = $recipeInfo" }
        val slug = recipeInfo?.recipeSummaryEntity?.imageId
        val imageUrl = slug?.let { recipeImageUrlProvider.generateImageUrl(slug) }
        val state = recipeInfo?.let { entity ->
            RecipeInfoUiState(
                showIngredients = entity.recipeIngredients.isNotEmpty(),
                showInstructions = entity.recipeInstructions.isNotEmpty(),
                summaryEntity = entity.recipeSummaryEntity,
                recipeIngredients = entity.recipeIngredients,
                recipeInstructions = associateInstructionsToIngredients(entity),
                title = entity.recipeSummaryEntity.name,
                description = entity.recipeSummaryEntity.description,
                imageUrl = imageUrl,
            )
        } ?: RecipeInfoUiState()
        emit(state)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RecipeInfoUiState())


    val uiState: StateFlow<RecipeInfoUiState> = _uiState

    val keepScreenOn: StateFlow<Boolean> = keepScreenOnStorage.isKeepScreenOnFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, KeepScreenOnStorage.DEFAULT)

    fun onKeepScreenOnChange(keepScreenOn: Boolean) {
        logger.v { "onKeepScreenOnChange() called with: keepScreenOn = $keepScreenOn" }
        viewModelScope.launch { keepScreenOnStorage.setKeepScreenOn(keepScreenOn) }
    }
}

private fun associateInstructionsToIngredients(
    recipe: RecipeWithSummaryAndIngredientsAndInstructions,
): Map<RecipeInstructionEntity, List<RecipeIngredientEntity>> {
    return recipe.recipeInstructions.associateWith { instruction ->
        recipe.recipeIngredientToInstructionEntity
            .filter { it.instructionId == instruction.id }
            .flatMap { mapping ->
                recipe.recipeIngredients.filter { ingredient ->
                    ingredient.id == mapping.ingredientId
                }
            }
    }
}
