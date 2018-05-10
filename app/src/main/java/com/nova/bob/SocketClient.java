package com.nova.bob;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class SocketClient {

    SocketChannel socketChannel;
    int port;
    protected Context context;

    protected LocalBroadcastManager broadCaster;
    public String phoneNumber;
    public String nickName;
    public String gender;
    public int age;

    Thread thread;

    final String serverAddress = "45.32.109.86";
    private final static int SERVERPORT = 9123;

    private Queue<byte[]> packetQueue;

    SocketClient(Context context, String phoneNumber, String nickName, String gender, int age){
        this.context = context;
        this.port = SERVERPORT;
        broadCaster = LocalBroadcastManager.getInstance(context);
        this.phoneNumber = phoneNumber;
        this.nickName = nickName;
        this.gender = gender;
        this.age = age;

        packetQueue = new LinkedList<>();
    }

    boolean isSocketOpen(){
        return socketChannel.isOpen();
    }

    void startClient(){
        if(socketChannel != null) {
            if (socketChannel.isOpen()) {
                return;
            }
        }
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(serverAddress, port));
                } catch (Exception e) {
                    Log.i("Chat", port + "서버 통신 안됨");
                    this.interrupt();
                    return;
                }
                Log.i("Chat", port + " 서버 연결 성공");
                Packet infoPacket = new Packet(SocketService.SOCKET_CONNECTED);
                infoPacket.addData(phoneNumber, nickName, Integer.toString(age), gender);
                send(infoPacket.toByteArray());
                broadCaster.sendBroadcast(new Intent(SocketService.SOCKET_CONNECT_SUCCESS));
                receive();
            }
        };
        thread.start();
    }

    void stopClient(){
        if(thread != null) {
            thread.interrupt();
            thread = null;
        }
        try{
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void receive(){
        while (true) {
            try {
                if(socketChannel.isOpen()){
                    ByteBuffer headerByteBuffer = ByteBuffer.allocate(7);
                    int byteCount = socketChannel.read(headerByteBuffer);
                    if (byteCount == -1) {
                        throw new IOException();
                    }
                    byte[] data = headerByteBuffer.array();
                    String protocol = new String(Arrays.copyOfRange(data, 0, 3),"utf-8");
                    int length = PacketUtil.byteArrayToInt(Arrays.copyOfRange(data, 3, 7));
                    ByteBuffer bodyByteBuffer = ByteBuffer.allocate(length);
                    while(bodyByteBuffer.hasRemaining()){
                        socketChannel.read(bodyByteBuffer);
                    }
                    PacketUtil packetUtil = new PacketUtil(protocol, bodyByteBuffer.array(), context);
                    packetUtil.setSocketClient(SocketClient.this);
                    packetUtil.execute();
                }
            }
            catch (Exception e) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "서버와의 연결이 끊어졌습니다", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
                stopClient();
                break;
            }
        }
    }

    public void send(final byte[] data) {
        if(!this.isSocketOpen()){
            return;
        }
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        new Thread() {
            @Override
            public void run() {
                try {
                    if(!socketChannel.isOpen()){
                        throw new Exception("socket is closed");
                    }
                    socketChannel.write(byteBuffer);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    stopClient();
                    this.interrupt();
                }
            }

        }.start();
    }
}
