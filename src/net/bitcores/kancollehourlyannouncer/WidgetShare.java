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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

public class WidgetShare {
	SettingsAdapter settingsAdapter;
	
	static final String UPDATE_WIDGET = "net.bitcores.kancollehourlyannouncer.UPDATE_WIDGET";
	
	public WidgetShare() {
		
	}
	
	public void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, RemoteViews widgetView) {	
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(context);
		}
		
		//	SET BACKGROUND IMAGE AND SECRETARY INTENT
		if (SettingsAdapter.kanmusu_use.size() > 0) {
			String kanmusu = settingsAdapter.getKanmusu();
			String filepath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/Image/image 21.png";
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath);
				widgetView.setImageViewBitmap(R.id.clockBack, bitmap);
				
				Intent secretaryIntent = new Intent(context, AudioService.class);
				secretaryIntent.putExtra("TYPE", "secretary");
				secretaryIntent.putExtra("FILE", "empty");
				secretaryIntent.putExtra("INTERRUPT", 0);
				PendingIntent psecretaryIntent = PendingIntent.getService(context, 50505, secretaryIntent, 0);
				widgetView.setOnClickPendingIntent(R.id.clockBack, psecretaryIntent);
			}	
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
			alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), timeIntent);
		}
		
		//	SET CLOCK TOUCH INTENT
		Intent clockIntent = new Intent(context, PagerActivity.class);
		PendingIntent pclockIntent = PendingIntent.getActivity(context, 49494, clockIntent, 0);
		widgetView.setOnClickPendingIntent(R.id.clockText, pclockIntent);
		
		
		appWidgetManager.updateAppWidget(appWidgetIds, widgetView);
	}
}
