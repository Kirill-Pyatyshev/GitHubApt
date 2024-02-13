package com.localtechnologies.githubapi.entity;

import java.util.ArrayList;

public class User {

    private String username;
    private ArrayList<Repository> repositories;

    public User() {}

    public User(String username, ArrayList<Repository> repositories) {
        this.username = username;
        this.repositories = repositories;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Repository> getRepositories() {
        return repositories;
    }

}
