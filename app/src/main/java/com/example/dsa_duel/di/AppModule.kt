package com.example.dsa_duel.di

import android.content.Context
import androidx.room.Room
import com.example.dsa_duel.data.AppDatabase
import com.example.dsa_duel.data.PlayerStatsDao
import com.example.dsa_duel.data.QuestionDao
import com.example.dsa_duel.data.TopicMasteryDao
import com.example.dsa_duel.repositories.DuelRepository
import com.example.dsa_duel.repositories.QuestionRepository
import com.example.dsa_duel.repositories.StatsRepository
 import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FILE: di/AppModule.kt
 *
 * Hilt Dependency Injection Module.
 */
@Module
@InstallIn(SingletonComponent::class)  // all deps live as long as the app lives
object AppModule {

    // ── FIREBASE ──────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // ── DATABASE ──────────────────────────────────────────────────

    /**
     * Provides the single Room database instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dsa_duel_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // ── DAOs ──────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun providePlayerStatsDao(database: AppDatabase): PlayerStatsDao {
        return database.playerStatsDao()
    }

    @Provides
    @Singleton
    fun provideTopicMasteryDao(database: AppDatabase): TopicMasteryDao {
        return database.topicMasteryDao()
    }

    // ── REPOSITORIES ──────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideQuestionRepository(
        questionDao: QuestionDao
    ): QuestionRepository {
        return QuestionRepository(questionDao)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(
        playerStatsDao: PlayerStatsDao,
        topicMasteryDao: TopicMasteryDao
    ): StatsRepository {
        return StatsRepository(playerStatsDao, topicMasteryDao)
    }

    @Provides
    @Singleton
    fun provideDuelRepository(
        questionDao: QuestionDao,
        playerStatsDao: PlayerStatsDao,
        topicMasteryDao: TopicMasteryDao,
        statsRepository: StatsRepository
    ): DuelRepository {
        return DuelRepository(
            questionDao      = questionDao,
            playerStatsDao   = playerStatsDao,
            topicMasteryDao  = topicMasteryDao,
            statsRepository  = statsRepository
        )
    }
}
