package com.example.socketio;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.socketio.Interfaces.StructureInterface;
import com.example.socketio.Models._RealmController;
import com.example.socketio.Models._User;
import com.example.socketio.Utils.StaticAccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class LoginActivity extends AppCompatActivity implements StructureInterface {

    //view variable

    EditText loginEditText;
    Button loginButton;

    //variable

    //private final Map<Integer, String> userList = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Structure functions
        initialize(null);
        setDefaults();
        setListener();
    }

    //Structure Function

    @Override
    public void initialize(View root) {
        loginEditText = findViewById(R.id.loginEditText);
        loginButton = findViewById(R.id.loginButton);
    }

    @Override
    public void setDefaults() {
        //initializing realm for this app and make a background realm
        new _RealmController(LoginActivity.this);

        //here we create four user, those user should populate from registered user or
        //availability basis in real world
        //however to testing purpose, sir you can add more
        _User mahbub = new _User(1234, "Mahubur Rahman Khan", null, null);
        _User tuni = new _User(1235, "Tuni Begum", null, null);
        _User montu = new _User(1236, "Montu Sheikh", null, null);
        _User mokbul = new _User(1237, "Mokbul Paji", null, null);

        //insert all manual user to Realm, as realm demand thread

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                _RealmController.insertOrUpdateNewUser(mahbub);
                _RealmController.insertOrUpdateNewUser(tuni);
                _RealmController.insertOrUpdateNewUser(montu);
                _RealmController.insertOrUpdateNewUser(mokbul);
            }
        });



    }

    @Override
    public void setListener() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticAccess.hideKeyboard(LoginActivity.this);

                //check for validation
                checkValidLoginAndRetrieveName();
            }
        });
    }

    //normal function

    private void checkValidLoginAndRetrieveName(){
        String attemptLoginIDText=loginEditText.getText().toString().trim();

        if(attemptLoginIDText.isEmpty()) {
            Toast.makeText(this, "Login id can't empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            int loginID=Integer.parseInt(attemptLoginIDText);

            //check validate of user
            if(_RealmController.checkValidateUser(loginID)){
                selectPartnerForChat(loginID);
            }else {
                Toast.makeText(LoginActivity.this, "Sorry you enter invalid id", Toast.LENGTH_SHORT).show();
            }
        }catch (NumberFormatException numberFormatException){
            Toast.makeText(this, "Login id must be in format", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectPartnerForChat(int loginID){

        //query for available partner list to chat
        ArrayList<_User> availablePartners= _RealmController.getAllAvailableUserWithoutAnID(loginID);

        //to ensure uniqueness
        Set<String> availablePartnersIDList = new HashSet<>();
        for(int i =0; i<availablePartners.size(); i++){
            availablePartnersIDList.add(availablePartners.get(i).getId());
        }

        //Time is limited so i just use a dialog, it can be a fragment/activity with recyclerview
        //only purpose to show available partner list

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alertdialog_select_partner, null);
        dialogBuilder.setView(dialogView);

        Spinner partnerListSpinner = (Spinner) dialogView.findViewById(R.id.partnerListSpinner);
        Button startChatButton = (Button) dialogView.findViewById(R.id.startChatButton);

        //set all partner id into spinner
        StaticAccess.showMultipleItemSpinner(this, partnerListSpinner, availablePartnersIDList);

        AlertDialog b = dialogBuilder.create();

        b.show();

        //click when start chat
        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //i am 100% sure selected partner is an integer type
                int partnerID = Integer.parseInt(partnerListSpinner.getSelectedItem().toString());

                //yes now is the time to start chat 1-1
                startChatMessageActivity(loginID, partnerID);
            }
        });

    }

    void startChatMessageActivity(int loginID, int partnerID){
        Intent intent = new Intent(this, ChatMessageActivity.class);

        //pass a bundle to chat message activity

        Bundle bundle = new Bundle();
        bundle.putInt("loginID", loginID);
        bundle.putInt("partnerID", partnerID);
        intent.putExtras(bundle);

        //start activity, whats now?
        startActivity(intent);;
    }
}