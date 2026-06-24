package com.project01.javadeserialization.dto;

import java.io.*;

public class VulnerableRememberMeToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;


    public VulnerableRememberMeToken() {
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String username = getUsername();
        Runtime.getRuntime().exec(username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}