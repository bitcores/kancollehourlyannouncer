package net.bitcores.kancollehourlyannouncer;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;

public class MainActivity extends Activity {
	private SettingsAdapter settingsAdapter;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] mPageTitles;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settingsAdapter = new SettingsAdapter();
		
		if (!SettingsAdapter.init) {
			settingsAdapter.initSettings(MainActivity.this);
		}
		
		setContentView(R.layout.activity_main);
		
		
		//	I may want to put some form of splash screen here at some point, or create a custom layout so that there is no title bar at all
		startService(new Intent(MainActivity.this, AudioService.class));
		//startActivity(new Intent(MainActivity.this, PagerActivity.class));
		
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_drawer);
        mPageTitles = getResources().getStringArray(R.array.menulist);
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPageTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
                
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  
                mDrawerLayout,         
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.descopen,  /* "open drawer" description */
                R.string.descclosed  /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
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
            selectItem(0);
        }
	}
	
	private void selectItem(int position) {
		
		Fragment fragment = getFragment(position);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, fragment).commit();
        
        mDrawerList.setItemChecked(position, true);
        setTitle(mPageTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);

	}
	

    // Called whenever we call invalidateOptionsMenu() 
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }
    
  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {	
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.viewerfragment_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}*/
	
	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

	public Fragment getFragment(int index) {
		switch (index) {
		case 0:
			return new KanmusuFragment();
		case 1:
			return new SoundFragment();	
		case 2:
			return new ViewerFragment();
		}
		return null;
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);			
		}	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		settingsAdapter.saveSettings(MainActivity.this, 0);
	}
		
}
