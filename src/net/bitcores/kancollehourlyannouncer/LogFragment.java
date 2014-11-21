package net.bitcores.kancollehourlyannouncer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LogFragment extends Fragment {
	
	private static View rootView;
	private static TextView logText;
	private static MenuItem logEnable;
    private static MenuItem logVerbose;
	
	public LogFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		rootView = inflater.inflate(R.layout.fragment_log, container, false);
		logText = (TextView)rootView.findViewById(R.id.logtextview);
		
		printLog();
		
		return rootView;
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {	
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.logfragment_actions, menu);
	    
	    logEnable = menu.findItem(R.id.action_logenable);
	    logVerbose = menu.findItem(R.id.action_logverbose);
	    checkBoxes();
	    
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_logrefreshbutton:
	    	printLog();
	    	return true;
	    case R.id.action_logenable:
	    	if (SettingsAdapter.enable_log == 1) {
	    		SettingsAdapter.enable_log = 0;
	    	} else {
	    		SettingsAdapter.enable_log = 1;
	    	}
	    	checkBoxes();
	        return true;
	    case R.id.action_logverbose:
	    	if (SettingsAdapter.verbose_log == 1) {
	    		SettingsAdapter.verbose_log = 0;
	    	} else {
	    		SettingsAdapter.verbose_log = 1;
	    	}
	    	checkBoxes();
	        return true;
	    default:
	        break;
	    }

	    return false;
	}
	
	private void checkBoxes() {
		if (SettingsAdapter.enable_log == 1) {
	    	logEnable.setChecked(true);
	    	logVerbose.setEnabled(true);
	    } else {
	    	logEnable.setChecked(false);
	    	logVerbose.setEnabled(false);
	    }
	    if (SettingsAdapter.verbose_log == 1) {
	    	logVerbose.setChecked(true);
	    } else {
	    	logVerbose.setChecked(false);
	    }
	}

	private void printLog() {
		logText.setText("");
		
		String output = "";
		
		for (String line : SettingsAdapter.event_log) {
			output = line + "\n" + output;
		}
		
		logText.setText(output);
	}
}
