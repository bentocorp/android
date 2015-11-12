package com.bentonow.bentonow.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.db.DBAdapter;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.user.Card;
import com.bentonow.bentonow.model.user.CouponRequest;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.Calendar;

/**
 * Created by Jose Torres on 11/3/15.
 */
public class UserDao {
    public static final String TAG = "UserDao";

    private DBAdapter dbAdapter;
    private boolean success = true;

    public final static String TABLE_NAME = "table_user";
    public final static String ID_PK = "user_pk";

    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final String PHONE = "phone";
    private static final String COUPON_CODE = "coupon_code";
    private static final String API_TOKEN = "api_token";
    private static final String STRIPE_TOKEN = "stripe_token";
    private static final String FB_TOKEN = "fb_token";
    private static final String FB_ID = "fb_id";
    private static final String FB_PROFILE_PIC = "fb_profile_pic";
    private static final String FB_AGE_RANGE = "fb_age_range";
    private static final String FB_GENDER = "fb_gender";
    private static final String CARD_BRAND = "card_brand";
    private static final String CARD_LAST4 = "card_last4";

    public static final String QUERY_TABLE = "" + "CREATE TABLE " + TABLE_NAME + " (" + ID_PK + " INTEGER PRIMARY KEY autoincrement, "
            + FIRST_NAME + " TEXT, " + LAST_NAME + " TEXT, " + PASSWORD + " TEXT, " + EMAIL + " TEXT, " + PHONE + " TEXT,"
            + COUPON_CODE + " TEXT, " + API_TOKEN + " TEXT, " + STRIPE_TOKEN + " TEXT, " + FB_TOKEN + " TEXT, " + FB_ID + " TEXT,"
            + FB_PROFILE_PIC + " TEXT," + FB_AGE_RANGE + " TEXT, " + FB_GENDER + " TEXT, " + CARD_BRAND + " TEXT, " + CARD_LAST4 + " TEXT" + ");";

    public final static String[] FIELDS = {ID_PK, FIRST_NAME, LAST_NAME, PASSWORD, EMAIL, PHONE,
            COUPON_CODE, API_TOKEN, STRIPE_TOKEN, FB_TOKEN, FB_ID,
            FB_PROFILE_PIC, FB_AGE_RANGE, FB_GENDER, CARD_BRAND, CARD_LAST4};


    public UserDao() {
        dbAdapter = new DBAdapter();
    }

    public boolean insertUser(User mUser) {
        DebugUtils.logDebug(TAG, "Insert User");
        clearAllData();

        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mUser);

