package com.um.socketdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    @BindView(R.id.btn_start_server)
    Button mBtnStartServer;
    @BindView(R.id.btn_connect_server)
    Button mBtnConnectServer;
    @BindView(R.id.btn_disconnect_server)
    Button mBtnDisconnectServer;
    @BindView(R.id.edit_msg)
    EditText mEditMsg;
    @BindView(R.id.btn_send)
    Button mBtnSend;
    @BindView(R.id.msg_text)
    TextView mMsgTextView;

    public static final int SOCKET_MSG = 10;

    @SuppressLint("HandlerLeak")
    public  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SOCKET_MSG:
                    Log.d(TAG, "handleMessage: ");
                    mMsgTextView.setText(msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_start_server, R.id.btn_connect_server, R.id.btn_disconnect_server, R.id.edit_msg, R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start_server:
                Intent intent = new Intent(this, SocketServerService.class);
                startService(intent);
                break;
            case R.id.btn_connect_server:
                SocketClientManager.getInstance().connectSocket(this, mHandler);
                break;
            case R.id.btn_disconnect_server:
                //--关闭连接，通过发送quit消息通知服务器关闭
                SocketClientManager.getInstance().sendMessage("quit");
                try {
                    SocketClientManager.getInstance().closeSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.edit_msg:
                break;
            case R.id.btn_send:
                //--客户端向服务器发送消息
                String msg =mEditMsg.getText().toString();
                Log.d(TAG, "onViewClicked: ");
                SocketClientManager.getInstance().sendMessage(msg);
                break;
        }
    }
}
