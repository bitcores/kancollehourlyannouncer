package net.bitcores.kancollehourlyannouncer;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class KanmusuFragment extends Fragment {
	private static SharedPreferences preferences;
	private static AlarmAdapter alarmAdapter;
	private static SettingsAdapter settingsAdapter;
	
	private static Activity context;	
	private static Button shuffleClipSelection;
	private static BackgroundReceiver backgroundReceiver;
	private static View rootView;	
	private static Spinner shuffleSpinner;
	private static CheckBox useShuffleBox;	
	private static CheckBox selectAllBox;
	private static TextView shuffleViewerText;	
	private static TextView dirView;
	private static TextView foundView;
	private static TextView useView;
	private static TextView announceStatus;
	private static TextView bgText;
	private static ImageView bgImage;
	private static Button pickButton;
	
	private static String activeDir = "";
    
	public KanmusuFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(false);
		rootView = inflater.inflate(R.layout.fragment_kanmusu, container, false);
		
		context = getActivity();	
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		preferences = context.getSharedPreferences(SettingsAdapter.PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		LinearLayout dirSelect = (LinearLayout)rootView.findViewById(R.id.dirSelect);
		shuffleSpinner = (Spinner)rootView.findViewById(R.id.shuffleSpinner);
		useShuffleBox = (CheckBox)rootView.findViewById(R.id.shuffleBox);	
		selectAllBox = (CheckBox)rootView.findViewById(R.id.selectAllBox);	
		shuffleViewerText = (TextView)rootView.findViewById(R.id.shuffleViewerText);	
		announceStatus = (TextView)rootView.findViewById(R.id.announceStatus);	
		dirView = (TextView)rootView.findViewById(R.id.dirText);
		foundView = (TextView)rootView.findViewById(R.id.foundText);
		useView = (TextView)rootView.findViewById(R.id.useText);
		bgText = (TextView)rootView.findViewById(R.id.kanmusuBgText);
		bgImage = (ImageView)rootView.findViewById(R.id.kanmusuBgImage);	
		pickButton = (Button)rootView.findViewById(R.id.pickButton);
		shuffleClipSelection = (Button)rootView.findViewById(R.id.shuffleClipSelection);
		Button enableButton = (Button)rootView.findViewById(R.id.enableButton);
		Button scanButton = (Button)rootView.findViewById(R.id.scanButton);
		
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
                    	dirView.setText(activeDir);
                    }
                }); 
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory(activeDir);
            }
        });
		
		shuffleSpinner.setSelection(SettingsAdapter.shuffle_action);
		shuffleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SettingsAdapter.shuffle_action = position;
				
				if (SettingsAdapter.shuffle_action == 3 && SettingsAdapter.viewer_kanmusu.equals("")) {
					SettingsAdapter.viewer_kanmusu = settingsAdapter.getKanmusu();
					
					updateTexts();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		});
		
		selectAllBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingsAdapter.kanmusu_use.clear();

				if (selectAllBox.isChecked()) {
					SettingsAdapter.kanmusu_use.addAll(SettingsAdapter.kanmusu_list);
				}
				updateTexts();
			}	
		});
		useShuffleBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (useShuffleBox.isChecked()) {
					SettingsAdapter.use_shuffle = 1;
					settingsAdapter.shuffleRingtone(context);
				} else {
					SettingsAdapter.use_shuffle = 0;
				}
			}
			
		});
		
		pickButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				useListDialog();
			}		
		});
		
		if (SettingsAdapter.use_shuffle == 1) {
			useShuffleBox.setChecked(true);
		} 
		
		shuffleClipSelection.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				shuffleListDialog();
			}			
		});
		
		if (!SettingsAdapter.viewer_kanmusu.equals("")) {
			shuffleViewerText.setText(SettingsAdapter.viewer_kanmusu);
		} else {
			shuffleViewerText.setText(getResources().getString(R.string.notset));
		}
		
		if (SettingsAdapter.kancolle_dir.equals("")) {
			dirView.setText(getResources().getString(R.string.directory_default));
		} else {
			dirView.setText(SettingsAdapter.kancolle_dir);
			// we dont want to change the global variable kancolle_dir until you scan the dir
			activeDir = SettingsAdapter.kancolle_dir;
		}
			
		scanButton.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
            	if (!activeDir.equals("")) {
	        	    try
	        	    {
	        	    	// RIP the lists and rescan the directory
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
	        	        	
	        	        	//	When you scan a dir we want to remove any kanmusu in use that aren't present and update the appwide kancolle_dir setting
	        	        	for (Integer i = 0; i < SettingsAdapter.kanmusu_use.size(); i++) {
	        	        		if (!SettingsAdapter.kanmusu_list.contains(SettingsAdapter.kanmusu_use.get(i))) {
	        	        			SettingsAdapter.kanmusu_use.remove(i);
	        	        		}
	        	        	}
	        	        	SettingsAdapter.kancolle_dir = activeDir;
	        	        	updateTexts();
	        	        	checkSelectAll();
	        	        }        	        
	        	    }
	        	    catch (Exception e) {
	        	    }
            	}
        	}     
        });
				
		
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
				updateTexts();
				SharedPreferences.Editor editor = preferences.edit();
				editor.putInt("enabled", SettingsAdapter.enabled);
				editor.commit();
            }
        });	
		
		settingsAdapter.doBackground(bgImage, bgText);
		checkSelectAll();
		updateTexts();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();		
		settingsAdapter.doBackground(bgImage, bgText);
		IntentFilter filter = new IntentFilter();	
		backgroundReceiver = new BackgroundReceiver();
		filter.addAction(WidgetShare.UPDATE_WIDGET);
		context.registerReceiver(backgroundReceiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();		
		if (backgroundReceiver != null) {
			context.unregisterReceiver(backgroundReceiver);
		}
	}
	
	private void updateTexts() {
		String fvt = String.valueOf(SettingsAdapter.kanmusu_list.size()) + " " + getResources().getString(R.string.kanmusu);
		String uvt = String.valueOf(SettingsAdapter.kanmusu_use.size()) + " " + getResources().getString(R.string.kanmusu);
		String ast;
		if (SettingsAdapter.enabled == 0) {
			ast = getResources().getString(R.string.disabled);
		} else {
			ast = getResources().getString(R.string.enabled);
		}
		
		foundView.setText(fvt);
		useView.setText(uvt);
		announceStatus.setText(ast);
		shuffleViewerText.setText(SettingsAdapter.viewer_kanmusu);
	}
	
	private void checkSelectAll() {
		if (SettingsAdapter.kanmusu_list.equals(SettingsAdapter.kanmusu_use)) {			
			selectAllBox.setChecked(true);
		} else {
			selectAllBox.setChecked(false);
		}
	}
         
    private void useListDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
	    
	    View dialogTitle = LayoutInflater.from(context).inflate(R.layout.dialog_title, (ViewGroup)context.findViewById(R.id.dialogLayout), false);
	    TextView titleView = (TextView)dialogTitle.findViewById(R.id.dialogTitleText);	    
	    titleView.setText(getResources().getString(R.string.usekanmususelectiontext));	        
	    dialogBuilder.setCustomTitle(dialogTitle);
	    
	    final String[] itemsArray = new String[SettingsAdapter.kanmusu_list.size()];
	    final boolean[] selectedItems = new boolean[SettingsAdapter.kanmusu_list.size()];
	    for (Integer c = 0; c < SettingsAdapter.kanmusu_list.size(); c++) {
	    	if (SettingsAdapter.kanmusu_use.contains(SettingsAdapter.kanmusu_list.get(c))) {
	    		selectedItems[c] = true;
	    	} else {
	    		selectedItems[c] = false;
	    	}
	    	itemsArray[c] = SettingsAdapter.kanmusu_list.get(c);
	    }
    
	    dialogBuilder.setMultiChoiceItems(itemsArray, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int button, boolean bool) {
				// do nothing				
			}
		});
	    
	    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    {
	        @Override
	        public void onClick(DialogInterface dialog, int button) 
	        {
	        	SettingsAdapter.kanmusu_use.clear();
	        	for (Integer i = 0; i < selectedItems.length; i++) {
	        		if (selectedItems[i]) {
	        			SettingsAdapter.kanmusu_use.add(SettingsAdapter.kanmusu_list.get(i));
	        		}
	        	}
	        	updateTexts();
	        	checkSelectAll();
	        	settingsAdapter.sortList(SettingsAdapter.kanmusu_use);
	        }
	    }).setNegativeButton("Cancel", null);
	    
	    AlertDialog useListDialog = dialogBuilder.create();
		
		useListDialog.show();
	}
    
    private void shuffleListDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
	    
	    View dialogTitle = LayoutInflater.from(context).inflate(R.layout.dialog_title, (ViewGroup)context.findViewById(R.id.dialogLayout), false);
	    TextView titleView = (TextView)dialogTitle.findViewById(R.id.dialogTitleText);	    
	    titleView.setText(getResources().getString(R.string.shuffleclipselectiontext));	        
	    dialogBuilder.setCustomTitle(dialogTitle);
	    
	    final String[] itemsArray = new String[29];
	    final boolean[] selectedItems = new boolean[29];
	    for (Integer c = 0; c < 29; c++) {
	    	if (SettingsAdapter.kanmusu_shufflelist.contains(c.toString())) {
	    		selectedItems[c] = true;
	    	} else {
	    		selectedItems[c] = false;
	    	}
	    	itemsArray[c] = SettingsAdapter.kArray[c];
	    }
    
	    dialogBuilder.setMultiChoiceItems(itemsArray, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int button, boolean bool) {
				// do nothing				
			}
		});
	    
	    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
	    {
	        @Override
	        public void onClick(DialogInterface dialog, int button) 
	        {
	        	SettingsAdapter.kanmusu_shufflelist.clear();
	        	for (Integer i = 0; i < selectedItems.length; i++) {
	        		if (selectedItems[i]) {
	        			SettingsAdapter.kanmusu_shufflelist.add(i.toString());
	        		}
	        	}
	        	
	        	//	We want to shuffle the ringtone when the shuffle list is changed
	        	if (SettingsAdapter.use_shuffle == 1) {
	        		settingsAdapter.shuffleRingtone(context);
	        	}
	        }
	    }).setNegativeButton("Cancel", null);
	    
	    AlertDialog shuffleListDialog = dialogBuilder.create();
		
		shuffleListDialog.show();
	}
    
    
    private class BackgroundReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			settingsAdapter.doBackground(bgImage, bgText);		
		}
	}
}
