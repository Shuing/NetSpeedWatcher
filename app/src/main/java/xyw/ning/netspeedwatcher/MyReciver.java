package xyw.ning.netspeedwatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ning on 15-4-24.
 */
public class MyReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, WatcherService.class));
    }
}
