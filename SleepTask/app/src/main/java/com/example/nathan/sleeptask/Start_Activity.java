package com.example.nathan.sleeptask;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.nathan.sleeptask.db.TaskContract;
import com.example.nathan.sleeptask.db.TaskDbHelper;

import java.util.Calendar;

/**
 * Created by hermano on 10/21/16.
 */

public class Start_Activity extends AppCompatActivity {
    public static int time;
    private TaskDbHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_day);
        mHelper = new TaskDbHelper(this);
    }

    public void set_bed_time(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
        updateDbTable();
        UpdateUI();
    }

    private void updateDbTable () {
        Spinner dayChooser = (Spinner) findViewById(R.id.day_select);
        String day = dayChooser.getSelectedItem().toString();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.DayEntry.TABLE,
                null, TaskContract.DayEntry.COL_DAY_TITLE + " = " + "Thusday", null, null, null, null);
        if (cursor.getCount() == 1) {
            ContentValues cv = new ContentValues();
            cv.put(TaskContract.DayEntry.COL_BED_TIME, time);
            db.update(TaskContract.DayEntry.TABLE,
                    cv,
                    TaskContract.DayEntry.COL_DAY_TITLE + "=" + day,
                    null);
        }
        else {
            ContentValues values = new ContentValues();
            values.put(TaskContract.DayEntry.COL_DAY_TITLE, day);
            values.put(TaskContract.DayEntry.COL_BED_TIME, time);
            db.insertWithOnConflict(TaskContract.DayEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private void UpdateUI() {
        TextView bedTime = (TextView) findViewById(R.id.bed_time_label);
        bedTime.setText(String.valueOf(time));
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Start_Activity.time = (hourOfDay * 100) + minute;
        }
    }
}
