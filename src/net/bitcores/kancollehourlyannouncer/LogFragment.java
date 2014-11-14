package net.bitcores.kancollehourlyannouncer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LogFragment extends Fragment {
	
	private static View rootView;
	private static TextView logText;
	
	public LogFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(false);
		rootView = inflater.inflate(R.layout.fragment_log, container, false);
		
		logText = (TextView)rootView.findViewById(R.id.logtextview);
		
		String output = "";
		
		for (String line : SettingsAdapter.event_log) {
			output = line + "\n" + output;
		}
		
		logText.setText(output);
		
		return rootView;
	}

}
