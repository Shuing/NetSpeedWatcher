package xyw.ning.netspeedwatcher;

import android.app.Application;
import android.content.Context;

/**
 * Created by ning on 15-4-24.
 */
public class MyApp extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}
