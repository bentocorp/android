package com.bentonow.bentonow;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.bentonow.bentonow.dialog.ErrorDialogFragment;
import com.bentonow.bentonow.dialog.ProgressDialogFragment;
import com.bentonow.bentonow.model.User;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Map;


public class EnterCreditCardActivity extends BaseFragmentActivity {

    private static final String TAG = "EnterCreditCardActivity";
    public static final String PUBLISHABLE_KEY = "pk_test_hFtlMiWcGFn9TvcyrLDI4Y6P";
    public static Activity _this;

    private ProgressDialogFragment progressFragment;

    AQuery aq;
    //private TextView btn_continue_to_payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_credit_card);
        aq = new AQuery(this);

        _this = this;

        initActionbar();
        initElements();
        addListeners();

        //progressFragment = ProgressDialogFragment.newInstance(R.string.progressMessage);
    }

    private void initElements() {
        //btn_continue_to_payment = (TextView)findViewById(R.id.btn_continue_to_payment);
    }

    private void addListeners(){
        /*btn_continue_to_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPayment();
            }
        });*/
    }

    public void saveCreditCard(PaymentForm form) {

        final ProgressDialog dialog = ProgressDialog.show(this, null, "Processing...", true);

        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());

        boolean validation = card.validateCard();
        if (validation) {
            //startProgress();
            new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            Log.i("PaymentActivity", "TOKEN: " + token.toString());

                            User user = User.findById(User.class,(long)1);
                            user.stripetoken = token.getId();
                            user.save();

                            dialog.dismiss();
                            returnToPayment();
                            //getTokenList().addToList(token);
                        }
                        public void onError(Exception error) {
                            dialog.dismiss();
                            Log.i(TAG, error.getLocalizedMessage());
                            //finishProgress();
                        }
                    });
        } else if (!card.validateNumber()) {
            String msg = "The card number that you entered is invalid";
            Log.i(TAG,msg);
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
        } else if (!card.validateExpiryDate()) {
            String msg = "The expiration date that you entered is invalid";
            Log.i(TAG, msg );
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
        } else if (!card.validateCVC()) {
            String msg = "The CVC code that you entered is invalid";
            Log.i(TAG, msg);
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
        } else {
            String msg = "The card details that you entered are invalid";
            Log.i(TAG, msg );
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
        }
    }

    private void initActionbar() {
        Log.i(TAG, "initActionbar()");
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText("Enter Credit Card");

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finishThisActivity();
                returnToPayment();
                //overridePendingTransitionGoLeft();
            }
        });
    }

    public static void returnToPayment() {
        _this.startActivity(new Intent(_this, CompleteOrderActivity.class));
        _this.finish();
        _this.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }
}
