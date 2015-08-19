package com.bentonow.bentonow.Utils;

public class Email {
    public static boolean isValid(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
