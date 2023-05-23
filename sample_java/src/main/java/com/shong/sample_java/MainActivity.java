package com.shong.sample_java;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.shong.klog.Klog;
import com.shong.klog.models.LogLevel;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
public class MainActivity extends AppCompatActivity {
    Klog log = Klog.INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        log.initialize("_sHong", BuildConfig.DEBUG); // if you want to keep show, set true
        log.runFloating(this);
//        final Function0 onPermissionOk = (Function0) () -> {
//            /* TODO */
//            return Unit.INSTANCE;
//        };
//
//        final Function1 onFailure = (Function1) (str) -> {
//            /* TODO */
//            return Unit.INSTANCE;
//        };
//        log.runFloating(this, true, 10, false, onPermissionOk, onFailure);
        log.addBackPressedFloatingClose((ComponentActivity) this);

        findViewById(R.id.goMain2Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.f("${(it as Button).text}Button", "clicked!");
                startActivity(new Intent(getApplicationContext(), MainActivity2.class));
            }
        });
        findViewById(R.id.goMain2FinishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log.f("${(it as Button).text}Button", "clicked!");
                startActivity(new Intent(getApplicationContext(), MainActivity2.class));
                finish();
            }
        });

        new LogThread().start();
    }

    private class LogThread extends Thread {
        public LogThread() {
            log.fl("test N", "n", LogLevel.N);
            log.fl("test V", "v", LogLevel.V);
            log.fl("test D", "d", LogLevel.D);
            log.fl("test I", "i", LogLevel.I);
            log.fl("test W", "w", LogLevel.W);
            log.fl("test E", "e", LogLevel.E);
        }
        public void run() {
            try {
                Thread.sleep(1000);
                throw new RuntimeException();
            } catch (Exception e) {
                log.fl(this, "v", e, LogLevel.E);
                e.printStackTrace();
            }
        }
    }
}