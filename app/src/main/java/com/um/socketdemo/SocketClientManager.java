package com.um.socketdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zeng
 * @package com.um.socketdemo
 * @date 2018/11/8 15:23
 * @describe TODO
 */

public class SocketClientManager {
    private static String TAG = "SocketClientManager";
    private static final int SOCKET_MSG = 10;
    private static SocketClientManager socketClientManager;
    private int port = 9002;
    private Socket socket;
    private ExecutorService executorService;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;

    private SocketClientManager(){
        executorService = Executors.newCachedThreadPool();
    }

    public static SocketClientManager getInstance(){
        if (socketClientManager == null){
            synchronized (SocketClientManager.class){
                if (socketClientManager == null){
                    socketClientManager = new SocketClientManager();
                }
            }
        }
        return socketClientManager;
    }

    public void connectSocket(Context context, final Handler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "connect start");
                try {
                    socket = new Socket(DeviceInfoUtils.getIpAddress(), port);
                    Log.d(TAG, "socket " + (socket == null));
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    //死循环守护，监控服务器发来的消息
                    while (true){
                        if (!socket.isClosed()){
                            if (socket.isConnected()){
                                if (!socket.isInputShutdown()){
                                    String getLine;
                                    if ((getLine = bufferedReader.readLine()) != null){
                                        getLine += "\n";
                                        Message msg = new Message();
                                        msg.obj = getLine;
                                        Log.d(TAG, "run: getLine" + getLine);
                                        msg.what = SOCKET_MSG;
                                        handler.sendMessage(msg);
                                    }
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

public void sendMessage(final String msg){
        if (executorService != null && socket != null){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "send msg" + msg);
                    try {
                        bufferedWriter.write(msg);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
}

    public void closeSocket() throws IOException {
        bufferedReader.close();
        bufferedWriter.close();
        if(socket!=null){
            socket.close();
        }
    }
}
