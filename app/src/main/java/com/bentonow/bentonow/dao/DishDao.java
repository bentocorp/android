package com.bentonow.bentonow.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.db.DBAdapter;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jose Torres on 27/09/15.
 */
public class DishDao extends MainDao {

    public final static String TABLE_NAME = "table_dish";
    public final static String ID_PK = "dish_pk";
    static final String TAG = "DishDao";
    private static final String ITEM_ID = "itemId";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String IMAGE1 = "image1";
    private static final String MAX_PER_ORDER = "max_per_order";
    private static final String PRICE = "price";
    private static final String QTY = "qty";
    private static final String BENTO_PK = "bento_pk";
    public static final String QUERY_TABLE = "" + "CREATE TABLE " + TABLE_NAME + " (" + ID_PK + " INTEGER PRIMARY KEY autoincrement, "
            + ITEM_ID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + TYPE + " TEXT, " + IMAGE1 + " TEXT," + MAX_PER_ORDER + " TEXT, " + BENTO_PK + " INTEGER, " + PRICE + " REAL,"
            + QTY + " TEXT);";
    public final static String[] FIELDS = {ID_PK, ITEM_ID, NAME, DESCRIPTION, TYPE, IMAGE1,
            MAX_PER_ORDER, BENTO_PK, PRICE, QTY};
    private DBAdapter dbAdapter;
    private boolean success = true;


    public DishDao() {
        dbAdapter = new DBAdapter();
    }

