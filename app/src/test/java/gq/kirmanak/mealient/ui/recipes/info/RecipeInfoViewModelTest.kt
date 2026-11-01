package gq.kirmanak.mealient.ui.recipes.info

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import gq.kirmanak.mealient.data.recipes.RecipeRepo
import gq.kirmanak.mealient.data.recipes.impl.RecipeImageUrlProvider
import gq.kirmanak.mealient.data.screen.KeepScreenOnStorage
import gq.kirmanak.mealient.database.BAKE_CAKE_RECIPE_INSTRUCTION_ENTITY
import gq.kirmanak.mealient.database.CAKE_BREAD_RECIPE_INGREDIENT_ENTITY
import gq.kirmanak.mealient.database.CAKE_SUGAR_RECIPE_INGREDIENT_ENTITY
import gq.kirmanak.mealient.database.FULL_CAKE_INFO_ENTITY
import gq.kirmanak.mealient.database.MIX_CAKE_RECIPE_INSTRUCTION_ENTITY
import gq.kirmanak.mealient.test.BaseUnitTest
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RecipeInfoViewModelTest : BaseUnitTest() {

    @MockK
    lateinit var recipeRepo: RecipeRepo

    @MockK
    lateinit var recipeImageUrlProvider: RecipeImageUrlProvider

    @MockK
    lateinit var keepScreenOnStorage: KeepScreenOnStorage

    @Before
    override fun setUp() {
        super.setUp()
        every { keepScreenOnStorage.isKeepScreenOnFlow } returns flowOf(true)
        coEvery { keepScreenOnStorage.setKeepScreenOn(any()) } just Runs
    }

    @Test
    fun `when recipe isn't found then UI state is empty`() = runTest {
        coEvery { recipeRepo.loadRecipeInfo(eq(RECIPE_ID)) } returns null
        val uiState = createSubject().uiState.first()
        assertThat(uiState).isEqualTo(RecipeInfoUiState())
    }

    @Test
    fun `when recipe is found then UI state has data`() = runTest {
        val returnedEntity = FULL_CAKE_INFO_ENTITY.copy(
            recipeIngredients = FULL_CAKE_INFO_ENTITY.recipeIngredients
        )
        coEvery { recipeRepo.loadRecipeInfo(eq(RECIPE_ID)) } returns returnedEntity
        coEvery { recipeImageUrlProvider.generateImageUrl(eq("1")) } returns "imageUrl"
        val expected = RecipeInfoUiState(
            showIngredients = true,
            showInstructions = true,
            summaryEntity = FULL_CAKE_INFO_ENTITY.recipeSummaryEntity,
            recipeIngredients = FULL_CAKE_INFO_ENTITY.recipeIngredients,
            recipeInstructions = mapOf(
                MIX_CAKE_RECIPE_INSTRUCTION_ENTITY to listOf(
                    CAKE_SUGAR_RECIPE_INGREDIENT_ENTITY,
                    CAKE_BREAD_RECIPE_INGREDIENT_ENTITY,
                ),
                BAKE_CAKE_RECIPE_INSTRUCTION_ENTITY to emptyList(),
            ),
            title = FULL_CAKE_INFO_ENTITY.recipeSummaryEntity.name,
            description = FULL_CAKE_INFO_ENTITY.recipeSummaryEntity.description,
            imageUrl = "imageUrl",
        )
        val actual = createSubject().uiState.first()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `when nothing is stored then the screen is kept on`() = runTest {
        assertThat(createSubject().keepScreenOn.first()).isTrue()
    }

    @Test
    fun `when keeping the screen on is stored as off then it is off`() = runTest {
        every { keepScreenOnStorage.isKeepScreenOnFlow } returns flowOf(false)
        assertThat(createSubject().keepScreenOn.first { !it }).isFalse()
    }

    @Test
    fun `when keep screen on is switched off then it is stored`() = runTest {
        createSubject().onKeepScreenOnChange(false)
        coVerify { keepScreenOnStorage.setKeepScreenOn(false) }
    }

    private fun createSubject(): RecipeInfoViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf("recipeId" to RECIPE_ID)

        )
        return RecipeInfoViewModel(
            recipeRepo,
            logger,
            recipeImageUrlProvider,
            keepScreenOnStorage,
            savedStateHandle,
        )
    }

    companion object {
        private const val RECIPE_ID = "1"
    }
}