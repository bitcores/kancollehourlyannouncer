package net.bitcores.kancollehourlyannouncer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmAdapter {
	PendingIntent pendingIntent;
	
	public AlarmAdapter() {
		
	}
	
	//	Calling the alarm multiple times doesnt set it multiple times but rather overwrites it, so you will see this called
	//	quite often in the logs to ensure the alarm is set.
	public void setAlarm(Context context) {	
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat f = new SimpleDateFormat("HH:mm E dd", Locale.US);
		
		Log.i("kancolle announcer", "Alarm set for: " + f.format(cal.getTimeInMillis()));
		
		Intent intent = new Intent(context, ServiceTask.class);
		pendingIntent = PendingIntent.getService(context, 55559, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
	}
	
	public void stopAlarm(Context context) {
		Intent intent = new Intent(context, ServiceTask.class);
		pendingIntent = PendingIntent.getService(context, 55559, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		Log.i("kancolle announcer", "Alarm disabled");
	}

}
