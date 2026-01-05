package com.example.navigationleftexample.websocket;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.navigationleftexample.utils.DataModel;
import com.example.navigationleftexample.utils.ErrorCallBack;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Map;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class MyWebSocketClient extends WebSocketClient {


    private static final String TAG = "MyWebSocket";
    private final Gson gson = new Gson();

    private  MessageListener listener;


    public MyWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    private String currentUsername = "Android";
    private String target = "Raspberry";

    public String getTarget() {
        return target;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }


    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }





    @Override
    public void onOpen() {
        Log.i("WebSocket", "Session is starting");

    }

    @Override
    public void onTextReceived(String message) {
        Log.i(TAG, "Message: " + message);

        try {
//            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
//            Map<String, Object> json = gson.fromJson(message, mapType);

            if (listener != null) {
                listener.onJsonReceived(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON parse error: " + e.getMessage());
        }
    }


    public void sendMessageToOtherUser(DataModel dataModel){

            //send the signal to other user
            send(new Gson().toJson(dataModel));
            Log.i("WebSocket", "Sent: " + dataModel);

    }






    @Override
    public void onBinaryReceived(byte[] data) {
        Log.i(TAG, "Message: ");
    }

    @Override
    public void onPingReceived(byte[] data) {
        Log.i(TAG, "Message: ");
    }

    @Override
    public void onPongReceived(byte[] data) {
        Log.i(TAG, "Message: ");
    }

    @Override
    public void onException(Exception e) {
        Log.e("WebSocket", e.getMessage());
    }

    @Override
    public void onCloseReceived(int i, String s) {
        Log.i(TAG, "Message: ");
    }


};