package net.bitcores.kancollehourlyannouncer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	SettingsAdapter settingsAdapter;
	BitmapAdapter bitmapAdapter;
	
	static final String UPDATE_WIDGET = "net.bitcores.kancollehourlyannouncer.UPDATE_WIDGET";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);		
		
		sendUpdate(context, appWidgetManager, appWidgetIds);
	}
		
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		int[] appWidgetIds = new int[1];
		appWidgetIds[0] = appWidgetId;

		sendUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	@SuppressLint("InlinedApi")
	public void sendUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		//	Seeing this can be called by onUpdate even if there are no applicable widgetids we jump out if there are none
		if (appWidgetIds.length == 0) {
			return;
		}
		
		final String WIDGET_MEDIUM = context.getResources().getString(R.string.medwidgettitle);
		final String WIDGET_LARGE = context.getResources().getString(R.string.lrgwidgettitle);
		final String WIDGET_MEDLRG = context.getResources().getString(R.string.mlwidgettitle);
		final String WIDGET_RESIZE = context.getResources().getString(R.string.reswidgettitle);
		final String WIDGET_SECRET = context.getResources().getString(R.string.secwidgettitle);
		
		settingsAdapter = new SettingsAdapter();		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(context);
		}	
		bitmapAdapter = new BitmapAdapter();
		bitmapAdapter.initBitmapCache();
		
		RemoteViews widgetView = null;
				
		//	UPDATE CLOCK
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.US);
		String currentTime = f.format(cal.getTimeInMillis());
		
		if (SettingsAdapter.enabled == 0 || (Calendar.MINUTE != 59 && SettingsAdapter.enabled == 1)) {
			cal.add(Calendar.MINUTE, 1);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			Intent broadcastIntent = new Intent();
			broadcastIntent.setAction(UPDATE_WIDGET);
			PendingIntent timeIntent = PendingIntent.getBroadcast(context, 94494, broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			// Kitkat uses inexact alarms unless setExact is used
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), timeIntent);
			} else {
				alarmManager.setExact(AlarmManager.RTC, cal.getTimeInMillis(), timeIntent);
			}
		}
		
		//	CREATE CLOCK TOUCH INTENT
		Intent clockIntent = new Intent(context, MainActivity.class);
		PendingIntent pclockIntent = PendingIntent.getActivity(context, 49494, clockIntent, 0);
			
		for (int appWidgetId : appWidgetIds) {
			AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
			String label = appWidgetProviderInfo.label;
			
			if (WIDGET_MEDIUM.equals(label)) {
				widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clockmedium);
			} else if (WIDGET_LARGE.equals(label)) {
				widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clocklarge);
			} else if (WIDGET_MEDLRG.equals(label)) {
				widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clockmediumlarge);
			} else if (WIDGET_SECRET.equals(label)) {
				widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_secretary);
			} else {
				widgetView = null;
			}
			
			if (SettingsAdapter.kanmusu_use.size() > 0) {
				String filepath = null;
				if (label.equals(WIDGET_SECRET)) {
					filepath = settingsAdapter.getSecretary("bg", "widget");
				} else {
					filepath = settingsAdapter.getSecretary("widget", "widget");
				}
				if (filepath != null) {
					Bitmap cropped = null;
					Bitmap bitmap = bitmapAdapter.getBitmap(filepath);
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
									
					if (label.equals(WIDGET_MEDLRG) || label.equals(WIDGET_RESIZE)) {
						if (label.equals(WIDGET_MEDLRG)) {
							width = 398;
							cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height);						
						} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							// The resizable widget shoud only be running if the android version is at least jellybean anyway but
							// this should make it so that the minimum sdk version can safely be 13 again
							Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
							int widgetWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
							int widgetHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
							int hCells = widgetHeight / 50;
							int wCells = widgetWidth / 90;
							
							// This is all fuggly mucking around trying to make shit work
							// There may be a better way of doing this, but I couldn't find any
							// The best way probably would have been to go through and make cropped versions of the image being used
							// for every ship with different layout files for each size
							
							if (hCells < 2) {
								if (wCells <=3) {
									width = 600;
								}
								widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clockresizablem);
	
								cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height);
							} else {
								width = 1099;
								if (wCells == 5) {
									width = 1091;
								}
								if (wCells == 4) {
									width = 810;
								}
								if (wCells <= 3) {
									width = 650;
								}
								widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clockresizablel);
								
								Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1099, 200, true);
								cropped = Bitmap.createBitmap(scaled, 0, 0, width, 200);
								scaled.recycle();
							}	
						}						
						
						widgetView.setImageViewBitmap(R.id.clockBack, cropped);
					} else {
						widgetView.setImageViewBitmap(R.id.clockBack, bitmap);
					}
				
					Intent secretaryIntent = new Intent(context, AnnounceService.class);
					secretaryIntent.putExtra("TYPE", "secretary");
					PendingIntent psecretaryIntent = PendingIntent.getService(context, 50505, secretaryIntent, 0);
					
					//	SETUP SECRETARY
					widgetView.setOnClickPendingIntent(R.id.clockBack, psecretaryIntent);				
				}	
			}
			
			//	SETUP CLOCK
			widgetView.setTextViewText(R.id.clockText, currentTime);	
			widgetView.setOnClickPendingIntent(R.id.clockText, pclockIntent);

			appWidgetManager.updateAppWidget(appWidgetId, widgetView);
		}
		
	}
	
}
