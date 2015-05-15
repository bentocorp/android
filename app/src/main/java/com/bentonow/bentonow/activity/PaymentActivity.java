package com.bentonow.bentonow.activity;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.PaymentForm;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.TokenList;
import com.bentonow.bentonow.dialog.ErrorDialogFragment;
import com.bentonow.bentonow.dialog.ProgressDialogFragment;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Map;


public class PaymentActivity extends FragmentActivity {

    /*
     * Change this to your publishable key.
     *
     * You can get your key here: https://manage.stripe.com/account/apikeys
     */
    public static final String PUBLISHABLE_KEY = "pk_test_hFtlMiWcGFn9TvcyrLDI4Y6P";

    private ProgressDialogFragment progressFragment;

    AQuery aq;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);

        aq = new AQuery(this);

        progressFragment = ProgressDialogFragment.newInstance(R.string.progressMessage);
    }

    public void saveCreditCard(PaymentForm form) {

        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());

        boolean validation = card.validateCard();
        if (validation) {
            startProgress();
            new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                    public void onSuccess(Token token) {
                            Log.i("PaymentActivity", "TOKEN: " + token.getId());

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("data", "{     \"OrderItems\": [         {             \"item_type\": \"CustomerBentoBox\",             \"items\": [                 {\"id\": 11,  \"type\": \"main\"},                  {\"id\": 1,  \"type\": \"side1\"},                 {\"id\": 1,  \"type\": \"side2\"},                 {\"id\": 5,  \"type\": \"side3\"},                  {\"id\": 3, \"type\": \"side4\"}             ]         },         {             \"item_type\": \"CustomerBentoBox\",             \"items\": [                 {\"id\": 9,  \"type\": \"main\"},                  {\"id\": 5,  \"type\": \"side1\"},                  {\"id\": 4, \"type\": \"side2\"},                 {\"id\": 6,  \"type\": \"side3\"},                 {\"id\": 5,  \"type\": \"side4\"}              ]         }     ],     \"OrderDetails\": {         \"address\": {             \"number\": \"1111\",             \"street\": \"Kearny st.\",             \"city\": \"San Francisco\",             \"state\": \"CA\",             \"zip\": \"94133\"         },         \"coords\": {             \"lat\": \"37.798220\",             \"long\": \"-122.405606\"         },         \"tax_cents\": 137,         \"tip_cents\": 200,         \"total_cents\": \"1537\"     },     \"Stripe\": {         \"stripeToken\": \""+ token.getId() +"\"     }}");
                            params.put("api_token", "$2y$10$5kO4d87uDkREdppjSyASsey/VYJXPXaiPhdJqiO0ZGpF/tq1CPXIq");

                            aq.ajax("https://dev.api.bentonow.com/order", params, String.class, new AjaxCallback<String>() {
                                @Override
                                public void callback(String url, String json, AjaxStatus status) {
                                    finishProgress();
                                    Log.i("PaymentActivity", "JSONObject: " + json);
                                    Log.i("PaymentActivity", "AjaxStatus: " + status.getCode());
                                }
                            });

                            getTokenList().addToList(token);
                        }
                    public void onError(Exception error) {
                            handleError(error.getLocalizedMessage());
                            finishProgress();
                        }
                    });
        } else if (!card.validateNumber()) {
        	handleError("The card number that you entered is invalid");
        } else if (!card.validateExpiryDate()) {
        	handleError("The expiration date that you entered is invalid");
        } else if (!card.validateCVC()) {
        	handleError("The CVC code that you entered is invalid");
        } else {
        	handleError("The card details that you entered are invalid");
        }
    }

    private void startProgress() {
        progressFragment.show(getSupportFragmentManager(), "progress");
    }

    private void finishProgress() {
        progressFragment.dismiss();
    }

    private void handleError(String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validationErrors, error);
        fragment.show(getSupportFragmentManager(), "error");
    }

    private TokenList getTokenList() {
        return (TokenList)(getSupportFragmentManager().findFragmentById(R.id.token_list));
    }
}
