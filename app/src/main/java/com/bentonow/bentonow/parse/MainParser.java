/**
 * @author Kokusho Torres
 */

package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.gson.Gson;

public class MainParser {

    public static final String TAG_SECTION_TITLE = "sectionTitle";
    public static final String TAG_ITEMS = "items";
    protected static Gson gson = new Gson();
    static long init, now;


//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";
//	public static final String TAG_= "";


    public static void startParsed() {
        init = System.currentTimeMillis();
    }

    public static void stopParsed() {
        now = System.currentTimeMillis();
        DebugUtils.logDebug("Parse en :: " + (now - init) + " ms");
    }
}
