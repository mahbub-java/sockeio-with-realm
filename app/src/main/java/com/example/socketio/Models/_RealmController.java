package com.example.socketio.Models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Button;

import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.io.File;
import java.util.ArrayList;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmException;

public class _RealmController {

    //initializing realm
    public _RealmController(Context context) {
        Realm.init(context);
        makeRealmDatabaseWithDefaultConfiguration();
    }

    public void makeRealmDatabaseWithDefaultConfiguration() {

        RealmConfiguration config = Realm.getDefaultConfiguration();
        assert config != null;
        //create realm db file if and only if is not exists, to avoid error
        if (!new File(config.getPath()).exists()) {
            config = new RealmConfiguration.Builder().name("default.realm").build();
            Realm.setDefaultConfiguration(config);
        }

    }

    public static RealmConfiguration getConfigurationOnlyForWriteQueriesInUIAsyc() {
        String realmName = "Chat";
        return new RealmConfiguration.Builder().allowWritesOnUiThread(true).allowQueriesOnUiThread(true).name(realmName).build();
    }

    //setter

    public static void insertOrUpdateNewUser(_User user) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        backgroundThreadRealm.executeTransaction(transactionRealm -> {
            transactionRealm.insertOrUpdate(user);
        });
    }

    public static void insertOrUpdateNewMessage(_Message message) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        backgroundThreadRealm.executeTransaction(transactionRealm -> {
            transactionRealm.insertOrUpdate(message);
        });
    }

    //get user

    public static boolean checkValidateUser(int id) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        _User user = backgroundThreadRealm.where(_User.class)
                .equalTo("id", id)
                .findFirst();
        return user != null;
    }

    public static ArrayList<_User> getAllAvailableUserWithoutAnID(int id) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        RealmResults<_User> users = backgroundThreadRealm
                .where(_User.class)
                .notEqualTo("id", id)
                .sort("id", Sort.DESCENDING)
                .findAll();
        return new ArrayList<>(users);
    }

    public static _User getAParticularUser(int id) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        return backgroundThreadRealm.where(_User.class)
                .equalTo("id", id)
                .findFirst();
    }

    public static ArrayList<_User> getAllAvailableUser() {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        RealmResults<_User> users = backgroundThreadRealm.where(_User.class).findAll();
        return new ArrayList<>(users);
    }


    //get messages

    public static ArrayList<_Message> getLastMessage(String roomName) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        RealmResults<_Message> messages = backgroundThreadRealm
                .where(_Message.class)
                .equalTo("roomName", roomName)
                .sort("incrementalID", Sort.DESCENDING)
                .limit(1)
                .findAll();

        return new ArrayList<>(messages);
    }


    public static ArrayList<_Message> getAllMessagesByLimit(String roomName, long lastMessageIncrementalID, int limit) {
        Realm backgroundThreadRealm = Realm.getDefaultInstance();
        RealmResults<_Message> messages = backgroundThreadRealm
                .where(_Message.class)
                .equalTo("roomName", roomName)
                .greaterThan("incrementalID", lastMessageIncrementalID)
                .sort("createdAt", Sort.DESCENDING)
                .limit(limit)
                .findAll();

        return new ArrayList<>(messages);
    }

    //delete message
    public static void deleteAMessage(Realm backgroundThreadRealm, _Message message) {

        backgroundThreadRealm.executeTransactionAsync(transactionRealm -> {
            //I am here failed to solve
            //"Realm access from incorrect thread. Realm objects can only be accessed on the thread they were created."
            //illegal statement through each time
            //even i created async for UI thread, its failed. its also failed in every situation
            //may be poorly design chatkit handle this listener inside Handler / or thread issue.
            //view its parent is onMessageLongClick(_Message message) please. i have to deep study for this.

            try {
                RealmResults<_Message> realmResults = backgroundThreadRealm
                        .where(_Message.class)
                        .equalTo("id", message.getId())
                        .findAllAsync();
                realmResults.deleteAllFromRealm();
            } catch (IllegalStateException | RealmException exception) {
                Log.e("Failed", exception.getMessage());
            }
        });


    }


    //listener

    public static void attachDeleteListenerWhenMessageDelete(_Message message, MessagesListAdapter messagesAdapter) {

        RealmObjectChangeListener<_Message> listener = (changedDog, changeSet) -> {
            assert changeSet != null;
            if (changeSet.isDeleted()) {
                messagesAdapter.deleteById(message.getId());
            }
        };

        message.addChangeListener(listener);
    }


}
