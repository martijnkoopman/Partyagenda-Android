package nl.martijn.partyagenda.partyDetail;

import nl.martijn.partyagenda.PartysContract;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PartyMapFragment extends MapFragment {

	CameraPosition mCameraPosition;
	
	public PartyMapFragment() {
		// Required empty public constructor
	}
		
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//View v = inflater.inflate(R.layout.fragment_party_map, container, false);    
		View v = super.onCreateView(inflater, container, savedInstanceState);

	    // Get party ID
	    final int partyId = getActivity().getIntent().getExtras().getInt("id");
	    
	    // Get location information
	    String[] PROJECTION = {
	    		PartysContract.Partys._ID,
	    		PartysContract.Partys.VENUE,
	    		PartysContract.Partys.ADDRESS,
	    		PartysContract.Partys.POSTCODE,
	    		PartysContract.Partys.CITY,
	    		PartysContract.Partys.GENRES,
	    		PartysContract.Partys.LATITUDE,
	    		PartysContract.Partys.LONGITUDE
	    };
	    
	    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, partyId);
	    Cursor c = getActivity().getContentResolver().query(partyItemUri, PROJECTION, null, null, null);
	    if(c == null || c.getCount() < 1)
	    	return v;
	    
		final String venue = c.getString(c.getColumnIndex(PartysContract.Partys.VENUE));
		final String address = c.getString(c.getColumnIndex(PartysContract.Partys.ADDRESS));
		final String postcode = c.getString(c.getColumnIndex(PartysContract.Partys.POSTCODE));
		final String city = c.getString(c.getColumnIndex(PartysContract.Partys.CITY));
	    final double latitude = c.getDouble(c.getColumnIndex(PartysContract.Partys.LATITUDE));
	    final double longitude = c.getDouble(c.getColumnIndex(PartysContract.Partys.LONGITUDE));
	    final LatLng latLon = new LatLng(latitude, longitude);

	    // Set-up initial map
		GoogleMap map = getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		// Set camera position
		CameraUpdate camUpdate = null;
	    if(mCameraPosition != null)
	    	camUpdate = CameraUpdateFactory.newCameraPosition(mCameraPosition);
	    else
	    	camUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLon, 9.0f, 0, 0));
		map.moveCamera(camUpdate);
		
		// Add marker
		Marker venueMarker = map.addMarker(new MarkerOptions()
			.position(latLon)
			.title(venue)
			.snippet(city));
		venueMarker.showInfoWindow();

		// Set navigation on click listener
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				showNavigationDialog(venue+" "+address+" "+postcode+" "+city);
			}
		});
		return v;
	}

	
	@Override
	public void onDestroyView()
	{
	    super.onDestroyView();
	    mCameraPosition = getMap().getCameraPosition();
	}
	
	private void showNavigationDialog(final String address)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Navigatie")
        	   .setMessage("Navigatie starten?")
               .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Start navigation
                	   Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+address)); 
                	   startActivity(i);
                   }
               })
               .setNegativeButton("Nee", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   dialog.dismiss();
                   }
               });
    	AlertDialog navigateDialog = builder.create();
    	navigateDialog.show();
	}
}