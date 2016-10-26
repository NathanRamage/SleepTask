package com.example.nathan.sleeptask;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
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

        Spinner spinner = (Spinner) findViewById(R.id.day_select);
        String day = spinner.getSelectedItem().toString();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.DayEntry.TABLE, null, TaskContract.DayEntry.COL_DAY_TITLE + "=?",
                new String[] {day},
                null, null, null);
        TextView tv = (TextView) findViewById(R.id.bed_time_label);
        while (cursor.moveToNext()) {
            tv.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(TaskContract.DayEntry.COL_BED_TIME))));
            Log.d("Start_Activity", "Day: " + cursor.getString(cursor.getColumnIndex(TaskContract.DayEntry.COL_DAY_TITLE)) + " Bed Time: "+
                    cursor.getInt(cursor.getColumnIndex(TaskContract.DayEntry.COL_BED_TIME)));
        }
    }

    public void startTasks(View view) {
        UpdateDbTable();
        UpdateUI();
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
        // TODO Auto-generated method stub
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                setTime = (selectedHour * 100) + selectedMinute;
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.setButton(TimePickerDialog.BUTTON_POSITIVE, "OK",
                new TimePickerDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UpdateDbTable();
                        UpdateUI();
                    }
                });
        mTimePicker.show();
    }

    public void UpdateDbTable () {
        Spinner dayChooser = (Spinner) findViewById(R.id.day_select);
        String day = dayChooser.getSelectedItem().toString();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        try {
            Cursor cursor = db.query(TaskContract.DayEntry.TABLE,
                    null, null, null, null, null, null);
            ContentValues cv = new ContentValues();
            Log.d("Start_Activity", "Query ok");
            cv.put(TaskContract.DayEntry.COL_BED_TIME, 2300);
            /*db.update(TaskContract.DayEntry.TABLE,
                    cv,
                    TaskContract.DayEntry.COL_DAY_TITLE + "=" + day,
                    null);*/
            Log.d("Start_Activity", "Day: " + cursor.getString(cursor.getColumnIndex(TaskContract.DayEntry.COL_DAY_TITLE)));
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                Log.d("Start_Activity", "Day: " + cursor.getString(cursor.getColumnIndex(TaskContract.DayEntry.COL_DAY_TITLE)) + " bed time: " + cursor.getInt(cursor.getColumnIndex(TaskContract.DayEntry.COL_BED_TIME)));
            }
        } catch (Exception e) {
            Log.d("Start_Activity", e.toString());
            ContentValues values = new ContentValues();
            values.put(TaskContract.DayEntry.COL_DAY_TITLE, day);
            values.put(TaskContract.DayEntry.COL_BED_TIME, setTime);
            db.insert(TaskContract.DayEntry.TABLE,
                    null,
                    values);
        }
    }

    public void UpdateUI() {
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
