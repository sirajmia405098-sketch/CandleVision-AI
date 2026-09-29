package com.example.data.local

import kotlinx.coroutines.flow.Flow

class TradeRepository(private val tradeDao: TradeDao) {
    val allTrades: Flow<List<TradeHistoryEntity>> = tradeDao.getAllTrades()
    val totalCount: Flow<Int> = tradeDao.getTotalCount()
    val winCount: Flow<Int> = tradeDao.getWinCount()
    val lossCount: Flow<Int> = tradeDao.getLossCount()

    fun getRecentTrades(limit: Int): Flow<List<TradeHistoryEntity>> = tradeDao.getRecentTrades(limit)

    suspend fun insert(trade: TradeHistoryEntity): Long = tradeDao.insertTrade(trade)

    suspend fun updateOutcome(id: Long, outcome: TradeOutcome) {
        tradeDao.updateOutcome(id, outcome.name)
    }

    suspend fun delete(id: Long) = tradeDao.deleteTradeById(id)

    suspend fun clearAll() = tradeDao.clearAll()
}
