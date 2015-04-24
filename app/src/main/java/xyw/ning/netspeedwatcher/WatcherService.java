package xyw.ning.netspeedwatcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import java.math.BigDecimal;

public class WatcherService extends Service {

    private boolean isAdded = false; // 是否已增加悬浮窗
    private static WindowManager wm;
    private static WindowManager.LayoutParams params;
    private View view;
    private TextView downTV;
    private TextView upTV;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
        new UpdateTask().execute(new Void[]{});
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        wm.removeViewImmediate(view);
        isAdded = false;
        super.onDestroy();
    }

    /**
     * 创建悬浮窗
     */
    private void createFloatView() {
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.netspeedview, null);
        downTV = (TextView) view.findViewById(R.id.tv_down);
        upTV = (TextView) view.findViewById(R.id.tv_up);
        wm = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);

        int[] hw = getScreenSize(wm);
        Log.d("Ning", hw[0] + "x" + hw[1]);

        params = new WindowManager.LayoutParams();

        // 设置window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        /*
         * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE; 那么优先级会降低一些,
		 * 即拉下通知栏不可见
		 */

        params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

        // 设置Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        /*
         * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */

        // 设置悬浮窗的长和宽
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        params.x = hw[0] / 3;
        params.y = hw[1] / 2;

        // 设置悬浮窗的Touch监听
        view.setOnTouchListener(new OnTouchListener() {
            int lastX, lastY;
            int paramX, paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        // 更新悬浮窗位置
                        wm.updateViewLayout(view, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });

//        wm.addView(btn_floatView, params);
        wm.addView(view, params);
        isAdded = true;
    }

    public int[] getScreenSize(WindowManager wm) {
        int[] hw = new int[2];
        int widthPixels = -1;
        int heightPixels = -1;
        Display d = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        heightPixels = metrics.heightPixels;
        // includes window decorations (statusbar bar/navigation bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
            try {
                widthPixels = (Integer) Display.class
                        .getMethod("getRawWidth").invoke(d);
                heightPixels = (Integer) Display.class
                        .getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
            // includes window decorations (statusbar bar/navigation bar)
        else if (Build.VERSION.SDK_INT >= 17)
            try {
                android.graphics.Point realSize = new android.graphics.Point();
                Display.class.getMethod("getRealSize",
                        android.graphics.Point.class).invoke(d, realSize);
                widthPixels = realSize.x;
                heightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        hw[0] = widthPixels;
        hw[1] = heightPixels;
        return hw;
    }

    class UpdateTask extends AsyncTask<Void, String, Void> {
        private static final long EXT_M = 1048576;
        private static final long EXT_K = 1024;
        private long lastDownBytes = 0;
        private long lastUpBytes = 0;
        private long curDownBytes = 0;
        private long curUpBytes = 0;

        @Override
        protected Void doInBackground(Void[] params) {

            while (isAdded) {
                publishProgress(getDownSpeed(), getUpSpeed());
                lastDownBytes = curDownBytes;
                lastUpBytes = curUpBytes;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String[] values) {
            downTV.setText(values[0]);
            upTV.setText(values[1]);
        }

        private String getDownSpeed() {
            String str;
            curDownBytes = TrafficStats.getTotalRxBytes();
            float downBytes = curDownBytes - lastDownBytes;
            str = getBytesAsString(downBytes);
            return str;
        }

        private String getUpSpeed() {
            String str;
            curUpBytes = TrafficStats.getTotalTxBytes();
            float upBytes = curUpBytes - lastUpBytes;
            str = getBytesAsString(upBytes);
            return str;
        }

        private String getBytesAsString(float f) {
            String str;
            if (f > EXT_M) {
                str = setScale(f / EXT_M) + "M";
            } else if (f > EXT_K) {
                str = setScale(f / EXT_K) + "K";
            } else {
                str = setScale(f) + "B";
            }
            return str;
        }

        private float setScale(float f) {
            return new BigDecimal(f).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        }

    }

}
