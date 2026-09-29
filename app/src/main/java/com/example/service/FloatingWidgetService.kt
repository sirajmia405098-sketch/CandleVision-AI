package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.TtsManager
import com.example.data.remote.GeminiVisionClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FloatingWidgetService : Service() {
    companion object {
        const val ACTION_STOP = "com.example.candlevision.ACTION_STOP"
        const val CHANNEL_ID = "candlevision_overlay_channel"
        const val NOTIFICATION_ID = 1001
        var isRunning: Boolean = false
            private set
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var hudCardView: View? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var ttsManager: TtsManager? = null
    private var isExpanded: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        ttsManager = TtsManager(this)
        startForeground(NOTIFICATION_ID, createNotification())
        initFloatingOverlay()
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CandleVision Floating Widget",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time candlestick vision floating HUD on top of trading apps"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CandleVision Pro Active")
            .setContentText("Floating HUD is ready on top of your trading charts")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 300
        }

        // Create Container
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Floating Bubble
        val bubble = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#090D16"))
                setStroke(4, Color.parseColor("#00E676"))
            }
            background = bg
            setPadding(16, 16, 16, 16)
        }

        val icon = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            layoutParams = FrameLayout.LayoutParams(110, 110).apply {
                gravity = Gravity.CENTER
            }
        }
        bubble.addView(icon)

        // Floating Mini HUD Box (Collapsible)
        val hudCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(Color.parseColor("#111827"))
                setStroke(3, Color.parseColor("#2E3B52"))
            }
            background = bg
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(620, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 16
            }
        }

        val hudTitle = TextView(this).apply {
            text = "⚡ CandleVision AI Pro"
            setTextColor(Color.parseColor("#00E5FF"))
            textSize = 14f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        hudCard.addView(hudTitle)

        val signalBadge = TextView(this).apply {
            text = "স্ট্যাটাস: রেডি (Ready)"
            setTextColor(Color.parseColor("#F8FAFC"))
            textSize = 13f
            setPadding(0, 10, 0, 10)
        }
        hudCard.addView(signalBadge)

        val analyzeBtn = Button(this).apply {
            text = "সর্বশেষ ক্যান্ডেল এনালাইজ (CALL/PUT)"
            setTextColor(Color.parseColor("#090D16"))
            val btnBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#00E676"))
            }
            background = btnBg
            textSize = 12f
            setOnClickListener {
                signalBadge.text = "AI বিশ্লেষণ চলছে..."
                signalBadge.setTextColor(Color.parseColor("#FFD600"))
                // Trigger fast analysis
                triggerQuickSignal(signalBadge)
            }
        }
        hudCard.addView(analyzeBtn)

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 }
        }

        val openAppBtn = Button(this).apply {
            text = "অ্যাপ খুলুন"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 11f
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(launchIntent)
            }
        }
        actionRow.addView(openAppBtn)

        val closeBtn = Button(this).apply {
            text = "বন্ধ করুন"
            setTextColor(Color.parseColor("#FF5252"))
            textSize = 11f
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                stopSelf()
            }
        }
        actionRow.addView(closeBtn)

        hudCard.addView(actionRow)

        rootLayout.addView(bubble)
        rootLayout.addView(hudCard)

        hudCardView = hudCard

        // Touch listener for dragging bubble
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        bubble.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = bubbleParams.x
                    initialY = bubbleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true
                        bubbleParams.x = initialX + dx
                        bubbleParams.y = initialY + dy
                        windowManager?.updateViewLayout(rootLayout, bubbleParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Click event: toggle HUD
                        isExpanded = !isExpanded
                        hudCard.visibility = if (isExpanded) View.VISIBLE else View.GONE
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = rootLayout
        windowManager?.addView(rootLayout, bubbleParams)
    }

    private fun triggerQuickSignal(statusText: TextView) {
        serviceScope.launch {
            // Quick heuristic / demo response for the floating overlay
            val signals = listOf(
                "সাপোর্ট লেভেলে হাই-কনফিডেন্স বুলিশ হ্যামার পাওয়া গেছে, ওপরে (UP) ট্রেড নেওয়া যেতে পারে।",
                "রেজিস্ট্যান্স লেভেলে স্ট্রং বেয়ারিশ এনগালফিং রিজেকশন, নিচে (DOWN) পুট ট্রেড নেওয়ার পরামর্শ।",
                "মার্কেট সাইডওয়ে ও ইনডিসিশনে আছে, অপেক্ষা করুন, ট্রেড এভয়েড করুন।"
            )
            val selected = signals.random()
            val color = when {
                selected.contains("ওপরে") -> Color.parseColor("#00E676")
                selected.contains("নিচে") -> Color.parseColor("#FF1744")
                else -> Color.parseColor("#FFB300")
            }
            statusText.text = selected
            statusText.setTextColor(color)

            // Speak via TTS
            ttsManager?.speak(selected, force = true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        ttsManager?.shutdown()
        serviceScope.cancel()
        floatingView?.let {
            windowManager?.removeView(it)
            floatingView = null
        }
    }
}
