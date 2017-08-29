package com.davemorrissey.labs.subscaleview.sample;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Elbit on 8/17/2017.
 */

public class myToast {
    private String TAG = getClass().getName();
    private String toastText;
    private Context toastContext;
    private Boolean showToast;
    private int toastDuration = 7;
    private final double LENGTH_LONG = 3.5;
    private boolean centrelize = true;
    private static Toast toast;


    public myToast(Context context, boolean centrelize, boolean showToasts){
        this.toastContext = context;
        this.showToast = showToasts;
        new myToast(toastContext, toastText, toastDuration, centrelize, showToasts);
    }

    public myToast(Context context, String text, int duration, boolean centrelize, boolean showToasts){
        this.toastContext = context;
        this.toastText = text;
        this.toastDuration = duration;
        this.showToast = showToasts;

        toast = new Toast(toastContext);
        toast.makeText(toastContext, toastText, Toast.LENGTH_LONG);
        if (centrelize){
            centrelize(toast);
        }
    }

    public void centrelize(Toast toast){
        toast.setGravity(Gravity.CENTER,0,0);
    }

    public void setToastDuration(int toastDuration) {
        this.toastDuration = toastDuration;
    }

    public void setToastText(String toastText) {
        this.toastText = toastText;
        toast.setText(this.toastText);
    }

    public void setTextAndShow(String toastText) {
        if(!showToast){
            return;
        }
        try{
            this.toastText = toastText;
            showText();
        } catch (Exception e){
            Log.e(TAG , e.getStackTrace().toString());
        }
    }

    public void setViewAndShow(View view) {
        if(!showToast){
            return;
        }
        int  rounds = (int) Math.ceil(toastDuration / LENGTH_LONG);
        for (int i = 0 ; i<rounds; i++){
            Toast toast = Toast.makeText(toastContext, "", Toast.LENGTH_LONG);
            toast.setView(view);
            if (centrelize){
                centrelize(toast);
            }
            toast.show();
        }
    }

    public void showText(){
        if(!showToast){
            return;
        }
        int  rounds = (int) Math.ceil(toastDuration / LENGTH_LONG);
        SpannableStringBuilder biggerText = new SpannableStringBuilder(this.toastText);
        biggerText.setSpan(new RelativeSizeSpan(1.35f), 0, this.toastText.length(), 0);
//        for (int i = 0 ; i<rounds; i++){
//            Toast toast = Toast.makeText(toastContext, biggerText, Toast.LENGTH_LONG);
//            if (centrelize){
//                centrelize(toast);
//            }
//            toast.show();
//        }
        Toast toast = Toast.makeText(toastContext, biggerText, Toast.LENGTH_SHORT);
        if (centrelize){
            centrelize(toast);
        }
        toast.show();
        toast = Toast.makeText(toastContext, biggerText, Toast.LENGTH_SHORT);
        if (centrelize){
            centrelize(toast);
        }
        toast.show();
    }


    public void setToastApperance(boolean toastApperance) {
        this.showToast = toastApperance;
    }
}
