package nl.martijn.partyagenda.partyList;

import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.partyDetail.PartyDetailActivity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class PartyListFragment extends ListFragment implements 
	LoaderCallbacks<Cursor>
{
	/* ******************************* */
	/* ********** Constants ********** */
	/* ******************************* */
	
	public final static String TAG = "PARTY_LIST_FRAGMENT";
	
	// Cursor loader constants
	private final static int PARTY_LIST_LOADER_ID = 0;
	private final static String[] PROJECTION = {
		PartysContract.Partys._ID,
		PartysContract.Partys.NAME,
		PartysContract.Partys.POPULARITY,
		PartysContract.Partys.DATE,
		PartysContract.Partys.VENUE,
		PartysContract.Partys.CITY,
		PartysContract.Partys.IS_FAVORITE
	};
	private static final String SELECTION_ALL_PARTYS = PartysContract.Partys.NAME + " LIKE ? " + "OR " +
														PartysContract.Partys.SUBNAME + " LIKE ? " + "OR " +
														PartysContract.Partys.VENUE + " LIKE ? " + "OR " +
														PartysContract.Partys.ADDRESS + " LIKE ? " + "OR " +
														PartysContract.Partys.CITY + " LIKE ? " + "OR " +
														PartysContract.Partys.GENRES + " LIKE ? " + "OR " +
														PartysContract.Partys.LINE_UP + " LIKE ?";
	private static final String SELECTION_MY_PARTYS = "( " + SELECTION_ALL_PARTYS + " ) AND " + PartysContract.Partys.IS_FAVORITE + " = ?";
	
	
	/* ******************************* */
	/* ********** Variables ********** */
	/* ******************************* */
	
	private PartyListAdapter mPartyListAdapter;
	
	private boolean mShowMyPartys = false;	// Set from parent activity (action bar action)
	private String mSearchTerm = "";		// Set from parent activity (action bar action)
	private String[] mSelectionArgs_AllPartys = new String[7];
	private String[] mSelectionArgs_MyPartys = new String[8];
	
	/* ***************************** */
	/* ********** Methods ********** */
	/* ***************************** */
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create a CursorAdapter
        mPartyListAdapter = new PartyListAdapter(getActivity(), null, 0);
        
        // Set cursor adapter for the list view
        setListAdapter(mPartyListAdapter);
       
        // Initializes the loader
        getLoaderManager().initLoader(PARTY_LIST_LOADER_ID, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// Get the id of the selected party
		Cursor c = (Cursor)getListView().getItemAtPosition(position);
		int partyId = c.getInt(c.getColumnIndex(PartysContract.Partys._ID));
		
		// Start a new activity
        Intent intent = new Intent(getActivity(), PartyDetailActivity.class);
        intent.putExtra("id", partyId);
        startActivity(intent);
    }
    
    /* ******************** Cursor loader ******************** */
    
   	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		// Set-up selection (All party's vs. My party's)
		for(int i = 0; i < 7; i++) {
			mSelectionArgs_AllPartys[i] = "%" + mSearchTerm + "%";
			mSelectionArgs_MyPartys[i] = "%" + mSearchTerm + "%";
		}
		mSelectionArgs_MyPartys[7] = "1";

		String selection;
		String[] selectionArgs;
		if(mShowMyPartys) {
			selection = SELECTION_MY_PARTYS;
			selectionArgs = mSelectionArgs_MyPartys;
		}
		else
		{
			selection = SELECTION_ALL_PARTYS;
			selectionArgs = mSelectionArgs_AllPartys;
		}

		// Create cursor loader
		return new CursorLoader(
                getActivity(),
                PartysContract.Partys.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                PartysContract.Partys.DATE);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mPartyListAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mPartyListAdapter.swapCursor(null);
	}
	
	public void setSearchTerm(String searchTerm)
	{
		mSearchTerm = searchTerm;
        getLoaderManager().restartLoader(0, null, PartyListFragment.this);
	}
	
	public void setShowMyPartys(boolean showMyPartys)
	{
		mShowMyPartys = showMyPartys;
        getLoaderManager().restartLoader(0, null, PartyListFragment.this);
	}
}
