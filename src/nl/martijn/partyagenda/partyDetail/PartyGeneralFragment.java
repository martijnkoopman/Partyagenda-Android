package nl.martijn.partyagenda.partyDetail;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.martijn.partyagenda.ExternalStorage;
import nl.martijn.partyagenda.ImageDownloadService;
import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.R;
import nl.martijn.partyagenda.partyDetail.onlineList.OnlineListDialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PartyGeneralFragment extends Fragment {

	SimpleDateFormat mDateFormater;
	
	private BroadcastReceiver mFlyerDownloadNotifier = null;
	
	private int mPartyId;
	private String mPartyName;
	
	public PartyGeneralFragment() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_party_general, container, false);
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Date formatter
		mDateFormater = new SimpleDateFormat("EEEEEEEEE d MMMMMMMMM");
		
	    // Get party ID
	    mPartyId = getActivity().getIntent().getExtras().getInt("id");
	    final int partyId = mPartyId;
	    
	    String[] PROJECTION = {
	    		PartysContract.Partys._ID,
	    		PartysContract.Partys.NAME,
	    		PartysContract.Partys.SUBNAME,
	    		PartysContract.Partys.DATE,
	    		PartysContract.Partys.TIME,
	    		PartysContract.Partys.PRICE,
	    		PartysContract.Partys.VENUE,
	    		PartysContract.Partys.CITY,
	    		PartysContract.Partys.GENRES,
	    		PartysContract.Partys.LINE_UP
	    };
	    
	    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, partyId);
	    Cursor c = getActivity().getContentResolver().query(partyItemUri, PROJECTION, null, null, null);
	    if(c == null || c.getCount() < 1)
	    	return; // TODO: Test me
	
		// Get references to layout views
		TextView nameTextView = (TextView)getActivity().findViewById(R.id.party_name);
		TextView subnameTextView = (TextView)getActivity().findViewById(R.id.party_subname);
		TextView dateTextView = (TextView)getActivity().findViewById(R.id.party_date);
		TextView timeTextView = (TextView)getActivity().findViewById(R.id.party_time);
		TextView priceTextView = (TextView)getActivity().findViewById(R.id.party_price);
		TextView venueTextView = (TextView)getActivity().findViewById(R.id.party_venue);
		TextView cityTextView = (TextView)getActivity().findViewById(R.id.party_city);
		TextView genresTextView = (TextView)getActivity().findViewById(R.id.party_genres);
		TextView lineUpTextView = (TextView)getActivity().findViewById(R.id.party_line_up);
		
		// Buy tickets online button ----------
		Button buyTicketButton = (Button)getActivity().findViewById(R.id.party_buy_ticket);
		buyTicketButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
            	OnlineListDialogFragment newFragment = OnlineListDialogFragment.newInstance(partyId, "Ticket", PartysContract.Partys.Tickets.TABLE_NAME);
                newFragment.show(getFragmentManager(), "dialog");
			}
		});
		// Enable/disable button
	    Uri ticketsUri = Uri.withAppendedPath(partyItemUri, PartysContract.Partys.Tickets.TABLE_NAME);
	    Cursor cur = getActivity().getContentResolver().query(ticketsUri, null, null, null, null);
	    if(cur != null) {
	    	int linkCount = cur.getCount();
	    	buyTicketButton.setText("Ticket kopen ("+linkCount+")");
	    	if(linkCount < 1)
	    		buyTicketButton.setEnabled(false);
	    	else
	    		buyTicketButton.setEnabled(true);
	    	cur.close();
	    }
	    
		// Book bus trip button ----------
		Button bookTripButton = (Button)getActivity().findViewById(R.id.party_book_trip);
		bookTripButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
            	OnlineListDialogFragment newFragment = OnlineListDialogFragment.newInstance(partyId, "Busreis", PartysContract.Partys.Travel.TABLE_NAME);
                newFragment.show(getFragmentManager(), "dialog");
			}
		});
		// Enable/disable button
	    Uri bustripUri = Uri.withAppendedPath(partyItemUri, PartysContract.Partys.Travel.TABLE_NAME);
	    cur = getActivity().getContentResolver().query(bustripUri, null, null, null, null);
	    if(cur != null) {
	    	int linkCount = cur.getCount();
	    	bookTripButton.setText("Busreis boeken ("+linkCount+")");
	    	if(linkCount < 1)
	    		bookTripButton.setEnabled(false);
	    	else
	    		bookTripButton.setEnabled(true);
	    	cur.close();
	    }
		
		// Visit website button ----------
		Button visitWebsiteButton = (Button)getActivity().findViewById(R.id.party_visit_website);
		visitWebsiteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	OnlineListDialogFragment newFragment = OnlineListDialogFragment.newInstance(partyId, "Website", PartysContract.Partys.Online.TABLE_NAME);
                newFragment.show(getFragmentManager(), "dialog");
            }
        });
		// Enable/disable button
	    Uri websitesUri = Uri.withAppendedPath(partyItemUri, PartysContract.Partys.Online.TABLE_NAME);
	    cur = getActivity().getContentResolver().query(websitesUri, null, null, null, null);
	    if(cur != null) {
	    	int linkCount = cur.getCount();
	    	visitWebsiteButton.setText("Website bezoeken ("+linkCount+")");
	    	if(linkCount < 1)
	    		visitWebsiteButton.setEnabled(false);
	    	else
	    		visitWebsiteButton.setEnabled(true);
	    	cur.close();
	    }

		// Get values from cursor
		String nameStr = c.getString(c.getColumnIndex(PartysContract.Partys.NAME));
		mPartyName = nameStr;
		long dateLong = c.getLong(c.getColumnIndex(PartysContract.Partys.DATE));
		Date date = new Date(dateLong);
		String dateStr = mDateFormater.format(date);
		String subnameStr = c.getString(c.getColumnIndex(PartysContract.Partys.SUBNAME));
		String timeStr = c.getString(c.getColumnIndex(PartysContract.Partys.TIME));
		String priceStr = c.getString(c.getColumnIndex(PartysContract.Partys.PRICE));
		String venueStr = c.getString(c.getColumnIndex(PartysContract.Partys.VENUE));
		String cityStr = c.getString(c.getColumnIndex(PartysContract.Partys.CITY));
		String genresStr = c.getString(c.getColumnIndex(PartysContract.Partys.GENRES));
		String lineUpStr = c.getString(c.getColumnIndex(PartysContract.Partys.LINE_UP));
		c.close();
		
		// Bind values to views
		nameTextView.setText(nameStr.toUpperCase());
		if(subnameStr.isEmpty()) {
			subnameTextView.setVisibility(View.GONE);
		} else {
			subnameTextView.setVisibility(View.VISIBLE);
			subnameTextView.setText(subnameStr);
		}
		dateTextView.setText(dateStr);
		timeTextView.setText(timeStr);
		priceTextView.setText(priceStr.replace(";", "\r\n"));
		venueTextView.setText(venueStr);
		cityTextView.setText(cityStr);
		genresTextView.setText(genresStr.replace(";", " / "));
		lineUpTextView.setText(lineUpStr.replace(";", "\r\n"));
		
		// Flyer - register for download event
		mFlyerDownloadNotifier = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Bitmap flyerThumb = ExternalStorage.openBitmap(ExternalStorage.FLYER_THUMB_STORAGE_DIR + partyId + ".jpg");
				if(flyerThumb != null) {
					PartyGeneralFragment.this.setFlyerThumbnail(flyerThumb);
				}
			}
		};
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
		lbm.registerReceiver(mFlyerDownloadNotifier, new IntentFilter("flyer_thumb"));
		
		// Flyer - load from external storage
		Bitmap flyerThumb = ExternalStorage.openBitmap(ExternalStorage.FLYER_THUMB_STORAGE_DIR + partyId + ".jpg");
		if(flyerThumb != null) {
			setFlyerThumbnail(flyerThumb);
		}
		else {
			// Not found - download flyer thumbnail from internet
	    	String flyerThumbUrl = "http://content.partyagenda-app.nl/v1/flyers/thumbs/" + partyId + ".jpg";
	    	ImageDownloadService.startDownloadingImage(getActivity(), flyerThumbUrl, ExternalStorage.FLYER_THUMB_STORAGE_DIR,""+partyId, "flyer_thumb");
		}
	}
	
	@Override
	public void onDestroyView (){
		super.onDestroyView();
		
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
		lbm.unregisterReceiver(mFlyerDownloadNotifier);
	}
	
	@Override
	public void onDestroy (){
		super.onDestroy();
	}

	public void setFlyerThumbnail(Bitmap thumb) {
		ImageView flyerThumbImageView = (ImageView)getActivity().findViewById(R.id.party_flyer_thumb);
		flyerThumbImageView.setImageBitmap(thumb);
		
		// onClick
		flyerThumbImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), PartyFlyerActivity.class);
				i.putExtra("id", mPartyId);
				i.putExtra("name", mPartyName);
				startActivity(i);
			}
		});
	}
}
