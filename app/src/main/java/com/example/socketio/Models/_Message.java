package com.example.socketio.Models;

import com.stfalcon.chatkit.commons.models.IMessage;

import java.util.Date;

import io.realm.RealmObject;


public class _Message extends RealmObject implements IMessage {

    //chat kit need it, pass just socket id, no work at all
    private String id;

    //system.nenotime() for each message, need to query
    private long incrementalID;

    // unique room name for me and partner
    private String roomName;

    // text of messsage
    private String text;

    // user responsible for this message
    private _User user;

    // to fulfill chatkit loading purpose
    private Date createdAt;

    public _Message() {
        //required for realm
    }

    public _Message(String id, long incrementalID, String roomName, _User user, String text, Date createdAt) {
        this.id = id;
        this.incrementalID = incrementalID;
        this.roomName = roomName;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    //setter

    public void setId(String id) {
        this.id = id;
    }

    public void setIncrementalID(long incrementalID) {
        this.incrementalID = incrementalID;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(_User user) {
        this.user = user;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    //getter

    @Override
    public String getId() {
        return id;
    }

    public long getIncrementalID() {
        return incrementalID;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public _User getUser() {
        return this.user;
    }

    public String getRoomName() {
        return roomName;
    }

}
