package com.bentonow.bentonow.model;

import android.util.Log;

import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BackendText {
    static final String TAG = "model.BackendText";
    public static List<BackendText> list = new ArrayList<>();

    public String key;
    public String value;
    public String text;

    public static void set(String data) {
        list = new ArrayList<>();

        try {
            if (data.contains("/ioscopy"))
                data = new JSONObject(data).getString("/ioscopy");


            Gson gson = new Gson();
            list = gson.fromJson(data, new TypeToken<List<BackendText>>() {
            }.getType());

            BentoNowUtils.saveSettings(ConstantUtils.optSaveSettings.BACKEND_TEXT);
            DebugUtils.logDebug(TAG, "backend texts: " + list.size());
        } catch (Exception e) {
            DebugUtils.logError(TAG, e.getMessage());
        }
    }

    public static String get(String key) {
        if (list == null)
            Settings.load();

        if (list != null && key != null) {
            for (BackendText text : list) {
                if (!text.key.equals(key))
                    continue;
                String value = text.value;

                if (text.key.equals("sign-up-sign-in-text"))
                    return value.replace(" %.", "");
                if (text.key.equals("sign-in-sign-up-text"))
                    return value.replace(" %.", "");

                return value;
            }
        }

        return "";
    }
}
