package com.example.nathan.sleeptask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.nathan.sleeptask.db.TaskContract;
import com.example.nathan.sleeptask.db.TaskDbHelper;

/**
 * Created by Nathan on 22/10/2016.
 */

// Gets called every alarm cycle
public class processTimerReceiver extends BroadcastReceiver {
        private TaskDbHelper mHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        mHelper = new TaskDbHelper(context);
        // check the time till bed time
        sendNotification(context);
        Log.d("Timer", "Notification");
    }

        private void sendNotification (Context context) {
            SQLiteDatabase db = mHelper.getReadableDatabase();
            Cursor cursor = db.query(TaskContract.DayEntry.TABLE,
                    null,
                    null, null, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(TaskContract.DayEntry.COL_BED_TIME);
            String bedTime = cursor.getString(idx);
            NotificationCompat.Builder mNotfiBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.sleep)
                    .setContentTitle("Almost time to sleep")
                    .setContentText("go to sleep at: " + bedTime);

            // Creates an explicit intent for an Activity in app
            Intent resultIntent = new Intent(context, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotfiBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mnotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update tyhe notification later on
            mnotificationManager.notify(0, mNotfiBuilder.build());
        }
}
