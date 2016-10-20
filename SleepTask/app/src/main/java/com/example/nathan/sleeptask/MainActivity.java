package com.example.nathan.sleeptask;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.nathan.sleeptask.db.TaskContract;
import com.example.nathan.sleeptask.db.TaskDbHelper;

import android.text.format.DateFormat;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    public static int time;
    private static int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        mHelper = new TaskDbHelper(this);
        UpdateUI();
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
                //final EditText newTask = (EditText) dialogView.findViewById(R.id.add_new_task);
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
                                values.put(TaskContract.TaskEntry.COL_DAY_TITLE, "Thursday");
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
        List<RowItem> rowItems = new ArrayList<RowItem>();;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE, null, null, null, null, null, TaskContract.TaskEntry.COL_START_TIME + " ASC");
        while (cursor.moveToNext()) {
            int title = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int isImportant = cursor.getColumnIndex(TaskContract.TaskEntry.COL_IMPORTANT);
            int startTime = cursor.getColumnIndex(TaskContract.TaskEntry.COL_START_TIME);
            int duration = cursor.getColumnIndex(TaskContract.TaskEntry.COL_DURATION);
            taskList.add(cursor.getString(title));

            if (cursor.getInt(isImportant) == 1) {
                int backgroundColor = checkTaskTime(cursor.getInt(startTime), cursor.getInt(duration));
                RowItem item = new RowItem(R.drawable.important, cursor.getString(title), backgroundColor);
                rowItems.add(item);
            }
            else {
                int backgroundColor = checkTaskTime(cursor.getInt(startTime), cursor.getInt(duration));
                RowItem item = new RowItem(R.drawable.transparent, cursor.getString(title), backgroundColor);
                rowItems.add(item);
            }
        }

        CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                R.layout.item_task, rowItems);
        mTaskListView.setAdapter(adapter);
        cursor.close();
        db.close();
    }

    private int checkTaskTime(int startTime, int duration) {
        Calendar c = Calendar.getInstance();

        int hours = (c.AM_PM + c.HOUR + 3) * 100;
        int minutes = c.MINUTE;
        Toast.makeText(this, "Startime: " + (startTime) + "is less than: " + (hours), Toast.LENGTH_SHORT).show();
        if (startTime + duration < hours + minutes) {
            return Color.GREEN;
        }
        else if (startTime < hours + minutes) {
            return Color.YELLOW;
        }
        else {
            return Color.WHITE;
        }
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getReadableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[] {task});
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
        final NumberPicker numberPicker =  new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);
        NumberPicker.OnValueChangeListener valueChangedListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange( NumberPicker picker, int oldVal, int newVal) {
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
