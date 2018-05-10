package com.nova.bob;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Activity_Splash extends AppCompatActivity {

    SharedPreferences pref;

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);

        registerReceiver();

        pref = getSharedPreferences("info", MODE_PRIVATE);
        if(!pref.getString("phoneNumber", "").equals("")){
            if(isMyServiceRunning(SocketService.class)){
                stopService(new Intent(Activity_Splash.this, SocketService.class));
            }
            startService(new Intent(Activity_Splash.this, SocketService.class));
        }
        else{
            Handler hd = new Handler();
            hd.postDelayed(new Runnable() {

                @Override
                public void run() {
                    finish();
                    startActivity(new Intent(Activity_Splash.this, MainActivity.class));// 3 초후 이미지를 닫아버림
                }
            }, 3000);
        }
    }

    private void registerReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(SocketService.SOCKET_CONNECT_SUCCESS)){
                    Log.i("asdf", "시작");
                    finish();
                    startActivity(new Intent(Activity_Splash.this, Have_one_meal_activity.class));
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(SocketService.SOCKET_CONNECT_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
