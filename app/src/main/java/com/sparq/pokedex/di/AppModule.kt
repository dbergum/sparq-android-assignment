package com.sparq.pokedex.di

import android.content.Context
import androidx.room.Room
import com.sparq.pokedex.data.local.PokemonDao
import com.sparq.pokedex.data.local.PokemonDatabase
import com.sparq.pokedex.data.remote.PokemonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule
{
    @Provides
    @Singleton
    fun providePokemonApi(): PokemonApi
    {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(PokemonApi.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PokemonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokemonDatabase =
        Room.databaseBuilder(context, PokemonDatabase::class.java, "pokedex.db").build()

    @Provides
    fun providePokemonDao(db: PokemonDatabase): PokemonDao = db.pokemonDao()
}
