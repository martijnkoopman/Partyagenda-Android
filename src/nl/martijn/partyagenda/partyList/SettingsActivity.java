package nl.martijn.partyagenda.partyList;

import java.util.ArrayList;

import nl.martijn.partyagenda.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Action bar
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Preferences
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SettingsFragment sf = new SettingsFragment();
        fragmentTransaction.replace(android.R.id.content,  sf);
        fragmentTransaction.commit();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Instellingen
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_settings);

        	// Set summary
            ArrayList<ListPreference> list = getListPreferenceList(getPreferenceScreen(), new ArrayList<ListPreference>());
            for (ListPreference p : list) {
                p.setSummary(p.getEntry());
            }
            
            final ListPreference automaticUpdatePreference = (ListPreference)findPreference("automatic_update");
            automaticUpdatePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					automaticUpdatePreference.setValue(newValue.toString());
		            preference.setSummary(automaticUpdatePreference.getEntry());
		            return false;
				}
			});
            
            final ListPreference listGroupPreference = (ListPreference)findPreference("list_group");
            listGroupPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					listGroupPreference.setValue(newValue.toString());
		            preference.setSummary(listGroupPreference.getEntry());
		            return false;
				}
			});
        }

        private ArrayList<ListPreference> getListPreferenceList(Preference p, ArrayList<ListPreference> list) {
            if( p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
                PreferenceGroup pGroup = (PreferenceGroup) p;
                int pCount = pGroup.getPreferenceCount();
                for(int i = 0; i < pCount; i++) {
                	getListPreferenceList(pGroup.getPreference(i), list); // recursive call
                }
            } else if( p instanceof ListPreference) {
                list.add((ListPreference)p);
            }
            return list;
        }
    }
}
