package com.example.user.chatroom;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by user on 2018/3/5.
 */

public class InternetStatus {
    static Context context;
    /**
     * We use this class to determine if the application has been connected to either WIFI Or Mobile
     * Network, before we make any network request to the server.
     * <p>
     * The class uses two permission - INTERNET and ACCESS NETWORK STATE, to determine the user's
     * connection stats
     */

    private static InternetStatus instance = new InternetStatus();
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static InternetStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            //System.out.println("CheckConnectivity Exception: " + e.getMessage());
        }
        return connected;
    }
}
