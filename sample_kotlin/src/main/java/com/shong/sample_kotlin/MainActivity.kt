package com.shong.sample_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.shong.klog.BuildConfig
import com.shong.klog.Klog
import com.shong.klog.models.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set base data. if you want to keep show, set true
        Klog.initialize("_sHong", BuildConfig.DEBUG)

        // show floating
        Klog.runFloating(this)
        /* if you want to set other options
        Klog.runFloating(
            activity = this,
            autoStop = true,
            max = 5,
            withActivityLog = true,
            onPermissionOk = {
                Klog.fl(this, "Floating Start Success!", LogLevel.D)
            },
            onFailure = { e ->
                Klog.fl(this, "Floating Error $e", LogLevel.E)
            },
        )
        */

        // If you want to close when you press the Back button on the "base", insert this cord
        Klog.addBackPressedFloatingClose(this)

        findViewById<Button>(R.id.goMain2Button).setOnClickListener {
            Klog.f("${(it as Button).text}Button", "clicked!")
            startActivity(Intent(this, MainActivity2::class.java))
        }

        findViewById<Button>(R.id.goMain2FinishButton).setOnClickListener {
            Klog.f("${(it as Button).text}Button", "clicked!")
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
        }

        testLog()
    }

    private fun testLog() {
        CoroutineScope(Dispatchers.Main).launch {
            Klog.fl("test N", "n", LogLevel.N)
            Klog.fl("test V", "vVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV", LogLevel.V)
            Klog.fl("test D", "d", LogLevel.D)
            Klog.fl("test I", "i", LogLevel.I)
            Klog.fl("test W", "w", LogLevel.W)
            Klog.fl("test E", "e", LogLevel.E)
            try {
                delay(1000)
                throw RuntimeException()
            } catch (e: Exception) {
                Klog.fl(this, "make exception test", e, LogLevel.E)
            }
        }
    }
}