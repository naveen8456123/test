package s172589.cheapfuel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Halvor on 22.11.2015.
 */
public class Prisoppdatering extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent i = new Intent(this, SjekkEksternDatabase.class);

        Calendar cal = Calendar.getInstance();

        Toast.makeText(this, "I Prisoppdatering.java",Toast.LENGTH_SHORT).show();
        PendingIntent pintent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 10 * 100, pintent);

        return super.onStartCommand(intent, flags, startId);
    }
}
