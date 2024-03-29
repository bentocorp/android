package com.bentonow.bentonow.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.db.DBAdapter;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.order.OrderAddress;
import com.bentonow.bentonow.model.order.OrderDetails;
import com.bentonow.bentonow.model.order.OrderEta;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.model.order.OrderLocation;
import com.bentonow.bentonow.model.order.OrderStripe;

/**
 * Created by Jose Torres on 10/27/15.
 */
public class OrderDao extends MainDao {

    public final static String TABLE_NAME = "table_order";
    public final static String ID_PK = "order_pk";
    static final String TAG = "OrderDao";
    private static final String ORDER_DETAIL_COUPON_DISCOUNT_CENTS = "OrderDetail_coupon_discount_cents";
    private static final String ORDER_DETAIL_TIP_PERCENTAGE = "OrderDetail_tip_percentage";
    private static final String ORDER_DETAIL_TAX_CENTS = "OrderDetail_tax_cents";
    private static final String ORDER_DETAIL_TIP_CENTS = "OrderDetail_tip_cents";
    private static final String ORDER_DETAIL_TOTAL_CENTS = "OrderDetail_total_cents";
    private static final String ORDER_DETAIL_DELIVERY_PRICE = "OrderDetail_delivery_price";
    private static final String ORDER_DETAIL_ITEMS_TOTAL = "OrderDetail_items_total";
    private static final String ORDER_DETAIL_TAX_PERCENTAGE = "OrderDetail_tax_percentage";
    private static final String ORDER_DETAIL_SUBTOTAL = "OrderDetail_subtotal";
    private static final String ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON = "total_cents_without_coupon";
    private static final String ORDER_ADDRESS_NUMBER = "OrderAddress_number";
    private static final String ORDER_ADDRESS_STREET = "OrderAddress_street";
    private static final String ORDER_ADDRESS_CITY = "OrderAddress_city";
    private static final String ORDER_ADDRESS_STATE = "OrderAddress_state";
    private static final String ORDER_ADDRESS_ZIP = "OrderAddress_zip";
    private static final String ORDER_LOCATION_LAT = "OrderLocation_lat";
    private static final String ORDER_LOCATION_LNG = "Order_Location_lng";
    private static final String ORDER_STRIPE_STRIPE_TOKEN = "OrderStripe_stripeToken";
    private static final String CURRENT_ORDER_ITEM = "currentOrderItem";
    private static final String COUPON_CODE = "CouponCode";
    private static final String IDEMPOTENT_TOKEN = "IdempotentToken";
    private static final String PLATFORM = "Platform";
    private static final String MENU_TYPE = "MenuType";
    private static final String MEAL_NAME = "MealName";
    private static final String ETA_MIN = "EtaMin";
    private static final String ETA_MAX = "EtaMax";
    private static final String ORDER_TYPE = "order_type";
    private static final String KITCHEN = "kitchen";
    private static final String ORDER_AHEAD_ZONE = "OrderAheadZone";
    private static final String FOR_DATE = "for_date";
    private static final String SCHEDULED_WINDOW_START = "scheduled_window_start";
    private static final String SCHEDULED_WINDOW_END = "scheduled_window_end";
    private static final String MENU_ID = "MenuId";
    public static final String QUERY_TABLE = "" + "CREATE TABLE " + TABLE_NAME + " (" + ID_PK + " INTEGER PRIMARY KEY autoincrement, "
            + CURRENT_ORDER_ITEM + " INTEGER, " + ORDER_DETAIL_COUPON_DISCOUNT_CENTS + " REAL, " + ORDER_DETAIL_TIP_PERCENTAGE + " REAL, " + ORDER_DETAIL_TAX_CENTS + " REAL, " + ORDER_DETAIL_TIP_CENTS + " REAL, "
            + ORDER_DETAIL_TOTAL_CENTS + " REAL, " + ORDER_DETAIL_DELIVERY_PRICE + " REAL, " + ORDER_DETAIL_ITEMS_TOTAL + " REAL, " + ORDER_DETAIL_TAX_PERCENTAGE + " REAL, " + ORDER_DETAIL_SUBTOTAL + " REAL, " + ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON + " REAL, "
            + ORDER_ADDRESS_NUMBER + " STRING, " + ORDER_ADDRESS_STREET + " STRING, " + ORDER_ADDRESS_CITY + " STRING, " + ORDER_ADDRESS_STATE + " STRING, " + ORDER_ADDRESS_ZIP + " STRING, "
            + ORDER_LOCATION_LAT + " REAL, " + ORDER_LOCATION_LNG + " REAL," + ORDER_STRIPE_STRIPE_TOKEN + " STRING," + COUPON_CODE + " STRING," + IDEMPOTENT_TOKEN + " STRING," + PLATFORM + " STRING,"
            + MENU_TYPE + " STRING," + MEAL_NAME + " STRING," + ETA_MIN + " STRING," + ETA_MAX + " STRING," + ORDER_TYPE + " STRING," + KITCHEN + " STRING," + ORDER_AHEAD_ZONE + " STRING," + FOR_DATE + " STRING,"
            + SCHEDULED_WINDOW_START + " STRING," + SCHEDULED_WINDOW_END + " STRING," + MENU_ID + " STRING);";
    public final static String[] FIELDS = {ID_PK, CURRENT_ORDER_ITEM, ORDER_DETAIL_COUPON_DISCOUNT_CENTS, ORDER_DETAIL_TIP_PERCENTAGE, ORDER_DETAIL_TAX_CENTS, ORDER_DETAIL_TIP_CENTS, ORDER_DETAIL_TOTAL_CENTS,
            ORDER_DETAIL_DELIVERY_PRICE, ORDER_DETAIL_ITEMS_TOTAL, ORDER_DETAIL_TAX_PERCENTAGE, ORDER_DETAIL_SUBTOTAL, ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON, ORDER_ADDRESS_NUMBER,
            ORDER_ADDRESS_STREET, ORDER_ADDRESS_CITY, ORDER_ADDRESS_STATE, ORDER_ADDRESS_ZIP, ORDER_LOCATION_LAT, ORDER_LOCATION_LNG, ORDER_STRIPE_STRIPE_TOKEN, COUPON_CODE, IDEMPOTENT_TOKEN, PLATFORM, MENU_TYPE,
            MEAL_NAME, ETA_MIN, ETA_MAX, ORDER_TYPE, KITCHEN, ORDER_AHEAD_ZONE, FOR_DATE, SCHEDULED_WINDOW_START, SCHEDULED_WINDOW_END, MENU_ID};
    private DBAdapter dbAdapter;
    private boolean success = true;
    private BentoDao mBentoDao = new BentoDao();

