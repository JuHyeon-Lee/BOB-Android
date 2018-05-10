package com.nova.bob;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;


public class SocketService extends Service {

    SocketClient socketClient;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    BroadcastReceiver broadcastReceiver;
    LocalBroadcastManager broadCaster;

    public final static String SOCKET_CONNECT_SUCCESS = "SOCKET_CONNECT_SUCCESS";

    //보내야하는 패킷 메세지 종류
    public final static String SOCKET_CONNECTED = "100";
    public final static String START_MATCHING = "101";
    public final static String CLICK_MARKER = "102";
    public final static String GET_MARKER_INFO = "200";
    public final static String MATCHING_SUCCESS = "201";
    public final static String MATCHING_FAILED = "202";

    Queue<byte[]> packetQueue;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        broadCaster = LocalBroadcastManager.getInstance(this);
        pref = getSharedPreferences("info", MODE_PRIVATE);
        editor = pref.edit();
        packetQueue = new LinkedList<>();

        socketClient = new SocketClient(SocketService.this,
                pref.getString("phoneNumber", "0000000000"),
                pref.getString("nickName", "nickName"),
                pref.getString("gender", "male"),
                pref.getInt("age", 1900));
        socketClient.startClient();
        registerBroadCastReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if(socketClient != null){
            socketClient.stopClient();
        }
    }

    private void flushQueue(){ // 패킷을 보내서 큐를 비워줌
        while(!packetQueue.isEmpty()){
            socketClient.send(packetQueue.poll());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerBroadCastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(SOCKET_CONNECT_SUCCESS)){
                    flushQueue();
                }
                else{
                    Log.i("chat", "받음"+intent.getAction());
                    if(socketClient.isSocketOpen() && packetQueue.isEmpty()){
                        socketClient.send((intent.getByteArrayExtra("Packet")));
                    }
                    else{
                        packetQueue.offer(intent.getByteArrayExtra("Packet"));
                        if(!socketClient.isSocketOpen()){
                            socketClient.startClient();
                        }
                        else{
                            flushQueue();
                        }
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SOCKET_CONNECT_SUCCESS);
        intentFilter.addAction(SOCKET_CONNECTED);
        intentFilter.addAction(START_MATCHING);
        intentFilter.addAction(CLICK_MARKER);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

}