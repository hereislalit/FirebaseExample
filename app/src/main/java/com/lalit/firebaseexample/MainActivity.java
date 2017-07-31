package com.lalit.firebaseexample;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lalit.firebaseexample.adapter.UserDataRecyclerAdapter;
import com.lalit.firebaseexample.model.User;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UserDataRecyclerAdapter.OnItemClickListener, ValueEventListener {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbReference;
    RecyclerView recyclerView;
    TextInputEditText etUserName, etUserEmail;
    private User selectedUser;
    Button btnAddUser, btnUpdateUser, btnDeleteUser;
    private ArrayList<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etUserEmail = (TextInputEditText) findViewById(R.id.tiet_user_email);
        etUserName = (TextInputEditText) findViewById(R.id.tiet_user_name);
        btnAddUser = (Button) findViewById(R.id.btn_add_user);
        btnDeleteUser = (Button) findViewById(R.id.btn_delete_user);
        btnUpdateUser = (Button) findViewById(R.id.btn_update_user);
        btnAddUser.setOnClickListener(this);
        btnUpdateUser.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);
        String path = "app_user/" + FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbReference = firebaseDatabase.getReference(path + "/user");
        dbReference.addValueEventListener(this);
        FirebaseApp.initializeApp(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnAddUser) {
            if (!etUserName.getText().toString().isEmpty() && !etUserEmail.getText().toString().isEmpty()) {
                String id = dbReference.push().getKey();
                User user = new User(etUserName.getText().toString(), etUserEmail.getText().toString(), id);
                dbReference.child(id).setValue(user);
            }
        } else if (v == btnDeleteUser) {
            if (selectedUser != null) {
                dbReference.child(selectedUser.getId()).removeValue();
            }
        } else if (v == btnUpdateUser) {
            if (selectedUser != null) {
                selectedUser.setName(etUserName.getText().toString());
                selectedUser.setEmail(etUserEmail.getText().toString());
                dbReference.child(selectedUser.getId()).updateChildren(User.getKeyMap(selectedUser));
            }
        }
        etUserEmail.setText("");
        etUserName.setText("");
        selectedUser = null;
    }

    @Override
    public void onItemClicked(User user) {
        selectedUser = user;
        etUserName.setText(user.getName());
        etUserEmail.setText(user.getEmail());
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (userList != null && userList.isEmpty()) {
            userList.clear();
        } else {
            userList = new ArrayList<User>();
        }
        for (DataSnapshot dbsnap : dataSnapshot.getChildren()) {
            User user = dbsnap.getValue(User.class);
            if (user.getId() == null) {
                user.setId(dbsnap.getKey());
            }
            userList.add(user);
        }
        UserDataRecyclerAdapter adapter = new UserDataRecyclerAdapter(userList);
        adapter.setOnItemClickListener(MainActivity.this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
