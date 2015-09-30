package xyw.ning.netspeedwatcher;

import android.app.Activity;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
            startService(new Intent(this, WatcherService.class));
        }
        finish();
    }

}