package net.bitcores.kancollehourlyannouncer;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class WidgetLarge extends WidgetProvider {

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (action.equals(WidgetProvider.UPDATE_WIDGET)) {
			ComponentName kancolleWidget = new ComponentName(context, WidgetLarge.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(kancolleWidget);

			sendUpdate(context, appWidgetManager, appWidgetIds);
		}
	}
}