    public static DishModel clone(DishModel mDishModel) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(gson.toJson(mDishModel), DishModel.class);
    }

    public static double getLowestMainPrice(Menu mMenu) {
        double dMinPrice = 0;

        for (DishModel dishModel : mMenu.dishModels)
            if (dishModel.type.equals("main")) {
                dishModel.price = DishDao.getDefaultPriceBento(dishModel.price);
                if (dMinPrice == 0)
                    dMinPrice = dishModel.price;
                else {
                    if (dMinPrice > dishModel.price)
                        dMinPrice = dishModel.price;
                }
            }

        return dMinPrice;
    }

    public static double getDefaultPriceBento(double dPrice) {
        if (dPrice <= 0)
            return SettingsDao.getCurrent().price;
        else
            return dPrice;
    }

    public boolean insertDish(DishModel mDish) {
        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mDish);

        mDish.dish_pk = (int) dbAdapter.insert(TABLE_NAME, cValues);

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "New Dish: " + mDish.dish_pk);

        return success;
    }

    public DishModel getEmptyDish(int iOrderPk, String sType) {
        DishModel mDish = new DishModel();
        mDish.bento_pk = iOrderPk;
        mDish.type = sType;

        dbAdapter.begginTransaction();

        ContentValues cValues = getContentValues(mDish);

        mDish.dish_pk = (int) dbAdapter.insert(TABLE_NAME, cValues);

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "New Dish: " + mDish.dish_pk);

        return mDish;
    }

    public List<DishModel> getAllDishByOrder(int iOrderPk) {
        List<DishModel> mListDish = new ArrayList<>();

        String conditional = BENTO_PK + " = " + iOrderPk;

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            int _ID_PK = cursor.getColumnIndex(ID_PK);
            int _ITEM_ID = cursor.getColumnIndex(ITEM_ID);
            int _NAME = cursor.getColumnIndex(NAME);
            int _DESCRIPTION = cursor.getColumnIndex(DESCRIPTION);
            int _TYPE = cursor.getColumnIndex(TYPE);
            int _IMAGE1 = cursor.getColumnIndex(IMAGE1);
            int _MAX_PER_ORDER = cursor.getColumnIndex(MAX_PER_ORDER);
            int _PRICE = cursor.getColumnIndex(PRICE);
            int _BENTO_PK = cursor.getColumnIndex(BENTO_PK);
            int _QTY = cursor.getColumnIndex(QTY);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                DishModel mDish = new DishModel();
                mDish.dish_pk = (cursor.getInt(_ID_PK));
                mDish.itemId = (cursor.getInt(_ITEM_ID));
                mDish.name = getStringFromColumn(cursor.getString(_NAME));
                mDish.description = getStringFromColumn(cursor.getString(_DESCRIPTION));
                mDish.type = getStringFromColumn(cursor.getString(_TYPE));
                mDish.image1 = getStringFromColumn(cursor.getString(_IMAGE1));
                mDish.max_per_order = getStringFromColumn(cursor.getString(_MAX_PER_ORDER));
                mDish.price = (cursor.getDouble(_PRICE));
                mDish.unit_price = (cursor.getDouble(_PRICE));
                mDish.bento_pk = (cursor.getInt(_BENTO_PK));
                mDish.qty = getStringFromColumn(cursor.getString(_QTY));
                mListDish.add(mDish);

                cursor.moveToNext();
            }

            cursor.close();

            dbAdapter.setTransacctionSuccesfull();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        return mListDish;
    }

    public int getNumDishes() {
        int iNum = 0;

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, null);

            int _NAME = cursor.getColumnIndex(NAME);

            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (!getStringFromColumn(cursor.getString(_NAME)).isEmpty())
                    iNum++;
                cursor.moveToNext();
            }

            cursor.close();

            dbAdapter.setTransacctionSuccesfull();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, ex);
        }

        DebugUtils.logDebug(TAG, "getNumDishes: " + iNum);

        return iNum;
    }

    public List<DishModel> getAllDishByType(ConstantUtils.optDishType optDish) {

        String sType = "";
        switch (optDish) {
            case MAIN:
                sType = "main";
                break;
            case SIDE:
                sType = "side";
                break;
            case ADDON:
                sType = "addon";
                break;
        }


        List<DishModel> mListDish = new ArrayList<>();

        String conditional = TYPE + " = '" + sType + "'";

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            int _ID_PK = cursor.getColumnIndex(ID_PK);
            int _ITEM_ID = cursor.getColumnIndex(ITEM_ID);
            int _NAME = cursor.getColumnIndex(NAME);
            int _DESCRIPTION = cursor.getColumnIndex(DESCRIPTION);
            int _TYPE = cursor.getColumnIndex(TYPE);
            int _IMAGE1 = cursor.getColumnIndex(IMAGE1);
            int _MAX_PER_ORDER = cursor.getColumnIndex(MAX_PER_ORDER);
            int _PRICE = cursor.getColumnIndex(PRICE);
            int _BENTO_PK = cursor.getColumnIndex(BENTO_PK);
            int _QTY = cursor.getColumnIndex(QTY);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                DishModel mDish = new DishModel();
                mDish.dish_pk = (cursor.getInt(_ID_PK));
                mDish.itemId = (cursor.getInt(_ITEM_ID));
                mDish.name = getStringFromColumn(cursor.getString(_NAME));
                mDish.description = getStringFromColumn(cursor.getString(_DESCRIPTION));
                mDish.type = getStringFromColumn(cursor.getString(_TYPE));
                mDish.image1 = getStringFromColumn(cursor.getString(_IMAGE1));
                mDish.max_per_order = getStringFromColumn(cursor.getString(_MAX_PER_ORDER));
                mDish.price = (cursor.getDouble(_PRICE));
                mDish.unit_price = (cursor.getDouble(_PRICE));
                mDish.bento_pk = (cursor.getInt(_BENTO_PK));
                mDish.qty = getStringFromColumn(cursor.getString(_QTY));
                mDish.count_max = 1;

                boolean bAdded = false;
                for (int a = 0; a < mListDish.size(); a++) {
                    if (mListDish.get(a).itemId == mDish.itemId) {
                        bAdded = true;
                        mListDish.get(a).count_max = mListDish.get(a).count_max + 1;
                        break;
                    }
                }


                if (!bAdded) {
                    mListDish.add(mDish);
                }

                cursor.moveToNext();
            }

            cursor.close();

        } catch (Exception ex) {
            DebugUtils.logError(TAG, "getAllDishByOrder: " + ex.toString());
        } finally {
            dbAdapter.setTransacctionSuccesfull();
        }

        return mListDish;
    }

    public boolean hasDishByType(ConstantUtils.optDishType optDish) {
        boolean hasDish;

        String sType = "";
        switch (optDish) {
            case MAIN:
                sType = "main";
                break;
            case SIDE:
                sType = "side";
                break;
            case ADDON:
                sType = "addon";
                break;
        }


        String conditional = TYPE + " = '" + sType + "'";

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            cursor.moveToFirst();

            hasDish = cursor.getCount() > 0;

            cursor.close();

        } catch (Exception ex) {
            hasDish = false;
            DebugUtils.logError(TAG, ex);
        } finally {
            dbAdapter.setTransacctionSuccesfull();
        }

        return hasDish;
    }

    public DishModel getDishItem(int iPk) {
        DishModel mDish = null;

        String conditional = ID_PK + " = " + iPk;

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            int _ID_PK = cursor.getColumnIndex(ID_PK);
            int _ITEM_ID = cursor.getColumnIndex(ITEM_ID);
            int _NAME = cursor.getColumnIndex(NAME);
            int _DESCRIPTION = cursor.getColumnIndex(DESCRIPTION);
            int _TYPE = cursor.getColumnIndex(TYPE);
            int _IMAGE1 = cursor.getColumnIndex(IMAGE1);
            int _MAX_PER_ORDER = cursor.getColumnIndex(MAX_PER_ORDER);
            int _PRICE = cursor.getColumnIndex(PRICE);
            int _BENTO_PK = cursor.getColumnIndex(BENTO_PK);
            int _QTY = cursor.getColumnIndex(QTY);


            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                mDish = new DishModel();
                mDish.dish_pk = (cursor.getInt(_ID_PK));
                mDish.itemId = (cursor.getInt(_ITEM_ID));
                mDish.name = getStringFromColumn(cursor.getString(_NAME));
                mDish.description = getStringFromColumn(cursor.getString(_DESCRIPTION));
                mDish.type = getStringFromColumn(cursor.getString(_TYPE));
                mDish.image1 = getStringFromColumn(cursor.getString(_IMAGE1));
                mDish.max_per_order = getStringFromColumn(cursor.getString(_MAX_PER_ORDER));
                mDish.price = (cursor.getDouble(_PRICE));
                mDish.unit_price = (cursor.getDouble(_PRICE));
                mDish.bento_pk = (cursor.getInt(_BENTO_PK));
                mDish.qty = getStringFromColumn(cursor.getString(_QTY));
                cursor.moveToNext();
            }

            cursor.close();

            dbAdapter.setTransacctionSuccesfull();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, "getDishItem: " + ex.toString());
        }

        return mDish;
    }

    public boolean updateDishItem(DishModel mDish) {
        String where = ID_PK + " =?";
        String[] whereArgs = new String[]{String.valueOf(mDish.dish_pk)};

        ContentValues cValues = getContentValues(mDish);

        dbAdapter.begginTransaction();

        long isInserted = dbAdapter.update(TABLE_NAME, cValues, where, whereArgs);

        success = isInserted == 1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Updated Dish: " + success);

        return success;
    }

    public DishModel updateDishItem(DishModel mOldDish, DishModel mNewDish) {
        String where = ID_PK + " =?";

        if (mNewDish == null)
            return null;

        mNewDish.type = mOldDish.type;
        mNewDish.bento_pk = mOldDish.bento_pk;
        mNewDish.dish_pk = mOldDish.dish_pk;

        String[] whereArgs = new String[]{String.valueOf(mNewDish.dish_pk)};

        ContentValues cValues = getContentValues(mNewDish);

        dbAdapter.begginTransaction();

        long isInserted = dbAdapter.update(TABLE_NAME, cValues, where, whereArgs);

        success = isInserted == 1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Updated Dish: " + success);

        return mNewDish;
    }

    public boolean removeAllDishBento(int iBentoPk) {
        String where = BENTO_PK + "=?";
        String[] whereArgs = new String[]{String.valueOf(iBentoPk)};

        dbAdapter.begginTransaction();

        long idInsert = dbAdapter.delete(TABLE_NAME, where, whereArgs);
        success = idInsert == 5;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Remove Dish: " + success);
        return success;
    }

    public boolean removeDish(DishModel mOldDish) {
        String where = ID_PK + "=?";
        String[] whereArgs = new String[]{String.valueOf(mOldDish.dish_pk)};

        dbAdapter.begginTransaction();

        long idInsert = dbAdapter.delete(TABLE_NAME, where, whereArgs);
        success = idInsert != -1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Remove Dish: " + success + " Pk: " + mOldDish.dish_pk);

        return success;
    }

    public boolean removeDish(int iId) {
        String where = ITEM_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(iId)};

        dbAdapter.begginTransaction();

        long idInsert = dbAdapter.delete(TABLE_NAME, where, whereArgs);
        success = idInsert != -1;

        dbAdapter.setTransacctionSuccesfull();

        DebugUtils.logDebug(TAG, "Remove Dish: " + success + " Id: " + iId);

        return success;
    }

    public void clearAllData() {
        DebugUtils.logDebug(TAG, "Clear All Data");
        dbAdapter.deleteAll(TABLE_NAME);
    }

    private ContentValues getContentValues(DishModel mDish) {
        ContentValues cValues = new ContentValues();
        cValues.put(ITEM_ID, mDish.itemId);
        cValues.put(NAME, mDish.name == null ? "" : mDish.name);
        cValues.put(DESCRIPTION, mDish.description == null ? "" : mDish.description);
        cValues.put(TYPE, mDish.type == null ? "" : mDish.type);
        cValues.put(IMAGE1, mDish.image1 == null ? "" : mDish.image1);
        cValues.put(MAX_PER_ORDER, mDish.max_per_order == null ? "" : mDish.max_per_order);
        cValues.put(PRICE, mDish.price);
        cValues.put(BENTO_PK, mDish.bento_pk);
        cValues.put(QTY, mDish.qty == null ? "" : mDish.qty);

        return cValues;
    }

    public boolean isSoldOut(DishModel mDishModel, boolean countCurrent, boolean bIsOD) {
        if (bIsOD)
            return StockDao.isSold(mDishModel.itemId, countCurrent);
        else
            return isDishSoldOut(mDishModel, countCurrent);
    }

    public boolean isDishSoldOut(DishModel mDishModel, boolean countCurrent) {
        int iCurrent = countCurrent ? 1 : 0;
        int iQty;

        try {
            iQty = Integer.parseInt(mDishModel.qty);
        } catch (Exception ex) {
            DebugUtils.logError(TAG, "isDishSoldOut: " + ex.toString());
            iQty = 0;
        }
        int iCurrentStock = iQty - (countItemsById(mDishModel.itemId) + iCurrent);

        if (iCurrentStock < 0) {
            DebugUtils.logDebug(TAG, "isSold: " + mDishModel.itemId + ": " + " QTY:" + mDishModel.qty + " Stock:" + iCurrentStock);
            return true;
        } else
            return false;
    }

    public boolean canBeAdded(DishModel mDishModel) {
        int iMaxPerOrder;
        try {
            iMaxPerOrder = Integer.parseInt(mDishModel.max_per_order);
        } catch (Exception ex) {
            iMaxPerOrder = 99;
        }
        boolean bCanBeAdded = iMaxPerOrder > countItemsById(mDishModel.itemId);

        if (!bCanBeAdded)
            DebugUtils.logDebug(TAG, "canNotBeAdded " + mDishModel.name + " : Max Per Order:" + mDishModel.max_per_order);

        return bCanBeAdded;
    }

    public DishModel getFirstAvailable(Menu mMenu, String type, int[] tryExcludeIds, boolean bIsOD) {
        if (mMenu != null) {
            List<DishModel> aDishes = new ArrayList<>();

            for (int a = 0; a < mMenu.dishModels.size(); a++) {
                aDishes.add(DishDao.clone(mMenu.dishModels.get(a)));
            }

            Collections.shuffle(aDishes);

            if (tryExcludeIds != null) {
                String ids = Arrays.toString(tryExcludeIds);

                for (DishModel dishModel : aDishes) {
                    if (dishModel.type.equals(type) && !isSoldOut(dishModel, true, bIsOD) && canBeAdded(dishModel) && !ids.contains("" + dishModel.itemId))
                        return dishModel;
                }
            }

            for (DishModel dishModel : aDishes) {
                if (dishModel.type.equals(type) && !isSoldOut(dishModel, true, bIsOD) && canBeAdded(dishModel))
                    return dishModel;
            }
        }

        return null;
    }

    public boolean canCreateAnotherBento(Menu mMenu, int iNumDish, boolean bIsOD) {
        if (mMenu != null) {
            if (getFirstAvailable(mMenu, "main", null, bIsOD) == null) {
                DebugUtils.logError(TAG, "Cant Create Other Bento: Main");
                return false;
            }

            int[] ids = new int[iNumDish - 1];

            for (int i = 1; i < iNumDish; ++i) {
                DishModel dishModel = getFirstAvailable(mMenu, "side", ids, bIsOD);

                if (dishModel == null) {
                    DebugUtils.logError(TAG, "Cant Create Other Bento: Side");
                    return false;
                }

                ids[i - 1] = dishModel.itemId;

            }
        }

        return true;
    }

    public int countItemsById(int itemId) {
        int count = 0;

        String conditional = ITEM_ID + " = " + itemId;

        try {

            dbAdapter.begginTransaction();

            Cursor cursor = dbAdapter.getData(TABLE_NAME, FIELDS, conditional);

            cursor.moveToFirst();

            count = cursor.getCount();

            cursor.close();

            dbAdapter.setTransacctionSuccesfull();
        } catch (Exception ex) {
            DebugUtils.logError(TAG, "countItemsById : " + ex.toString());
        }


        return count;
    }
}
