package com.localtechnologies.githubapi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.localtechnologies.githubapi.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = BuildConfig.API_KEY;

    private final String URL_ORGS = "https://api.github.com/orgs/%s/repos?per_page=40";
    private final String URL_USERS = "https://api.github.com/users/%s/repos?per_page=40";
    private final String LOGO_ORGS = "Organization";
    private final String LOGO_USERS = "User";
    private final String LOGO_DB = "From Data Base";

    private EditText username_field;
    private CheckBox check_box_org;
    private Button seach_button;

    private ArrayAdapter<String> adapter;
    private ArrayList<User> listData;
    private ArrayList<String> listView;
    private ExecutorService executor;
    private OkHttpClient client;
    private DatabaseReference dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getDataFromDB();

        seach_button.setOnClickListener(view -> {
            if (TextUtils.isEmpty(username_field.getText().toString())) {
                Toast.makeText(MainActivity.this, "Введите имя пользователя",
                                Toast.LENGTH_LONG)
                        .show();
            } else if (!isNetworkConnected()) {
                Toast.makeText(MainActivity.this, "Нет подключючения к Интернету",
                                Toast.LENGTH_LONG)
                        .show();
            } else {
                String username = username_field.getText().toString();

                String checkList = listData.stream()
                        .map(User::getUsername)
                        .collect(Collectors.joining(", "));

                if(checkList.contains(username)){
                    Toast.makeText(MainActivity.this, "Аккаунт найден в базе!",
                                    Toast.LENGTH_LONG)
                            .show();

                    try {
                        ObjectMapper objectMapper = new ObjectMapper();

                        Optional<User> userFromBase = listData.stream()
                                .filter(user -> user.getUsername().contains(username))
                                .findFirst();

                        if(userFromBase.isPresent()){
                            String repos = objectMapper
                                    .writeValueAsString(userFromBase.get()
                                    .getRepositories());

                            Intent intent = new Intent(this,
                                    RepositoriesActivity.class);
                            intent.putExtra("containsInBase",true);
                            intent.putExtra("user", username);
                            intent.putExtra("repos", repos);
                            intent.putExtra("logo_type", LOGO_DB);
                            startActivity(intent);
                        }else {
                            throw new NullPointerException();
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                }else {
                    if (check_box_org.isChecked()) {
                        getRepositoryByUser(String.format(
                                URL_ORGS, username), username, LOGO_ORGS);
                    } else {
                        getRepositoryByUser(String.format(
                                URL_USERS, username), username, LOGO_USERS);
                    }
                }


            }
        });
    }

    private void init() {

        username_field = findViewById(R.id.username_field);
        check_box_org = findViewById(R.id.check_box_org);
        seach_button = findViewById(R.id.button_search);
        ListView listUsers = findViewById(R.id.list_users);

        listData = new ArrayList<>();
        listView = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                listView);
        listUsers.setAdapter(adapter);

        dataBase = FirebaseDatabase.getInstance().getReference();
        executor = Executors.newSingleThreadExecutor();
        client = new OkHttpClient();
    }

    private void getDataFromDB (){
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(listData.size() > 0) listData.clear();

                for (DataSnapshot dataSnapshot: snapshot.child("Users").getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    listData.add(user);
                    listView.add(user.getUsername());
                }
                for (DataSnapshot dataSnapshot: snapshot.child("Organizations").getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    listData.add(user);
                    listView.add(user.getUsername());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                ///
            }
        };

        dataBase.addValueEventListener(vListener);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void getRepositoryByUser(String url, String user, String logo_type) {
        executor.execute(() -> {
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .url(url)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String json = response.body().string();

                    Intent intent = new Intent(this, RepositoriesActivity.class);
                    intent.putExtra("containsInBase",false);
                    intent.putExtra("user", user);
                    intent.putExtra("repos", json);
                    intent.putExtra("logo_type", logo_type);
                    startActivity(intent);

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                        "Ошибка! Статус код: " + response.code(),
                                        Toast.LENGTH_LONG)
                                .show();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
