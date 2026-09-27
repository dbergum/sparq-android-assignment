package com.sparq.pokedex.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "pokemon")
data class PokemonEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

@Entity(tableName = "page_key")
data class PageKeyEntity(@PrimaryKey val id: Int = 0, val nextUrl: String?)

@Dao
interface PokemonDao
{
    @Query("SELECT * FROM pokemon WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun pageAfter(afterId: Long, limit: Int): List<PokemonEntity>

    @Query("SELECT * FROM page_key WHERE id = 0")
    suspend fun pageKey(): PageKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(pokemon: List<PokemonEntity>, key: PageKeyEntity)
}

@Database(entities = [PokemonEntity::class, PageKeyEntity::class], version = 1, exportSchema = false)
abstract class PokemonDatabase : RoomDatabase()
{
    abstract fun pokemonDao(): PokemonDao
}
