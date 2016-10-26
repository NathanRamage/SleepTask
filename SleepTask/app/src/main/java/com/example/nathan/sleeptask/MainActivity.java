package com.example.nathan.sleeptask;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.nathan.sleeptask.db.TaskContract;
import com.example.nathan.sleeptask.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    public static int time;
    private static int duration;
    private static String day;
    private List<Integer> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle b = getIntent().getExtras();
        ids = new ArrayList<>();
        if (b != null) {
            day = b.getString("Day");
            Toast.makeText(this, "Day is " + day, Toast.LENGTH_SHORT).show();
        }
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        mHelper = new TaskDbHelper(this);
        UpdateUI();

        int repeatTime = 10;// time in seconds
        AlarmManager processTimer = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, processTimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //Repeat alarm every second
        processTimer.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), repeatTime * 1000, pendingIntent);
    }

    @Override
    public void onDestroy() {
        // cancel the alarm
        Intent intent = new Intent(this, processTimerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.add_new_task, null);
                final Spinner spinner = (Spinner) dialogView.findViewById(R.id.task_chooser);
                final CheckBox importantTask = (CheckBox) dialogView.findViewById(R.id.important_task);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(dialogView)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = spinner.getSelectedItem().toString();
                                Boolean isImportant = importantTask.isChecked();
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                                values.put(TaskContract.TaskEntry.COL_DAY_TITLE, day);
                                values.put(TaskContract.TaskEntry.COL_IMPORTANT, isImportant);
                                values.put(TaskContract.TaskEntry.COL_START_TIME, time);
                                values.put(TaskContract.TaskEntry.COL_DURATION, duration);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                UpdateUI();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void UpdateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        List<RowItem> rowItems = new ArrayList<RowItem>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        ids = new ArrayList<>();
        try {

            Cursor cursor = db.query(TaskContract.TaskEntry.TABLE, null, TaskContract.TaskEntry.COL_DAY_TITLE + "=?", new String[]{day}, null, null, TaskContract.TaskEntry.COL_START_TIME + " ASC");
            while (cursor.moveToNext()) {
                int id = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
                int title = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
                int isImportant = cursor.getColumnIndex(TaskContract.TaskEntry.COL_IMPORTANT);
                int startTime = cursor.getColumnIndex(TaskContract.TaskEntry.COL_START_TIME);
                int duration = cursor.getColumnIndex(TaskContract.TaskEntry.COL_DURATION);
                taskList.add(cursor.getString(title));
                Toast.makeText(this, "Found day: " + cursor.getString(cursor.getColumnIndex(TaskContract.TaskEntry.COL_DAY_TITLE)), Toast.LENGTH_SHORT).show();

                if (cursor.getInt(isImportant) == 1) {
                    int backgroundColor = checkTaskTime(cursor.getInt(startTime), cursor.getInt(duration));
                    RowItem item = new RowItem(R.drawable.important, cursor.getString(title), backgroundColor);
                    rowItems.add(item);
                    ids.add(cursor.getInt(id));
                } else {
                    int backgroundColor = checkTaskTime(cursor.getInt(startTime), cursor.getInt(duration));
                    RowItem item = new RowItem(R.drawable.transparent, cursor.getString(title), backgroundColor);
                    rowItems.add(item);
                    ids.add(cursor.getInt(id));
                }
            }

            CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                    R.layout.item_task, rowItems);
            mTaskListView.setAdapter(adapter);
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(this, "No tasks added yet", Toast.LENGTH_SHORT).show();
            Cursor cursor = db.query(TaskContract.TaskEntry.TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_DAY_TITLE);
                Toast.makeText(this, "day: " + cursor.getString(idx), Toast.LENGTH_SHORT).show();
            }
        }
        db.close();
    }

    private int checkTaskTime(int startTime, int duration) {
        Calendar c = Calendar.getInstance();

        int hours = c.get(Calendar.HOUR_OF_DAY) * 100;
        int minutes = c.get(Calendar.MINUTE);

        Log.d(TAG, "Time: " + c.get(Calendar.HOUR_OF_DAY));

        if (startTime + duration < hours + minutes) {
            return Color.GREEN;
        } else if (startTime < hours + minutes) {
            return Color.YELLOW;
        } else {
            return Color.WHITE;
        }
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        ListView listView = (ListView) parent.getParent();
        final int position = listView.getPositionForView(parent);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry._ID + " = ?",
                new String[]{ids.get(position).toString()});
        db.close();
        UpdateUI();
    }

    public void set_time(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void set_duration(View view) {
        NumberPickerDialog();
    }

    private void NumberPickerDialog() {
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);
        NumberPicker.OnValueChangeListener valueChangedListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                duration = newVal;
            }
        };
        numberPicker.setOnValueChangedListener(valueChangedListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(numberPicker);
        builder.setTitle("Enter duration in minutes")
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null)
                .show();
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
            MainActivity.time = (hourOfDay * 100) + minute;
        }
    }
}
