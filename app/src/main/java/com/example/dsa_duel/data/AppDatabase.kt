package com.example.dsa_duel.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FILE: data/AppDatabase.kt
 *
 * The central Room database class for DSA Duel.
 *
 * This file does 4 things:
 *   1. Registers all 3 entities → tells Room which tables to create
 *   2. Registers all 3 DAOs    → provides access to database operations
 *   3. Builds the database     → creates the SQLite file on device
 *   4. Seeds initial data      → inserts default topics + questions
 *                                on very first launch
 *
 * VERSIONING:
 * version = 1 → first release
 * If you ever ADD a column or table in the future,
 * increment version to 2 and provide a Migration.
 * Never change entities without bumping the version —
 * it will crash on user devices that already have version 1.
 *
 * DATABASE FILE LOCATION ON DEVICE:
 * /data/data/com.example.dsa_duel/databases/dsa_duel_database.db
 * (only accessible with root or Android Studio Device Explorer)
 */
@Database(
    entities = [
        QuestionEntity::class,      // → "questions" table
        PlayerStatsEntity::class,   // → "player_stats" table
        TopicMasteryEntity::class,  // → "topic_mastery" table
    ],
    version = 1,
    exportSchema = false  // set true in production to track schema history
)
abstract class AppDatabase : RoomDatabase() {

    // ── DAO accessors ─────────────────────────────────────────────
    // Room auto-generates implementations of these.
    // You never call these directly — Hilt injects the DAOs
    // wherever they are needed.

    abstract fun questionDao(): QuestionDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun topicMasteryDao(): TopicMasteryDao

    // ── Singleton companion ───────────────────────────────────────
    // We use a singleton pattern to ensure only ONE database
    // instance exists across the entire app lifetime.
    // Multiple instances = multiple connections = potential corruption.

    companion object {

        // @Volatile means every thread always sees the latest value.
        // Without this, two threads could each create their own instance.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         * Creates it on first call, returns existing on subsequent calls.
         *
         * NOTE: In a Hilt project, you don't call this directly.
         * Hilt calls this once via AppModule.kt and then injects
         * the instance wherever needed. This is kept here for
         * reference and for non-Hilt testing scenarios.
         */
        fun getInstance(context: Context): AppDatabase {
            // Double-checked locking pattern for thread safety
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "dsa_duel_database"        // SQLite file name on device
            )
                // ── Callback: runs on first database creation ─────
                // This is where we seed default topics and questions.
                // addCallback fires ONCE — when the DB file is first created.
                // After that, every app restart skips this block entirely.
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Run seeding on IO thread — never on main thread
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedDatabase(database)
                            }
                        }
                    }
                })
                // ── Migration strategy ────────────────────────────
                // fallbackToDestructiveMigration = if version increases
                // and no migration is provided, wipe and recreate DB.
                // ONLY safe during development. In production,
                // provide proper Migration objects instead.
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Seeds the database on first launch.
         * Inserts:
         *   1. Default topic mastery rows (9 topics)
         *   2. Starter question bank (inserted from QuestionSeeder)
         *
         * This runs ONCE ever — the addCallback onCreate only fires
         * when the database file is first created on the device.
         */
        private suspend fun seedDatabase(database: AppDatabase) {
            // Seed 9 default topic rows
            // DefaultTopics.all is defined in TopicMasteryEntity.kt
            database.topicMasteryDao().insertDefaultTopics(DefaultTopics.all)

            // Seed the question bank
            // QuestionSeeder.questions is defined in QuestionSeeder.kt
            // (you will create this file separately with all 100+ questions)
            database.questionDao().insertQuestions(QuestionSeeder.questions)
        }
    }
}

/*
 * ── HOW ROOM USES THIS FILE ───────────────────────────────────────
 *
 * When your app launches for the first time:
 *
 * 1. Hilt calls AppModule.kt → provideDatabase()
 * 2. provideDatabase() calls buildDatabase()
 * 3. Room creates the SQLite file on the device
 * 4. addCallback.onCreate() fires
 * 5. seedDatabase() runs:
 *      - 9 topic rows inserted into topic_mastery
 *      - 100+ questions inserted into questions
 * 6. Database is ready — app continues to Home screen
 *
 * On every subsequent launch:
 * 1. Hilt calls AppModule.kt → provideDatabase()
 * 2. Room finds existing SQLite file → returns it immediately
 * 3. addCallback.onCreate() does NOT fire (already created)
 * 4. Database ready instantly — no seeding delay
 *
 * ── ENTITY → TABLE MAPPING ────────────────────────────────────────
 *
 * @Database(entities = [...]) tells Room:
 *   QuestionEntity      → CREATE TABLE questions (...)
 *   PlayerStatsEntity   → CREATE TABLE player_stats (...)
 *   TopicMasteryEntity  → CREATE TABLE topic_mastery (...)
 *
 * Room generates the CREATE TABLE SQL automatically from
 * your entity field names and types. You never write DDL SQL.
 *
 * ── VERSION MIGRATION EXAMPLE (future reference) ─────────────────
 *
 * If you add a new column "totalDraws" to PlayerStatsEntity:
 *
 * Step 1: Add field to PlayerStatsEntity
 *   val totalDraws: Int = 0
 *
 * Step 2: Bump version in @Database
 *   version = 2
 *
 * Step 3: Provide migration in AppModule.kt
 *   val MIGRATION_1_2 = object : Migration(1, 2) {
 *       override fun migrate(database: SupportSQLiteDatabase) {
 *           database.execSQL(
 *               "ALTER TABLE player_stats ADD COLUMN totalDraws INTEGER NOT NULL DEFAULT 0"
 *           )
 *       }
 *   }
 *
 * Step 4: Add migration to builder
 *   .addMigrations(MIGRATION_1_2)
 *
 * Without this, users who already have version 1 installed
 * will get a crash when they update to version 2.
 *
 * ── DATABASE INSPECTION DURING DEVELOPMENT ───────────────────────
 *
 * To inspect the actual SQLite file during development:
 *   Android Studio → View → Tool Windows → App Inspection
 *   → Database Inspector → dsa_duel_database
 *
 * You can run SQL queries directly and see all rows in real time.
 * Extremely useful for debugging questions not appearing,
 * mastery scores not updating, etc.
 */