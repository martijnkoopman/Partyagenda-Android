package nl.martijn.partyagenda.partyDetail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nl.martijn.partyagenda.R;
import nl.martijn.partyagenda.PartysContract;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class PartyDetailActivity extends Activity {
	
	private int mPartyId = 0;
	private boolean mPartyIsFavorite = false;
	private SimpleDateFormat mDateFormater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Date formatters
		mDateFormater = new SimpleDateFormat("EEEE d MMMM yyyy");
		
		// Get party ID
		mPartyId = getIntent().getExtras().getInt("id");
		
		// Determine if this party has media
	    boolean hasMedia = false;
	    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, mPartyId);
	    Uri videosUri = Uri.withAppendedPath(partyItemUri, PartysContract.Partys.Video.TABLE_NAME);
	    Cursor cur = getContentResolver().query(videosUri, null, null, null, null);
	    if(cur != null) {
	    	if(cur.getCount() > 0)
	    		hasMedia = true;
	    	cur.close();
	    }
	    
		// Action bar + tabs
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    Tab tab = actionBar.newTab()
		                .setText("Algemeen")
	    				.setTabListener(new TabListener<PartyGeneralFragment>(this, "general", PartyGeneralFragment.class));
	    actionBar.addTab(tab);
	    if(hasMedia) {
			tab = actionBar.newTab()
		            .setText("Video's")
		            .setTabListener(new TabListener<PartyMediaFragment>(this, "media", PartyMediaFragment.class));
			actionBar.addTab(tab);
	    }
		tab = actionBar.newTab()
		            .setText("Kaart")
		            .setTabListener(new TabListener<PartyMapFragment>(this, "map", PartyMapFragment.class));
		actionBar.addTab(tab);
	}
	
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
	
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  savedInstanceState.putInt("curSelectedTabIndex", getActionBar().getSelectedNavigationIndex());  
	  super.onSaveInstanceState(savedInstanceState);  
	} 
	
	// TODO: Restore in onCreate?!
    @Override  
	public void onRestoreInstanceState(Bundle savedInstanceState) {  
    	super.onRestoreInstanceState(savedInstanceState);  
    	int selectedNavigationIndex = savedInstanceState.getInt("curSelectedTabIndex");
    	getActionBar().setSelectedNavigationItem(selectedNavigationIndex);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.party_detail, menu);
		
		// Share
		MenuItem item = menu.findItem(R.id.menu_share);
		ShareActionProvider shareActionProvider = (ShareActionProvider)item.getActionProvider();
		shareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		shareActionProvider.setShareIntent(createShareIntent());
		
		// Favo icon ('Mijn feesten')
		mPartyIsFavorite = isPartyFavorite();
		if(mPartyIsFavorite)
		{
			MenuItem favoItem = menu.findItem(R.id.menu_favo);
			favoItem.setIcon(R.drawable.ic_menu_favo_on);
		}
		
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
                return true;
            case R.id.menu_favo:
            	mPartyIsFavorite = togglePartyIsFavorite(mPartyIsFavorite);
            	if(mPartyIsFavorite) {
                	item.setIcon(R.drawable.ic_menu_favo_on);
            		Toast.makeText(this, "Toegevoegd aan 'Mijn feesten'", Toast.LENGTH_SHORT).show();
            	}
            	else {
                	item.setIcon(R.drawable.ic_menu_favo_off);
            		Toast.makeText(this, "Verwijderd uit 'Mijn feesten'", Toast.LENGTH_SHORT).show();
            	}
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private boolean isPartyFavorite() {
    	Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, mPartyId);
    	String[] projection = { PartysContract.Partys.IS_FAVORITE };
    	String selection = PartysContract.Partys.IS_FAVORITE + " = ?";
    	String[] selectionArgs = { ""+1 };
	    Cursor c = getContentResolver().query(partyItemUri, projection, selection, selectionArgs, null);
	    if(c.getCount() < 1) {
	    	c.close();
	    	return false;
	    } else {
	    	c.close();
	    	return true;
	    }
    }
    
    private boolean togglePartyIsFavorite(boolean oldValue) {
    	// Determine new value
    	int newValue = oldValue ? 0 : 1;
    	
    	// Update old value by new value
		ContentValues values = new ContentValues(1);
		values.put(PartysContract.Partys.IS_FAVORITE, newValue);
		String[] selectionArgs = { ""+mPartyId };
		int rowsAffected = getContentResolver().update(PartysContract.Partys.CONTENT_URI, values, PartysContract.Partys._ID + " = ?", selectionArgs);
		
		// Check for success
		if(rowsAffected > 0) {
			return (newValue == 1 ? true : false);
		} else {
			return oldValue;
		}
    }
    
    private Intent createShareIntent()
    {
	    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, mPartyId);
    	String[] projection = { PartysContract.Partys.NAME,
    			 PartysContract.Partys.SUBNAME,
    			 PartysContract.Partys.DATE,
    			 PartysContract.Partys.VENUE,
    			 PartysContract.Partys.CITY };
	    Cursor c = getContentResolver().query(partyItemUri, projection, null, null, null);
	    if(c == null || c.getCount() < 1)
	    	return null;
	    
    	String name = c.getString(c.getColumnIndex(PartysContract.Partys.NAME));
    	String subname = c.getString(c.getColumnIndex(PartysContract.Partys.SUBNAME));
    	long dateLong = c.getLong(c.getColumnIndex(PartysContract.Partys.DATE)); 
    	String venue = c.getString(c.getColumnIndex(PartysContract.Partys.VENUE)); 
    	String city = c.getString(c.getColumnIndex(PartysContract.Partys.CITY)); 
    	c.close();

    	String shareText = name + (!TextUtils.isEmpty(subname) ? " - "+subname : "") + "\r\n";
    	shareText += mDateFormater.format(dateLong)+"\r\n";
    	shareText += venue + ", " + city;
    	
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		return shareIntent;
    }
}
