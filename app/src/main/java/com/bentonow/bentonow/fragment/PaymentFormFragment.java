package com.bentonow.bentonow.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.auth.FacebookHandle;
import com.bentonow.bentonow.InputFilterMinMax;
import com.bentonow.bentonow.PaymentForm;
import com.bentonow.bentonow.R;
import com.bentonow.bentonow.EnterCreditCardActivity;
import com.bentonow.bentonow.model.User;

import org.json.JSONObject;

public class PaymentFormFragment extends Fragment implements PaymentForm {

    private static final String TAG = "PaymentFormFragment";
    TextView saveButton;
    EditText cardNumber;
    //TextView cardType;
    ImageView ic_card;
    EditText cardDigits;
    EditText cvc;
    EditText monthSpinner;
    EditText yearSpinner;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.payment_form_fragment, container, false);

        final AQuery aq = new AQuery(getActivity(), view);

        this.saveButton = (TextView) view.findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(getActivity());
                saveForm(view);
            }
        });

        this.ic_card = (ImageView) view.findViewById(R.id.ic_card);
        //this.cardType = (TextView) view.findViewById(R.id.cardType);
        this.cardDigits = (EditText) view.findViewById(R.id.cardDigits);
        this.cardNumber = (EditText) view.findViewById(R.id.number);
        this.cvc = (EditText) view.findViewById(R.id.cvc);
        this.monthSpinner = (EditText) view.findViewById(R.id.expMonth);
        this.yearSpinner = (EditText) view.findViewById(R.id.expYear);

        ////////////////////
        this.monthSpinner.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "12")});
        this.yearSpinner.setFilters(new InputFilter[]{new InputFilterMinMax("1", "99")});
        this.cvc.setFilters(new InputFilter[]{new InputFilterMinMax("1", "999")});
        ////////////////////

        TextWatcher textWacher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                chechIfCompleted();
            }
        };

        this.cvc.addTextChangedListener(textWacher);
        this.yearSpinner.addTextChangedListener(textWacher);
        this.monthSpinner.addTextChangedListener(textWacher);

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
                    yearSpinner.setVisibility(View.GONE);
                }
            }
        });

        this.cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                chechIfCompleted();
                String charSequence = cardNumber.getText().toString();
                if (charSequence.length() > 1) {
                    String cardCode = charSequence.substring(0, 2);

                    if (cardCode.equals("34") || cardCode.equals("37")) {
                        //cardType.setText("American Express");
                        if (charSequence.length() == 15) {
                            hideFullNumber();
                        }
                    } else if (cardCode.equals("36") || cardCode.equals("51") || cardCode.equals("52") || cardCode.equals("53") || cardCode.equals("54") || cardCode.equals("55")) {
                        //cardType.setText("Mastercard");
                        if (charSequence.length() == 16) {
                            hideFullNumber();
                        }
                    } else if (cardCode.startsWith("4")) {
                        //cardType.setText("Visa");
                        ic_card.setImageResource(R.drawable.card_visa);
                        User user = User.findById(User.class,(long)1);
                        user.cardbrand = "Visa";
                        user.save();
                        if (charSequence.length() == 16) {
                            hideFullNumber();
                        }
                    } else if (cardCode.equals("60") || cardCode.equals("65")) {
                        //cardType.setText("Discover Card");

                        if (charSequence.length() == 16) {
                            hideFullNumber();
                        }
                    } else if (cardCode.equals("30") || cardCode.equals("38")) {
                        //cardType.setText("Diners Club");

                        if (charSequence.length() == 14) {
                            hideFullNumber();
                        }
                    } else if (cardCode.equals("35")) {
                        //cardType.setText("JBC");

                        if (charSequence.length() == 16) {
                            hideFullNumber();
                        }
                    } else {
                        //cardType.setText("Card Type");
                    }
                }
            }
        });

        return view;
    }

    private void chechIfCompleted() {
        boolean completed = true;
        if( cardNumber.getText().toString().length() != 16 ){
            completed = false;
        }
        if( cvc.getText().toString().length() < 3 ){
            completed = false;
        }
        if( yearSpinner.getText().toString().length() < 2 ){
            completed = false;
        }
        if( monthSpinner.getText().toString().length() < 2 ){
            completed = false;
        }

        Log.i(TAG,"COMPLETED: "+completed);
        if( completed ){
            saveButton.setBackgroundResource(R.drawable.bg_green_cornered);
        }else{
            saveButton.setBackgroundResource(R.drawable.btn_dark_gray);
        }
    }

    void hideFullNumber () {
        cardNumber.clearFocus();

        if (cardNumber.getText().length() >= 4){
            String last4 = cardNumber.getText().toString().substring(cardNumber.getText().length()-4);
            cardDigits.setText(last4);
            User user = User.findById(User.class,(long)1);
            user.cardlast4 = last4;
            user.save();
        }

        cardNumber.setVisibility(View.GONE);
        cvc.setVisibility(View.VISIBLE);
        monthSpinner.setVisibility(View.VISIBLE);
        yearSpinner.setVisibility(View.VISIBLE);
        cardDigits.setVisibility(View.VISIBLE);
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
        return getInteger(this.monthSpinner);
    }

    @Override
    public Integer getExpYear() {
        return getInteger(this.yearSpinner);
    }

    public void saveForm(View button) {
        ((EnterCreditCardActivity)getActivity()).saveCreditCard(this);
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
