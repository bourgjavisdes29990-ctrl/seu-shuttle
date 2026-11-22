package edu.seu.shuttle;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShuttleWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TOGGLE_DIR = "edu.seu.shuttle.ACTION_TOGGLE_DIR";
    public static final String ACTION_TOGGLE_JIZHONG = "edu.seu.shuttle.ACTION_TOGGLE_JIZHONG";
    public static final String ACTION_REFRESH = "edu.seu.shuttle.ACTION_REFRESH";

    private static final String PREFS_NAME = "shuttle_widget_prefs";
    private static final String KEY_TO_SCHOOL = "key_to_school";
    private static final String KEY_ONLY_JIZHONG = "key_only_jizhong";
    private static final String KEY_OVERRIDE_TIME = "key_override_time";

    // 坐标常量 (WGS84 / GCJ-02 大致范围)
    // 兰台研究生公寓 (兰台街附近): 31.8999° N, 118.8223° E
    private static final double LAT_LANTAI = 31.8999;
    private static final double LNG_LANTAI = 118.8223;

    // 九龙湖教学区/校区中心: 31.8873° N, 118.8209° E
    private static final double LAT_CAMPUS = 31.8873;
    private static final double LNG_CAMPUS = 118.8209;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_TOGGLE_DIR.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean toSchool = prefs.getBoolean(KEY_TO_SCHOOL, true);
            prefs.edit()
                 .putBoolean(KEY_TO_SCHOOL, !toSchool)
                 .putLong(KEY_OVERRIDE_TIME, System.currentTimeMillis())
                 .apply();
            updateAllWidgets(context);
        } else if (ACTION_TOGGLE_JIZHONG.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            boolean onlyJizhong = prefs.getBoolean(KEY_ONLY_JIZHONG, false);
            prefs.edit().putBoolean(KEY_ONLY_JIZHONG, !onlyJizhong).apply();
            updateAllWidgets(context);
        } else if (ACTION_REFRESH.equals(intent.getAction()) ||
                   Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            updateAllWidgets(context);
        }
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName name = new ComponentName(context, ShuttleWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(name);
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
    }

    private static Boolean determineDirectionByLocation(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return null;

            Location bestLocation = null;
            List<String> providers = lm.getProviders(true);
            for (String provider : providers) {
                try {
                    Location l = lm.getLastKnownLocation(provider);
                    if (l != null) {
                        if (bestLocation == null || l.getTime() > bestLocation.getTime()) {
                            bestLocation = l;
                        }
                    }
                } catch (SecurityException ignored) {}
            }

            if (bestLocation != null) {
                float[] distLantai = new float[1];
                float[] distCampus = new float[1];
                Location.distanceBetween(bestLocation.getLatitude(), bestLocation.getLongitude(), LAT_LANTAI, LNG_LANTAI, distLantai);
                Location.distanceBetween(bestLocation.getLatitude(), bestLocation.getLongitude(), LAT_CAMPUS, LNG_CAMPUS, distCampus);

                // 如果距离兰台更近 -> 去上课 (true)；如果距离九龙湖更近 -> 回宿舍 (false)
                return distLantai[0] <= distCampus[0];
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean onlyJizhong = prefs.getBoolean(KEY_ONLY_JIZHONG, false);

        // 判断当前方向：优先智能定位判断（如果最近没有手动强行覆盖）
        boolean toSchool;
        boolean isGpsDetermined = false;
        long overrideTime = prefs.getLong(KEY_OVERRIDE_TIME, 0);
        long timeSinceOverride = System.currentTimeMillis() - overrideTime;

        Boolean locDirection = determineDirectionByLocation(context);
        if (locDirection != null && timeSinceOverride > 45 * 60 * 1000) { // 45分钟后重新恢复智能位置感知
            toSchool = locDirection;
            isGpsDetermined = true;
            prefs.edit().putBoolean(KEY_TO_SCHOOL, toSchool).apply();
        } else {
            toSchool = prefs.getBoolean(KEY_TO_SCHOOL, true);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_shuttle);

        // 1. Direction Button
        String gpsPrefix = isGpsDetermined ? "📍" : "";
        String toggleText = toSchool ? (gpsPrefix + "去上课 →") : (gpsPrefix + "← 回宿舍");
        views.setTextViewText(R.id.btn_toggle_dir, toggleText);

        Intent toggleIntent = new Intent(context, ShuttleWidgetProvider.class);
        toggleIntent.setAction(ACTION_TOGGLE_DIR);
        PendingIntent togglePending = PendingIntent.getBroadcast(
            context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_toggle_dir, togglePending);

        // 2. Jizhonglou Quick Filter Button
        if (onlyJizhong) {
            views.setTextViewText(R.id.btn_toggle_jizhong, "🏫 纪忠楼");
            views.setTextColor(R.id.btn_toggle_jizhong, Color.parseColor("#38bdf8")); // cyan
        } else {
            views.setTextViewText(R.id.btn_toggle_jizhong, "全部");
            views.setTextColor(R.id.btn_toggle_jizhong, Color.parseColor("#94a3b8")); // muted
        }

        Intent jizhongIntent = new Intent(context, ShuttleWidgetProvider.class);
        jizhongIntent.setAction(ACTION_TOGGLE_JIZHONG);
        PendingIntent jizhongPending = PendingIntent.getBroadcast(
            context, 2, jizhongIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_toggle_jizhong, jizhongPending);

        // 3. Whole widget tap = Refresh
        Intent refreshIntent = new Intent(context, ShuttleWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(
            context, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, refreshPending);

        // 4. Calculate next buses
        Calendar now = Calendar.getInstance();
        int currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        List<ScheduleData.Bus> allBuses = ScheduleData.getBuses(toSchool);
        List<ScheduleData.Bus> buses = new ArrayList<>();
        if (onlyJizhong) {
            for (ScheduleData.Bus b : allBuses) {
                if (b.isTeaching) {
                    buses.add(b);
                }
            }
        } else {
            buses = allBuses;
        }

        ScheduleData.Bus nextBus = null;
        ScheduleData.Bus bus2 = null;
        ScheduleData.Bus bus3 = null;

        for (ScheduleData.Bus b : buses) {
            if (b.getMinutes() > currentMinute) {
                if (nextBus == null) {
                    nextBus = b;
                } else if (bus2 == null) {
                    bus2 = b;
                } else if (bus3 == null) {
                    bus3 = b;
                    break;
                }
            }
        }

        if (nextBus != null) {
            int diff = nextBus.getMinutes() - currentMinute;
            views.setTextViewText(R.id.tv_next_time, nextBus.time);

            // Countdown styling
            String cdText;
            int cdColor;
            if (diff <= 5) {
                cdText = "⚡ 快跑！还有 " + diff + " 分钟";
                cdColor = Color.parseColor("#ef4444"); // Red
            } else if (diff <= 15) {
                cdText = "🏃 抓紧！还有 " + diff + " 分钟";
                cdColor = Color.parseColor("#f59e0b"); // Orange
            } else {
                cdText = "☕ 还有 " + diff + " 分钟";
                cdColor = Color.parseColor("#22c55e"); // Green
            }
            views.setTextViewText(R.id.tv_countdown, cdText);
            views.setTextColor(R.id.tv_countdown, cdColor);

            views.setTextViewText(R.id.tv_route_title, nextBus.route);

            // Teaching line tag
            if (nextBus.isTeaching) {
                views.setViewVisibility(R.id.tv_tag_teaching, View.VISIBLE);
                views.setTextViewText(R.id.tv_tag_teaching, "📚 直达纪忠楼专线");
            } else {
                views.setViewVisibility(R.id.tv_tag_teaching, View.GONE);
            }

            // Upcoming line
            StringBuilder upcoming = new StringBuilder("接下来: ");
            if (bus2 != null) upcoming.append(bus2.time).append(" ");
            if (bus3 != null) upcoming.append(bus3.time);
            views.setTextViewText(R.id.tv_upcoming, upcoming.toString());

        } else {
            views.setTextViewText(R.id.tv_next_time, "--:--");
            views.setTextViewText(R.id.tv_countdown, "今日末班已过");
            views.setTextColor(R.id.tv_countdown, Color.parseColor("#94a3b8"));
            if (onlyJizhong) {
                views.setTextViewText(R.id.tv_route_title, toSchool ? "今日已无纪忠楼直达车" : "今日已无纪忠楼发车班次");
                views.setTextViewText(R.id.tv_upcoming, toSchool ? "首班专线 07:30" : "首班专线 10:50");
            } else {
                views.setTextViewText(R.id.tv_route_title, toSchool ? "明天早点出发" : "已无回宿舍班次");
                views.setTextViewText(R.id.tv_upcoming, "首班车 07:20 发车");
            }
            views.setViewVisibility(R.id.tv_tag_teaching, View.GONE);
        }

        manager.updateAppWidget(widgetId, views);
    }
}