    public OrderDao() {
        dbAdapter = new DBAdapter();
    }

    public static double getPriceByOrder(OrderItem mOrder) {
        double dDishPrice = 0;

        for (DishModel mDish : mOrder.items)
            if (mDish.type.equals("main")) {
                dDishPrice = DishDao.getDefaultPriceBento(mDish.price);
                mOrder.unit_price = dDishPrice;
                break;
            }

        return DishDao.getDefaultPriceBento(dDishPrice);
    }

    public static int countDishesAdded(OrderItem mOrder) {
        int iNumDishes = 0;

        for (DishModel dish : mOrder.items) {
            if (dish != null && !dish.name.isEmpty())
                iNumDishes++;
        }

        return iNumDishes;
    }

    public Order insertNewOrder(Order mOrder) {

        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mOrder);

        mOrder.order_pk = (int) dbAdapter.insert(TABLE_NAME, cValues);

        DebugUtils.logDebug(TAG, "Insert Order: " + mOrder.order_pk);

        dbAdapter.setTransacctionSuccesfull();

        return mOrder;
    }

    public Order getNewOrder(boolean bIsOnDemand) {

        Order mOrder = new Order();
        mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
        mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.ADD_ON));

        if (bIsOnDemand) {
            mOrder.order_type = "1";
            mOrder.OrderDetails.delivery_price = SettingsDao.getCurrent().delivery_price;
        } else {
            mOrder.order_type = "2";
            mOrder.kitchen = MenuDao.gateKeeper.getAvailableServices().mOrderAhead.kitchen;
            mOrder.OrderAheadZone = MenuDao.gateKeeper.getAvailableServices().mOrderAhead.zone;
        }

        OrderEta mOrderEta = new OrderEta();
        mOrderEta.max = String.valueOf(MenuDao.eta_max);
        mOrderEta.min = String.valueOf(MenuDao.eta_min);

        mOrder.Eta = mOrderEta;

        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mOrder);

        mOrder.order_pk = (int) dbAdapter.insert(TABLE_NAME, cValues);

        DebugUtils.logDebug(TAG, "New Order: " + mOrder.order_pk + " OrderType: " + (mOrder.order_type.equals("2") ? "Order Ahead" : "On Demand"));

        dbAdapter.setTransacctionSuccesfull();

        return mOrder;
    }

    public Order getCurrentOrder() {
        DebugUtils.logDebug(TAG, "Get Current Order");

        Order mOrder = null;

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, null);

            int _ID_PK = cursor.getColumnIndex(ID_PK);
            int _CURRENT_ORDER_ITEM = cursor.getColumnIndex(CURRENT_ORDER_ITEM);
            int _ORDER_DETAIL_COUPON_DISCOUNT_CENTS = cursor.getColumnIndex(ORDER_DETAIL_COUPON_DISCOUNT_CENTS);
            int _ORDER_DETAIL_TIP_PERCENTAGE = cursor.getColumnIndex(ORDER_DETAIL_TIP_PERCENTAGE);
            int _ORDER_DETAIL_TAX_CENTS = cursor.getColumnIndex(ORDER_DETAIL_TAX_CENTS);
            int _ORDER_DETAIL_TIP_CENTS = cursor.getColumnIndex(ORDER_DETAIL_TIP_CENTS);
            int _ORDER_DETAIL_TOTAL_CENTS = cursor.getColumnIndex(ORDER_DETAIL_TOTAL_CENTS);
            int _ORDER_DETAIL_DELIVERY_PRICE = cursor.getColumnIndex(ORDER_DETAIL_DELIVERY_PRICE);
            int _ORDER_DETAIL_ITEMS_TOTAL = cursor.getColumnIndex(ORDER_DETAIL_ITEMS_TOTAL);
            int _ORDER_DETAIL_TAX_PERCENTAGE = cursor.getColumnIndex(ORDER_DETAIL_TAX_PERCENTAGE);
            int _ORDER_DETAIL_SUBTOTAL = cursor.getColumnIndex(ORDER_DETAIL_SUBTOTAL);
            int _ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON = cursor.getColumnIndex(ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON);
            int _ORDER_ADDRESS_NUMBER = cursor.getColumnIndex(ORDER_ADDRESS_NUMBER);
            int _ORDER_ADDRESS_STREET = cursor.getColumnIndex(ORDER_ADDRESS_STREET);
            int _ORDER_ADDRESS_CITY = cursor.getColumnIndex(ORDER_ADDRESS_CITY);
            int _ORDER_ADDRESS_STATE = cursor.getColumnIndex(ORDER_ADDRESS_STATE);
            int _ORDER_ADDRESS_ZIP = cursor.getColumnIndex(ORDER_ADDRESS_ZIP);
            int _ORDER_LOCATION_LAT = cursor.getColumnIndex(ORDER_LOCATION_LAT);
            int _ORDER_LOCATION_LNG = cursor.getColumnIndex(ORDER_LOCATION_LNG);
            int _ORDER_STRIPE_STRIPE_TOKEN = cursor.getColumnIndex(ORDER_STRIPE_STRIPE_TOKEN);
            int _COUPON_CODE = cursor.getColumnIndex(COUPON_CODE);
            int _IDEMPOTENT_TOKEN = cursor.getColumnIndex(IDEMPOTENT_TOKEN);
            int _PLATFORM = cursor.getColumnIndex(PLATFORM);
            int _MENU_TYPE = cursor.getColumnIndex(MENU_TYPE);
            int _MEAL_NAME = cursor.getColumnIndex(MEAL_NAME);
            int _ETA_MIN = cursor.getColumnIndex(ETA_MIN);
            int _ETA_MAX = cursor.getColumnIndex(ETA_MAX);
            int _ORDER_TYPE = cursor.getColumnIndex(ORDER_TYPE);
            int _KITCHEN = cursor.getColumnIndex(KITCHEN);
            int _ORDER_AHEAD_ZONE = cursor.getColumnIndex(ORDER_AHEAD_ZONE);
            int _FOR_DATE = cursor.getColumnIndex(FOR_DATE);
            int _SCHEDULED_WINDOW_START = cursor.getColumnIndex(SCHEDULED_WINDOW_START);
            int _SCHEDULED_WINDOW_END = cursor.getColumnIndex(SCHEDULED_WINDOW_END);
            int _MENU_ID = cursor.getColumnIndex(MENU_ID);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                mOrder = new Order();
                mOrder.order_pk = (cursor.getInt(_ID_PK));
                mOrder.currentOrderItem = (cursor.getInt(_CURRENT_ORDER_ITEM));
                mOrder.CouponCode = getStringFromColumn(cursor.getString(_COUPON_CODE));
                mOrder.IdempotentToken = getStringFromColumn(cursor.getString(_IDEMPOTENT_TOKEN));
                mOrder.Platform = getStringFromColumn(cursor.getString(_PLATFORM));
                mOrder.MenuType = getStringFromColumn(cursor.getString(_MENU_TYPE));
                mOrder.MealName = getStringFromColumn(cursor.getString(_MEAL_NAME));
                mOrder.order_type = getStringFromColumn(cursor.getString(_ORDER_TYPE));
                mOrder.kitchen = getStringFromColumn(cursor.getString(_KITCHEN));
                mOrder.OrderAheadZone = getStringFromColumn(cursor.getString(_ORDER_AHEAD_ZONE));
                mOrder.for_date = getStringFromColumn(cursor.getString(_FOR_DATE));
                mOrder.scheduled_window_start = getStringFromColumn(cursor.getString(_SCHEDULED_WINDOW_START));
                mOrder.scheduled_window_end = getStringFromColumn(cursor.getString(_SCHEDULED_WINDOW_END));
                mOrder.MenuId = getStringFromColumn(cursor.getString(_MENU_ID));

                OrderDetails mOrderDetail = new OrderDetails();
                mOrderDetail.coupon_discount_cents = (cursor.getInt(_ORDER_DETAIL_COUPON_DISCOUNT_CENTS));
                mOrderDetail.tip_percentage = (cursor.getDouble(_ORDER_DETAIL_TIP_PERCENTAGE));
                mOrderDetail.tax_cents = (cursor.getDouble(_ORDER_DETAIL_TAX_CENTS));
                mOrderDetail.tip_cents = (cursor.getDouble(_ORDER_DETAIL_TIP_CENTS));
                mOrderDetail.total_cents = (cursor.getDouble(_ORDER_DETAIL_TOTAL_CENTS));
                mOrderDetail.delivery_price = (cursor.getDouble(_ORDER_DETAIL_DELIVERY_PRICE));
                mOrderDetail.items_total = (cursor.getDouble(_ORDER_DETAIL_ITEMS_TOTAL));
                mOrderDetail.tax_percentage = (cursor.getDouble(_ORDER_DETAIL_TAX_PERCENTAGE));
                mOrderDetail.subtotal = (cursor.getDouble(_ORDER_DETAIL_SUBTOTAL));
                mOrderDetail.total_cents_without_coupon = (cursor.getInt(_ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON));

                OrderAddress mOrderAddress = new OrderAddress();
                mOrderAddress.number = getStringFromColumn(cursor.getString(_ORDER_ADDRESS_NUMBER));
                mOrderAddress.street = getStringFromColumn(cursor.getString(_ORDER_ADDRESS_STREET));
                mOrderAddress.city = getStringFromColumn(cursor.getString(_ORDER_ADDRESS_CITY));
                mOrderAddress.state = getStringFromColumn(cursor.getString(_ORDER_ADDRESS_STATE));
                mOrderAddress.zip = getStringFromColumn(cursor.getString(_ORDER_ADDRESS_ZIP));

                OrderLocation mOrderLocation = new OrderLocation();
                mOrderLocation.lat = (cursor.getDouble(_ORDER_LOCATION_LAT));
                mOrderLocation.lng = (cursor.getDouble(_ORDER_LOCATION_LNG));

                OrderStripe mOrderStripe = new OrderStripe();
                mOrderStripe.stripeToken = getStringFromColumn(cursor.getString(_ORDER_STRIPE_STRIPE_TOKEN));

                OrderEta mOrderEta = new OrderEta();
                mOrderEta.max = getStringFromColumn(cursor.getString(_ETA_MAX));
                mOrderEta.min = getStringFromColumn(cursor.getString(_ETA_MIN));

                mOrderDetail.address = mOrderAddress;
                mOrderDetail.coords = mOrderLocation;

                mOrder.OrderDetails = mOrderDetail;
                mOrder.Stripe = mOrderStripe;
                mOrder.Eta = mOrderEta;

                cursor.moveToNext();
            }
            cursor.close();


        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        } finally {
            dbAdapter.setTransacctionSuccesfull();
        }

        if (mOrder != null)
            mOrder.OrderItems = mBentoDao.getAllBento();

        return mOrder;
    }

    public boolean updateOrder(Order mOrder) {
        String where = ID_PK + " =?";
        String[] whereArgs = new String[]{String.valueOf(mOrder.order_pk)};

        ContentValues cValues = getContentValues(mOrder);

        dbAdapter.begginTransaction();

        long isInserted = (int) dbAdapter.update(TABLE_NAME, cValues, where, whereArgs);

        success = isInserted == 1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Update Order: " + success);

        return success;
    }

    private ContentValues getContentValues(Order mOrder) {
        ContentValues cValues = new ContentValues();
        cValues.put(CURRENT_ORDER_ITEM, mOrder.currentOrderItem);
        cValues.put(ORDER_DETAIL_COUPON_DISCOUNT_CENTS, mOrder.OrderDetails.coupon_discount_cents);
        cValues.put(ORDER_DETAIL_TIP_PERCENTAGE, mOrder.OrderDetails.tip_percentage);
        cValues.put(ORDER_DETAIL_TAX_CENTS, mOrder.OrderDetails.tax_cents);
        cValues.put(ORDER_DETAIL_TIP_CENTS, mOrder.OrderDetails.tip_cents);
        cValues.put(ORDER_DETAIL_TOTAL_CENTS, mOrder.OrderDetails.total_cents);
        cValues.put(ORDER_DETAIL_DELIVERY_PRICE, mOrder.OrderDetails.delivery_price);
        cValues.put(ORDER_DETAIL_TAX_CENTS, mOrder.OrderDetails.tax_cents);
        cValues.put(ORDER_DETAIL_ITEMS_TOTAL, mOrder.OrderDetails.items_total);
        cValues.put(ORDER_DETAIL_TAX_PERCENTAGE, mOrder.OrderDetails.tax_percentage);
        cValues.put(ORDER_DETAIL_SUBTOTAL, mOrder.OrderDetails.subtotal);
        cValues.put(ORDER_DETAIL_TOTAL_CENTS_WITHOUT_COUPON, mOrder.OrderDetails.total_cents_without_coupon);
        cValues.put(ORDER_ADDRESS_NUMBER, mOrder.OrderDetails.address.number == null ? "" : mOrder.OrderDetails.address.number);
        cValues.put(ORDER_ADDRESS_STREET, mOrder.OrderDetails.address.street == null ? "" : mOrder.OrderDetails.address.street);
        cValues.put(ORDER_ADDRESS_CITY, mOrder.OrderDetails.address.city == null ? "" : mOrder.OrderDetails.address.city);
        cValues.put(ORDER_ADDRESS_STATE, mOrder.OrderDetails.address.state == null ? "" : mOrder.OrderDetails.address.state);
        cValues.put(ORDER_ADDRESS_ZIP, mOrder.OrderDetails.address.zip == null ? "" : mOrder.OrderDetails.address.zip);
        cValues.put(ORDER_LOCATION_LAT, mOrder.OrderDetails.coords.lat);
        cValues.put(ORDER_LOCATION_LNG, mOrder.OrderDetails.coords.lng);
        cValues.put(ORDER_STRIPE_STRIPE_TOKEN, mOrder.Stripe.stripeToken == null ? "" : mOrder.Stripe.stripeToken);
        cValues.put(COUPON_CODE, mOrder.CouponCode == null ? "" : mOrder.CouponCode);
        cValues.put(IDEMPOTENT_TOKEN, mOrder.IdempotentToken == null ? "" : mOrder.IdempotentToken);
        cValues.put(PLATFORM, mOrder.Platform == null ? "" : mOrder.Platform);
        cValues.put(MENU_TYPE, mOrder.MenuType == null ? "" : mOrder.MenuType);
        cValues.put(MEAL_NAME, mOrder.MealName == null ? "" : mOrder.MealName);
        cValues.put(ETA_MAX, mOrder.Eta == null || mOrder.Eta.max == null ? "" : mOrder.Eta.max);
        cValues.put(ETA_MIN, mOrder.Eta == null || mOrder.Eta.min == null ? "" : mOrder.Eta.min);
        cValues.put(ORDER_TYPE, mOrder.order_type == null ? "" : mOrder.order_type);
        cValues.put(KITCHEN, mOrder.kitchen == null ? "" : mOrder.kitchen);
        cValues.put(ORDER_AHEAD_ZONE, mOrder.OrderAheadZone == null ? "" : mOrder.OrderAheadZone);
        cValues.put(FOR_DATE, mOrder.for_date == null ? "" : mOrder.for_date);
        cValues.put(SCHEDULED_WINDOW_START, mOrder.scheduled_window_start == null ? "" : mOrder.scheduled_window_start);
        cValues.put(SCHEDULED_WINDOW_END, mOrder.scheduled_window_end == null ? "" : mOrder.scheduled_window_end);
        cValues.put(MENU_ID, mOrder.MenuId == null ? "" : mOrder.MenuId);
        return cValues;
    }

    public void clearAllData() {
        DebugUtils.logDebug(TAG, "Clear All Data");
        dbAdapter.deleteAll(TABLE_NAME);
        dbAdapter.deleteAll(BentoDao.TABLE_NAME);
        dbAdapter.deleteAll(DishDao.TABLE_NAME);
    }

   /* public static boolean isSoldOutOrder(OrderItem mOrder) {
        boolean bIsSoldOut = false;
        DishDao mDishDao = new DishDao();
        for (DishModel mDishModel : mOrder.items) {
            if (mDishDao.isSoldOut(mDishModel, false)) {
                bIsSoldOut = true;
                DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
            }
        }

        return bIsSoldOut;
    }*/

    public String calculateSoldOutItems(Order mOrder, boolean bIsMenuOD) {
        String sSoldOutItems = "";
        DishDao mDishDao = new DishDao();

        for (int a = 0; a < mOrder.OrderItems.size(); a++) {
            boolean bIsSoldOut = false;
            for (DishModel mDishModel : mOrder.OrderItems.get(a).items) {
                if (mDishModel != null && mDishModel.name != null && !sSoldOutItems.contains(mDishModel.name) && mDishDao.isSoldOut(mDishModel, false, bIsMenuOD)) {
                    bIsSoldOut = true;
                    sSoldOutItems += "\n- " + mDishModel.name;
                    DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
                }
            }

            mOrder.OrderItems.get(a).bIsSoldoOut = bIsSoldOut;
        }
        return sSoldOutItems;
    }

    public String calculateSoldOutItems(Order mOrder, int iOrderIndex, boolean bIsMenuOD) {
        String sSoldOutItems = "";
        DishDao mDishDao = new DishDao();

        boolean bIsSoldOut = false;
        for (DishModel mDishModel : mOrder.OrderItems.get(iOrderIndex).items) {
            if (mDishModel != null && mDishModel.name != null && !sSoldOutItems.contains(mDishModel.name) && mDishDao.isSoldOut(mDishModel, false, bIsMenuOD)) {
                bIsSoldOut = true;
                sSoldOutItems += "\n- " + mDishModel.name;
                DebugUtils.logDebug("calculateSoldOutItems:", mDishModel.name);
            }
        }

        mOrder.OrderItems.get(iOrderIndex).bIsSoldoOut = bIsSoldOut;

        return sSoldOutItems;
    }


