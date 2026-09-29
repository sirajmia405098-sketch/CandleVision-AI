package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TradeOutcome {
    PENDING,
    WIN,
    LOSS,
    TIE,
    SKIPPED
}

@Entity(tableName = "trade_history")
data class TradeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val signal: String, // CALL, PUT, AVOID
    val confidence: String, // HIGH, MEDIUM, LOW
    val bengaliAnalysis: String,
    val pattern: String,
    val trend: String,
    val outcome: String = TradeOutcome.PENDING.name, // WIN, LOSS, TIE, SKIPPED
    val latencyMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null
)
