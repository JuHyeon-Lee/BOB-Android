package com.nova.bob;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> years = new ArrayList<String>();
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        for(int i = 2017 ; i>=1900 ; i--){
            years.add(String.valueOf(i));
        }

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_dropdown_item, years);
        spinner.setPrompt("출생년도");
        spinner.setAdapter(adapter);

        final SharedPreferences UserData = getSharedPreferences("info", MODE_PRIVATE);
        final SharedPreferences.Editor editor = UserData.edit();

        Button next = (Button) findViewById(R.id.signin);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioButton male = (RadioButton) findViewById(R.id.male);
                String gender;
                if(male.isChecked()==true){
                    gender = "male";
                }
                else
                    gender = "female";


                EditText nickname = (EditText) findViewById(R.id.nickname);
                String name = nickname.getText().toString();


                EditText phone = (EditText) findViewById(R.id.phonenumber);
                String phonenum = phone.getText().toString();


                int age = Integer.valueOf(spinner.getSelectedItem().toString());

                editor.putString("gender", gender);
                editor.putString("nickName", name);
                editor.putString("phoneNumber", phonenum);
                editor.putInt("age", age);
                editor.commit();

                if(isMyServiceRunning(SocketService.class)){
                    stopService(new Intent(MainActivity.this, SocketService.class));
                }
                startService(new Intent(MainActivity.this, SocketService.class));

            }
        });

        registerReceiver();

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

    private void registerReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(SocketService.SOCKET_CONNECT_SUCCESS)){
                    finish();
                    startActivity(new Intent(MainActivity.this, Have_one_meal_activity.class));
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(SocketService.SOCKET_CONNECT_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
