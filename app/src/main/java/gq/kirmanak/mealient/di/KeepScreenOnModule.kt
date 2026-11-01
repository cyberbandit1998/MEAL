package gq.kirmanak.mealient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gq.kirmanak.mealient.data.screen.KeepScreenOnStorage
import gq.kirmanak.mealient.data.screen.KeepScreenOnStorageImpl

@Module
@InstallIn(SingletonComponent::class)
interface KeepScreenOnModule {

    @Binds
    fun bindKeepScreenOnStorage(keepScreenOnStorageImpl: KeepScreenOnStorageImpl): KeepScreenOnStorage
}
