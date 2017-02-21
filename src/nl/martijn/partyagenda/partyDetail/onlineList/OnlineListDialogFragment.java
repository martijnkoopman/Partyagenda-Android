package nl.martijn.partyagenda.partyDetail.onlineList;

import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.R;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class OnlineListDialogFragment extends DialogFragment implements LoaderCallbacks<Cursor> {
	
	/* ******************************* */
	/* ********** Constants ********** */
	/* ******************************* */
	
	public final static String TAG = "PARTY_LIST_FRAGMENT";
	
	// Cursor loader constants
	private final static int ONLINE_LIST_LOADER_ID = 1;
	private final static String[] PROJECTION = {
		PartysContract.Partys.Online._ID,
		PartysContract.Partys.Online.PARTY_ID,
		PartysContract.Partys.Online.TYPE,
		PartysContract.Partys.Online.NAME,
		PartysContract.Partys.Online.URL
	};
	
	/* ******************************* */
	/* ********** Variables ********** */
	/* ******************************* */
	
	int mPartyId;
	String mName;
	String mTable;
	
	private OnlineListAdapter mOnlineListAdapter;

	/* ***************************** */
	/* ********** Methods ********** */
	/* ***************************** */
	
    /**
     * Create a new instance of OnlineListDialogFragment, providing "name"
     * as an argument.
     */
    public static OnlineListDialogFragment newInstance(int partyId, String name, String table) {
    	OnlineListDialogFragment f = new OnlineListDialogFragment();

        // Supply name input as an argument.
        Bundle args = new Bundle();
        args.putInt("party_id", partyId);
        args.putString("name", name);
        args.putString("table", table);
        f.setArguments(args);

        return f;
    }

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPartyId = getArguments().getInt("party_id");
        mName = getArguments().getString("name");
        mTable = getArguments().getString("table");
        
        // Create a CursorAdapter
        mOnlineListAdapter = new OnlineListAdapter(getActivity(), null, 0);
    }
    	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
		getDialog().setTitle(mName);
        
		View v = inflater.inflate(R.layout.fragment_online_list, container, false);
        ListView lv = (ListView)v.findViewById(R.id.online_list);
        
        // Create onClick listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	// Form online item URI
            	Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, mPartyId);
        	    Uri onlineUri = Uri.withAppendedPath(partyItemUri, mTable);
        	    Uri onlineItemUri = ContentUris.withAppendedId(onlineUri, id);
        	    
        	    // Read online item
        	    Cursor c = getActivity().getContentResolver().query(onlineItemUri, PROJECTION, null, null, null);
        	    if(c != null) {
        	    	if(c.getCount() > 0) { 
		        	    String url = c.getString(c.getColumnIndex(PartysContract.Partys.Online.URL));
		        		       	    
		        	    // Open URL in browser (TODO: Intent handler available)
		        	    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
		        		startActivity(intent);
        	    	}
        	    	c.close();
        	    }
        		
        		// Dismiss dialog
        		dismiss();
            }
        });
        
    	lv.setAdapter(mOnlineListAdapter);
        getLoaderManager().initLoader(ONLINE_LIST_LOADER_ID, null, this);  
        
        return v;
	}

	/* ********** Cursor loader ********** */
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
	    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, mPartyId);
	    Uri onlineUri = Uri.withAppendedPath(partyItemUri, mTable);
	    
		// Create cursor loader
		return new CursorLoader(
                getActivity(),
                onlineUri,
                PROJECTION,
                null,
                null,
                null);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mOnlineListAdapter.swapCursor(cursor);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mOnlineListAdapter.swapCursor(null);
	}
}
