package xyw.ning.netspeedwatcher;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by ning on 15-4-27.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
