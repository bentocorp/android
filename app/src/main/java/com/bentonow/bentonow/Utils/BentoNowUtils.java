package com.bentonow.bentonow.Utils;

import android.content.Context;
import android.content.Intent;

import com.bentonow.bentonow.controllers.errors.ErrorActivity;
import com.bentonow.bentonow.controllers.init.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Jose Torres on 8/17/15.
 */
public class BentoNowUtils {

    public static final SimpleDateFormat sdfBento = new SimpleDateFormat("yyyyMMdd");

    public static String getTodayDate() {
        Calendar mToday = Calendar.getInstance();
        return sdfBento.format(mToday.getTime());
    }

    public static String getTomorrowDate() {
        Calendar cDate = Calendar.getInstance();
        cDate.add(Calendar.DATE, 1);

        return  sdfBento.format(cDate.getTime());
    }

    public static void openMainActivity(Context mContext) {
        if (!MainActivity.bIsOpen) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }

    public static void openErrorActivity(Context mContext) {
        if (!ErrorActivity.bIsOpen) {
            Intent intent = new Intent(mContext, ErrorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
    }

    public static String getUUIDBento() {
        String sUuidBento = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.UUID_BENTO);

        if (sUuidBento.isEmpty())
            sUuidBento = UUID.randomUUID().toString();

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.UUID_BENTO, sUuidBento);

        return sUuidBento;
    }

}
