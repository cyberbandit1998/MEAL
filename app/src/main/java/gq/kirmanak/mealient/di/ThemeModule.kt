package gq.kirmanak.mealient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gq.kirmanak.mealient.data.theme.ThemeStorage
import gq.kirmanak.mealient.data.theme.ThemeStorageImpl

@Module
@InstallIn(SingletonComponent::class)
interface ThemeModule {

    @Binds
    fun bindThemeStorage(themeStorageImpl: ThemeStorageImpl): ThemeStorage
}
