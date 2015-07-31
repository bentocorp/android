package com.bentonow.bentonow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.bentonow.bentonow.model.User;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

public class EnterCreditCardActivity extends BaseFragmentActivity {

    private static final String TAG = "EnterCreditCardActivity";
    public static Activity _this;

    AQuery aq;
    private RelativeLayout ok_overlay;
    private TextView ok_btn;
    private TextView ok_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_credit_card);
        aq = new AQuery(this);

        _this = this;

        initActionbar();
        initElements();
        addListeners();
    }

    private void initElements() {
        ok_overlay = (RelativeLayout)findViewById(R.id.ok_overlay);
        ok_btn = (TextView)findViewById(R.id.ok_btn);
        ok_message = (TextView)findViewById(R.id.ok_message);
    }

    private void addListeners(){
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ok_overlay.setVisibility(View.GONE);
            }
        });
    }

    public void saveCreditCard(final PaymentForm form) {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Processing...", true);

        Card card = new Card(
                form.getCardNumber(),
                form.getExpMonth(),
                form.getExpYear(),
                form.getCvc());

        boolean validation = card.validateCard();
        if (validation) {
            new Stripe().createToken(
                    card,
                    getResources().getString(R.string.stripe_key),
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            Log.i(TAG, "TOKEN: " + token.toString());

                            form.saved();

                            User user = User.findById(User.class,(long)1);
                            user.stripetoken = token.getId();
                            user.save();

                            dialog.dismiss();
                            returnToPayment();
                        }
                        public void onError(Exception error) {
                            dialog.dismiss();
                            String msg = error.getLocalizedMessage();
                            ok_message.setText(msg);
                            ok_overlay.setVisibility(View.VISIBLE);
                        }
                    });
        } else if (!card.validateNumber()) {
            dialog.dismiss();
            String msg = "The card number that you entered is invalid";
            ok_message.setText(msg);
            ok_overlay.setVisibility(View.VISIBLE);
        } else if (!card.validateExpiryDate()) {
            dialog.dismiss();
            String msg = "The expiration date that you entered is invalid";
            ok_message.setText(msg);
            ok_overlay.setVisibility(View.VISIBLE);
        } else if (!card.validateCVC()) {
            dialog.dismiss();
            String msg = "The CVC code that you entered is invalid";
            ok_message.setText(msg);
            ok_overlay.setVisibility(View.VISIBLE);
        } else {
            dialog.dismiss();
            String msg = "The card details that you entered are invalid";
            ok_message.setText(msg);
            ok_overlay.setVisibility(View.VISIBLE);
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
                returnToPayment();
            }
        });
    }

    public static void returnToPayment() {
        _this.startActivity(new Intent(_this, CompleteOrderActivity.class));
        _this.finish();
        _this.overridePendingTransition(R.anim.left_slide_in, R.anim.right_slide_out);
    }
}
