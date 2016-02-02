/**
 * @author Kokusho Torres
 */

package com.bentonow.bentonow.parse;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

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

    public static boolean parseSection(JSONObject mJson, String sTag) {
        try {
            if (mJson == null || !mJson.has(sTag) || mJson.getString(sTag) == null || mJson.getString(sTag).equals("null") || mJson.getString(sTag).isEmpty())
                return false;
            else
                return true;
        } catch (Exception ex) {
            DebugUtils.logError(sTag + " : " + ex.toString());
            return false;
        }
    }

}
