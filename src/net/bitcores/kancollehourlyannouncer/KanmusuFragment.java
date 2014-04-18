package net.bitcores.kancollehourlyannouncer;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class KanmusuFragment extends Fragment {
	SharedPreferences preferences;
	AlarmAdapter alarmAdapter;
	SettingsAdapter settingsAdapter;
	ViewerFragment viewerFragment;
	Activity context;
	
	public static CheckBox selectAllBox;
	
	public View rootView;
	public TextView dirview;
	public String activeDir = "";
	public Integer fullList = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_kanmusu, container, false);
		
		context = getActivity();	
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		viewerFragment = new ViewerFragment();
		preferences = context.getSharedPreferences(SettingsAdapter.PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		dirview = (TextView)rootView.findViewById(R.id.dirText);
		if (SettingsAdapter.kancolle_dir.equals("")) {
			dirview.setText(getResources().getString(R.string.directory_default));
		} else {
			dirview.setText(SettingsAdapter.kancolle_dir);
			// we dont want to change the global variable kancolle_dir until the setting is saved
			activeDir = SettingsAdapter.kancolle_dir;
		}
								
		LinearLayout dirSelect = (LinearLayout)rootView.findViewById(R.id.dirSelect);
		dirSelect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Toast.makeText(context, getResources().getString(R.string.back_hint), Toast.LENGTH_LONG).show();
                // Create DirectoryChooserDialog and register a callback 
                DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(context, new DirectoryChooserDialog.ChosenDirectoryListener() {
                    @Override
                    public void onChosenDir(String chosenDir) {
                    	activeDir = chosenDir;
                    	dirview.setText(activeDir);
                    }
                }); 
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory(activeDir);
            }
        });
		
		Button scanButton = (Button)rootView.findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	if (!activeDir.equals("")) {
	        	    try
	        	    {
	        	    	// RIP the lists and rescan the directory
	        	    	// these lists should really be seperate so it doesnt affect viewer
	        	    	SettingsAdapter.kanmusu_list.clear();
	        	    	SettingsAdapter.full_list.clear();
	        	        File dirFile = new File(activeDir);
	        	        if (dirFile.exists() && dirFile.isDirectory())
	        	        {
	        	        	for (File file : dirFile.listFiles()) 
	            	        {
	            	            if ( file.isDirectory() )
	            	            {
	            	                File checkFile = new File(activeDir + "/" + file.getName() + "/30.mp3");
	            	                if (checkFile.exists()) {
	            	                	SettingsAdapter.kanmusu_list.add(file.getName());
	            	                }
	            	                
	            	                File checkDir = new File(activeDir + "/" + file.getName() + "/Image");
	            	                if (checkDir.exists() && checkDir.isDirectory()) {
	            	                	SettingsAdapter.full_list.add(file.getName());
	            	                }
	            	            }
	            	        }
	        	        	
	        	        	settingsAdapter.sortList(SettingsAdapter.kanmusu_list);
	        	        	settingsAdapter.sortList(SettingsAdapter.full_list);
		            	    updateList();
		            	    fullList = 1;
	        	        }        	        
	        	    }
	        	    catch (Exception e) {
	        	    }
            	}
        	}     
        });
		
		Button saveButton = (Button)rootView.findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	saveSelection();
        	}     
        });
		
		Button enableButton = (Button)rootView.findViewById(R.id.enableButton);
		if (SettingsAdapter.enabled == 0) {
			enableButton.setText(getResources().getString(R.string.enable));
		} else {
			enableButton.setText(getResources().getString(R.string.disable));
		}
		enableButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Button enableButton = (Button)v;
				if (SettingsAdapter.enabled == 0) { 
					enableButton.setText(getResources().getString(R.string.disable));
					alarmAdapter.setAlarm(context);	
					SettingsAdapter.enabled = 1;
					Toast.makeText(context, getResources().getString(R.string.alarmset), Toast.LENGTH_SHORT).show();
					
				} else {
					enableButton.setText(getResources().getString(R.string.enable));
					alarmAdapter.stopAlarm(context);
					SettingsAdapter.enabled = 0;
					Toast.makeText(context, getResources().getString(R.string.alarmstop), Toast.LENGTH_SHORT).show();
				}
				SharedPreferences.Editor editor = preferences.edit();
				editor.putInt("enabled", SettingsAdapter.enabled);
				editor.commit();
            }
        });
		
		selectAllBox = (CheckBox)rootView.findViewById(R.id.selectAllBox);
		selectAllBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingsAdapter.kanmusu_select.clear();
				
				for (String tag : SettingsAdapter.kanmusu_list) {
					CheckBox thisBox = (CheckBox)rootView.findViewWithTag(tag);
					if (thisBox != null){
						if (selectAllBox.isChecked()) {
							thisBox.setChecked(true);
							SettingsAdapter.kanmusu_select.add(tag);
						} else {
							thisBox.setChecked(false);
						}
					} 
				}
			}	
		});
		
		updateList();
		
		return rootView;
	}

	public void saveSelection() {
		SettingsAdapter.kancolle_dir = activeDir;
		SettingsAdapter.kanmusu_use = SettingsAdapter.kanmusu_select;
		
		settingsAdapter.saveSettings(context, 1);
		if (fullList == 1) {
			viewerFragment.setupPage();
			fullList = 0;
		}
	}
	
	public void updateList() {
		checkSelectAll();
		
		TableLayout table = (TableLayout)rootView.findViewById(R.id.kanmusuTable);
	
		table.removeAllViews();		
		settingsAdapter.sortList(SettingsAdapter.kanmusu_list);
		
		// Get info about the device display width, use this size to set the width of the
		// table for displaying in landscape mode
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		width = width / 2 - 150;
		int o = 0;
		TableRow row = null;
		TextView kanmusu = null;
		CheckBox kanmusu_box = null;
		String kanmusu_name = null;
		for(int i = 0; i < SettingsAdapter.kanmusu_list.size(); i++) {
			if (o == 0) {
				row = new TableRow(context);
			}
			kanmusu_name = SettingsAdapter.kanmusu_list.get(i);
			kanmusu = new TextView(context);
			kanmusu_box = new CheckBox(context);
			kanmusu.setText(kanmusu_name);
			kanmusu.setTextAppearance(context, R.style.medtext);
			kanmusu.setWidth(width);
			kanmusu_box.setTag(kanmusu_name);
			
			if (SettingsAdapter.kanmusu_select.contains(kanmusu_name)) {
				kanmusu_box.setChecked(true);
			}
			
			kanmusu_box.setOnClickListener (kanmusuBoxListener);
			
			row.addView(kanmusu_box);
			row.addView(kanmusu);
			
			// Create a second column when the display is in landscape mode
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				if (o == 1) {
					table.addView(row);
					o = 0;
				} else {
					o++;
				}
			} else {
				table.addView(row);
			}
		}	
	}
	
	public void checkSelectAll() {
		if (SettingsAdapter.kanmusu_list.equals(SettingsAdapter.kanmusu_select)) {			
			selectAllBox.setChecked(true);
		} else {
			selectAllBox.setChecked(false);
		}
	}
	
	OnClickListener kanmusuBoxListener = new OnClickListener() {
    	public void onClick(View v) {
    		CheckBox cb = (CheckBox)v;
    		String name = (String)cb.getTag();
    		if (cb.isChecked()) {
    			SettingsAdapter.kanmusu_select.add(name);
    		} else {
    			SettingsAdapter.kanmusu_select.remove(name);
    		}
    		settingsAdapter.sortList(SettingsAdapter.kanmusu_select);
    		checkSelectAll();
    	}
    };
    

}
