package com.shong.sample_kotlin

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.shong.klog.Klog
import java.text.SimpleDateFormat

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        findViewById<Button>(R.id.goBackButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.makeLogButton).setOnClickListener {
            Klog.f("${System.currentTimeMillis()}", "test log ${System.currentTimeMillis()}")
        }

        findViewById<Button>(R.id.showTestButton).setOnClickListener {
            Klog.f("testKey", "show Test!")
        }

        findViewById<Button>(R.id.removeKeyButton).setOnClickListener {
            Klog.removeFloatLog("testKey")
        }

        findViewById<Button>(R.id.removeAllFloatButton).setOnClickListener {
            Klog.removeAllFloatLog()
        }

        findViewById<Button>(R.id.runFloatingButton).setOnClickListener {
            Klog.runFloating(this)
        }

        findViewById<Button>(R.id.stopFloatingButton).setOnClickListener {
            Klog.stopFloating(this)
        }
    }
}