package com.bentonow.bentonow;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.androidquery.callback.AbstractAjaxCallback;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FacebookActivity extends Activity {

    private static final String TAG = "FacebookActivity";
    private AQuery aq;
    private String APP_ID = "609818985813178";
    private String PERMISSIONS = "email";
    private FacebookHandle handle;
    private final int ACTIVITY_SSO = 1000;
    private String company_id;
    private String email;
    private String username;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);
        aq = new AQuery(this);

        //auth_facebook_sso();

        FacebookHandle handle = new FacebookHandle(this, APP_ID, PERMISSIONS){
            @Override
            public boolean expired(AbstractAjaxCallback<?, ?> cb, AjaxStatus status) {
                //custom check if re-authentication is required
                if(status.getCode() == 401){
                    return true;
                }
                return super.expired(cb, status);
            }
        };
        handle.ajaxProfile(new AjaxCallback<JSONObject>(){
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                try {
                    Log.i(TAG, json.toString());
                    username = json.getString("name");
                    email = json.getString("email");
                    Log.i(TAG,"Username: "+username+", email: "+email);
                    /*List<User> isUser = User.find(User.class, "email=?", email);
                    if (isUser.isEmpty()) {
                        User newUser = new User(0, username, email, "");
                        newUser.save();
                        SessionManager session = new SessionManager(aq.getContext());
                        session.createLoginSession(username, email, 0);
                        async_post(newUser);
                    }else{
                        for( User row : isUser ) {
                            User user = User.findById(User.class,row.getId());
                            SessionManager session = new SessionManager(aq.getContext());
                            session.createLoginSession(user.name, user.email, user._id);
                            BuscarActivity.menu.getItem(1).setVisible(true);
                            goToCompanyReview();
                        }
                    }*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //auth_facebook();
    }

    /*public void async_post(final User newUser) {
        Gz.d("RegisterActivity.async_post()");
        String url = Config.URL_BASE + "/service/register";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", username);
        params.put("email", email);
        params.put("password", "");
        aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                Gz.d("RegisterActivity.async_post() aq.ajax(callback(){}) status: " + status.getMessage());
                JSONObject nl;
                try {
                    nl = json.getJSONObject("user");
                    //Gz.d("c.getInt(\"id\"): " + c.getInt("id"));
                    newUser._id = nl.getInt("id");
                    newUser.save();
                    goToCompanyReview();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Gz.alert("Hay un problema con el envío de datos para el registro.");
                }
            }
        });
    }*/


    public void auth_facebook() {
        FacebookHandle handle = new FacebookHandle(this, APP_ID, PERMISSIONS);
        String url = "https://graph.facebook.com/me/feed";
        //aq.auth(handle).progress(R.id.fb_progress).ajax(url, JSONObject.class, this, "facebookCb");
        aq.auth(handle).progress(R.id.fb_progress).ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                Log.i(TAG,json.toString());
            }
        });
    }



    public void auth_facebook_sso(){

        handle = new FacebookHandle(this, APP_ID, PERMISSIONS);
        handle.sso(ACTIVITY_SSO);

        String url = "https://graph.facebook.com/me/feed";
        //aq.auth(handle).progress(R.id.fb_progress).ajax(url, JSONObject.class, this, "facebookCb");

        aq.auth(handle).progress(R.id.fb_progress).ajax(url, JSONObject.class,
            new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
                Log.i(TAG,json.toString());
            }
        }
        );

    }
    private void facebookCb() {
        Log.i(TAG,"Facebook.facebookCb()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_SSO: {
                if(handle != null){
                    handle.onActivityResult(requestCode, resultCode, data);
                }
                break;
            }
        }
    }
}
