package com.example.socketio.Utils;

import android.util.Log;

import com.example.socketio.Models._Message;
import com.example.socketio.Models._User;
import com.google.gson.Gson;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {

    private Socket mSocket;
    private final Gson gson = new Gson();
    private final _User me;
    private boolean isValidUser=false;

    public SocketManager(_User me){

        this.me = me;

        try {
            //This address is for my pc
            //After several failed, i investigate and found that localhost or 127.0.0.1 will not work in real device
            //Avd check up http://10.0.2.2:3000
            //to check sir, you need to change this address for ip of pc where node.js running

            mSocket = IO.socket("http://192.168.1.105:3000");
            Log.e("Success", "Success to make socket");


        } catch (Exception exception) {
            exception.printStackTrace();
            Log.e("fail", "Failed to connect");
        }

    }

    //getter
    public Socket getSocket(){
        return mSocket;
    }

    public String getSocketID(){
        return mSocket.id();
    }

    public boolean isUserValid(){
        return isValidUser;
    }

    //connect with socket

    public void connectSocket(){
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, connectListener);
        mSocket.on("newUserInValidRoom", newUserInValidRoom);
    }

    //emitting controller
    private final Emitter.Listener connectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            performEmit("subscribe", me);
        }
    };

    private final Emitter.Listener newUserInValidRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            String roomName = (String) args[0];

            //check and save validity of room incoming user
            isValidUser = me.isValidRoomForUser(roomName);
        }
    };

    public void performEmit(String event, Object object){
        String sendData = gson.toJson(object); // Gson changes user data object to Json type.
        mSocket.emit(event, sendData);
    }

    //disconnect
    public void disconnectSocket(){
        mSocket.disconnect();
    }

}
