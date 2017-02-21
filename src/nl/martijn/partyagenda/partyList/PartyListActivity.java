package nl.martijn.partyagenda.partyList;

import java.util.Calendar;

import nl.martijn.partyagenda.PartyApp;
import nl.martijn.partyagenda.PartysUpdateService;
import nl.martijn.partyagenda.R;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class PartyListActivity extends Activity implements OnNavigationListener {

	// UI elements
	private PartyListFragment mPartyListFragment;
	private Menu mMenu;
	
	// UI states
	private boolean mIsSearchViewExpanded = false;
	private String mSearchTerm = "";
	private boolean mIsRefreshing = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Notice that setContentView() is not used, because we use the root
        // android.R.id.content as the container for each fragment

        // Action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        // Spinner (Drop down list adapter)
        SpinnerAdapter navigationSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.navigation_list,
                android.R.layout.simple_spinner_dropdown_item);
        actionBar.setListNavigationCallbacks(navigationSpinnerAdapter, this);      
              
        if (savedInstanceState != null) {
            // Restore last state for selected item in drop down list.
        	int selectedNavIndex = savedInstanceState.getInt("curSelectedNavIndex", 0);
        	actionBar.setSelectedNavigationItem(selectedNavIndex);
        	
        	// Restore last state for search view (1/2)
        	mIsSearchViewExpanded = savedInstanceState.getBoolean("curIsSearchViewExpanded", false); 
        	mSearchTerm = savedInstanceState.getString("curSearchTerm", "");
        	
        	// Restore refreshing state
        	mIsRefreshing = savedInstanceState.getBoolean("curIsRefreshing", false); 
        }
        
        // Party list fragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mPartyListFragment = new PartyListFragment();
        fragmentTransaction.replace(android.R.id.content, mPartyListFragment);
        fragmentTransaction.commit();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    	int selectedNavIndex = getActionBar().getSelectedNavigationIndex();
        outState.putInt("curSelectedNavIndex", selectedNavIndex);				// Drop down list navigation index
        outState.putBoolean("curIsSearchViewExpanded", mIsSearchViewExpanded);	// Is search view expanded
        outState.putString("curSearchTerm", mSearchTerm);						// Search term
        outState.putBoolean("curIsRefreshing", mIsRefreshing);					// Is refreshing party list
    }
        
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Navigation item from drop down list selected
		if(itemPosition == 1) {
			mPartyListFragment.setShowMyPartys(true);
		} else {
			mPartyListFragment.setShowMyPartys(false);
		}
		return true;
	}
	
	/*
	 * ********** Options menu configuration ********** 
	 * Search item: is expanded + search term
	 * Refresh item: is refreshing
	 */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.party_list, menu);
        mMenu = menu;
        
        // Configure search item
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				mIsSearchViewExpanded = true;
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				mIsSearchViewExpanded = false;
				mSearchTerm = "";
				mPartyListFragment.setSearchTerm(mSearchTerm);
				return true;
			}
		});
        
        // 1.1 Restore last state for search view
        if(mIsSearchViewExpanded)
        	searchItem.expandActionView();
        
        // Configure search view
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				// Don't care about this
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				mSearchTerm = newText;
				//Log.e("SRCQRY", "Search term = \'" + mSearchTerm + "\'");
				mPartyListFragment.setSearchTerm(mSearchTerm);
				return true;
			}
		});
        
        // 1.2 Restore last state for search view
        if(!TextUtils.isEmpty(mSearchTerm)) {
        	searchView.setQuery(mSearchTerm, true);
        }
        
        // 2. Configure refresh item + view
        MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
        ImageView refreshView = (ImageView)refreshItem.getActionView();
        refreshView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRefreshingPartyList();
			}
		});
        
        // Restore refresh icon state
        if(mIsRefreshing) {
        	startRotatingRefreshIcon();
        } else {
        	stopRotatingRefreshIcon();
        }
        
        // Refresh list at first startup
        PartyApp app = (PartyApp)getApplication();
        if(app.getNumStartups() == 1)
        {
        	startRefreshingPartyList();
        }
        else
        {
        	// Refresh list if it's X days old
        	Calendar lastRefresh = app.getLastRefresh();
        	Calendar fewDaysAgo = Calendar.getInstance();
        	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        	String numDaysAgoStr = sp.getString("automatic_update", "3");
        	int numDaysAgo = Integer.parseInt(numDaysAgoStr);
        	fewDaysAgo.add(Calendar.DAY_OF_YEAR, -numDaysAgo);
        	if(fewDaysAgo.after(lastRefresh))
        	{
        		//Toast.makeText(this, "Time expired: refresh!", Toast.LENGTH_SHORT).show();
        		startRefreshingPartyList();
        	}
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
            	Intent settingsIntent = new Intent(this, SettingsActivity.class);
            	startActivity(settingsIntent);
                return true;
            case R.id.menu_about:
            	Intent aboutIntent = new Intent(this, AboutActivity.class);
            	startActivity(aboutIntent);
                return true;
            case R.id.menu_refresh:
            	startRefreshingPartyList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
    	// Called when PartyUpdateService is finished
    	// On error -> show error message
    	if(resultCode != 0) {
    		String errorMessage = data.getExtras().getString("errorMessage");
    		Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    	}
    	
    	stopRefreshingPartyList();
    }

    /* Bug: Back button not working
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
    	if(keyCode == KeyEvent.KEYCODE_SEARCH)
    	{
    		Menu menu = mMenu;
    		MenuItem menuItem = menu.findItem(R.id.menu_search);
    		menuItem.expandActionView();
    		return true;
    	}
    	return false;
    }
    */

    /* ********** Refresh party list ********** */
    
    private void startRefreshingPartyList() {
    	mIsRefreshing = true;
    	
    	// Start rotating refresh icon + disable button
    	startRotatingRefreshIcon();
    	
    	// Start the asynchronous service
    	Intent serviceIntent = new Intent(this, PartysUpdateService.class);
    	PendingIntent pendingServiceIntent = createPendingResult(1, serviceIntent, PendingIntent.FLAG_ONE_SHOT);
    	serviceIntent.putExtra("pendingIntent", pendingServiceIntent);
    	startService(serviceIntent);
    }
    
    private void stopRefreshingPartyList() {
    	mIsRefreshing = false;
    	
    	PartyApp app = (PartyApp)getApplication();
    	app.setLastRefreshToNow();
    	
    	// Stop rotating refresh icon + enable button
    	stopRotatingRefreshIcon();
    }
    

    private void startRotatingRefreshIcon() {
    	MenuItem refreshItem = mMenu.findItem(R.id.menu_refresh);
    	// Disable menu item
    	refreshItem.setEnabled(false);
    	
    	// Start rotating action view
    	ImageView refreshView = (ImageView)refreshItem.getActionView();
    	Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
        refreshView.startAnimation(rotateAnim);
    }
    
    private void stopRotatingRefreshIcon() {
    	// Stop rotating action view
    	MenuItem refreshItem = mMenu.findItem(R.id.menu_refresh);
    	ImageView refreshView = (ImageView)refreshItem.getActionView();
    	refreshView.clearAnimation();
    	
    	// Enable menu item
    	refreshItem.setEnabled(true);
    }
}
