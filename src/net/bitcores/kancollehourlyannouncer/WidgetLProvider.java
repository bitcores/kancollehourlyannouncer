package net.bitcores.kancollehourlyannouncer;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetLProvider extends AppWidgetProvider {
	WidgetShare widgetShare;
	String size = "l";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		sendUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (action.equals(WidgetShare.UPDATE_WIDGET)) {
			ComponentName kancolleWidget = new ComponentName(context, WidgetLProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(kancolleWidget);

			sendUpdate(context, appWidgetManager, appWidgetIds);
		}
	}	
	
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		int[] appWidgetIds = new int[1];
		appWidgetIds[0] = appWidgetId;

		sendUpdate(context, appWidgetManager, appWidgetIds);
	}
	
	public void sendUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clocklarge);
		
		widgetShare = new WidgetShare();
		widgetShare.updateWidget(context, appWidgetManager, appWidgetIds, widgetView, size);
	}
}
