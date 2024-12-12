package com.mobilecomputing.trackandtaste;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import java.util.ArrayList;
import java.util.List;

public final class Tools {

    private Tools(){
        throw new UnsupportedOperationException("tools class");
    }

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void askPermissions(String[] permissions, Context context, Activity activity){
        List<String> permissionsTorequest = new ArrayList<>();
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                permissionsTorequest.add(permission);
            }
        }
        if(!permissionsTorequest.isEmpty()){
            ActivityCompat.requestPermissions(activity, permissionsTorequest.toArray(new String[0]), 1);
        }
    }

}
