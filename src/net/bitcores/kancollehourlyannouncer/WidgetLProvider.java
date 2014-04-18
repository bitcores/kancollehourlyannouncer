package net.bitcores.kancollehourlyannouncer;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetLProvider extends AppWidgetProvider {
	WidgetShare widgetShare;
		
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clocklarge);
		
		widgetShare = new WidgetShare();
		widgetShare.updateWidget(context, appWidgetManager, appWidgetIds, widgetView);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (action.equals(WidgetShare.UPDATE_WIDGET)) {
			ComponentName kancolleWidget = new ComponentName(context, WidgetLProvider.class);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(kancolleWidget);
			RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_clocklarge);
			
			widgetShare = new WidgetShare();
			widgetShare.updateWidget(context, appWidgetManager, appWidgetIds, widgetView);
		}
	}
	
	
	
	
}
