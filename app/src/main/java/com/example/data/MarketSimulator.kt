package com.example.data

import com.example.data.model.Candlestick
import com.example.data.model.MarketPattern
import kotlin.random.Random

object MarketSimulator {
    fun generateScenario(pattern: MarketPattern): Pair<List<Candlestick>, Pair<Float, Float>> {
        val candles = mutableListOf<Candlestick>()
        var currentPrice = 1.0850f
        val timeStep = 60_000L // 1 minute candle
        var time = System.currentTimeMillis() - (15 * timeStep)

        // Generate baseline trend candles leading up to the pattern
        when (pattern) {
            MarketPattern.BULLISH_HAMMER -> {
                // Downtrend leading into strong support
                val supportLevel = 1.0800f
                val resistanceLevel = 1.0890f
                currentPrice = 1.0870f

                for (i in 0 until 12) {
                    val drop = Random.nextFloat() * 0.0006f + 0.0001f
                    val o = currentPrice
                    val c = o - drop
                    val h = o + Random.nextFloat() * 0.0002f
                    val l = c - Random.nextFloat() * 0.0002f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                // Final far-right candle: Bullish Hammer at Support!
                // Small body at top, very long lower wick touching support level
                val hammerOpen = supportLevel + 0.0003f
                val hammerClose = hammerOpen + 0.00025f // green body
                val hammerHigh = hammerClose + 0.0001f
                val hammerLow = supportLevel // touches support line!
                candles.add(Candlestick(hammerOpen, hammerHigh, hammerLow, hammerClose, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }

            MarketPattern.BEARISH_ENGULFING -> {
                // Uptrend leading into resistance
                val supportLevel = 1.0810f
                val resistanceLevel = 1.0900f
                currentPrice = 1.0830f

                for (i in 0 until 11) {
                    val gain = Random.nextFloat() * 0.0005f + 0.0002f
                    val o = currentPrice
                    val c = o + gain
                    val h = c + Random.nextFloat() * 0.0002f
                    val l = o - Random.nextFloat() * 0.0002f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                // Preceding small green candle right at resistance
                val prevO = currentPrice
                val prevC = prevO + 0.0003f
                val prevH = prevC + 0.0001f
                val prevL = prevO - 0.0001f
                candles.add(Candlestick(prevO, prevH, prevL, prevC, time))
                time += timeStep

                // Far-right candle: Giant Bearish Engulfing candle opening above prev close and plunging down
                val engOpen = resistanceLevel
                val engClose = prevO - 0.0005f
                val engHigh = resistanceLevel + 0.0001f
                val engLow = engClose - 0.0002f
                candles.add(Candlestick(engOpen, engHigh, engLow, engClose, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }

            MarketPattern.DOJI_CONSOLIDATION, MarketPattern.RANGING_MARKET -> {
                // Flat choppy market
                val supportLevel = 1.0830f
                val resistanceLevel = 1.0860f
                currentPrice = 1.0845f

                for (i in 0 until 12) {
                    val isGreen = i % 2 == 0
                    val delta = (Random.nextFloat() * 0.0003f) * (if (isGreen) 1 else -1)
                    val o = currentPrice
                    val c = (o + delta).coerceIn(supportLevel + 0.0003f, resistanceLevel - 0.0003f)
                    val h = maxOf(o, c) + Random.nextFloat() * 0.0002f
                    val l = minOf(o, c) - Random.nextFloat() * 0.0002f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                // Far-right candle: Doji (open == close, neutral wicks in middle of range)
                val dojiO = currentPrice
                val dojiC = dojiO + 0.00002f
                val dojiH = dojiO + 0.0004f
                val dojiL = dojiO - 0.0004f
                candles.add(Candlestick(dojiO, dojiH, dojiL, dojiC, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }

            MarketPattern.SHOOTING_STAR -> {
                val supportLevel = 1.0820f
                val resistanceLevel = 1.0895f
                currentPrice = 1.0840f

                for (i in 0 until 12) {
                    val gain = Random.nextFloat() * 0.0004f + 0.0001f
                    val o = currentPrice
                    val c = o + gain
                    val h = c + Random.nextFloat() * 0.00015f
                    val l = o - Random.nextFloat() * 0.00015f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                // Far right: Shooting Star (long upper wick rejected from resistance level, tiny body at bottom)
                val ssOpen = resistanceLevel - 0.0004f
                val ssClose = ssOpen - 0.0002f
                val ssHigh = resistanceLevel
                val ssLow = ssClose - 0.00005f
                candles.add(Candlestick(ssOpen, ssHigh, ssLow, ssClose, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }

            MarketPattern.PINBAR_REJECTION_UP -> {
                val supportLevel = 1.0815f
                val resistanceLevel = 1.0880f
                currentPrice = 1.0860f

                for (i in 0 until 12) {
                    val drop = Random.nextFloat() * 0.0004f + 0.0001f
                    val o = currentPrice
                    val c = o - drop
                    val h = o + Random.nextFloat() * 0.00015f
                    val l = c - Random.nextFloat() * 0.00015f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                val pbOpen = supportLevel + 0.0004f
                val pbClose = pbOpen + 0.0002f
                val pbHigh = pbClose + 0.0001f
                val pbLow = supportLevel - 0.0001f
                candles.add(Candlestick(pbOpen, pbHigh, pbLow, pbClose, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }

            MarketPattern.STRONG_UPTREND -> {
                val supportLevel = 1.0800f
                val resistanceLevel = 1.0920f
                currentPrice = 1.0810f

                for (i in 0 until 12) {
                    val gain = Random.nextFloat() * 0.0006f + 0.0002f
                    val o = currentPrice
                    val c = o + gain
                    val h = c + Random.nextFloat() * 0.0002f
                    val l = o - Random.nextFloat() * 0.0001f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                // Giant bullish continuation marubozu
                val o = currentPrice
                val c = o + 0.0008f
                val h = c + 0.00005f
                val l = o - 0.00005f
                candles.add(Candlestick(o, h, l, c, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }

            MarketPattern.STRONG_DOWNTREND -> {
                val supportLevel = 1.0780f
                val resistanceLevel = 1.0900f
                currentPrice = 1.0890f

                for (i in 0 until 12) {
                    val drop = Random.nextFloat() * 0.0006f + 0.0002f
                    val o = currentPrice
                    val c = o - drop
                    val h = o + Random.nextFloat() * 0.0001f
                    val l = c - Random.nextFloat() * 0.0002f
                    candles.add(Candlestick(o, h, l, c, time))
                    currentPrice = c
                    time += timeStep
                }

                val o = currentPrice
                val c = o - 0.0008f
                val h = o + 0.00005f
                val l = c - 0.00005f
                candles.add(Candlestick(o, h, l, c, time))

                return Pair(candles, Pair(supportLevel, resistanceLevel))
            }
        }
    }

    /**
     * Ticks the latest candle in real-time
     */
    fun tickCandle(lastCandle: Candlestick): Candlestick {
        val delta = (Random.nextFloat() - 0.48f) * 0.00015f
        val newClose = lastCandle.close + delta
        val newHigh = maxOf(lastCandle.high, newClose)
        val newLow = minOf(lastCandle.low, newClose)
        return lastCandle.copy(
            close = newClose,
            high = newHigh,
            low = newLow,
            volume = lastCandle.volume + 5f
        )
    }
}