/*    public int countItemsById(int itemId) {
        int count = 0;
        mOrder = getCurrentOrder();

        if (mOrder != null) {
            for (OrderItem orderItem : mOrder.OrderItems) {
                for (DishModel dishModel : orderItem.items) {
                    if (dishModel != null && dishModel.itemId == itemId)
                        ++count;
                }
            }
        }

        return count;
    }*/

    public void calculateOrder(Order mOrder) {
        double items_total = 0;
        double delivery_fee;
        double pre_coupon_subtotal;
        double post_coupon_subtotal;
        double coupon_discount;
        double coupon = mOrder.OrderDetails.coupon_discount_cents;
        double tax;
        double tax_w_o_coupon;
        double subtotal_w_o_coupon;
        double subtotal;
        double tip;
        double total;
        double tax_percent = SettingsDao.getCurrent().tax_percent;
        double total_w_o_coupon;

        coupon_discount = (coupon / 100.0);
        delivery_fee = SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION) ? 0 : mOrder.OrderDetails.delivery_price;

        for (int a = 0; a < mOrder.OrderItems.size(); a++) {
            if (mOrder.OrderItems.get(a).item_type.equals("CustomerBentoBox")) {
                items_total += OrderDao.getPriceByOrder(mOrder.OrderItems.get(a));
            } else if (mOrder.OrderItems.get(a).item_type.equals("AddonList")) {
                for (DishModel mDishModel : mOrder.OrderItems.get(a).items)
                    items_total += mDishModel.price;
            }
        }

        pre_coupon_subtotal = items_total + delivery_fee;

        if (pre_coupon_subtotal - coupon_discount < 0)
            post_coupon_subtotal = 0;
        else
            post_coupon_subtotal = pre_coupon_subtotal - coupon_discount;

        tax_w_o_coupon = pre_coupon_subtotal * (tax_percent / 100);

        subtotal_w_o_coupon = pre_coupon_subtotal + tax_w_o_coupon;

        tax = post_coupon_subtotal * (tax_percent / 100);

        tip = items_total * (mOrder.OrderDetails.tip_percentage / 100);

        subtotal = post_coupon_subtotal + tax;

        if ((subtotal_w_o_coupon + tip) <= 0)
            total_w_o_coupon = 0;
        else if ((subtotal_w_o_coupon + tip) < 0.5)
            total_w_o_coupon = 50;
        else
            total_w_o_coupon = subtotal_w_o_coupon + tip;

        if (pre_coupon_subtotal + tax + tip - coupon_discount <= 0)
            total = 0;
        else if (pre_coupon_subtotal + tax + tip - coupon_discount < 0.5)
            total = 0.5;
        else
            total = pre_coupon_subtotal + tax + tip - coupon_discount;


        mOrder.OrderDetails.items_total = AndroidUtil.round(items_total, 2);
        mOrder.OrderDetails.subtotal = AndroidUtil.round(subtotal, 2);
        mOrder.OrderDetails.tax_cents = AndroidUtil.round(tax, 2) * 100;
        mOrder.OrderDetails.tax_percentage = AndroidUtil.round(tax_percent, 2);
        mOrder.OrderDetails.tip_cents = AndroidUtil.round(tip, 2) * 100;
        mOrder.OrderDetails.total_cents = AndroidUtil.round(total, 2) * 100;
        mOrder.OrderDetails.total_cents_without_coupon = AndroidUtil.round(total_w_o_coupon, 2) * 100;

        DebugUtils.logDebug(TAG, "Item Price: " + mOrder.OrderDetails.items_total);
        DebugUtils.logDebug(TAG, "Delivery Fee: " + delivery_fee);
        DebugUtils.logDebug(TAG, "Delivery Fee w/o coupon: " + mOrder.OrderDetails.delivery_price);
        DebugUtils.logDebug(TAG, "Pre-Coupon Subtotal: " + pre_coupon_subtotal);
        DebugUtils.logDebug(TAG, "Post-Coupon Subtotal: " + post_coupon_subtotal);
        DebugUtils.logDebug(TAG, "Coupon (Promo) Discount: " + coupon_discount);
        DebugUtils.logDebug(TAG, "Coupon (Promo) Discount Cents: " + mOrder.OrderDetails.coupon_discount_cents);
        DebugUtils.logDebug(TAG, "Tax w/o coupon: " + tax_w_o_coupon);
        DebugUtils.logDebug(TAG, "Tax: " + tax);
        DebugUtils.logDebug(TAG, "Tax double: " + SettingsDao.getCurrent().tax_percent);
        DebugUtils.logDebug(TAG, "Subtotal w/o coupon: " + subtotal_w_o_coupon);
        DebugUtils.logDebug(TAG, "Subtotal: " + subtotal);
        DebugUtils.logDebug(TAG, "Tip: " + tip);
        DebugUtils.logDebug(TAG, "Total Cent W O Coupon: " + total_w_o_coupon);
        DebugUtils.logDebug(TAG, "Total: " + total);
        DebugUtils.logDebug(TAG, "Total Discount: " + (total_w_o_coupon - total));

        updateOrder(mOrder);
    }

    public int countCompletedOrders(Order mOrder) {
        int count = 0;

        for (OrderItem item : mOrder.OrderItems) {
            if (item.item_type.equals("CustomerBentoBox")) {
                if (mBentoDao.isBentoComplete(item))
                    ++count;
            } else {
                count = count + item.items.size();
            }
        }

        return count;
    }

    public void cleanUp() {
        DebugUtils.logDebug(TAG, "Clean Up");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU, false);
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.UUID_BENTO, "");
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.POD_MODE, "");
        OrderDao mOrderDao = new OrderDao();
        mOrderDao.clearAllData();
    }

}
