package com.um.socketdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SocketServerService extends Service {
    public SocketServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SocketServerManager.getInstance().startSocketServer();
    }
}
