package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeHistoryEntity>>

    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTrades(limit: Int): Flow<List<TradeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeHistoryEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeHistoryEntity)

    @Query("UPDATE trade_history SET outcome = :outcome WHERE id = :id")
    suspend fun updateOutcome(id: Long, outcome: String)

    @Query("DELETE FROM trade_history WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trade_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM trade_history")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trade_history WHERE outcome = 'WIN'")
    fun getWinCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trade_history WHERE outcome = 'LOSS'")
    fun getLossCount(): Flow<Int>
}
