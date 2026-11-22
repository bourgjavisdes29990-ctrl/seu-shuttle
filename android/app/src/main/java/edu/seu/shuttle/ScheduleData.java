package edu.seu.shuttle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleData {

    public static class Bus {
        public String time;        // "08:30" or "07:20-09:40"
        public String route;       // "兰台 → 北门转盘"
        public boolean isTeaching; // 教学专线到纪忠楼
        public boolean isCycle;    // 循环发车时段
        public int startMin;
        public int endMin;
        public String note;        // 提示信息，如 "时间不确定"

        public Bus(String time, String route, boolean isTeaching) {
            this(time, route, isTeaching, false, 0, 0, null);
        }

        public Bus(String time, String route, boolean isTeaching, String note) {
            this(time, route, isTeaching, false, 0, 0, note);
        }

        public Bus(String time, String route, boolean isTeaching, boolean isCycle, int startMin, int endMin, String note) {
            this.time = time;
            this.route = route;
            this.isTeaching = isTeaching;
            this.isCycle = isCycle;
            this.startMin = startMin;
            this.endMin = endMin;
            this.note = note;
        }

        public int getMinutes() {
            if (isCycle) return startMin;
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        }
    }

    public static boolean isWeekend() {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    public static List<Bus> getBuses(boolean toSchool) {
        boolean weekend = isWeekend();
        List<Bus> list = new ArrayList<>();

        if (!weekend) { // 工作日
            if (toSchool) {
                // 早间段 (07:20 - 09:40 往纪忠楼循环发车，准点班次标注)
                list.add(new Bus("07:20-09:40", "兰台 → 北门 → 纪忠楼", true, true, 7*60+20, 9*60+40, "早高峰循环发车"));
                list.add(new Bus("07:30", "兰台 → 北门 → 纪忠楼", true, "准点发车(2辆车)"));
                list.add(new Bus("07:40", "兰台 → 北门 → 纪忠楼", true, "准点发车"));
                list.add(new Bus("09:30", "兰台 → 北门 → 纪忠楼", true, "准点发车(2辆车)"));

                // 平峰段
                list.add(new Bus("10:00", "兰台 → 北门转盘", false));
                list.add(new Bus("10:20", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("10:30", "兰台 → 北门转盘", false));
                list.add(new Bus("11:00", "兰台 → 北门转盘", false));
                list.add(new Bus("11:10", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("11:20", "兰台 → 北门转盘", false));
                list.add(new Bus("11:40", "兰台 → 北门转盘", false));
                list.add(new Bus("11:55", "兰台 → 北门转盘", false));
                list.add(new Bus("12:10", "兰台 → 北门转盘", false));
                list.add(new Bus("12:20", "兰台 → 北门转盘", false));
                list.add(new Bus("12:40", "兰台 → 北门转盘", false));
                list.add(new Bus("12:50", "兰台 → 北门转盘", false));
                list.add(new Bus("13:05", "兰台 → 北门转盘", false));
                list.add(new Bus("13:20", "兰台 → 北门转盘", false));
                list.add(new Bus("13:40", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("14:00", "兰台 → 北门转盘", false));
                list.add(new Bus("14:20", "兰台 → 北门转盘", false));
                list.add(new Bus("14:30", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("14:40", "兰台 → 北门转盘", false));
                list.add(new Bus("15:00", "兰台 → 北门转盘", false));
                list.add(new Bus("15:20", "兰台 → 北门转盘", false));
                list.add(new Bus("15:30", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("15:40", "兰台 → 北门转盘", false));
                list.add(new Bus("16:00", "兰台 → 北门转盘", false));
                list.add(new Bus("16:20", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("16:30", "兰台 → 北门转盘", false));
                list.add(new Bus("16:50", "兰台 → 北门转盘", false));
                list.add(new Bus("17:10", "兰台 → 北门转盘", false));
                list.add(new Bus("17:10", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("17:20", "兰台 → 北门转盘", false));
                list.add(new Bus("17:40", "兰台 → 北门转盘", false));
                list.add(new Bus("17:50", "兰台 → 北门转盘", false));
                list.add(new Bus("18:00", "兰台 → 北门转盘", false));
                list.add(new Bus("18:20", "兰台 → 北门转盘", false));
                list.add(new Bus("18:30", "兰台 → 北门转盘", false));
                list.add(new Bus("18:40", "兰台 → 北门转盘", false));
                list.add(new Bus("18:40", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("18:50", "兰台 → 北门转盘", false));

                // 晚间段 (19:45至21:45受南师附中放学影响，时间不确定)
                list.add(new Bus("19:10", "兰台 → 北门转盘", false));
                list.add(new Bus("19:30", "兰台 → 北门转盘", false));
                list.add(new Bus("19:30", "兰台 → 北门 → 纪忠楼", true));
                list.add(new Bus("19:45", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("20:05", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("20:25", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("20:25", "兰台 → 北门 → 纪忠楼", true, "南师附中放学·时间不确定"));
                list.add(new Bus("20:45", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("21:05", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("21:25", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("21:45", "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                list.add(new Bus("22:05", "兰台 → 北门转盘", false));
                list.add(new Bus("22:15", "兰台 → 北门转盘", false));
                list.add(new Bus("22:25", "兰台 → 北门转盘", false));

            } else { // 回宿舍
                // 早间段 (07:30 - 09:50 纪忠楼/北门循环往兰台)
                list.add(new Bus("07:30-09:50", "纪忠楼 → 北门 → 兰台", true, true, 7*60+30, 9*60+50, "早高峰循环发车"));

                // 平峰段
                list.add(new Bus("10:10", "北门转盘 → 兰台", false));
                list.add(new Bus("10:40", "北门转盘 → 兰台", false));
                list.add(new Bus("10:50", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("11:10", "北门转盘 → 兰台", false));
                list.add(new Bus("11:30", "北门转盘 → 兰台", false));
                list.add(new Bus("11:40", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("11:50", "北门转盘 → 兰台", false));
                list.add(new Bus("12:00", "北门转盘 → 兰台", false));
                list.add(new Bus("12:20", "北门转盘 → 兰台", false));
                list.add(new Bus("12:30", "北门转盘 → 兰台", false));
                list.add(new Bus("12:30", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("12:40", "北门转盘 → 兰台", false));
                list.add(new Bus("13:00", "北门转盘 → 兰台", false));
                list.add(new Bus("13:10", "北门转盘 → 兰台", false));
                list.add(new Bus("13:30", "北门转盘 → 兰台", false));
                list.add(new Bus("14:10", "北门转盘 → 兰台", false));
                list.add(new Bus("14:30", "北门转盘 → 兰台", false));
                list.add(new Bus("14:50", "北门转盘 → 兰台", false));
                list.add(new Bus("15:00", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("15:10", "北门转盘 → 兰台", false));
                list.add(new Bus("15:30", "北门转盘 → 兰台", false));
                list.add(new Bus("15:50", "北门转盘 → 兰台", false));
                list.add(new Bus("15:50", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("16:10", "北门转盘 → 兰台", false));
                list.add(new Bus("16:40", "北门转盘 → 兰台", false));
                list.add(new Bus("16:50", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("17:00", "北门转盘 → 兰台", false));
                list.add(new Bus("17:10", "北门转盘 → 兰台", false));
                list.add(new Bus("17:30", "北门转盘 → 兰台", false));
                list.add(new Bus("17:40", "北门转盘 → 兰台", false));
                list.add(new Bus("17:40", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("17:50", "北门转盘 → 兰台", false));
                list.add(new Bus("18:00", "北门转盘 → 兰台", false));
                list.add(new Bus("18:10", "北门转盘 → 兰台", false));
                list.add(new Bus("18:30", "北门转盘 → 兰台", false));
                list.add(new Bus("18:30", "纪忠楼 → 北门 → 兰台", true));
                list.add(new Bus("18:40", "北门转盘 → 兰台", false));
                list.add(new Bus("18:50", "北门转盘 → 兰台", false));
                list.add(new Bus("19:00", "北门转盘 → 兰台", false));

                // 晚间段 (19:45至21:45受南师附中放学影响，时间不确定)
                list.add(new Bus("19:20", "北门转盘 → 兰台", false));
                list.add(new Bus("19:40", "北门转盘 → 兰台", false));
                list.add(new Bus("19:55", "纪忠楼 → 北门 → 兰台", true, "附中放学高峰·时间不确定"));
                list.add(new Bus("20:00", "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                list.add(new Bus("20:20", "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                list.add(new Bus("20:40", "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                list.add(new Bus("20:50", "纪忠楼 → 北门 → 兰台", true, "附中放学高峰·时间不确定"));
                list.add(new Bus("21:00", "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                list.add(new Bus("21:20", "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                list.add(new Bus("21:40", "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                list.add(new Bus("21:40", "纪忠楼 → 北门 → 兰台", true, "附中放学高峰·时间不确定"));
                list.add(new Bus("22:00", "北门转盘 → 兰台", false));
                list.add(new Bus("22:10", "北门转盘 → 兰台", false));
                list.add(new Bus("22:20", "北门转盘 → 兰台", false));
                list.add(new Bus("22:30", "北门转盘 → 兰台", false));
            }
        } else { // 节假日
            if (toSchool) {
                list.add(new Bus("07:20", "兰台 → 北门转盘", false));
                list.add(new Bus("07:40", "兰台 → 北门 → 纪忠楼", true));
                String[] regularTimes = {
                    "08:00", "08:20", "08:40", "09:00", "09:20", "09:40",
                    "10:00", "10:30", "10:50", "11:10", "11:30", "11:50", "12:10", "12:30",
                    "12:50", "13:00", "13:20", "13:40", "14:00", "14:20", "14:40", "15:00",
                    "15:20", "15:40", "16:00", "16:20", "16:40", "17:00", "17:20", "17:40",
                    "18:00", "18:20", "18:40", "19:00", "19:20"
                };
                for (String t : regularTimes) {
                    list.add(new Bus(t, "兰台 → 北门转盘", false));
                }
                // 节假日晚间段 19:35至21:35时间不确定
                String[] uncertainTimes = {
                    "19:35", "19:55", "20:15", "20:35", "20:55", "21:15", "21:35"
                };
                for (String t : uncertainTimes) {
                    list.add(new Bus(t, "兰台 → 北门转盘", false, "南师附中放学·时间不确定"));
                }
                list.add(new Bus("21:55", "兰台 → 北门转盘", false));
                list.add(new Bus("22:15", "兰台 → 北门转盘", false));
                list.add(new Bus("22:25", "兰台 → 北门转盘", false));

            } else {
                String[] times = {
                    "07:30", "07:50", "08:10", "08:30", "08:50", "09:10", "09:30", "09:50",
                    "10:10", "10:40", "11:00", "11:20", "11:40", "12:00", "12:20", "12:40",
                    "12:55", "13:10", "13:30", "13:50", "14:10", "14:30", "14:50", "15:10",
                    "15:30", "15:50", "16:10", "16:30", "16:50", "17:10", "17:30", "17:50",
                    "18:10", "18:30", "18:50", "19:10", "19:30"
                };
                for (String t : times) {
                    list.add(new Bus(t, "北门转盘 → 兰台", false));
                }
                // 节假日晚间段 19:35至21:35
                String[] uncertainTimes = {
                    "19:50", "20:10", "20:30", "20:50", "21:10", "21:30"
                };
                for (String t : uncertainTimes) {
                    list.add(new Bus(t, "北门转盘 → 兰台", false, "附中放学高峰·时间不确定"));
                }
                list.add(new Bus("21:50", "北门转盘 → 兰台", false));
                list.add(new Bus("22:10", "北门转盘 → 兰台", false));
                list.add(new Bus("22:20", "北门转盘 → 兰台", false));
                list.add(new Bus("22:30", "北门转盘 → 兰台", false));
            }
        }
        return list;
    }
}
