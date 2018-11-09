package com.um.socketdemo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zeng
 * @package com.um.socketdemo
 * @date 2018/11/8 14:31
 * @describe TODO
 */

public class SocketServerManager {
    private static String TAG = "SocketDemo";
    private static SocketServerManager socketServerManager;
    private ServerSocket serverSocket;
    private List<Socket> mClientList = new ArrayList<Socket>();
    private ExecutorService mExecutors = null; // 创建线程池对象

    public static SocketServerManager getInstance() {
        if (socketServerManager == null) {
            synchronized (SocketServerManager.class) {
                if (socketServerManager == null) {
                    socketServerManager = new SocketServerManager();
                }
            }
        }
        return socketServerManager;
    }

    public void startSocketServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startServerSync();
            }
        }).start();
    }

    private void startServerSync() {
        Log.d(TAG, "startServerSync: ");
        try {
            serverSocket = new ServerSocket(9002);
            serverSocket.getInetAddress().getHostAddress();
            mExecutors = Executors.newCachedThreadPool(); // 创建线程池
            Socket client = null;
            // 用死循环等待多个客户端连接
            while (true) {
                client = serverSocket.accept();
                Log.d(TAG, "server get socket");
                mClientList.add(client);
                // 启动一个线程，用以守候从客户端发来的消息
                mExecutors.execute(new SocketHandle(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SocketHandle implements Runnable {
        private Socket socket;
        private BufferedReader bufferedReader = null;
        private BufferedWriter bufferedWriter = null;
        private String message = "";

        public SocketHandle(Socket socket) {
            this.socket = socket;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // 客户端只要一连接到服务器，便发送连接成功的消息
                message = "服务器地址：" + this.socket.getInetAddress();
                this.sendMessage(message);
                message = "当前连接的客户端总数" + mClientList.size();
                this.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                while (true) {
                    if ((message = bufferedReader.readLine()) != null){
                        if (message.equals("quit")){
                            closeSocket();
                            break;
                        }
                        // 接收客户端发送过来的消息，然后转发给客户端
                        message = "服务器收到：" + message;
                        this.sendMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeSocket() throws IOException{
            message = "主机:" + socket.getInetAddress() + "关闭连接\n目前在线：" + mClientList.size();
            this.sendMessage(message);
            bufferedWriter.close();
            bufferedReader.close();
            socket.close();
            mClientList.remove(socket);
        }

        private void sendMessage(String message) {
            Log.d(TAG, "server sendMessage: ");
            try {
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
