package com.example.data.model

enum class SignalType(val label: String, val bengaliLabel: String, val action: String) {
    CALL("CALL", "ওপরে (UP)", "BUY / HIGHER"),
    PUT("PUT", "নিচে (DOWN)", "SELL / LOWER"),
    AVOID("AVOID", "ট্রেড এভয়েড", "WAIT / NO TRADE")
}

enum class ConfidenceLevel(val label: String, val bengaliLabel: String) {
    HIGH("HIGH", "হাই-কনফিডেন্স (উচ্চ)"),
    MEDIUM("MEDIUM", "মিডিয়াম-কনফিডেন্স (মাঝারি)"),
    LOW("LOW", "লো-কনফিডেন্স (নিম্ন)")
}

data class AnalysisResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawBengaliText: String,
    val signal: SignalType,
    val confidence: ConfidenceLevel,
    val patternDetected: String,
    val trend: String,
    val supportResistanceNotes: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null,
    val latencyMs: Long = 0
) {
    companion object {
        fun parseFromGeminiResponse(rawText: String, latencyMs: Long = 0, imageBase64: String? = null): AnalysisResult {
            val text = rawText.trim()
            val lower = text.lowercase()

            // Determine Signal Type
            val signal = when {
                lower.contains("এভয়েড") || lower.contains("অপেক্ষা") || lower.contains("avoid") || lower.contains("wait") -> {
                    SignalType.AVOID
                }
                lower.contains("ওপরে") || lower.contains("উপরে") || lower.contains("(up)") || lower.contains("up") || lower.contains("কল") || lower.contains("বুলিশ") -> {
                    SignalType.CALL
                }
                lower.contains("নিচে") || lower.contains("(down)") || lower.contains("down") || lower.contains("পুট") || lower.contains("বেয়ারিশ") -> {
                    SignalType.PUT
                }
                else -> SignalType.AVOID
            }

            // Determine Confidence
            val confidence = when {
                lower.contains("হাই") || lower.contains("high") || lower.contains("উচ্চ") -> ConfidenceLevel.HIGH
                lower.contains("লো") || lower.contains("low") || lower.contains("নিম্ন") -> ConfidenceLevel.LOW
                else -> ConfidenceLevel.MEDIUM
            }

            // Detect pattern keywords in Bengali/English
            val pattern = when {
                lower.contains("হ্যামার") || lower.contains("hammer") -> "হ্যামার (Hammer)"
                lower.contains("এনগালফিং") || lower.contains("engulfing") -> "এনগালফিং (Engulfing)"
                lower.contains("দোজি") || lower.contains("doji") -> "দোজি (Doji)"
                lower.contains("পিনবার") || lower.contains("pin bar") || lower.contains("pinbar") -> "পিনবার (Pin Bar)"
                lower.contains("মারুবোজু") || lower.contains("marubozu") -> "মারুবোজু (Marubozu)"
                lower.contains("শুটিং স্টার") || lower.contains("shooting star") -> "শুটিং স্টার (Shooting Star)"
                lower.contains("মর্নিং স্টার") || lower.contains("morning star") -> "মর্নিং স্টার (Morning Star)"
                lower.contains("ইভনিং স্টার") || lower.contains("evening star") -> "ইভনিং স্টার (Evening Star)"
                else -> "প্রাইস অ্যাকশন ক্যান্ডেল"
            }

            val trend = when {
                lower.contains("বুলিশ") || lower.contains("আপট্রেন্ড") || lower.contains("uptrend") -> "আপট্রেন্ড (Uptrend)"
                lower.contains("বেয়ারিশ") || lower.contains("ডাউনট্রেন্ড") || lower.contains("downtrend") -> "ডাউনট্রেন্ড (Downtrend)"
                else -> "কনসোলিডেশন / সাইডওয়ে"
            }

            val srNotes = when {
                lower.contains("সাপোর্ট") || lower.contains("support") -> "সাপোর্ট লেভেলে রিঅ্যাকশন"
                lower.contains("রেজিস্ট্যান্স") || lower.contains("resistance") -> "রেজিস্ট্যান্স লেভেলে রিজেকশন"
                else -> "লেভেল চেক করা হয়েছে"
            }

            return AnalysisResult(
                rawBengaliText = text,
                signal = signal,
                confidence = confidence,
                patternDetected = pattern,
                trend = trend,
                supportResistanceNotes = srNotes,
                timestamp = System.currentTimeMillis(),
                imageBase64 = imageBase64,
                latencyMs = latencyMs
            )
        }
    }
}
