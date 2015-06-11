package com.bentonow.bentonow.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bentonow.bentonow.InputFilterMinMax;
import com.bentonow.bentonow.PaymentForm;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.EnterCreditCardActivity;
import com.bentonow.bentonow.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PaymentFormFragment extends Fragment implements PaymentForm {

    private static final String TAG = "PaymentFormFragment";
    TextView saveButton;
    EditText cardNumber;
    //TextView cardType;
    ImageView ic_card;
    EditText cardDigits;
    EditText cvc;
    EditText monthSpinner;
    private LinearLayout second_step;
    private ImageView btn_clear;
    private boolean completed;
    private boolean isPreparedForSending = true;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_form_fragment, container, false);

        //final AQuery aq = new AQuery(getActivity(), view);

        this.saveButton = (TextView) view.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(completed) {
                    hideSoftKeyboard(getActivity());
                    saveForm();
                }
            }
        });

        this.ic_card = (ImageView) view.findViewById(R.id.ic_card);
        //this.cardType = (TextView) view.findViewById(R.id.cardType);
        this.cardDigits = (EditText) view.findViewById(R.id.cardDigits);
        this.cardNumber = (EditText) view.findViewById(R.id.number);
        this.cvc = (EditText) view.findViewById(R.id.cvc);
        this.monthSpinner = (EditText) view.findViewById(R.id.expMonth);
        this.second_step = (LinearLayout) view.findViewById(R.id.second_step);
        this.btn_clear = (ImageView)view.findViewById(R.id.btn_clear);

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardDigits.setText("");
                cardNumber.setText("");
                cvc.setText("");
                monthSpinner.setText("");

                cardNumber.requestFocus();
                cardNumber.setVisibility(View.VISIBLE);
                cardDigits.setVisibility(View.GONE);
                cvc.setVisibility(View.GONE);
                monthSpinner.setVisibility(View.GONE);
                second_step.setVisibility(View.GONE);
            }
        });

        ////////////////////
        this.cvc.setFilters(new InputFilter[]{new InputFilterMinMax("1", "999")});
        ////////////////////

        this.cvc.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean b) {
                User user = User.findById(User.class, (long) 1);

                if (b) {
                    if (user.cardbrand.equals("American Express")) {
                        ic_card.setImageResource(R.drawable.card_cvv_amex);
                    } else {
                        ic_card.setImageResource(R.drawable.card_cvv);
                    }
                } else {
                    if (user.cardbrand.equals("American Express")) {
                        ic_card.setImageResource(R.drawable.card_amex);
                    } else if (user.cardbrand.equals("Mastercard")) {
                        ic_card.setImageResource(R.drawable.card_mastercard);
                    } else if (user.cardbrand.equals("Visa")) {
                        ic_card.setImageResource(R.drawable.card_visa);
                    } else if (user.cardbrand.equals("Diners Club")) {
                        ic_card.setImageResource(R.drawable.card_diners);
                    } else if (user.cardbrand.equals("Discover Card")) {
                        ic_card.setImageResource(R.drawable.card_discover);
                    } else if (user.cardbrand.equals("JCB")) {
                        ic_card.setImageResource(R.drawable.card_jcb);
                    } else {
                        ic_card.setImageResource(R.drawable.card_empty);
                    }
                }
            }
        });

        this.cvc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cvc.getText().length() == 0) {
                    monthSpinner.requestFocus();
                } else {
                    chechIfCompleted();
                }
            }
        });

        this.monthSpinner.addTextChangedListener(new TextWatcher() {
            int oldLength;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String charSequence = monthSpinner.getText().toString().replaceAll("[^0-9]", "");
                StringBuilder sb = null;

                if (charSequence.length() == 0) {
                    cardNumber.requestFocus();
                    cardNumber.setVisibility(View.VISIBLE);
                    cardDigits.setVisibility(View.GONE);
                    cvc.setVisibility(View.GONE);
                    monthSpinner.setVisibility(View.GONE);
                    second_step.setVisibility(View.GONE);
                } else if (oldLength != charSequence.length()) {
                    oldLength = charSequence.length();

                    if (charSequence.length() == 1) {
                        try {
                            int month = Integer.parseInt(charSequence);

                            if (month > 1) {
                                charSequence = "0" + charSequence;
                            }
                        } catch (Exception e) {}
                    } else if (charSequence.length() == 2) {
                        try {
                            int month = Integer.parseInt(charSequence);
                            if (month > 12) {
                                Log.i(TAG,"month: "+month);
                                charSequence = charSequence.substring(0, 1);
                                monthSpinner.setText(charSequence);
                                monthSpinner.setSelection(charSequence.length());
                            }
                        } catch (Exception e) {}
                    }

                    if (charSequence.length() == 3) {
                        try {
                            int year = Integer.parseInt(charSequence.substring(2));

                            if (year == 0) {
                                charSequence = charSequence.substring(0, 2);
                            }
                        } catch (Exception e) {}
                    } else if (charSequence.length() == 4) {
                        try {
                            int year = Integer.parseInt(charSequence.substring(2));

                            SimpleDateFormat sdf = new SimpleDateFormat("yy"); // Just the year, with 2 digits
                            int currYear = Integer.parseInt(sdf.format(Calendar.getInstance().getTime()));

                            if (year < currYear) {
                                charSequence = charSequence.substring(0, 3);
                            }
                        } catch (Exception e) {}
                    }

                    if (charSequence.length() >= 2) {
                        sb = new StringBuilder(charSequence)
                                .insert(2, "/");
                    }

                    if (sb != null) {
                        monthSpinner.setText(sb.toString());
                        monthSpinner.setSelection(sb.length());
                    }

                    if (monthSpinner.getText().length() == 5) {
                        cvc.requestFocus();
                    }
                }

                chechIfCompleted();
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        this.cardDigits.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    cardNumber.requestFocus();
                    cardNumber.setVisibility(View.VISIBLE);
                    cardDigits.setVisibility(View.GONE);
                    cvc.setVisibility(View.GONE);
                    monthSpinner.setVisibility(View.GONE);
                    second_step.setVisibility(View.GONE);
                }
            }
        });

        this.cvc.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    // if shift key is down, then we want to insert the '\n' char in the TextView;
                    // otherwise, the default action is to send the message.
                    if (!event.isShiftPressed()) {
                        if (isPreparedForSending) {
                            if (completed) {
                                hideSoftKeyboard(getActivity());
                                saveForm();
                            }
                        }
                        return true;
                    }
                    return false;
                }

                if (isPreparedForSending) {
                    if (completed) {
                        hideSoftKeyboard(getActivity());
                        saveForm();
                    }
                }
                return true;
            }
        });

        this.cardNumber.addTextChangedListener(new TextWatcher() {
            int oldLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String charSequence = cardNumber.getText().toString().replaceAll("[^0-9]", "");
                StringBuilder sb = null;

                if (oldLength != charSequence.length()) {
                    oldLength = charSequence.length();

                    if (charSequence.length() > 1) {
                        String cardCode = charSequence.substring(0, 2);
                        cardNumber.setTextColor(getResources().getColor(R.color.gray));
                        if (cardCode.equals("34") || cardCode.equals("37")) {
                            //cardType.setText("American Express");
                            ic_card.setImageResource(R.drawable.card_amex);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "American Express";
                            user.save();

                            if (charSequence.length() == 15) {
                                hideFullNumber();
                            }
                        } else if (cardCode.equals("36") || cardCode.equals("51") || cardCode.equals("52") || cardCode.equals("53") || cardCode.equals("54") || cardCode.equals("55")) {
                            //cardType.setText("Mastercard");
                            ic_card.setImageResource(R.drawable.card_mastercard);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "Mastercard";
                            user.save();

                            if (charSequence.length() == 16) {
                                hideFullNumber();
                            }
                        } else if (cardCode.startsWith("4")) {
                            //cardType.setText("Visa");
                            ic_card.setImageResource(R.drawable.card_visa);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "Visa";
                            user.save();

                            if (charSequence.length() == 16) {
                                hideFullNumber();
                            }
                        } else if (cardCode.equals("60") || cardCode.equals("65")) {
                            //cardType.setText("Discover Card");

                            ic_card.setImageResource(R.drawable.card_discover);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "Discover Card";
                            user.save();

                            if (charSequence.length() == 16) {
                                hideFullNumber();
                            }
                        } else if (cardCode.equals("30") || cardCode.equals("38")) {
                            //cardType.setText("Diners Club");

                            ic_card.setImageResource(R.drawable.card_diners);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "Diners Club";
                            user.save();

                            if (charSequence.length() == 14) {
                                hideFullNumber();
                            }
                        } else if (cardCode.equals("35")) {
                            //cardType.setText("JCB");
                            ic_card.setImageResource(R.drawable.card_jcb);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "JCB";
                            user.save();

                            if (charSequence.length() == 16) {
                                hideFullNumber();
                            }
                        } else {
                            //cardType.setText("Card Type");
                            ic_card.setImageResource(R.drawable.card_empty);

                            User user = User.findById(User.class, (long) 1);
                            user.cardbrand = "";
                            user.save();

                            if (charSequence.length() == 16) {
                                hideFullNumber();
                            }
                        }

                        if (charSequence.length() >= 13) {
                            sb = new StringBuilder(charSequence)
                                    .insert(4, " ")
                                    .insert(9, " ")
                                    .insert(14, " ");
                        } else if (charSequence.length() >= 8) {
                            sb = new StringBuilder(charSequence)
                                    .insert(4, " ")
                                    .insert(9, " ");
                        } else if (charSequence.length() >= 4) {
                            sb = new StringBuilder(charSequence)
                                    .insert(4, " ");
                        }

                        if (sb != null) {
                            cardNumber.setText(sb.toString());
                            cardNumber.setSelection(sb.length());
                        }
                    } else {
                        ic_card.setImageResource(R.drawable.card_empty);
                    }
                }

                chechIfCompleted();
            }
        });

        return view;
    }

    private void chechIfCompleted() {
        completed = true;
        if( cardNumber.getText().toString().length() < 14){
            completed = false;
        }
        if( cvc.getText().toString().length() < 3 ){
            completed = false;
        }
        if( monthSpinner.getText().toString().length() < 5 ){
            completed = false;
        }

        Log.i(TAG, "COMPLETED: " + completed);
        if( completed ){
            saveButton.setBackgroundResource(R.drawable.bg_green_cornered);
        }else{
            saveButton.setBackgroundResource(R.drawable.btn_dark_gray);
        }
    }

    void hideFullNumber () {
        if (!isValidLuhn(cardNumber.getText().toString())) {
            cardNumber.setTextColor(getResources().getColor(R.color.orange));
            return;
        }

        cardNumber.clearFocus();

        if (cardNumber.getText().length() >= 4){
            String last4 = cardNumber.getText().toString().substring(cardNumber.getText().length()-4);
            cardDigits.setText(last4);
            User user = User.findById(User.class,(long)1);

            if (user != null) {
                user.cardlast4 = last4;
                user.save();
            }
        }

        cardNumber.setVisibility(View.GONE);
        monthSpinner.setVisibility(View.VISIBLE);
        monthSpinner.requestFocus();
        cvc.setVisibility(View.VISIBLE);
        cardDigits.setVisibility(View.VISIBLE);
        second_step.setVisibility(View.VISIBLE);
    }

    private boolean isValidLuhn(String _number) {
        _number = _number.replaceAll("[^0-9]", "");

        boolean odd = true;
        int sum = 0;
        String[] digits = new String[_number.length()];

        for (int i=0; i<_number.length(); ++i) {
            digits[i] = _number.charAt(i) + "";
        }

        for (int i=_number.length(); i>0; --i) {
            int digit = Integer.parseInt(digits[i-1]);
            if ((odd = !odd)) digit *=2;
            if (digit > 9) digit -= 9;
            sum += digit;
        }

        return sum % 10 == 0;
    }

    @Override
    public String getCardNumber() {
        Log.i("PaymentForm", this.cardNumber.getText().toString());
        return this.cardNumber.getText().toString();
    }

    @Override
    public String getCvc() {
        return this.cvc.getText().toString();
    }

    @Override
    public Integer getExpMonth() {
        try {
            return Integer.parseInt(this.monthSpinner.getText().toString().substring(0, 2));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public Integer getExpYear() {
        try {
            return Integer.parseInt(this.monthSpinner.getText().toString().substring(3));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void saveForm() {
        if (isPreparedForSending) {
            ((EnterCreditCardActivity) getActivity()).saveCreditCard(this);
            isPreparedForSending = false;
        }
    }

    private Integer getInteger(EditText editText) {
        try {
            return Integer.parseInt(editText.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer getInteger(Spinner spinner) {
        try {
            return Integer.parseInt(spinner.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
