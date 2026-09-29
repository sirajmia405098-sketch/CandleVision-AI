package com.example

import com.example.data.MarketSimulator
import com.example.data.model.AnalysisResult
import com.example.data.model.ConfidenceLevel
import com.example.data.model.MarketPattern
import com.example.data.model.SignalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun parseGeminiResponse_bullishHammer_returnsCallSignal() {
        val sampleBengali = "সাপোর্ট লেভেলে হাই-কনফিডেন্স বুলিশ হ্যামার পাওয়া গেছে, ওপরে (UP) ট্রেড নেওয়া যেতে পারে।"
        val result = AnalysisResult.parseFromGeminiResponse(sampleBengali)

        assertEquals(SignalType.CALL, result.signal)
        assertEquals(ConfidenceLevel.HIGH, result.confidence)
        assertTrue(result.patternDetected.contains("হ্যামার"))
    }

    @Test
    fun parseGeminiResponse_bearishEngulfing_returnsPutSignal() {
        val sampleBengali = "রেজিস্ট্যান্স লেভেলে স্ট্রং বেয়ারিশ এনগালফিং রিজেকশন, নিচে (DOWN) পুট নেওয়ার সুযোগ।"
        val result = AnalysisResult.parseFromGeminiResponse(sampleBengali)

        assertEquals(SignalType.PUT, result.signal)
    }

    @Test
    fun parseGeminiResponse_consolidation_returnsAvoidSignal() {
        val sampleBengali = "মার্কেট বর্তমানে রেঞ্জিং ও ইনডিসিশন, অপেক্ষা করুন, ট্রেড এভয়েড করুন।"
        val result = AnalysisResult.parseFromGeminiResponse(sampleBengali)

        assertEquals(SignalType.AVOID, result.signal)
    }

    @Test
    fun marketSimulator_generatesValidScenario() {
        val scenario = MarketSimulator.generateScenario(MarketPattern.BULLISH_HAMMER)
        val candles = scenario.first
        assertTrue(candles.isNotEmpty())
        assertTrue(candles.size >= 10)

        val lastCandle = candles.last()
        assertTrue(lastCandle.low <= scenario.second.first + 0.0001f) // Support touched
    }
}
