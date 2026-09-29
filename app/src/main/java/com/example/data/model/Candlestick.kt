package com.example.data.model

data class Candlestick(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val volume: Float = 100f
) {
    val isBullish: Boolean get() = close >= open
    val bodyTop: Float get() = maxOf(open, close)
    val bodyBottom: Float get() = minOf(open, close)
    val bodySize: Float get() = bodyTop - bodyBottom
    val upperWick: Float get() = high - bodyTop
    val lowerWick: Float get() = bodyBottom - low
    val totalRange: Float get() = high - low
}

enum class MarketPattern(val displayName: String, val bengaliName: String, val expectedSignal: SignalType) {
    BULLISH_HAMMER("Bullish Hammer at Support", "বুলিশ হ্যামার (সাপোর্ট)", SignalType.CALL),
    BEARISH_ENGULFING("Bearish Engulfing at Resistance", "বেয়ারিশ এনগালফিং (রেজিস্ট্যান্স)", SignalType.PUT),
    DOJI_CONSOLIDATION("Doji / Consolidation Range", "দোজি / কনসোলিডেশন", SignalType.AVOID),
    PINBAR_REJECTION_UP("Bullish Pinbar Rejection", "বুলিশ পিনবার রিজেকশন", SignalType.CALL),
    SHOOTING_STAR("Shooting Star Reversal", "শুটিং স্টার রিভার্সাল", SignalType.PUT),
    STRONG_UPTREND("Strong Momentum Uptrend", "স্ট্রং আপট্রেন্ড", SignalType.CALL),
    STRONG_DOWNTREND("Strong Momentum Downtrend", "স্ট্রং ডাউনট্রেন্ড", SignalType.PUT),
    RANGING_MARKET("Ranging / Choppy Market", "রেঞ্জিং মার্কেট (ঝুঁকিপূর্ণ)", SignalType.AVOID)
}