        long idInsert = dbAdapter.insert(TABLE_NAME, cValues);
        success = idInsert != -1;

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CURRENT_USER_ID, idInsert);

        dbAdapter.setTransacctionSuccesfull();

        return success;
    }


    public User getCurrentUser() {
        DebugUtils.logDebug(TAG, "Get Current User");
        User mUser = null;

        String conditional = ID_PK + " = " + SharedPreferencesUtil.getLongPreference(SharedPreferencesUtil.CURRENT_USER_ID);

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            int _FIRST_NAME = cursor.getColumnIndex(FIRST_NAME);
            int _LAST_NAME = cursor.getColumnIndex(LAST_NAME);
            int _PASSWORD = cursor.getColumnIndex(PASSWORD);
            int _EMAIL = cursor.getColumnIndex(EMAIL);
            int _PHONE = cursor.getColumnIndex(PHONE);
            int _COUPON_CODE = cursor.getColumnIndex(COUPON_CODE);
            int _API_TOKEN = cursor.getColumnIndex(API_TOKEN);
            int _STRIPE_TOKEN = cursor.getColumnIndex(STRIPE_TOKEN);
            int _FB_TOKEN = cursor.getColumnIndex(FB_TOKEN);
            int _FB_ID = cursor.getColumnIndex(FB_ID);
            int _FB_PROFILE_PIC = cursor.getColumnIndex(FB_PROFILE_PIC);
            int _FB_AGE_RANGE = cursor.getColumnIndex(FB_AGE_RANGE);
            int _FB_GENDER = cursor.getColumnIndex(FB_GENDER);
            int _CARD_BRAND = cursor.getColumnIndex(CARD_BRAND);
            int _CARD_LAST4 = cursor.getColumnIndex(CARD_LAST4);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                mUser = new User();
                mUser.firstname = (cursor.getString(_FIRST_NAME));
                mUser.lastname = (cursor.getString(_LAST_NAME));
                mUser.password = (cursor.getString(_PASSWORD));
                mUser.email = (cursor.getString(_EMAIL));
                mUser.phone = (cursor.getString(_PHONE));
                mUser.coupon_code = (cursor.getString(_COUPON_CODE));
                mUser.api_token = (cursor.getString(_API_TOKEN));
                mUser.stripe_token = (cursor.getString(_STRIPE_TOKEN));
                mUser.fb_token = (cursor.getString(_FB_TOKEN));
                mUser.fb_id = (cursor.getString(_FB_ID));
                mUser.fb_profile_pic = (cursor.getString(_FB_PROFILE_PIC));
                mUser.fb_age_range = (cursor.getString(_FB_AGE_RANGE));
                mUser.fb_gender = (cursor.getString(_FB_GENDER));
                Card mCard = new Card();
                mCard.brand = (cursor.getString(_CARD_BRAND));
                mCard.last4 = (cursor.getString(_CARD_LAST4));
                mUser.card = mCard;

                cursor.moveToNext();
            }

            cursor.close();

            dbAdapter.setTransacctionSuccesfull();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        return mUser;
    }


    public boolean updateUser(User mUser) {
        DebugUtils.logDebug(TAG, "Update User");
        String where = ID_PK + " =?";
        String[] whereArgs = new String[]{String.valueOf(SharedPreferencesUtil.getLongPreference(SharedPreferencesUtil.CURRENT_USER_ID))};

        ContentValues cValues = getContentValues(mUser);

        dbAdapter.begginTransaction();

        long idInsert = dbAdapter.update(TABLE_NAME, cValues, where, whereArgs);
        success = idInsert != 1;

        dbAdapter.setTransacctionSuccesfull();

        return success;
    }

    public boolean removeUser() {
        DebugUtils.logDebug(TAG, "Remove User");
        String where = ID_PK + "=?";
        String[] whereArgs = new String[]{String.valueOf(SharedPreferencesUtil.getLongPreference(SharedPreferencesUtil.CURRENT_USER_ID))};

        dbAdapter.begginTransaction();

        long idInsert = dbAdapter.delete(TABLE_NAME, where, whereArgs);
        success = idInsert != -1;

        dbAdapter.setTransacctionSuccesfull();

        return success;
    }

    public void clearAllData() {
        DebugUtils.logDebug(TAG, "Clear All Data");
        dbAdapter.deleteAll(TABLE_NAME);
    }

    private ContentValues getContentValues(User mUser) {
        ContentValues cValues = new ContentValues();
        cValues.put(FIRST_NAME, mUser.firstname);
        cValues.put(LAST_NAME, mUser.lastname);
        cValues.put(PASSWORD, mUser.password);
        cValues.put(EMAIL, mUser.email);
        cValues.put(PHONE, mUser.phone);
        cValues.put(COUPON_CODE, mUser.coupon_code);
        cValues.put(API_TOKEN, mUser.api_token);
        cValues.put(STRIPE_TOKEN, mUser.stripe_token);
        cValues.put(FB_TOKEN, mUser.fb_token);
        cValues.put(FB_ID, mUser.fb_id);
        cValues.put(FB_PROFILE_PIC, mUser.fb_profile_pic);
        cValues.put(FB_AGE_RANGE, mUser.fb_age_range);
        cValues.put(FB_GENDER, mUser.fb_gender);

        if (mUser.card != null) {
            cValues.put(CARD_BRAND, mUser.card.brand);
            cValues.put(CARD_LAST4, mUser.card.last4);
        }
        return cValues;
    }

    public boolean isCreditCardValid(User mUser) {
        boolean bCardIsValid;

        try {
            bCardIsValid = !mUser.card.brand.isEmpty() || !mUser.card.last4.isEmpty();
        } catch (Exception e) {
            bCardIsValid = false;
            DebugUtils.logError(TAG, e);
        }

        return bCardIsValid;
    }
}
