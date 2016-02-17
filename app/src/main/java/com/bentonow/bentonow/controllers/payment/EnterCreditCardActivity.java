package com.bentonow.bentonow.controllers.payment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.CreditCard;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.dialog.ProgressDialog;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.dao.UserDao;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.ui.BackendButton;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EnterCreditCardActivity extends BaseFragmentActivity implements View.OnClickListener, OnFocusChangeListener {

    private static final String TAG = "EnterCreditCardActivity";

    private ImageView img_credit_card;
    private int focused = R.id.txt_number;
    private EditText txt_number;
    private TextView txt_last4;
    private EditText txt_date;
    private EditText txt_cvc;
    private BackendButton btn_save;
    private TextView txtBentoPrice;
    private ConfirmationDialog mDialog;
    private ProgressDialog mProgressDialog;
    private ConstantUtils.optOpenScreen optOpenScreen;
    private UserDao userDao = new UserDao();
    private User mCurrentUser;
    private String sNumber = "";
    private String sCvc = "";
    private String sDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_enter_credit_card);

        try {
            optOpenScreen = (ConstantUtils.optOpenScreen) getIntent().getExtras().getSerializable(ConstantUtils.TAG_OPEN_SCREEN);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
            optOpenScreen = ConstantUtils.optOpenScreen.NORMAL;
        }

        mCurrentUser = userDao.getCurrentUser();

        img_credit_card = (ImageView) findViewById(R.id.img_credit_card);

        txt_number = (EditText) findViewById(R.id.txt_number);
        txt_last4 = (TextView) findViewById(R.id.txt_last4);
        txt_date = (EditText) findViewById(R.id.txt_date);
        txt_cvc = (EditText) findViewById(R.id.txt_cvc);

        btn_save = (BackendButton) findViewById(R.id.btn_save);

        txt_number.requestFocus();

        initActionbar();
        initEditText();
        updateUI();
    }

    //region UI

    private void initActionbar() {
        TextView actionbar_title = (TextView) findViewById(R.id.actionbar_title);
        actionbar_title.setText(IosCopyDao.get("complete-enter-credit-card"));

        ImageView actionbar_left_btn = (ImageView) findViewById(R.id.actionbar_left_btn);
        actionbar_left_btn.setImageResource(R.drawable.ab_x_close);
        actionbar_left_btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        GoogleAnalyticsUtil.sendScreenView("Credit Card");
        super.onResume();
    }

    void updateUI() {

        switch (optOpenScreen) {
            case COMPLETE_ORDER:
                btn_save.setText(IosCopyDao.get("credit-card-button"));
                break;
            default:
                btn_save.setText("SAVE CREDIT CARD");
                break;
        }

        btn_save.setBackgroundResource(isValid() ? R.drawable.bg_green_cornered : R.drawable.btn_dark_gray);

        getTxtBentoPrice().setText(BentoNowUtils.getNumberFromPrice(SettingsDao.getCurrent().price));

        txt_number.setVisibility(focused == R.id.txt_number ? View.VISIBLE : View.GONE);
        txt_last4.setVisibility(focused == R.id.txt_number ? View.GONE : View.VISIBLE);
        txt_date.setVisibility(focused == R.id.txt_number ? View.GONE : View.VISIBLE);
        txt_cvc.setVisibility(focused == R.id.txt_number ? View.GONE : View.VISIBLE);

        if (focused == R.id.txt_cvc) {
            if (CreditCard.isAmex(txt_number.getText().toString())) {
                img_credit_card.setImageResource(R.drawable.card_cvv_amex);
            } else {
                img_credit_card.setImageResource(R.drawable.card_cvv);
            }
        } else {
            switch (CreditCard.getHolder(txt_number.getText().toString())) {
                case "American Express":
                    img_credit_card.setImageResource(R.drawable.card_amex);
                    break;
                case "Mastercard":
                    img_credit_card.setImageResource(R.drawable.card_mastercard);
                    break;
                case "Visa":
                    img_credit_card.setImageResource(R.drawable.card_visa);
                    break;
                case "Diners Club":
                    img_credit_card.setImageResource(R.drawable.card_diners);
                    break;
                case "Discover Card":
                    img_credit_card.setImageResource(R.drawable.card_discover);
                    break;
                case "JCB":
                    img_credit_card.setImageResource(R.drawable.card_jcb);
                    break;
                default:
                    img_credit_card.setImageResource(R.drawable.card_empty);
                    break;
            }
        }

        if (focused != R.id.txt_number) {
            if (txt_number.getText().length() >= 4) {
                String last4 = txt_number.getText().toString().substring(txt_number.getText().length() - 4);
                txt_last4.setText(last4);
            }

            int maxLength = CreditCard.isAmex(txt_number.getText().toString()) ? 4 : 3;
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            txt_cvc.setFilters(fArray);
        }
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    //endregion

    //region Validation

    private boolean isValid() {
        return isNumberValid() && isDateValid() && isCVCValid();
    }

    private boolean isNumberValid() {
        return CreditCard.isValidLuhn(txt_number.getText().toString());
    }

    private boolean isDateValid() {
        return txt_date.getText().length() == 5;
    }

    private boolean isCVCValid() {
        if (CreditCard.getHolder(txt_number.getText().toString()).equals("American Express"))
            return txt_cvc.length() == 4;
        else
            return txt_cvc.length() == 3;
    }

    //endregion

    //region EditText

    void initEditText() {
        txt_number.setOnFocusChangeListener(this);
        txt_date.setOnFocusChangeListener(this);
        txt_cvc.setOnFocusChangeListener(this);

        txt_cvc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sCvc = txt_cvc.getText().toString();

                if (sCvc.isEmpty()) {
                    txt_date.requestFocus();
                    focused = R.id.txt_date;
                }

                updateUI();
            }
        });

        txt_date.addTextChangedListener(new TextWatcher() {
            int oldLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sDate = txt_date.getText().toString().replaceAll("[^0-9]", "");

                StringBuilder sb = null;

                if (sDate.isEmpty()) {
                    if (sNumber.length() != CreditCard.getNumberMaxLength(sNumber))
                        focused = R.id.txt_number;

                } else if (oldLength != sDate.length()) {
                    oldLength = sDate.length();

                    if (sDate.length() == 1) {
                        try {
                            int month = Integer.parseInt(sDate);

                            if (month > 1) {
                                sDate = "0" + sDate;
                            }
                        } catch (Exception ignored) {
                        }
                    } else if (sDate.length() == 2) {
                        try {
                            int month = Integer.parseInt(sDate);
                            if (month > 12) {
                                DebugUtils.logDebug(TAG, "month: " + month);
                                sDate = sDate.substring(0, 1);
                                txt_date.setText(sDate);
                                txt_date.setSelection(sDate.length());
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    if (sDate.length() == 3) {
                        try {
                            int year = Integer.parseInt(sDate.substring(2));

                            if (year == 0) {
                                sDate = sDate.substring(0, 2);
                            }
                        } catch (Exception ignored) {
                        }
                    } else if (sDate.length() == 4) {
                        try {
                            int year = Integer.parseInt(sDate.substring(2));

                            SimpleDateFormat sdf = new SimpleDateFormat("yy", Locale.US); // Just the year, with 2 digits
                            int currYear = Integer.parseInt(sdf.format(Calendar.getInstance().getTime()));

                            if (year < currYear) {
                                sDate = sDate.substring(0, 3);
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    if (sDate.length() >= 2) {
                        sb = new StringBuilder(sDate).insert(2, "/");
                    }

                    if (sb != null) {
                        txt_date.setText(sb.toString());
                        txt_date.setSelection(sb.length());
                    }

                    if (txt_date.getText().length() == 5) {
                        txt_cvc.requestFocus();
                    }
                }

                updateUI();
            }
        });

        txt_cvc.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    if (!event.isShiftPressed()) {
                        onSavePressed(null);
                        return true;
                    }
                    return false;
                }

                onSavePressed(null);
                return true;
            }
        });

        txt_number.addTextChangedListener(new TextWatcher() {
            int oldLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sNumber = txt_number.getText().toString().replaceAll("[^0-9]", "");

                if (oldLength != sNumber.length()) {
                    oldLength = sNumber.length();

                    if (sNumber.length() > 1) {
                        txt_number.setTextColor(getResources().getColor(R.color.gray));

                        if (sNumber.length() == CreditCard.getNumberMaxLength(sNumber)) {
                            focused = R.id.txt_date;
                        }

                        txt_number.setText(CreditCard.format(sNumber));
                        txt_number.setSelection(txt_number.getText().length());
                    }
                }

                updateUI();
            }
        });
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) focused = v.getId();
        updateUI();
    }

    public void onClearPressed(View v) {
        txt_number.setText("");
        txt_date.setText("");
        txt_cvc.setText("");
        txt_last4.setText("");

        focused = R.id.txt_number;

        updateUI();
    }

    public void onLast4Pressed(View v) {
        focused = R.id.txt_number;
        updateUI();
    }

    public void onSavePressed(View v) {
        hideSoftKeyboard();

        Card card = new Card(
                txt_number.getText().toString(),
                getMonth(),
                getYear(),
                txt_cvc.getText().toString()
        );

        if (!card.validateNumber()) {
            mDialog = new ConfirmationDialog(EnterCreditCardActivity.this, "Error", "Please enter a valid credit card number");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
            focused = R.id.txt_number;
            updateUI();
        } else if (!card.validateExpiryDate()) {
            mDialog = new ConfirmationDialog(EnterCreditCardActivity.this, "Error", "Please enter a valid expiration date");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
            focused = R.id.txt_date;
            updateUI();
        } else if (!card.validateCVC()) {
            mDialog = new ConfirmationDialog(EnterCreditCardActivity.this, "Error", "Please enter a valid CVC code");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
            focused = R.id.txt_cvc;
            updateUI();
        } else if (!card.validateCard()) {
            mDialog = new ConfirmationDialog(EnterCreditCardActivity.this, "Error", "Please enter a valid credit card details");
            mDialog.addAcceptButton("OK", null);
            mDialog.show();
            focused = R.id.txt_number;
            updateUI();
        } else {
            mProgressDialog = new ProgressDialog(EnterCreditCardActivity.this, R.string.processing_label, true);
            mProgressDialog.show();

            new Stripe().createToken(card, getResources().getString(R.string.stripe_key), new TokenCallback() {
                public void onSuccess(Token token) {
                    mCurrentUser.stripe_token = token.getId();
                    mCurrentUser.card.last4 = txt_last4.getText().toString();
                    mCurrentUser.card.brand = CreditCard.getHolder(txt_number.getText().toString());

                    userDao.updateUser(mCurrentUser);

                    dismissDialog();

                    switch (optOpenScreen) {
                        case COMPLETE_ORDER:
                            if (BentoNowUtils.isValidCompleteOrder(EnterCreditCardActivity.this))
                                BentoNowUtils.openCompleteOrderActivity(EnterCreditCardActivity.this, MenuDao.getCurrentMenu());
                            break;
                        default:
                            onBackPressed();
                            break;
                    }
                    finish();
                }

                public void onError(Exception error) {
                    dismissDialog();
                    mDialog = new ConfirmationDialog(EnterCreditCardActivity.this, "Error", error.getLocalizedMessage());
                    mDialog.addAcceptButton("OK", null);
                    mDialog.show();
                }
            });
        }
    }

    //endregion

    //region Credit Card

    int getMonth() {
        try {
            String[] date = txt_date.getText().toString().split("/");
            return Integer.parseInt(date[0]);
        } catch (Exception ignore) {
        }

        return 0;
    }

    int getYear() {
        try {
            String[] date = txt_date.getText().toString().split("/");
            return Integer.parseInt(date[1]);
        } catch (Exception ignore) {
        }

        return 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                onBackPressed();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        MixpanelUtils.track("Viewed Credit Card Screen");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        AndroidUtil.hideKeyboard(getTxtBentoPrice());
    }

    private void dismissDialog() {
        if (mDialog != null)
            mDialog.dismiss();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    private TextView getTxtBentoPrice() {
        if (txtBentoPrice == null)
            txtBentoPrice = (TextView) findViewById(R.id.txt_bento_price);
        return txtBentoPrice;
    }
}
