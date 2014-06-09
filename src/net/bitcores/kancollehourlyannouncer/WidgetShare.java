package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetShare {
	SettingsAdapter settingsAdapter;
	int lWidth = 560;
	int mWidth = 320;
	
	static final String UPDATE_WIDGET = "net.bitcores.kancollehourlyannouncer.UPDATE_WIDGET";
	
	public WidgetShare() {
		
	}
	
	public void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, RemoteViews widgetView, String size) {	
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(context);
		}
		
		//	UPDATE CLOCK
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.US);
		widgetView.setTextViewText(R.id.clockText, f.format(cal.getTimeInMillis()));
		
		if (Calendar.MINUTE != 59 && SettingsAdapter.enabled == 1) {
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
		
		//	SET CLOCK TOUCH INTENT
		Intent clockIntent = new Intent(context, PagerActivity.class);
		PendingIntent pclockIntent = PendingIntent.getActivity(context, 49494, clockIntent, 0);
		widgetView.setOnClickPendingIntent(R.id.clockText, pclockIntent);
		
		
		for (int appWidgetId : appWidgetIds) {			
			//	SET BACKGROUND IMAGE AND SECRETARY INTENT
			if (SettingsAdapter.kanmusu_use.size() > 0) {
				String kanmusu = settingsAdapter.getKanmusu();
				String filepath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/Image/image 21.png";
				File checkfile = new File(filepath);
				if (checkfile.exists()) {
					Bitmap bitmap = BitmapFactory.decodeFile(filepath);		
					
					
					Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
					int widgetWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
					int widgetHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();					
					
					int hCells = widgetHeight / 70;
					int wCells = widgetWidth / 90;
					
					if (size.equals("m")) {
						if (wCells <=3) {
							width = 600;
						}
					}
					
					if (size.equals("l")) {
						if (wCells == 5) {
							width = 630;
						}
						if (wCells == 4) {
							width = 490;
						}
						if (wCells <= 3) {
							width = 400;
						}
					}
					
					if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						width = width + 90;
						if (width > bitmap.getWidth()) {
							width = bitmap.getWidth();
						}
					}
						
						Log.i("kancolle announcer", "widget width: " + widgetWidth + " height: " + widgetHeight + " width cells: " + wCells + "height cells: " + hCells);


						Bitmap cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height);
						
						Log.i("kancolle announcer", "image width: " + cropped.getWidth() + " image height: " + cropped.getHeight());
						widgetView.setImageViewBitmap(R.id.clockBack, cropped);
					
					
					Intent secretaryIntent = new Intent(context, AudioService.class);
					secretaryIntent.putExtra("TYPE", "secretary");
					secretaryIntent.putExtra("FILE", "empty");
					secretaryIntent.putExtra("INTERRUPT", 0);
					PendingIntent psecretaryIntent = PendingIntent.getService(context, 50505, secretaryIntent, 0);
					widgetView.setOnClickPendingIntent(R.id.clockBack, psecretaryIntent);
				}	
			}
		
			appWidgetManager.updateAppWidget(appWidgetId, widgetView);
		}
	}
}
