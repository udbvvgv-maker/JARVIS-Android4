
package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(8, 12, 25))
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 32f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Hello! I am ready."
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }

        val listenButton = Button(this).apply {
            text = "🎙 Speak to JARVIS"
            setOnClickListener { startListening() }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(listenButton)

        setContentView(layout)

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            status.text = "Please allow microphone permission."
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }

        try {
            startActivityForResult(intent, 102)
        } catch (e: Exception) {
            status.text = "Speech recognition is unavailable."
        }
    }

    @Deprecated("Deprecated Android callback; retained for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102 && resultCode == RESULT_OK) {
            val words = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            val command = words?.firstOrNull()?.lowercase(Locale.ROOT)

            if (command != null) {
                handleCommand(command)
            }
        }
    }

    private fun handleCommand(command: String) {
        when {
            "youtube" in command -> {
                speak("Opening YouTube")
                openAppOrWebsite(
                    "com.google.android.youtube",
                    "https://www.youtube.com"
                )
            }

            "settings" in command -> {
                speak("Opening settings")
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            }

            "time" in command -> {
                val time = java.text.SimpleDateFormat(
                    "hh:mm a", Locale.getDefault()
                ).format(java.util.Date())
                speak("The time is $time")
            }

            "hello" in command || "hi" == command -> {
                speak("Hello! How can I help you?")
            }

            else -> {
                status.text = "You said: $command"
                speak("I heard you say $command. Online AI is not connected yet.")
            }
        }
    }

    private fun openAppOrWebsite(packageName: String, url: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            startActivity(launch)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun speak(message: String) {
        status.text = message
        if (::tts.isInitialized) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_REPLY")
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts.language = Locale("en", "IN")
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
