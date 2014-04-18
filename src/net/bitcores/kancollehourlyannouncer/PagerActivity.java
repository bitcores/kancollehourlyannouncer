package net.bitcores.kancollehourlyannouncer;

import java.io.File;

import android.os.Bundle;
import android.app.ActionBar.Tab;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

public class PagerActivity extends FragmentActivity implements ActionBar.TabListener {
	SharedPreferences preferences;
	AlarmAdapter alarmAdapter;
	SettingsAdapter settingsAdapter;
	PagerAdapter pagerAdapter;
	ActionBar actionBar;
	ViewPager viewPager;
		
	public TextView dirview;
	private String[] tabs = { "Viewer", "Kanmusu", "Settings" };
	private String currentBg = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pager);		
		
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(PagerActivity.this);
		}
		
		if (SettingsAdapter.enabled == 1) {
			alarmAdapter.setAlarm(PagerActivity.this);
		}
		
		preferences = getSharedPreferences(SettingsAdapter.PREF_FILE_NAME, MODE_PRIVATE);
		
		viewPager = (ViewPager)this.findViewById(R.id.pager);
		
		pagerAdapter = new PagerAdapter(getSupportFragmentManager());
		
		viewPager.setAdapter(pagerAdapter);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		for (String name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(name).setTabListener(this));
		}
		
		actionBar.setSelectedNavigationItem(1);
		
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
				if (ViewerFragment.kanmusuSpinner.isEnabled()) {
					ViewerFragment.kanmusuSpinner.setSelection(SettingsAdapter.full_list.indexOf(SettingsAdapter.hourly_kanmusu));
				}
				doBackground();
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		doBackground();
	}
	
	public void doBackground() {
		if (SettingsAdapter.kanmusu_use.size() > 0) {
			String kanmusu = settingsAdapter.getKanmusu();
			
			if (!kanmusu.equals(currentBg)) {
				// The Arpeggio event characters didn't include an image 17.png but instead had a 16 and 18 that were usually combined to fill the place of 17
				// we use 16 if 17 isn't present
				currentBg = kanmusu;
				Integer exist = 0;
				String filepath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/Image/image 17.png";
				File checkfile = new File(filepath);
				if (checkfile.exists()) {
					exist = 1;
				} else {
					filepath = SettingsAdapter.kancolle_dir + "/" + kanmusu + "/Image/image 16.png";
					checkfile = new File(filepath);				
					if (checkfile.exists()) {
						exist = 1;
					}
				}
				
				if (exist == 1) {
					Bitmap bitmap = BitmapFactory.decodeFile(filepath);
					ImageView bgimage = (ImageView)this.findViewById(R.id.bgimage);	
					bgimage.setImageBitmap(bitmap);
				}
			}
		}
	}
	
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
	
	

}
