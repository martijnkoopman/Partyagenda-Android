package nl.martijn.partyagenda.partyList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import nl.martijn.partyagenda.ExternalStorage;
import nl.martijn.partyagenda.ExternalStorageCached;
import nl.martijn.partyagenda.ImageDownloadService;
import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.R;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class PartyListAdapter extends CursorAdapter implements OnSharedPreferenceChangeListener {
	private Context mContext = null;
	private SimpleDateFormat mDateFormater;
	
	private ExternalStorageCached mCachedIconStorage = null;
	
	public PartyListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mContext = context;

		// Date formatters
		mDateFormater = new SimpleDateFormat("EE d MMM");
		
		// Register for broadcast messages:
		//	Sent when a thumbnail has been downloaded
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
		lbm.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				notifyDataSetChanged();
			}
		}, new IntentFilter("icon"));
		
        // Create icon cache
        mCachedIconStorage = new ExternalStorageCached();
        
	    // Attach onChangePreference listener for week / month grouping
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	    sp.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		notifyDataSetChanged();
	}
	
	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		View rowView = null;
		
		// Get the layout inflater
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	
		// Instantiate layout XML file into its corresponding View objects
		rowView = layoutInflater.inflate(R.layout.party_list_item, parent, false);
		return rowView; 
	}

	@Override
	public void bindView(View v, Context context, Cursor c) {
		// Get references to layout views
		TextView sectionTextView = (TextView)v.findViewById(R.id.party_list_item_section);
		ImageView iconImageView = (ImageView)v.findViewById(R.id.party_list_item_icon);
		TextView nameTextView = (TextView)v.findViewById(R.id.party_list_item_name);
		TextView venueTextView = (TextView)v.findViewById(R.id.party_list_item_venue);
		ImageView popularity1ImageView = (ImageView)v.findViewById(R.id.party_list_item_popularity1);
		ImageView popularity2ImageView = (ImageView)v.findViewById(R.id.party_list_item_popularity2);
		ImageView popularity3ImageView = (ImageView)v.findViewById(R.id.party_list_item_popularity3);
		ImageView popularity4ImageView = (ImageView)v.findViewById(R.id.party_list_item_popularity4);
		ImageView popularity5ImageView = (ImageView)v.findViewById(R.id.party_list_item_popularity5);
		TextView dateTextView = (TextView)v.findViewById(R.id.party_list_item_date);
			
		// Get values from cursor
		int partyId = c.getInt(c.getColumnIndex(PartysContract.Partys._ID));
		String nameStr = c.getString(c.getColumnIndex(PartysContract.Partys.NAME));
		String venueStr = c.getString(c.getColumnIndex(PartysContract.Partys.VENUE));
		String cityStr = c.getString(c.getColumnIndex(PartysContract.Partys.CITY));
		int popularity = c.getInt(c.getColumnIndex(PartysContract.Partys.POPULARITY));
		long dateLong = c.getLong(c.getColumnIndex(PartysContract.Partys.DATE));
		
		// Bind values to views
		nameTextView.setText(nameStr);
		venueTextView.setText(venueStr + ", " + cityStr);

		// date
		Date date = new Date(dateLong);
		String dateStr = mDateFormater.format(date);
		dateTextView.setText(dateStr);
		
		// popularity
		popularity1ImageView.setImageResource(R.drawable.ic_star_off);
		popularity2ImageView.setImageResource(R.drawable.ic_star_off);
		popularity3ImageView.setImageResource(R.drawable.ic_star_off);
		popularity4ImageView.setImageResource(R.drawable.ic_star_off);
		popularity5ImageView.setImageResource(R.drawable.ic_star_off);
		if(popularity > 0)
			popularity1ImageView.setImageResource(R.drawable.ic_star_on);
		if(popularity > 1)
			popularity2ImageView.setImageResource(R.drawable.ic_star_on);
		if(popularity > 2)
			popularity3ImageView.setImageResource(R.drawable.ic_star_on);
		if(popularity > 3)
			popularity4ImageView.setImageResource(R.drawable.ic_star_on);
		if(popularity > 4)
			popularity5ImageView.setImageResource(R.drawable.ic_star_on);
		
		
		// icon
		Bitmap icon = mCachedIconStorage.openBitmap(ExternalStorage.ICON_STORAGE_DIR + partyId + ".jpg");
		if(icon != null) {
			iconImageView.setImageBitmap(icon);
		} else {
			iconImageView.setImageResource(R.drawable.ic_launcher);
	    	String iconUrl = "http://content.partyagenda-app.nl/v1/icons/" + partyId + ".jpg";
	    	ImageDownloadService.startDownloadingImage(mContext, iconUrl, ExternalStorage.ICON_STORAGE_DIR, ""+partyId, "icon");
		}
		
		// TODO: Optimize code
		// ---------- Section header ----------
		GregorianCalendar rightNow = new GregorianCalendar();
		rightNow.setTime(new Date());
	    
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dateLong);
		// The section header is visible when the following requirements are met:
		//1. Cursor's position (p) = 0 (first row of list)
		//2a. When p's month differs from p-1's month
		//2b. When p's week differs from p-1's week
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean weeklyView = true ? sp.getString("list_group", "week").equals("week") : false;
		if(c.getPosition() < 1) {
			// First row
			sectionTextView.setVisibility(View.VISIBLE);
			
			// Set section header text
			if(weeklyView) {
				// TODO: Optimize code
				if(rightNow.get(GregorianCalendar.WEEK_OF_YEAR) == calendar.get(GregorianCalendar.WEEK_OF_YEAR))
				{
					sectionTextView.setText("Deze week");
				} else if(rightNow.get(GregorianCalendar.WEEK_OF_YEAR)+1 == calendar.get(GregorianCalendar.WEEK_OF_YEAR)) {
					sectionTextView.setText("Volgende week (" + calendar.get(GregorianCalendar.WEEK_OF_YEAR) + ")");
				} else {
					sectionTextView.setText("Week " + calendar.get(GregorianCalendar.WEEK_OF_YEAR));
				}
			} else {
				sectionTextView.setText(monthNumberToString(calendar.get(GregorianCalendar.MONTH)));
			}
		} else
		{
			// Second to Nth row
			c.moveToPrevious();
			long prevDateLong = c.getLong(c.getColumnIndex(PartysContract.Partys.DATE));
			c.moveToNext();
			
			GregorianCalendar previousCalendar = new GregorianCalendar();
			previousCalendar.setTimeInMillis(prevDateLong);
			
			if(weeklyView)
			{
				// Sections by week
				if(calendar.get(GregorianCalendar.WEEK_OF_YEAR) != previousCalendar.get(GregorianCalendar.WEEK_OF_YEAR))
				{
					// Current row is new week
					sectionTextView.setVisibility(View.VISIBLE);
					if(rightNow.get(GregorianCalendar.WEEK_OF_YEAR) == calendar.get(GregorianCalendar.WEEK_OF_YEAR))
					{
						sectionTextView.setText("Deze week");
					} else if(rightNow.get(GregorianCalendar.WEEK_OF_YEAR)+1 == calendar.get(GregorianCalendar.WEEK_OF_YEAR)) {
						sectionTextView.setText("Volgende week (" + calendar.get(GregorianCalendar.WEEK_OF_YEAR) + ")");
					} else {
						sectionTextView.setText("Week " + calendar.get(GregorianCalendar.WEEK_OF_YEAR));
					}
				} else 
				{
					sectionTextView.setVisibility(View.GONE);
				}
			} else
			{
				// Sections by month
				if(calendar.get(GregorianCalendar.MONTH) != previousCalendar.get(GregorianCalendar.MONTH))
				{
					// Current row is new month
					sectionTextView.setVisibility(View.VISIBLE);
					sectionTextView.setText(monthNumberToString(calendar.get(GregorianCalendar.MONTH)));
				} else
				{
					sectionTextView.setVisibility(View.GONE);
				}
			}
		}
	}
	
	// Utility
	private String monthNumberToString(int monthNumber){
		String[] months = { "Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli",
							"Augustus", "September", "Oktober", "November", "December" };
		return months[monthNumber % months.length];
	}
}
