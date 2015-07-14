package com.bentonow.bentonow.Utils;

import java.util.Arrays;

public class CreditCard {
    static class CODE {
        static String[] AMEX = new String[]{"34", "37"};
        static String[] MASTERCARD = new String[]{"36", "51", "52", "53", "54", "55"};
        static String[] VISA = new String[]{"4"};
        static String[] DISCOVER_CARD = new String[]{"60", "65"};
        static String[] DINERS_CLUB = new String[]{"30", "38"};
        static String[] JCB = new String[]{"35"};
    }

    public static String format (String credit_card_number) {
        if (credit_card_number == null) return "";
        if (credit_card_number.length() <= 1) return credit_card_number;

        credit_card_number = credit_card_number.replaceAll("[^0-9]", "");

        if (isAmex(credit_card_number)) {
            // American Express
            return formatAmex(credit_card_number);
        } else if (isMastercard(credit_card_number)) {
            // Mastercard
            return formatMastercard(credit_card_number);
        } else if (isVisa(credit_card_number)) {
            // Visa
            return formatVisa(credit_card_number);
        } else if (isDiscover(credit_card_number)) {
            // Discover Card
            return formatDiscoverCard(credit_card_number);
        } else if (isDinersClub(credit_card_number)) {
            // Diners Club
            return formatDinersClub(credit_card_number);
        } else if (isJCB(credit_card_number)) {
            // JCB
            return formatJCB(credit_card_number);
        }

        return formatGeneric(credit_card_number);
    }

    public static boolean isAmex (String credit_card_number) {
        return is(CODE.AMEX, credit_card_number);
    }

    public static boolean isMastercard (String credit_card_number) {
        return is (CODE.MASTERCARD, credit_card_number);
    }

    public static boolean isVisa (String credit_card_number) {
        return is (CODE.VISA, credit_card_number);
    }

    public static boolean isDiscover (String credit_card_number) {
        return is (CODE.DISCOVER_CARD, credit_card_number);
    }

    public static boolean isDinersClub (String credit_card_number) {
        return is (CODE.DINERS_CLUB, credit_card_number);
    }

    public static boolean isJCB (String credit_card_number) {
        return is (CODE.JCB, credit_card_number);
    }

    static boolean is (String[] codes, String credit_card_number) {
        if (credit_card_number == null) return false;
        if (credit_card_number.length() <= 1) return false;

        credit_card_number = credit_card_number.replaceAll("[^0-9]", "");
        String card_code = credit_card_number.substring(0, 2);

        return Arrays.asList(codes).contains(card_code);
    }

    static String formatAmex (String credit_card_number)  {
        if (credit_card_number.length() >= 11) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ")
                    .insert(11, " ").toString();
        } else if (credit_card_number.length() >= 4) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ").toString();
        }

        return credit_card_number;
    }

    static String formatMastercard (String credit_card_number)  {
        return formatGeneric(credit_card_number);
    }

    static String formatVisa (String credit_card_number)  {
        return formatGeneric(credit_card_number);
    }

    static String formatDiscoverCard (String credit_card_number)  {
        return formatGeneric(credit_card_number);
    }

    static String formatDinersClub (String credit_card_number)  {
        if (credit_card_number.length() >= 11) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ")
                    .insert(11, " ").toString();
        } else if (credit_card_number.length() >= 4) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ").toString();
        }

        return credit_card_number;
    }

    static String formatJCB (String credit_card_number)  {
        return formatGeneric(credit_card_number);
    }

    static String formatGeneric (String credit_card_number)  {
        if (credit_card_number.length() >= 13) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ")
                    .insert(9, " ")
                    .insert(14, " ").toString();
        } else if (credit_card_number.length() >= 8) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ")
                    .insert(9, " ").toString();
        } else if (credit_card_number.length() >= 4) {
            return new StringBuilder(credit_card_number)
                    .insert(4, " ").toString();
        }

        return credit_card_number;
    }
}
