package net.bitcores.kancollehourlyannouncer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ViewerFragment extends Fragment {
	AlarmAdapter alarmAdapter;
	SettingsAdapter settingsAdapter;
	LayoutInflater viewerInflater;

	private static Activity context;		
	private static View rootView;
	private static TextView msgBox;
	private static TableLayout playerTable;
	private static ImageView playerImage;
	private static String currentDir;
	
	public static String currentKanmusu;
	public static ProgressBar pb;
	public static Spinner kanmusuSpinner;
	public Integer currentView;
	public Integer currentImg;
	
	private final Handler handler = new Handler();
	List<String> imageList = new ArrayList<String>();
	
	public ViewerFragment() {
		
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_viewer, container, false);

		context = getActivity();	
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		
		pb = (ProgressBar)rootView.findViewById(R.id.playprogress);
		kanmusuSpinner = (Spinner)rootView.findViewById(R.id.kanmususpinner);
		msgBox = (TextView)rootView.findViewById(R.id.msgbox);
		playerTable = (TableLayout)rootView.findViewById(R.id.playertable);
		playerImage = (ImageView)rootView.findViewById(R.id.viewerimage);
		
		playerImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeImage();
			}			
		});
		playerImage.setOnLongClickListener(imgLongClickListener);
		
		kanmusuSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currentView = position;
				generateViewer();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {			
			}
		});
		
		setupPage();	
		
		return rootView;
	}
	
	public void setupPage() {
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, SettingsAdapter.full_list);
		kanmusuSpinner.setAdapter(spinnerAdapter);
		if (SettingsAdapter.full_list.size() == 0) {
			kanmusuSpinner.setEnabled(false);
			imageList.clear();
			playerTable.removeAllViews();
			playerImage.setImageDrawable(null);
			currentDir = null;
			if (SettingsAdapter.kanmusu_list.size() > 0) {
				msgBox.setText(getResources().getString(R.string.viewererror1));
			} else {			
				msgBox.setText(getResources().getString(R.string.viewererror2));
			}
		} else { 
			kanmusuSpinner.setEnabled(true);
			kanmusuSpinner.setSelection(SettingsAdapter.full_list.indexOf(SettingsAdapter.hourly_kanmusu));
			msgBox.setText("");
		}
	}
	
	public void generateViewer() {
		//generate sound clip list
		currentKanmusu = SettingsAdapter.full_list.get(currentView);
		currentDir = SettingsAdapter.kancolle_dir + "/" + currentKanmusu;
		
		currentImg = 2;
		imageList.clear();
		playerTable.removeAllViews();	
				
		String[] iArray = new String[30];
			
	    try
	    {
	        File dirFile = new File(currentDir);
	        File dirImage = new File(currentDir + "/Image");
	        if (dirFile.exists() && dirFile.isDirectory())
	        {
	        	for(int i = 1; i <= 53; i++) {
	        		File testFile = new File(currentDir + "/" + i + ".mp3");
	        		if (testFile.exists()) {
	        			String clipName = SettingsAdapter.kArray[i-1];
    	            	TableRow row = new TableRow(context);
    	            	TextView kanmusu = new TextView(context);    	            	
    	            	kanmusu.setText(clipName);
    	            	kanmusu.setTextAppearance(context, R.style.hugetext);
    	            	kanmusu.setPadding(2, 2, 2, 2);
    	            	
    	            	row.setId(i);
    	            	row.addView(kanmusu);
    	            	   	            	
    	            	row.setOnTouchListener(rowTouchListener);
    	            	row.setOnClickListener(rowClickListener); 	            	
    	            	row.setOnLongClickListener(rowLongClickListener);
    	            	
    	            	playerTable.addView(row);
	        		}
	        	}
	        	
	        	// Because the number and numbering of image files is not entirely consistent we have to actually build an array of them
	        	for (File file : dirImage.listFiles()) {
	        		String imgname = file.getName();
	        		if (imgname.contains("image") && (imgname.contains(".jpg") || imgname.contains(".png"))) {
	        			String[] parts = imgname.split(" ");
		        		String[] parts2 = parts[1].split("\\.");
		        		iArray[Integer.parseInt(parts2[0])] = imgname;
	        		}
    	        }
	        	
	        	for (String img : iArray) {
	        		if (img != null) {
	        			imageList.add(img);
	        		}
	        	}
	        }
	        
	    } catch (Exception e) {
	    }
        
	    if (imageList.size() > 0) {
		    String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath);
				playerImage.setImageBitmap(bitmap);
			}
	    }
	}
	
	public void playClip(Integer filename) {
		String filepath = currentDir + "/" + filename + ".mp3";
		File checkfile = new File(filepath);
		if (checkfile.exists()) {
			Intent intent = new Intent(context, AudioService.class);
			intent.putExtra("TYPE", "file");
			intent.putExtra("FILE", filepath);
			intent.putExtra("INTERRUPT", 0);
			context.startService(intent);
		}
	}
	
	public void changeImage() {
		if (currentDir != null && imageList.size() > 0) {
			if (currentImg == imageList.size()-1) {
				currentImg = 0;
			} else {
				currentImg++;
			}
			
		    String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath);
				playerImage.setImageBitmap(bitmap);
			}
		}
	}
	
	public void updateProgressBar(final MediaPlayer mp) {
		pb.setProgress(mp.getCurrentPosition());	
		if (mp.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					updateProgressBar(mp);
				}
			};
			handler.postDelayed(notification, 1000);
		}
	}
		
	OnTouchListener rowTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			LinearLayout layout = (LinearLayout)view;
			int action = event.getAction();
			switch (action) {
				case (MotionEvent.ACTION_DOWN) :
					layout.setBackgroundColor(0xFFA7A7A7);
					break;
				case (MotionEvent.ACTION_CANCEL) :
				case (MotionEvent.ACTION_UP) :
					layout.setBackgroundColor(0x00A7A7A7);
					break;
			}   	         				
			return false;
		}
	};
	
	OnClickListener rowClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			playClip(view.getId());	
		}            		
	};
	
	OnLongClickListener rowLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final Integer clipFile = view.getId();
			final CharSequence[] listitem = { context.getResources().getString(R.string.rtonesetlist), context.getResources().getString(R.string.rtonesetkanmusu) };
			AlertDialog.Builder listBuilder = new AlertDialog.Builder(context);
			listBuilder.setItems(listitem, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {				
					if (item == 0) {
						String filepath = currentDir + "/" + clipFile + ".mp3";						
						settingsAdapter.setRingtone(context, filepath, currentKanmusu, clipFile);
	
						if (SettingsAdapter.use_shuffle == 1) {
							SettingsAdapter.use_shuffle = 0;
							settingsAdapter.saveSettings(context, 0);						
							Toast.makeText(context, context.getResources().getString(R.string.rtoneunset), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(context, context.getResources().getString(R.string.rtoneset), Toast.LENGTH_SHORT).show();
						}
						
						SettingsFragment.useShuffleBox.setChecked(false);
					} else if (item == 1) {					
						if (SettingsAdapter.use_shuffle == 0) {
							SettingsAdapter.use_shuffle = 1;							
							Toast.makeText(context, context.getResources().getString(R.string.rtoneshufenset), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(context, context.getResources().getString(R.string.rtoneshufset), Toast.LENGTH_SHORT).show();
						}
						
						SettingsAdapter.viewer_kanmusu = currentKanmusu;
						SettingsAdapter.shuffle_action = 3;
						settingsAdapter.saveSettings(context, 0);						
						SettingsFragment.useShuffleBox.setChecked(true);
						SettingsFragment.shuffleSpinner.setSelection(SettingsAdapter.shuffle_action);
						SettingsFragment.shuffleViewerText.setText(context.getResources().getString(R.string.viewerkantext) + " " + SettingsAdapter.viewer_kanmusu);						
						settingsAdapter.shuffleRingtone(context);
					}
				}
			});
			AlertDialog alertList = listBuilder.create();
			alertList.show();
			
			return true;
		}            		
	};
	
	OnLongClickListener imgLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			final Dialog imageDialog = new Dialog(context);
			View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image, (ViewGroup)context.findViewById(R.id.dialogImageLayout), false);
			ImageView dialogImage = (ImageView)dialogView.findViewById(R.id.dialogImage);
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			String filepath = currentDir + "/Image/" + imageList.get(currentImg);
			File checkfile = new File(filepath);
			if (checkfile.exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
				dialogImage.setImageBitmap(bitmap);
			}
			imageDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			imageDialog.setContentView(dialogView);
			
			// Scaling the popup image to fit the screen
			// I hope there is a simpler way to do this, but I cant think of it so here we are
			View content = context.findViewById(Window.ID_ANDROID_CONTENT);
			float displayWidth = content.getWidth() - 8;
			float displayHeight = content.getHeight() - 8;
			float widthRatio = (float) options.outWidth / (float) options.outHeight;
			float heightRatio = (float) options.outHeight / (float)options.outWidth;			
			int ratioedWidth = (int) (displayHeight / heightRatio);
			int ratioedHeight = (int) (displayWidth / widthRatio);
						
			if (displayWidth > options.outWidth && displayHeight > options.outHeight) {	
				ViewGroup.LayoutParams dialogParams = dialogView.getLayoutParams();	
	
				if (((Math.round(displayHeight / heightRatio)) > displayWidth && displayWidth > displayHeight) || ((Math.round(displayWidth / widthRatio)) < displayHeight) && displayWidth < displayHeight) {
					dialogParams.width = (int) displayWidth;
					dialogParams.height = ratioedHeight; 
					
				} else {
					dialogParams.height = (int) displayHeight;
					dialogParams.width = ratioedWidth;
				}

				dialogView.setLayoutParams(dialogParams);
			}
			
			// Long press the image to dismiss it
			dialogImage.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					imageDialog.dismiss();
					return true;
				}
				
			});
			
			imageDialog.show();
			
			return true;
		}
		
	};
}
