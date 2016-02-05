package com.bentonow.bentonow.dao;

/**
 * Created by Kokusho on 05/02/16.
 */
public class MainDao {

    protected String getStringFromColumn(String sColumn) {
        return sColumn == null ? "" : sColumn;
    }
}
