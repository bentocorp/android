package com.bentonow.bentonow.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.db.DBAdapter;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.order.OrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose Torres on 10/27/15.
 */
public class BentoDao extends MainDao {

    public final static String TABLE_NAME = "table_bento";
    public final static String ID_PK = "bento_pk";
    static final String TAG = "BentoDao";
    private static final String ITEM_TYPE = "item_type";
    private static final String IS_SOLD_OUT = "bIsSoldOut";
    private static final String UNIT_PRICE = "unit_price";
    public static final String QUERY_TABLE = "" + "CREATE TABLE " + TABLE_NAME + " (" + ID_PK + " INTEGER PRIMARY KEY autoincrement, "
            + ITEM_TYPE + " TEXT, " + IS_SOLD_OUT + " INTEGER, " + UNIT_PRICE + " REAL);";
    public final static String[] FIELDS = {ID_PK, ITEM_TYPE, IS_SOLD_OUT, UNIT_PRICE};
    private DBAdapter dbAdapter;
    private boolean success = true;

    public BentoDao() {
        dbAdapter = new DBAdapter();
    }


    public boolean insertBento(OrderItem mOrder) {
        DebugUtils.logDebug(TAG, "Insert Bento");

        MixpanelUtils.track("Began Building A Bento");

        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mOrder);

        long idInsert = dbAdapter.insert(TABLE_NAME, cValues);
        success = idInsert != -1;

        dbAdapter.setTransacctionSuccesfull();

        return success;
    }

    public OrderItem getNewBento(ConstantUtils.optItemType optItemType) {
        MixpanelUtils.track("Began Building A Bento");
        int iNumBento = SettingsDao.getCurrent().pod_mode.equals("4") ? 4 : 5;

        OrderItem mOrder = new OrderItem();

        mOrder.item_type = optItemType == ConstantUtils.optItemType.CUSTOM_BENTO_BOX ? "CustomerBentoBox" : "AddonList";

        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mOrder);

        mOrder.order_pk = (int) dbAdapter.insert(TABLE_NAME, cValues);

        dbAdapter.setTransacctionSuccesfull();

        switch (optItemType) {
            case CUSTOM_BENTO_BOX:
                DishDao mDishDao = new DishDao();
                for (int a = 0; a < iNumBento; a++) {
                    DishModel mDish = mDishDao.getEmptyDish(mOrder.order_pk, a == 0 ? "main" : String.valueOf("side" + a));
                    mOrder.items.add(mDish);
                }
                break;
        }


        DebugUtils.logDebug(TAG, "New Bento: " + mOrder.order_pk + " Type: " + mOrder.item_type);
        return mOrder;
    }

    public OrderItem updateBento(OrderItem mOrder) {

        String where = ID_PK + " =?";
        String[] whereArgs = new String[]{String.valueOf(mOrder.order_pk)};

        ContentValues cValues = getContentValues(mOrder);

        dbAdapter.begginTransaction();

        long isInserted = (int) dbAdapter.update(TABLE_NAME, cValues, where, whereArgs);

        success = isInserted == 1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Updated Bento :" + success);
        return mOrder;
    }

    public List<OrderItem> getAllBento() {

        List<OrderItem> mListBento = new ArrayList<>();

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, null);

            int _ID_PK = cursor.getColumnIndex(ID_PK);
            int _ITEM_TYPE = cursor.getColumnIndex(ITEM_TYPE);
            int _IS_SOLD_OUT = cursor.getColumnIndex(IS_SOLD_OUT);
            int _UNIT_PRICE = cursor.getColumnIndex(UNIT_PRICE);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                OrderItem mOrder = new OrderItem();
                mOrder.order_pk = (cursor.getInt(_ID_PK));
                mOrder.item_type = getStringFromColumn(cursor.getString(_ITEM_TYPE));
                mOrder.bIsSoldoOut = (cursor.getInt(_IS_SOLD_OUT) == 1 ? true : false);
                mOrder.unit_price = (cursor.getDouble(_UNIT_PRICE));
                mListBento.add(mOrder);
                cursor.moveToNext();
            }

            cursor.close();

            dbAdapter.setTransacctionSuccesfull();

            DishDao mDisDao = new DishDao();

            for (int a = 0; a < mListBento.size(); a++)
                mListBento.get(a).items = mDisDao.getAllDishByOrder(mListBento.get(a).order_pk);

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        DebugUtils.logDebug(TAG, "Get All Bento: " + mListBento.size());

        return mListBento;
    }

    public OrderItem getAddOnBento() {

        OrderItem mOrder = new OrderItem();

        try {

            String conditional = ITEM_TYPE + " = 'AddonList'";

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            int _ID_PK = cursor.getColumnIndex(ID_PK);
            int _ITEM_TYPE = cursor.getColumnIndex(ITEM_TYPE);
            int _IS_SOLD_OUT = cursor.getColumnIndex(IS_SOLD_OUT);
            int _UNIT_PRICE = cursor.getColumnIndex(UNIT_PRICE);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                mOrder.order_pk = (cursor.getInt(_ID_PK));
                mOrder.item_type = getStringFromColumn(cursor.getString(_ITEM_TYPE));
                mOrder.bIsSoldoOut = (cursor.getInt(_IS_SOLD_OUT) == 1 ? true : false);
                mOrder.unit_price = (cursor.getDouble(_UNIT_PRICE));
                cursor.moveToNext();
            }

            cursor.close();


            DebugUtils.logDebug(TAG, "Get Bento: " + mOrder.toString());

        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        } finally {
            dbAdapter.setTransacctionSuccesfull();

            DishDao mDisDao = new DishDao();

            mOrder.items = mDisDao.getAllDishByOrder(mOrder.order_pk);
        }


        return mOrder;
    }

    public boolean removeBento(int iBentoPk) {
        String where = ID_PK + "=?";
        String[] whereArgs = new String[]{String.valueOf(iBentoPk)};

        dbAdapter.begginTransaction();

        long idInsert = dbAdapter.delete(TABLE_NAME, where, whereArgs);
        success = idInsert == 1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Remove Bento: " + success);

        DishDao mDishDao = new DishDao();
        mDishDao.removeAllDishBento(iBentoPk);

        OrderDao mOrderDao = new OrderDao();
        Order mOrder = mOrderDao.getCurrentOrder();

        int iNewOrderItemIndex = 0;
        for (int a = 0; a < mOrder.OrderItems.size(); a++) {
            if (mOrder.OrderItems.get(a).item_type.equals("CustomerBentoBox")) {
                iNewOrderItemIndex = a;
            }
        }
        mOrder.currentOrderItem = iNewOrderItemIndex;
        mOrderDao.updateOrder(mOrder);

        return success;
    }


    private ContentValues getContentValues(OrderItem mOrder) {
        ContentValues cValues = new ContentValues();
        cValues.put(IS_SOLD_OUT, mOrder.bIsSoldoOut);
        cValues.put(ITEM_TYPE, mOrder.item_type);
        cValues.put(UNIT_PRICE, mOrder.unit_price);
        return cValues;
    }


    public boolean isBentoComplete(OrderItem mBento) {
        boolean bIsComplete = true;

        for (int a = 0; a < mBento.items.size(); a++)
            if (mBento.items.get(a) == null || mBento.items.get(a).name.isEmpty())
                return false;

        return bIsComplete;
    }

}
