package com.xixia.appetizing.Services;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by macbook on 9/7/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    private AlertDialog mAlertDialog;

    public NetworkChangeReceiver(){

    };


    @Override
    public void onReceive(Context context, Intent intent) {

        try
        {
            if (isOnline(context)) {
            } else {
                displayWarningDialog(context);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public  boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void displayWarningDialog(Context context) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setMessage("Many Appetizing capabilities will not work without internet")
                .setTitle("No Internet")
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        }
                );
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }
}
