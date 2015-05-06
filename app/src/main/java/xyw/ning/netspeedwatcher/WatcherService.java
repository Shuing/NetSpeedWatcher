package xyw.ning.netspeedwatcher;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.IBinder;
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
        Notification notification = new Notification(R.drawable.icon,
                getString(R.string.app_name), System.currentTimeMillis());
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        notification.setLatestEventInfo(this, "网速监控", "保持应用在后台运行",
                pendingintent);
        startForeground(0x111, notification);
        return super.onStartCommand(intent, START_STICKY, startId);
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
        params = new WindowManager.LayoutParams();
        // 设置window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
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
        // 设置悬浮窗的位置
        String positions = SharedPreferencesHelper.getSharedPreferences().getString(SharedPreferencesHelper.KEY_POSITION, "0x0");
        String[] strs = positions.split("x");
        int x = Integer.valueOf(strs[0]);
        int y = Integer.valueOf(strs[1]);
        params.x = x;
        params.y = y;
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
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        SharedPreferencesHelper.putString(SharedPreferencesHelper.KEY_POSITION, params.x + "x" + params.y);
                        break;
                }
                return true;
            }
        });
        wm.addView(view, params);
        isAdded = true;
    }

    class UpdateTask extends AsyncTask<Void, String, Void> {
        private static final long EXT_M = 1048576;
        private static final long EXT_K = 1024;
        private long lastDownBytes = 0;
        private long lastUpBytes = 0;
        private long curDownBytes = 0;
        private long curUpBytes = 0;

        public UpdateTask() {
            lastDownBytes = TrafficStats.getTotalRxBytes();
            lastUpBytes = TrafficStats.getTotalTxBytes();
        }

        @Override
        protected Void doInBackground(Void[] params) {
            while (isAdded) {
                publishProgress(getDownSpeed(), getUpSpeed());
                lastDownBytes = curDownBytes;
                lastUpBytes = curUpBytes;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
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
            curDownBytes = TrafficStats.getTotalRxBytes();
            float downBytes = curDownBytes - lastDownBytes;
            return getBytesAsString(downBytes);
        }

        private String getUpSpeed() {
            curUpBytes = TrafficStats.getTotalTxBytes();
            float upBytes = curUpBytes - lastUpBytes;
            return getBytesAsString(upBytes);
        }

        private String getBytesAsString(float f) {
            if (f > EXT_M) {
                return setScale(f / EXT_M) + "M";
            } else if (f > EXT_K) {
                return setScale(f / EXT_K) + "K";
            } else if (0 == f) {
                return "-";
            } else {
                return setScale(f) + "B";
            }
        }

        /**
         * 精确到小数点后两位
         */
        private float setScale(float f) {
            return new BigDecimal(f).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        }

    }

}