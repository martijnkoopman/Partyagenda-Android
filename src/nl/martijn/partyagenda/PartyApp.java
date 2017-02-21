package nl.martijn.partyagenda;

import java.util.Calendar;
import java.util.GregorianCalendar;

import nl.martijn.partyagenda.partyList.PartyListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class PartyApp extends Application {
	
	SharedPreferences mPrefs;
	
	// Stored preferences:
	// - Number of application startups
	// - Whether or not the application has been rated
	// - Date of last party list refresh
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//mPrefs = getSharedPreferences("nl.martijn.partyagenda", Context.MODE_PRIVATE);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(getNumStartups() == 0) {
			// Load default preferences from XML file
			PreferenceManager.setDefaultValues(this, R.xml.preference_settings, false);
		}
		
		super.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityStopped(Activity activity) {}
			
			@Override
			public void onActivityStarted(final Activity activity) {
				if(activity instanceof PartyListActivity){
					incrementNumStartups();
					
					if((getNumStartups() == 20 || getNumStartups() == 100) && isAppRated() == false) {
						// Prompt user to rate app
			        	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			        	builder.setTitle("Geef jouw waardering");
			        	builder.setMessage("Wat vind jij van Partyagenda - Harder Styles? Geef een waardering op Google Play.");
			        	builder.setPositiveButton("Oké", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
						        final Uri uri = Uri.parse("market://details?id=" + getPackageName());
						        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);
						        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0)
						        {
						            activity.startActivity(rateAppIntent);
						            setAppRated(true);
						            dialog.dismiss();
						        }
							}
						});
			        	builder.setNegativeButton("Niet nu", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								setAppRated(false);
								dialog.dismiss();
							}
						});	
			        	AlertDialog rateAppDialog = builder.create();
			        	rateAppDialog.show();
					}
				}
			}
			
			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
			@Override
			public void onActivityResumed(Activity activity) {}
			@Override
			public void onActivityPaused(Activity activity) {}
			@Override
			public void onActivityDestroyed(Activity activity) {}
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
		});
	}
	
	// Num startups 
	public long getNumStartups(){
		return mPrefs.getLong("num_startups", 0);
	}
	private void incrementNumStartups() {
		mPrefs.edit().putLong("num_startups", getNumStartups()+1).commit();
	}
	
	// Is rated
	private boolean isAppRated(){
		return mPrefs.getBoolean("is_rated", false);
	}
	
	public void setAppRated(boolean rated) {
		mPrefs.edit().putBoolean("is_rated", rated).commit();
	}
	
	// Last refresh
	public Calendar getLastRefresh()
	{
		Calendar lastRefresh = new GregorianCalendar();
		lastRefresh.setTimeInMillis(mPrefs.getLong("last_refresh", 0));
		return lastRefresh;
	}
	public void setLastRefreshToNow()
	{
		Calendar now = Calendar.getInstance();
		long lastRefresh = now.getTimeInMillis();
		mPrefs.edit().putLong("last_refresh", lastRefresh).commit();
	}
}
