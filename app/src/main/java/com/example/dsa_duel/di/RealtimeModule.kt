package com.example.dsa_duel.di

import com.example.dsa_duel.repositories.RealtimeDuelRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    @Provides
    @Singleton
    fun provideRealtimeDuelRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): RealtimeDuelRepository = RealtimeDuelRepository(firestore, auth)
}
