package com.example.nathan.sleeptask;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nathan.sleeptask.db.TaskContract;
import com.example.nathan.sleeptask.db.TaskDbHelper;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        mHelper = new TaskDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        while(cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            Log.d(TAG, "Task: " + cursor.getString(idx));
        }
        cursor.close();
        db.close();
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
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
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
        ListView listview = (ListView) findViewById(R.id.list_todo);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                null, null, null, null, null);
        int numKids = 0;
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            taskList.add(cursor.getString(idx));
            numKids++;
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_task, // what view to use for teh items
                    R.id.task_title, // where to put the String of data
                    taskList); // where to get all the data
            mTaskListView.setAdapter(mAdapter); // set it as teh adapter of the ListView instance
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }


        int firstPosition = listview.getFirstVisiblePosition() - listview.getHeaderViewsCount(); // This is the same as child #0
        int wantedChild = firstPosition;
        for (int i  = 0; i < numKids; i++) {
            // Say, first visible position is 8, you want position 10, wantedChild will now be 2
            // So that means your view is child #2 in the ViewGroup:
            if (wantedChild < 0 || wantedChild >= listview.getChildCount()) {
                Log.w(TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
                return;
            }
            // Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
            View wantedView = listview.getChildAt(wantedChild);
            ImageView image = (ImageView) wantedView.findViewById(R.id.image_view);
            image.setImageResource(R.drawable.important);
            Log.d(TAG, "Task flagged");
            wantedChild++;
        }

        cursor.close();
        db.close();
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
}
