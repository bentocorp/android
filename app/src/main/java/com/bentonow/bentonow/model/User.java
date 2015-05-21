package com.bentonow.bentonow.model;

import com.orm.SugarRecord;

/**
 * Created by gonzalo on 10/05/2015.
 */
public class User extends SugarRecord<User> {
    public String firstname;
    public String lastname;
    public String email;
    public String phone;
    public String couponcode;
    public String apitoken;
    public String cardbrand;
    public String cardlast4;
    public String stripetoken;
    //FB
    public String fbid;
    public String fbtoken;
    public String fbprofilepic;
    public String fbagerange;
    public String fbgender;
    //

    public User() {}

    public User(String firstname, String lastname, String email, String phone, String couponcode, String apitoken, String cardbrand, String cardlast4) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phone = phone;
        this.couponcode = couponcode;
        this.apitoken = apitoken;
        this.cardbrand = cardbrand;
        this.cardlast4 = cardlast4;
    }

    @Override
    public String toString() {
        super.toString();
        String stripetkn = (stripetoken != null) ? stripetoken : "";
        return "id: "+getId()+", firstname: "+firstname+", lastname: "+lastname+", email: "+email+", phone: "+phone+", apitoken: "+apitoken+", cardbrand: "+cardbrand+", cardlast4: "+cardlast4+", StripeToken: "+stripetkn;
    }
}
