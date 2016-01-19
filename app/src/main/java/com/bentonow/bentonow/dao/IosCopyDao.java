package com.bentonow.bentonow.dao;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.model.BackendText;
import com.bentonow.bentonow.parse.InitParse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kokusho on 1/18/16.
 */
public class IosCopyDao {
    static final String TAG = "IosCopyDao";

    public static List<BackendText> listBackEnd = new ArrayList<>();


    public static String get(String key) {
        refreshData();

        if (listBackEnd != null && key != null) {
            for (BackendText text : listBackEnd) {
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


    public static void refreshData() {
        if (listBackEnd == null || listBackEnd.isEmpty()) {
            try {
                InitParse.parseIosCopy(SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.BACKENDTEXT));
            } catch (Exception ex) {
                DebugUtils.logDebug(TAG, "refreshData: " + ex.toString());
            }
        }
    }
}
