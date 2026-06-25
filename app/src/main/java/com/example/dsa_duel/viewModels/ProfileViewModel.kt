package com.example.dsa_duel.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsa_duel.data.PlayerStatsEntity
import com.example.dsa_duel.repositories.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    val playerStats: StateFlow<PlayerStatsEntity?> = statsRepository.observePlayerStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun resetProgress() {
        viewModelScope.launch {
            val current = statsRepository.getPlayerStats()
            if (current != null) {
                statsRepository.resetAllProgress(current.displayName, current.email)
            }
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
