/**
 * @author Kokusho Torres
 * 02/09/2014
 */
package com.bentonow.bentonow.Utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.freakybyte.wallpaper.Application;
import com.freakybyte.wallpaper.R;
import com.freakybyte.wallpaper.widget.RandomWidgetProvider;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class WidgetsUtils {

    /**
     * Method that creates a short toast based in a String
     *
     * @param message The String that is going to be show
     */
    public static void createShortToast(final String message) {
        Application.getInstance().handlerPost(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method that creates a long Toast Message based in a string
     *
     * @param message The String that is going to be show
     */
    public static void createLongToast(final String message) {
        Application.getInstance().handlerPost(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Method that creates a short Toast based in a string id
     *
     * @param id The Int from the String id
     */
    public static void createShortToast(final int id) {
        Application.getInstance().handlerPost(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), Application.getInstance().getString(id), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method that creates a long Toast Message based in a string id
     *
     * @param id
     */
    public static void createLongToast(final int id) {
        Application.getInstance().handlerPost(new Runnable() {
            public void run() {
                Toast.makeText(Application.getInstance(), Application.getInstance().getString(id), Toast.LENGTH_LONG).show();
            }
        });
    }

//	public static DatePicker generateDatePicker(final Context context, Calendar hCalendar) {
//		DatePicker dpView = new DatePicker(context);
//		dpView.setCalendarViewShown(false);
//
//		if (hCalendar==null) {
//			hCalendar=Calendar.getInstance();
//			hCalendar.set(Calendar.YEAR, hCalendar.get(Calendar.YEAR));
//		}
//
//		dpView.updateDate(hCalendar.get(Calendar.YEAR), hCalendar.get(Calendar.MONTH),hCalendar.get(Calendar.DAY_OF_MONTH));
//
//		dpView.setMaxDate(Calendar.getInstance().getTimeInMillis());
//
//		LinearLayout llFirst = (LinearLayout) dpView.getChildAt(0);
//		LinearLayout llSecond = (LinearLayout) llFirst.getChildAt(0);
//		for (int i = 0; i < llSecond.getChildCount(); i++) {
//			try {
//				NumberPicker numberPicker = (NumberPicker) llSecond.getChildAt(i);
//				Field[] pickerFields = NumberPicker.class.getDeclaredFields();
//				for (Field pf : pickerFields) {
//					if (pf.getName().equals("mSelectionDivider")) {
//						pf.setAccessible(true);
//						pf.set(numberPicker,context.getResources().getDrawable(R.drawable.numberpicker_selection_divider));
//						break;
//					}
//				}
//				final int count = numberPicker.getChildCount();
//				for (int a = 0; a < count; a++) {
//					View child = numberPicker.getChildAt(a);
//					if (child instanceof EditText) {
//						Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
//						selectorWheelPaintField.setAccessible(true);
//						((Paint) selectorWheelPaintField.get(numberPicker)).setColor(context.getResources().getColor(R.color.black));
//						((EditText) child).setTextColor(context.getResources().getColor(R.color.black));
//						numberPicker.invalidate();
//					}
//				}
//			} catch (NoSuchFieldException e) {
//				DebugUtils.logError("setNumberPickerTextColor");
//			} catch (IllegalAccessException e) {
//				DebugUtils.logError("setNumberPickerTextColor");
//			} catch (IllegalArgumentException e) {
//				DebugUtils.logError("setNumberPickerTextColor");
//			} catch (NotFoundException e) {
//				DebugUtils.logError(e);
//			} catch (Exception e) {
//				DebugUtils.logError(e);
//			}
//		}
//		return dpView;
//	}


    public static void updateRandomWidget(final Context context) {

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // Get all ids
        final ComponentName thisWidget = new ComponentName(context, RandomWidgetProvider.class);

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_random);

        final int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            if (!SharedPreferencesUtil.getBooelanPreference(SharedPreferencesUtil.IS_IMAGE_NOT_CHANGING) && SharedPreferencesUtil.getBooelanPreference(SharedPreferencesUtil.CHANGE_WIDGET_IMAGE))
                updateWidget(context, appWidgetManager, remoteViews, widgetId);
            else
                WidgetsUtils.createShortToast("The image is changing... wait god damn it");


            // Register an onClickListener
            Intent intent = new Intent(context, RandomWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.img_random_widget, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

    }

    static void updateWidget(final Context context, final AppWidgetManager appWidgetManager, final RemoteViews remoteViews, final int appWidgetId) {

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CHANGE_WIDGET_IMAGE, false);
        OtakuWallpaperUtil.setRandomWallpaper(context);

        final String sWallpaper = SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.WALLPAPER_NEXT_URI);


        if (!sWallpaper.isEmpty()) {
            ImageUtil.getImageLoader().loadImage(FileUtil.getAbsolutePathFromUri(sWallpaper), ImageUtil.getOptionsBigLoader(), new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    DebugUtils.logDebug("WidgetPrevImg", "Started");
                    GoogleAnalyticsUtil.trackEvent("App", "General", "Widget Update", "Started");
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_IMAGE_NOT_CHANGING, true);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    DebugUtils.logDebug("WidgetPrevImg", "Failed");
                    GoogleAnalyticsUtil.trackEvent("App", "General", "Widget Update", "Failed");
                    OtakuWallpaperUtil.removeUrlFromFavorites(s);
                    remoteViews.setImageViewResource(R.id.img_random_widget, R.drawable.widget_preview);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_IMAGE_NOT_CHANGING, false);
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bWallpaper) {
                    DebugUtils.logDebug("WidgetPrevImg", "Complete");
                    GoogleAnalyticsUtil.trackEvent("App", "General", "Widget Update", "Complete");
                    bWallpaper = ImageUtil.cropCenterBitmap(bWallpaper);
                    if (bWallpaper == null)
                        DebugUtils.logDebug("WidgetPrevImg", "Error: Center Crop");
                    else
                        bWallpaper = ImageUtil.resizeImageToWidth(bWallpaper, 200);

                    if (bWallpaper == null)
                        DebugUtils.logDebug("WidgetPrevImg", "Error: Resize");
                    else
                        remoteViews.setImageViewBitmap(R.id.img_random_widget, bWallpaper);

                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_IMAGE_NOT_CHANGING, false);
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    DebugUtils.logDebug("WidgetPrevImg", "Cancelled");
                    GoogleAnalyticsUtil.trackEvent("App", "General", "Widget Update", "Cancelled");
                    remoteViews.setImageViewResource(R.id.img_random_widget, R.drawable.widget_preview);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_IMAGE_NOT_CHANGING, false);
                }

            });

        } else {
            remoteViews.setImageViewResource(R.id.img_random_widget, R.drawable.widget_preview);
        }

    }
}
