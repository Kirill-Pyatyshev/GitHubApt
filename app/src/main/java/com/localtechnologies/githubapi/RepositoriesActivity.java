package com.localtechnologies.githubapi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.localtechnologies.githubapi.entity.Repository;
import com.localtechnologies.githubapi.entity.User;

import java.util.ArrayList;

public class RepositoriesActivity extends AppCompatActivity {

    private TextView username_field;
    private ImageButton button_back;
    private ListView listRepos;

    private DatabaseReference dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        init();

        try {
            Intent intent = getIntent();

            String logo = String.format("%s: %s", intent.getStringExtra("logo_type"),
                    intent.getStringExtra("user"));

            username_field.setText(logo);

            ArrayList<Repository> list =
                    serializableJson(intent.getStringExtra("repos"));

            if(!intent.getBooleanExtra("containsInBase", false)){
               onSave(logo, list);
            }

            ArrayAdapter<Repository> adapter = new RepositoryAdapter(this,
                    R.layout.item_list, R.id.name_repo, list);
            listRepos.setAdapter(adapter);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        button_back.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void init(){
        username_field = findViewById(R.id.username_field_page2);
        button_back = findViewById(R.id.button_back);
        listRepos = findViewById(R.id.list_repos);

        dataBase = FirebaseDatabase.getInstance().getReference();
    }

    private ArrayList<Repository> serializableJson(String repos) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Repository> list = mapper.readerForListOf(Repository.class)
                .readValue(repos);

        return list;
    }

    private void onSave(@NonNull String user, ArrayList<Repository> list){
        String[] name = user.split(":");
        User newUser = new User(name[1], list);

        if(!name[0].equals("User")){
            dataBase.child("Organizations")
                    .push().setValue(newUser);
        }else {
            dataBase.child("Users")
                    .push().setValue(newUser);
        }
    }
}
