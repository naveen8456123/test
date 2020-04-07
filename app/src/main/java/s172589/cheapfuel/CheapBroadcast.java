package s172589.cheapfuel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Halvor on 22.11.2015.
 */
public class CheapBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "I BroadcastReciever", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(context,Prisoppdatering.class);
        context.startService(i);
    }
}