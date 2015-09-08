package com.bentonow.bentonow.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.net.Uri;
import android.util.Base64;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.controllers.BentoApplication;

import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

public class SocialNetworksUtil {

    public static void generateKeyHash() {
        try {
            PackageInfo info = BentoApplication.instance.getPackageManager().getPackageInfo(BentoApplication.instance.getString(R.string.package_name), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                DebugUtils.logDebug(Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            DebugUtils.logError(e);
        }
    }

    public static void openFacebookPage(Activity act, String pageId) {
        final String urlFb = "fb://page/" + pageId;
        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlFb));

        // If Facebook application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.facebook.com/pages/"
                    + pageId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openFacebookProfile(Activity act, String userId) {
        final String urlFb = "fb://profile/" + userId;
        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlFb));

        // If Facebook application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.facebook.com/"
                    + userId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

//	public static void postStatusFacebook(String message) {
//		Bundle postParams = new Bundle();
//		postParams.putString("name", "Swipe Messenger");
//		postParams.putString("message", message);
//		postParams.putString("link", "https://play.google.com/store/apps/details?id=com.gameloft.android.ANMP.GloftSIHM");
////		postParams.putString("picture", urlImage);
//		Request.Callback callback = new Request.Callback() {
//			public void onCompleted(Response response) {
//				DebugUtils.logDebug(response.toString());
//				FacebookRequestError error = response.getError();
//				if (error != null) {
//					DebugUtils.logError(error.getErrorMessage());
//					WidgetsUtils.createShortToast(R.string.error_facebook_post);
//				} else {
//					WidgetsUtils.createShortToast(R.string.succes_facebook_post);
//					JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
//					String postId = null;
//					try {
//						postId = graphResponse.getString("id");
//						DebugUtils.logDebug(postId);
//					} catch (Exception e) {
//						DebugUtils.logError(e);
//					}
//
//				}
//
//			}
//		};
//
//		Request request = new Request(Session.getActiveSession(), "me/feed", postParams, HttpMethod.POST, callback);
//
//		RequestAsyncTask task = new RequestAsyncTask(request);
//		task.execute();
//	}

    public static void openTwitterUser(Activity act, String userId) {
        final String urlTwitter = "twitter://user?user_id=" + userId;

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlTwitter));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://twitter.com/" + userId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openWebUrl(Activity act, String url) {
        try {
            Intent urlIntent = new Intent(Intent.ACTION_VIEW);
            urlIntent.setData(Uri.parse(url));
            act.startActivity(urlIntent);

        } catch (Exception e) {
            DebugUtils.logError("OpenWebUrl", e);
        }
    }

    public static void openGoogleUser(Activity act, String userId) {
        final String urlTwitter = "https://plus.google.com/" + userId;

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlTwitter));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://plus.google.com/" + userId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openYoutubeVideo(Activity act, String videoId) {
        final String urlVideo = "vnd.youtube://" + videoId;

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlVideo));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.youtube.com/watch?v=" + videoId;
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openWazeDirection(Activity act, double latitude, double longitude) {
        final String urlVideo = "waze://?ll=" + latitude + "," + longitude + "&navigate=yes";

        Intent pageIntent = new Intent(Intent.ACTION_VIEW);
        pageIntent.setData(Uri.parse(urlVideo));

        // If Twitter application is installed, use that else launch a browser
        final PackageManager packageManager = act.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                pageIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "market://details?id=com.waze";
            pageIntent.setData(Uri.parse(urlBrowser));
        }

        act.startActivity(pageIntent);
    }

    public static void openMapDirection(Activity act, double latitude, double longitude, String title) {
        Intent pageIntent = new Intent(Intent.ACTION_VIEW);

//		String uriBegin = "geo:" + latitude + "," + longitude;
//		String query = latitude + "," + longitude + "(" + label + ")";
//		String encodedQuery = Uri.encode(query);
//		String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";

        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", latitude, longitude, latitude, longitude);

        pageIntent.setData(Uri.parse(uri));
        act.startActivity(pageIntent);
    }


    public static void shareWebItem(Context context, String sTitle, String subject, String url) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(sendIntent, sTitle));
    }

    public static void shareTextItem(String subject, String url) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");
//		startActivity(Intent.createChooser(sendIntent, Application.getInstance().getResources().getText(R.string.)));
    }

    public static void shareImageIntent(Context context, String uriImage, String sTitle) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriImage);
        shareIntent.setType("image/jpeg");
        context.startActivity(Intent.createChooser(shareIntent, sTitle));
    }
}
