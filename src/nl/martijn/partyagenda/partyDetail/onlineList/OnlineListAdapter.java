package nl.martijn.partyagenda.partyDetail.onlineList;

import java.util.HashMap;

import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OnlineListAdapter extends CursorAdapter {

	private HashMap<String, Integer> mOnlineIconMap;
	private HashMap<String, Integer> mTicketsIconMap;
	private HashMap<String, Integer> mTravelIconMap;
	
	public OnlineListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		
		// onlineIconMap
		mOnlineIconMap = new HashMap<String, Integer>();
		mOnlineIconMap.put("website", R.drawable.browser);
		mOnlineIconMap.put("facebook", R.drawable.facebook);
		mOnlineIconMap.put("twitter", R.drawable.twitter);
		mOnlineIconMap.put("youtube", R.drawable.youtube);
        
		// ticketsIconMap
		mTicketsIconMap = new HashMap<String, Integer>();
		mTicketsIconMap.put("paylogic", R.drawable.paylogic);
		mTicketsIconMap.put("vakantie-veilingen", R.drawable.vakantieveilingen);
		mTicketsIconMap.put("ticketscript", R.drawable.ticketscript);
		
		// travelIconMap
		mTravelIconMap = new HashMap<String, Integer>();
		mTravelIconMap.put("event-travel", R.drawable.event_travel);
		mTravelIconMap.put("partybussen", R.drawable.partybussen);
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		View rowView = null;
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rowView = layoutInflater.inflate(R.layout.online_list_item, parent, false);
		return rowView; 
	}
	
	@Override
	public void bindView(View v, Context context, Cursor c) {
		ImageView iconImageView = (ImageView)v.findViewById(R.id.online_list_item_icon);
		TextView nameTextView = (TextView)v.findViewById(R.id.online_list_item_text);
		
		String name = c.getString(c.getColumnIndex(PartysContract.Partys.Online.NAME));
		String type = c.getString(c.getColumnIndex(PartysContract.Partys.Online.TYPE));
		String url = c.getString(c.getColumnIndex(PartysContract.Partys.Online.URL));

		nameTextView.setText(name);
		Integer iconDrawable = null;
		if(type != null && !type.equals("")){
			// Check in onlineIconMap
			iconDrawable = mOnlineIconMap.get(type);
			if(iconDrawable == null){
				// Check in ticketsIconMap
				iconDrawable = mTicketsIconMap.get(type);
				if(iconDrawable == null){
					// Check in travelIconMap
					iconDrawable = mTravelIconMap.get(type);
				}
			}
		}
		if(iconDrawable != null)
			iconImageView.setImageResource(iconDrawable);
		else 
			iconImageView.setImageResource(R.drawable.browser);
	}
}
