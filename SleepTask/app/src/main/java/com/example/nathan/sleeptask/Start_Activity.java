package com.example.nathan.sleeptask;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.nathan.sleeptask.db.TaskContract;
import com.example.nathan.sleeptask.db.TaskDbHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Created by hermano on 10/21/16.
 */

public class Start_Activity extends AppCompatActivity {
    public static int setTime;
    private TaskDbHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_day);
        mHelper = new TaskDbHelper(this);

        try {
            Spinner spinner = (Spinner) findViewById(R.id.day_select);
            String day = spinner.getSelectedItem().toString();
            SQLiteDatabase db = mHelper.getReadableDatabase();
            Cursor cursor = db.query(TaskContract.DayEntry.TABLE, new String[]{TaskContract.DayEntry.COL_BED_TIME}, TaskContract.DayEntry.COL_DAY_TITLE + "=?", new String[]{day},
                    null, null, null);
            TextView tv = (TextView) findViewById(R.id.bed_time_label);
            while (cursor.moveToNext()) {
                tv.setText(cursor.getInt(cursor.getColumnIndex(TaskContract.DayEntry.COL_BED_TIME)));
            }
        }
        catch (Exception e) {

        }
    }

    public void startTasks(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.day_select);
        String day = spinner.getSelectedItem().toString();
        Intent start = new Intent(Start_Activity.this, MainActivity.class);
        Bundle b = new Bundle();
        b.putString("Day", day);
        start.putExtras(b);
        startActivity(start);
        finish();
    }

    public void set_bed_time(View view) {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
        while( hasOpenedDialogs(this) ){};
        updateDbTable();
        UpdateUI();
        Toast.makeText(this, "time: " + setTime, Toast.LENGTH_SHORT).show();
    }

    private void updateDbTable () {
        Spinner dayChooser = (Spinner) findViewById(R.id.day_select);
        String day = dayChooser.getSelectedItem().toString();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query(TaskContract.DayEntry.TABLE,
                    null, TaskContract.DayEntry.COL_DAY_TITLE + "=?", new String[]{day}, null, null, null);
            ContentValues cv = new ContentValues();
            cv.put(TaskContract.DayEntry.COL_BED_TIME, setTime);
            db.update(TaskContract.DayEntry.TABLE,
                    cv,
                    TaskContract.DayEntry.COL_DAY_TITLE + "=" + day,
                    null);
        } catch (Exception e) {
            ContentValues values = new ContentValues();
            values.put(TaskContract.DayEntry.COL_DAY_TITLE, day);
            values.put(TaskContract.DayEntry.COL_BED_TIME, setTime);
            db.insertWithOnConflict(TaskContract.DayEntry.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private void UpdateUI() {
        TextView bedTime = (TextView) findViewById(R.id.bed_time_label);
        bedTime.setText(String.valueOf(setTime));
    }

    public static boolean hasOpenedDialogs(FragmentActivity activity) {
        List<Fragment> fragments = activity.getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof DialogFragment) {
                    return true;
                }
            }
        }

        return false;
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
            Start_Activity.setTime = (hourOfDay * 100) + minute;
        }
    }
}
