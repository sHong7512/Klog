package com.shong.sample_java;

import android.os.Bundle;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.shong.klog.Klog;

public class MainActivity2 extends AppCompatActivity {
    Klog log = Klog.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        findViewById(R.id.goBackButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.makeLogButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.f("" + System.currentTimeMillis(), "test log " + System.currentTimeMillis());
            }
        });

        findViewById(R.id.showTestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.f("testKey", "show Test!");
            }
        });

        findViewById(R.id.removeKeyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.removeFloatLog("testKey");
            }
        });

        findViewById(R.id.removeAllFloatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.removeAllFloatLog();
            }
        });

        ComponentActivity activity = this;
        findViewById(R.id.runFloatingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.runFloating(activity);
            }
        });

        findViewById(R.id.stopFloatingButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.stopFloating(activity);
            }
        });
    }
}