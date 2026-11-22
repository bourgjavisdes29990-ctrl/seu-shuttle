package edu.seu.shuttle;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;

public class ShuttleWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TOGGLE_DIR = "edu.seu.shuttle.ACTION_TOGGLE_DIR";
    public static final String ACTION_REFRESH = "edu.seu.shuttle.ACTION_REFRESH";
    private static final String PREFS_NAME = "shuttle_widget_prefs";
    private static final String KEY_TO_SCHOOL = "key_to_school";

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
            prefs.edit().putBoolean(KEY_TO_SCHOOL, !toSchool).apply();

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

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean toSchool = prefs.getBoolean(KEY_TO_SCHOOL, true);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_shuttle);

        // Direction Toggle button text & PendingIntent
        String toggleText = toSchool ? "去上课 →" : "← 回宿舍";
        views.setTextViewText(R.id.btn_toggle_dir, toggleText);

        Intent toggleIntent = new Intent(context, ShuttleWidgetProvider.class);
        toggleIntent.setAction(ACTION_TOGGLE_DIR);
        PendingIntent togglePending = PendingIntent.getBroadcast(
            context, 0, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btn_toggle_dir, togglePending);

        // Whole widget tap = Refresh
        Intent refreshIntent = new Intent(context, ShuttleWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(
            context, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, refreshPending);

        // Calculate next buses
        Calendar now = Calendar.getInstance();
        int currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        List<ScheduleData.Bus> buses = ScheduleData.getBuses(toSchool);
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
                views.setTextViewText(R.id.tv_tag_teaching, "📚 教学专线 (直达纪忠楼)");
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
            views.setTextViewText(R.id.tv_route_title, toSchool ? "明天早点出发" : "已无回宿舍班次");
            views.setViewVisibility(R.id.tv_tag_teaching, View.GONE);
            views.setTextViewText(R.id.tv_upcoming, "首班车 07:20 发车");
        }

        manager.updateAppWidget(widgetId, views);
    }
}
