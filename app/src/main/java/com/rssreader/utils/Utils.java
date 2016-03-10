package com.rssreader.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void showAlertMessage(Context context, String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void showFragment(FragmentActivity activity, Fragment fragment, int containerId) {
        showFragment(activity, fragment, containerId, null);
    }

    public static void showFragment(FragmentActivity activity, Fragment fragment, int containerId, String backStackTag) {
        if (activity == null || fragment == null) {
            Log.e(TAG, "Unable to show fragment. Invalid args.");
            return;
        }

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(containerId, fragment);
        if (backStackTag != null) {
            transaction.addToBackStack(backStackTag);
        }
        transaction.commitAllowingStateLoss();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            Log.e(TAG, "Unable to check internet connection because context is null");
            return false;
        }

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
