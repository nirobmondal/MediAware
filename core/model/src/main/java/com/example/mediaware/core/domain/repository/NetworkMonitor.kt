package com.example.mediaware.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnlineStream: Flow<Boolean>
}
