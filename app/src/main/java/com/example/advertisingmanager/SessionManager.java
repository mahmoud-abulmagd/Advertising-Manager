package com.example.advertisingmanager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String sharedPreferenceName = "Ads-App";
    private static SessionManager sessionManager = new SessionManager();
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    SessionManager() {
    }

    public static SessionManager getInstance(Context context) {
        sharedPreferences = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
        return sessionManager;
    }

    public void logIn(String token) {
        editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public void logout() {
        editor = sharedPreferences.edit();
        editor.remove("token");
        editor.remove("name");
        editor.remove("password");
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString("token", null);
    }

    public String getPassword() {
        return sharedPreferences.getString("password", null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}