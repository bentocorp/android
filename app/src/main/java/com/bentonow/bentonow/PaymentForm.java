package com.bentonow.bentonow;

public interface PaymentForm {
    String getCardNumber();
    String getCvc();
    Integer getExpMonth();
    Integer getExpYear();
    void saved();
}
