package com.example.socketio.Models;

import com.stfalcon.chatkit.commons.models.IUser;

import io.realm.RealmObject;

public class _User extends RealmObject implements IUser {

    private int id;
    private String name;
    private String roomName;
    private String avatar;

    public _User(){
        //required for realm
    }

    public _User(int id, String name, String roomName, String avatar) {
        this.id = id;
        this.name = name;
        this.roomName = roomName;
        this.avatar = avatar;
    }

    public _User(int id, String roomName) {
        this.id = id;
        this.roomName = roomName;
    }

    //getter

    @Override
    public String getId() {
        return id+""; //chat kit want's string, what can i do :)
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return null;
    }

    public String getRoomName() {
        return roomName;
    }


    //setter

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setRoomName(String roomName){
        this.roomName = roomName;
    }

    //check validation of a roomName

    public boolean isValidRoomForUser(String roomName){
        return roomName.contains(id+"");
    }


}