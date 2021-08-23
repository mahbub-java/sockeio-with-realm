package com.example.socketio;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socketio.Interfaces.StructureInterface;
import com.example.socketio.Models._Message;
import com.example.socketio.Models._RealmController;
import com.example.socketio.Models._User;
import com.example.socketio.Utils.SocketManager;
import com.google.gson.Gson;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmException;
import io.socket.emitter.Emitter;

public class ChatMessageActivity extends AppCompatActivity implements StructureInterface,
        MessageInput.InputListener, MessagesListAdapter.OnLoadMoreListener,
        MessagesListAdapter.OnMessageLongClickListener<_Message> {

    //view variable
    private MessagesList messagesList;
    private MessageInput input;

    //variable

    private _User me;

    private SocketManager socketManager;

    private MessagesListAdapter messagesAdapter;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private long lastMessageIncrementalID = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_messages);

        //Structure functions
        initialize(null);
        setDefaults();
        setListener();
    }

    //Structure Function

    @Override
    public void initialize(View root) {
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        input = (MessageInput) findViewById(R.id.input);
    }

    @Override
    public void setDefaults() {
        Bundle bundle = ChatMessageActivity.this.getIntent().getExtras();

        if (bundle != null) {

            //retrieve data from login login activity

            int loginID = bundle.getInt("loginID");

            int partnerID = bundle.getInt("partnerID");

            //make a unique room name for me and my partner
            String roomName = getUniqueRoomName(partnerID, loginID);

            //set title
            setTitle("Chat Between: " + roomName);

            //because lack of bundle passing of java object
            //i simply make a new user instance from current loginID
            //and you see this user has temporary room name :)
            me = getUser(loginID, roomName);


            //make ready message adapter
            messagesAdapter = new MessagesListAdapter<>(me.getId(), null);
            messagesList.setAdapter(messagesAdapter);


            //initializing socket
            socketManager = new SocketManager(me);

            //here i am connect to socket, from other device partner will connect,
            //where partner will be me (but not me) (:D)
            socketManager.connectSocket();


            //load only last message, it helps list adapter to determine load more or not
            loadLastMessage();

        }


    }

    @Override
    public void setListener() {

        //bind on submit listener
        input.setInputListener(this);

        //bind load more listener, it will track scroll up
        messagesAdapter.setLoadMoreListener(this);

        //bind on message long click
        messagesAdapter.setOnMessageLongClickListener(this);

        //get called when a incoming message come..
        socketManager.getSocket().on("updateChat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String messageData = (String) args[0];
                Gson gson = new Gson();
                _Message message = gson.fromJson(messageData, _Message.class);

                //double check for valid room
                if (me.isValidRoomForUser(message.getRoomName())) {

                    //sometime nodejs can broadcast own message so check and message should be view immediately

                    if(!message.getUser().getId().equals(me.getId())) {
                        //save new incoming message to realm
                        saveOrUpdateNewMessageRealm(message);

                        //check and save validity of room incoming user
                        runOnUiThread(() -> {

                            messagesAdapter.addToStart(message, true);
                        });
                    }

                }
            }
        });
    }

    //normal methods

    //make a unique room name for two id, they may exchange but roomName will remain same
    String getUniqueRoomName(int partnerID, int loginID) {

        String uniqueRoomName = "";

        if (partnerID < loginID) {
            uniqueRoomName = partnerID + "::" + loginID;
        } else if (loginID < partnerID) {
            uniqueRoomName = loginID + "::" + partnerID;
        }

        return uniqueRoomName;
    }

    //make a new user from existing user
    private _User getUser(int id, String roomName) {
        return new _User(id, roomName);
    }

    //save new message by realm

    private void saveOrUpdateNewMessageRealm(_Message message) {

        //realm required to submit from separate thread. IF I do operation in ui thread,
        //I need .allowQueriesOnUiThread(true), that's no need in my case
        executor.execute(() -> {
            // use Realm on background thread
            _RealmController.insertOrUpdateNewMessage(message);
        });
    }


    //its for ui implementation when valid input from UI (Chat kit)

    //When submit new message

    @Override
    public boolean onSubmit(CharSequence input) {

        //make a message
        long systemNenoTime = System.nanoTime();

        _Message message = new _Message(
                systemNenoTime + "::" + socketManager.getSocketID(), //just make incremental,
                // need for chat kit only (No need socket id ref, in this project, but may be future)
                systemNenoTime, // this will need for me as primary key, because chatkit abnormally demand ID as String
                //but i need long for realm operation
                me.getRoomName(), //pass 1-1 unique room name
                me, //responsible person is me for this message
                input.toString(), //text of message
                new Date());

        //check is user valid and ready to broadcast his message
        if (socketManager.isUserValid()) {

            //save new submitted message to realm
            saveOrUpdateNewMessageRealm(message);

            //emit submitted messages to socket IO for broadcast in current room
            socketManager.performEmit("newMessage", message);

            runOnUiThread(() -> {
                //submitted message should be view immediately
                messagesAdapter.addToStart(message, true);

                //update last chat incremental id
                lastMessageIncrementalID = message.getIncrementalID();
            });


            //know the adapter submission has completed
            return true;

        } else {

            Toast.makeText(ChatMessageActivity.this, "Please wait until valid connection confirmed. " +
                    "Try again after sometime! In real project there should be a progress bar.", Toast.LENGTH_LONG).show();

            //alas adapter will omit submission
            return false;

        }

    }

    //Load more messages

    private void loadLastMessage() {
        ArrayList<_Message> messages = _RealmController.getLastMessage(me.getRoomName());
        if (messages.size() > 0) {
            messagesAdapter.addToStart(messages.get(0), false);

            // ok paging started with page no and total item count which is 1,
            // this method auto call by chat UI during scroll
            onLoadMore(1, 1);
        }
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

        ArrayList<_Message> messages = _RealmController.getAllMessagesByLimit(me.getRoomName(), lastMessageIncrementalID, 10);

        if (messages.size() > 0) {
            runOnUiThread(() -> {

                //add all messages in the end
                messagesAdapter.addToEnd(messages, false);

                //save last loading messages info
                lastMessageIncrementalID = messages.get(0).getIncrementalID();

            });
        }
    }

    //on message click
    @Override
    public void onMessageLongClick(_Message message) {

        //check validation if message has permission
        if (message.getUser().getId().equals(me.getId())) {

            AlertDialog.Builder customBuilder = new AlertDialog.Builder(ChatMessageActivity.this);
            customBuilder.setMessage("Are you sure you want to delete this message?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //delete object from realm
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                        Realm.getInstanceAsync(_RealmController.getConfigurationOnlyForWriteQueriesInUIAsyc(),
                                                new Realm.Callback() {
                                                    @Override
                                                    public void onSuccess(@NonNull Realm realm) {
                                                        _RealmController.deleteAMessage(realm, message);
                                                    }
                                                });


                                }
                            });

                        }
                    }).setNegativeButton("CANCEL", null);

            AlertDialog dialog = customBuilder.create();
            dialog.show();

            Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            if (b != null) {
                b.setTextColor(getResources().getColor(R.color.white_five));
            }

        }
    }


    //disconnect socket when destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketManager.disconnectSocket();
    }


}
