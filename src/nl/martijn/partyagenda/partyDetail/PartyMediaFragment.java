package nl.martijn.partyagenda.partyDetail;

import nl.martijn.partyagenda.PartysContract;
import nl.martijn.partyagenda.R;
import nl.martijn.partyagenda.partyDetail.videoList.VideoListAdapter;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
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
import android.widget.ListView;

public class PartyMediaFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	
	/* ******************************* */
	/* ********** Constants ********** */
	/* ******************************* */
	
	public final static String TAG = "PARTY_LIST_FRAGMENT";
	
	// Cursor loader constants
	private final static int VIDEO_LIST_LOADER_ID = 2;
	private final static String[] PROJECTION = {
		PartysContract.Partys.Video._ID,
		PartysContract.Partys.Video.YOUTUBE_ID,
		PartysContract.Partys.Video.TITLE,
		PartysContract.Partys.Video.DURATION,
		PartysContract.Partys.Video.UPLOADER,
		PartysContract.Partys.Video.UPLOADED
	};
	
	/* ******************************* */
	/* ********** Variables ********** */
	/* ******************************* */
	
	private int mPartyId;
	
	private VideoListAdapter mVideoListAdapter = null;
	
	public PartyMediaFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mPartyId = getActivity().getIntent().getExtras().getInt("id");
		
		// Create a custom cursor adapter
		mVideoListAdapter = new VideoListAdapter(getActivity(), null, 0);
		setListAdapter(mVideoListAdapter);
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// Get the id of the selected party
		Cursor c = (Cursor)getListView().getItemAtPosition(position);
		String youtubeId = c.getString(c.getColumnIndex(PartysContract.Partys.Video.YOUTUBE_ID));
		
		// Start a new activity        
		try{
		    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtubeId));
		    startActivity(intent);                 
		} catch (ActivityNotFoundException ex){
		    Intent intent=new Intent(Intent.ACTION_VIEW, 
		    Uri.parse("http://www.youtube.com/watch?v="+youtubeId));
		    startActivity(intent);
		}
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_party_media, container, false);
		getLoaderManager().initLoader(VIDEO_LIST_LOADER_ID, null, this);  
		
		return v;
	}

	/* ********** Cursor loader ********** */
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    Uri partyItemUri = ContentUris.withAppendedId(PartysContract.Partys.CONTENT_URI_ID, mPartyId);
	    Uri videoUri = Uri.withAppendedPath(partyItemUri, "video");
	    
		// Create cursor loader
		return new CursorLoader(
                getActivity(),
                videoUri,
                PROJECTION,
                null,
                null,
                PartysContract.Partys.Video.UPLOADED);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mVideoListAdapter.swapCursor(cursor);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mVideoListAdapter.swapCursor(null);
	}
}

