package xyw.ning.netspeedwatcher;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreference封装
 *
 * @author tonyzhao
 */
public class SharedPreferencesHelper {

    private final static String TAG = "SharedPreferencesHelper";
    public final static String SHAREDPREFERENCE_NAME = "preferences";

    public final static String KEY_SLEEP = "key_sleep";
    public final static String KEY_COLOR_DOWN = "key_color_down";
    public final static String KEY_COLOR_UP = "key_color_up";
    public final static String KEY_TEXTSIZE = "key_textsize";
    public final static String KEY_POSITION = "key_position";

    private static SharedPreferences mSharedPreferences = null;

    /**
     * 获取SharedPreferences实例
     */
    public static SharedPreferences getSharedPreferences() {
        if (null == mSharedPreferences) {
            mSharedPreferences = MyApp.getContext()
                    .getSharedPreferences(SHAREDPREFERENCE_NAME,
                            Context.MODE_PRIVATE);
        }
        return mSharedPreferences;
    }

    /**
     * 设置Boolean值
     *
     * @param key
     * @param value
     */
    public static void putBoolean(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).commit();
    }

    /**
     * 设置String值
     *
     * @param key
     * @param value
     */
    public static void putString(String key, String value) {
        getSharedPreferences().edit().putString(key, value).commit();
    }

    /**
     * 设置Int值
     *
     * @param key
     * @param value
     */
    public static void putInt(String key, int value) {
        getSharedPreferences().edit().putInt(key, value).commit();
    }

    /**
     * 设置Float值
     *
     * @param key
     * @param value
     */
    public static void putFloat(String key, float value) {
        getSharedPreferences().edit().putFloat(key, value).commit();
    }

    /**
     * 设置Long值
     *
     * @param key
     * @param value
     */
    public static void putLong(String key, long value) {
        getSharedPreferences().edit().putLong(key, value).commit();
    }

}
