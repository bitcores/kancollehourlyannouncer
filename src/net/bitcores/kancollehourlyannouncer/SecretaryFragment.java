package net.bitcores.kancollehourlyannouncer;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SecretaryFragment extends Fragment {
	private static SettingsAdapter settingsAdapter;
	
	private static Activity context;
	private static BackgroundReceiver backgroundReceiver;
	private static View rootView;
	private static TextView bgText;
	private static ImageView bgImage;

	
	public SecretaryFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(false);
		rootView = inflater.inflate(R.layout.fragment_secretary, container, false);
		
		context = getActivity();
		settingsAdapter = new SettingsAdapter();
		
		TextView secretaryUseText = (TextView)rootView.findViewById(R.id.secretaryUseText);
		Spinner bgusesecretarySpinner = (Spinner)rootView.findViewById(R.id.bgusesecretarySpinner);
		Spinner bgusetypeSpinner = (Spinner)rootView.findViewById(R.id.bgusetypeSpinner);
		Spinner widgetusesecretarySpinner = (Spinner)rootView.findViewById(R.id.widgetusesecretarySpinner);
		Spinner widgetusetypeSpinner = (Spinner)rootView.findViewById(R.id.widgetusetypeSpinner);
		bgText = (TextView)rootView.findViewById(R.id.settingsBgText);
		bgImage = (ImageView)rootView.findViewById(R.id.settingsBgImage);
		
		secretaryUseText.setText(SettingsAdapter.secretary_kanmusu);
		
		bgusesecretarySpinner.setSelection(SettingsAdapter.secretary_bg);
		bgusesecretarySpinner.setOnItemSelectedListener(spinnerListener);
		
		bgusetypeSpinner.setSelection(SettingsAdapter.secretary_bgimgtype);
		bgusetypeSpinner.setOnItemSelectedListener(spinnerListener);
		
		widgetusesecretarySpinner.setSelection(SettingsAdapter.secretary_widget);
		widgetusesecretarySpinner.setOnItemSelectedListener(spinnerListener);
		
		widgetusetypeSpinner.setSelection(SettingsAdapter.secretary_widgetimgtype);
		widgetusetypeSpinner.setOnItemSelectedListener(spinnerListener);
		
		settingsAdapter.doBackground(bgImage, bgText);
		
		return rootView;
	}
	
	OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			Spinner spinner = (Spinner)parent;
			
			switch (spinner.getId()) {
				case R.id.bgusesecretarySpinner:
					SettingsAdapter.secretary_bg = position;
					break;
				case R.id.bgusetypeSpinner:
					SettingsAdapter.secretary_bgimgtype = position;
					break;
				case R.id.widgetusesecretarySpinner:
					SettingsAdapter.secretary_widget = position;
					break;
				case R.id.widgetusetypeSpinner:
					SettingsAdapter.secretary_widgetimgtype = position;
					break;
				default:
					return;
			}
			
			settingsAdapter.updateWidgets(context);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}	
	};
	
	@Override
	public void onResume() {
		super.onResume();
		settingsAdapter.doBackground(bgImage, bgText);
		IntentFilter filter = new IntentFilter();
		backgroundReceiver = new BackgroundReceiver();
		filter.addAction(WidgetProvider.UPDATE_WIDGET);
		context.registerReceiver(backgroundReceiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();		
		if (backgroundReceiver != null) {
			context.unregisterReceiver(backgroundReceiver);
		}
	}
	
	private class BackgroundReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			settingsAdapter.doBackground(bgImage, bgText);		
		}
	}
}
