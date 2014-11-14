package net.bitcores.kancollehourlyannouncer;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;

public class MainActivity extends Activity {
	private AlarmAdapter alarmAdapter;
	private SettingsAdapter settingsAdapter;
	private DrawerLayout mDrawerLayout;
	private RelativeLayout mDrawerList;
	private ListView mTop;
	private ListView mBottom;
	private String[] mTopTitles;
	private String[] mBottomTitles;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    
    private int currentPosition = 0;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		alarmAdapter = new AlarmAdapter();
		settingsAdapter = new SettingsAdapter();	
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(MainActivity.this);
		}
				
		setContentView(R.layout.activity_main);
		
		//startService(new Intent(MainActivity.this, AnnounceService.class));

		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RelativeLayout) findViewById(R.id.menu_drawer);
        mTop = (ListView) findViewById(R.id.top_menu);
        mBottom = (ListView) findViewById(R.id.bottom_menu);
        mTopTitles = getResources().getStringArray(R.array.topmenu);
        mBottomTitles = getResources().getStringArray(R.array.bottommenu);
        
        mTop.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mTopTitles));
        mBottom.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mBottomTitles));
        mTop.setOnItemClickListener(new DrawerItemClickListener());
        mBottom.setOnItemClickListener(new DrawerItemClickListener());
                      
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.descopen, R.string.descclosed) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        
        if (savedInstanceState == null) {
            selectItem(mTop, 0);
            mTop.setItemChecked(0, true);
            
            if (SettingsAdapter.enabled == 1) {
    			alarmAdapter.setAlarm(MainActivity.this);
    		}
        }
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		settingsAdapter.saveSettings(MainActivity.this);
		settingsAdapter.writeLog(MainActivity.this);
	}
	
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void setTitle(View view) {
		if (view.getId() == R.id.top_menu) {
			mTitle = mTopTitles[currentPosition];
		} else if (view.getId() == R.id.bottom_menu) {
			mTitle = mBottomTitles[currentPosition];
		}
        getActionBar().setTitle(mTitle);
    }
    
    private void selectItem(View view, int position) {
		
		Fragment fragment = getFragment(view, position);
		if (fragment != null) {
	        FragmentManager fragmentManager = getFragmentManager();
	        fragmentManager.beginTransaction().replace(R.id.fragment_frame, fragment).commit();
	        
	        currentPosition = position;
	        setTitle(view);
	        mDrawerLayout.closeDrawer(mDrawerList);
		}

	}

	private Fragment getFragment(View view, int index) {
		if (view.getId() == R.id.top_menu) {
			mBottom.setItemChecked(currentPosition, false);
			switch (index) {
			case 0:
				return new KanmusuFragment();
			case 1:
				return new SoundFragment();	
			case 2:
				return new ViewerFragment();
			}
		} else if (view.getId() == R.id.bottom_menu) {
			mTop.setItemChecked(currentPosition, false);
			switch (index) {
			case 0:
				return new LogFragment();
			}
		}
		return null;
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(parent, position);			
		}	
	}
		
}
