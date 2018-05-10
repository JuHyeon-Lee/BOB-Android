package com.nova.bob;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class PacketUtil  {

    private SocketClient socketClient;
    String protocol;
    protected byte[] data;
    protected Context context;
    LocalBroadcastManager broadCaster;

    public PacketUtil(String protocol, byte[] data, Context context){
        this.protocol = protocol;
        this.data = data;
        this.context = context;
        broadCaster = LocalBroadcastManager.getInstance(context);
    }

    public void setSocketClient(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void execute(){ // 패킷을 받았을 때 해야하는 일
        switch (protocol){
            case SocketService.GET_MARKER_INFO: {
                try {
                    String[] bodyStr = (new String(data, "utf-8")).split("\\|", 6);
                    Log.i("chat", "GET_MARKER_INFO");
                    String phoneNumber = bodyStr[0];
                    int age = Integer.parseInt(bodyStr[1]);
                    String gender = bodyStr[2];
                    String nickName = bodyStr[3];
                    double latitude = Double.parseDouble(bodyStr[4]);
                    double longitude = Double.parseDouble(bodyStr[5]);
                    Intent intent = new Intent(SocketService.GET_MARKER_INFO);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("nickName", nickName);
                    intent.putExtra("age", age);
                    intent.putExtra("gender", gender);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    broadCaster.sendBroadcast(intent);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            }

            case SocketService.MATCHING_SUCCESS : { //매칭 성공
                try {
                    String[] bodyStr = (new String(data, "utf-8")).split("\\|", 4);
                    String phoneNumber = bodyStr[0];
                    String nickName = bodyStr[1];
                    int age = Integer.parseInt(bodyStr[2]);
                    String gender = bodyStr[3];
                    Intent intent = new Intent(SocketService.MATCHING_SUCCESS);
                    intent.putExtra("phoneNumber", phoneNumber);
                    intent.putExtra("nickName", nickName);
                    intent.putExtra("age", age);
                    intent.putExtra("gender", gender);
                    broadCaster.sendBroadcast(intent);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            }

            case SocketService.MATCHING_FAILED : { // 매칭 실패
                try {
                    String bodyStr = new String(data, "utf-8");
                    Intent intent = new Intent(SocketService.MATCHING_FAILED);
                    broadCaster.sendBroadcast(intent);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            }

            case "300" : {
                try {
                    final String bodyStr = new String(data, "utf-8");
                    Log.i("chat", bodyStr + " 패킷에서 에러가 발생했습니다");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public static int byteArrayToInt(byte[] byteArray) {
        return (byteArray[0] & 0xff) << 24 | (byteArray[1] & 0xff) << 16 | (byteArray[2] & 0xff) << 8
                | (byteArray[3] & 0xff);
    }

    public static byte[] intToByteArray(int a) {
        return ByteBuffer.allocate(4).putInt(a).array();
    }
}

