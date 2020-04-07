package s172589.cheapfuel;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Halvor on 14.11.2015.
 */
public class Innstillinger extends AppCompatActivity {

    Button gps_knapp;
    boolean gpsStatus;
    TextView gpsTekst, oppdateringTekst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_innstillinger);

        setTitle(R.string.inst_tittel);

        gpsTekst = (TextView)findViewById(R.id.gpsTekst);
        oppdateringTekst = (TextView)findViewById(R.id.startAutomatiskHenting);
        gpsTekst.setText(R.string.GPS_tekst);
        oppdateringTekst.setText(R.string.oppdatering);

        Intent i = getIntent();
        gpsStatus = (boolean)i.getSerializableExtra("gps");

        gps_knapp = (Button)findViewById(R.id.gps_knapp);

        if(gpsStatus){
            gps_knapp.setText(R.string.inst_GPSav_tittel);
        } else {
            gps_knapp.setText(R.string.inst_GPSpå_tittel);
        }

        gps_knapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aktiverGps();
            }
        });

        Switch s = (Switch) findViewById(R.id.startHentingSlider);

        SharedPreferences shared = getSharedPreferences("HentingAvPå", MODE_PRIVATE);
        s.setChecked(shared.getBoolean("hentAvPå",false));

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = getSharedPreferences("HentingAvPå", MODE_PRIVATE).edit();
                    editor.putBoolean("hentAvPå", true);
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "SWITCH PÅ", Toast.LENGTH_SHORT).show();
                    Log.d("SWITCH", "Jeg har blitt satt som på");
                    startHentingAvStasjoner();
                } else {
                    SharedPreferences.Editor editor = getSharedPreferences("HentingAvPå", MODE_PRIVATE).edit();
                    editor.putBoolean("hentAvPå", false);
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "SWITCH AV", Toast.LENGTH_SHORT).show();
                    Log.d("SWITCH", "Jeg har blitt satt som av");
                    stoppService();
                }
            }
        });
    }

    // Må sende bruker ut av appen for å slå på GPS.
    // Dette siden å slå på GPS fra appen blir sett på som et sikkerhetsbrudd
    public void aktiverGps(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Innstillinger.this);
        if(gpsStatus){
            builder.setTitle("Slå av GPS?");
            builder.setMessage("Vil du slå av GPS? \r\nDu vil bli sendt til telefonens egne instillinger.");
        } else {
            builder.setTitle("Slå på GPS?");
            builder.setMessage("Vil du slå på GPS? \r\nDu vil bli sendt til telefonens egne instillinger.");
        }

        builder.setCancelable(true);
        builder.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });

        AlertDialog a = builder.create();
        a.show();
    }

    public void startHentingAvStasjoner() {
        Intent intent = new Intent();
        intent.setAction("s172589.startHentingAvStasjoner");
        sendBroadcast(intent);
    }

    public void stoppService() {

        Intent i = new Intent(this, SjekkEksternDatabase.class);
        Toast.makeText(getApplicationContext(),"STOPPER OPPDATERING", Toast.LENGTH_SHORT).show();
        Log.d("STOPPOPPDATER","Stopper oppdatering");

        PendingIntent pintent = PendingIntent.getService(this, 0, i, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);

    }
}