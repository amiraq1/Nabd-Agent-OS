package com.nabd.ai.local.memory.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nabd.ai.local.memory.MemoryEntity
import com.nabd.ai.local.embedding.db.MemoryEmbeddingDao
import com.nabd.ai.local.embedding.db.MemoryEmbeddingEntity
import com.nabd.ai.local.rag.db.*

@Database(
    entities = [
        MemoryEntity::class, 
        MemoryEmbeddingEntity::class,
        KnowledgeDocumentEntity::class,
        KnowledgeChunkEntity::class,
        KnowledgeEmbeddingEntity::class
    ], 
    version = 3
)
@TypeConverters(Converters::class)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun memoryEmbeddingDao(): MemoryEmbeddingDao
    abstract fun knowledgeDao(): KnowledgeDao

    companion object {
        @Volatile
        private var INSTANCE: MemoryDatabase? = null

        fun getDatabase(context: Context): MemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemoryDatabase::class.java,
                    "memory_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
