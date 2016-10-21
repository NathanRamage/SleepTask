package com.example.nathan.sleeptask.db;

/**
 * Created by nathan on 12/10/16.
 */

import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "com.example.nathan.sleeptask.db";
    public static final int DB_VERSION = 4;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
        public static final String COL_DAY_TITLE = "day";
        public static final String COL_IMPORTANT = "important";
        public static final String COL_START_TIME = "start_time";
        public static final String COL_DURATION = "duration";
    }

    public class DayEntry implements BaseColumns {
        public static final String TABLE = "days";

        public static final String COL_DAY_TITLE = "title";
        public static final String COL_BED_TIME = "bed_time";
    }
}
