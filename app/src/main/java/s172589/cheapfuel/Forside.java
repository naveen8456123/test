package s172589.cheapfuel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Forside extends AppCompatActivity {

    ListView stasjonsListe, billigste;
    LocationManager lm;
    DBHandler db = new DBHandler(this);
    LocationHandler lh = new LocationHandler(this);
    ArrayAdapter arrayAdapter;
    ImageButton slett, slettTilbake;
    TextView t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forside);

        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_cheapfuel_meny_ikon);

        /////////// TESTING //////////
        // Dette er de stasjonene som ligger på den eksterne databasen.
        // SharedPreferences ble her brukt under testing for å at jeg ikke skulle legge til
        // stasjonene unødvendig mange ganger.

//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean install = sp.getBoolean("Install",false);
//
//
//        if(!install){
//            Stasjon s1 = new Stasjon("Statoil Kiellands Plass",59.9278345,10.7489184,16.00,R.drawable.ic_statoil);
//            Stasjon s2 = new Stasjon("Statoil Økern",59.9204648,10.7796995,14.32,R.drawable.ic_statoil);
//            Stasjon s3 = new Stasjon("Shell 7-Eleven",59.9251658,10.8312896,12.14,R.drawable.ic_shell);
//            Stasjon s4 = new Stasjon("Statoil Storo",59.933488,10.7861819,15.45,R.drawable.ic_statoil);
//            Stasjon s5 = new Stasjon("Shell Lørenskog",59.9422142,10.8745154,11.04,R.drawable.ic_shell);
//            Stasjon s6 = new Stasjon("Shell Linderud",59.9438439,10.8387374,10.94,R.drawable.ic_shell);
//            Stasjon s7 = new Stasjon("Esso Lillestrøm",59.9537479,10.9470023,13.12,R.drawable.ic_esso);
//            Stasjon s8 = new Stasjon("Shell Trondheimsveien",59.9805841,10.9973604,11.36,R.drawable.ic_shell);
//            db.leggTilStasjon(s1,false);
//            db.leggTilStasjon(s2,false);
//            db.leggTilStasjon(s3,false);
//            db.leggTilStasjon(s4,false);
//            db.leggTilStasjon(s5,false);
//            db.leggTilStasjon(s6,false);
//            db.leggTilStasjon(s7,false);
//            db.leggTilStasjon(s8,false);
//
//            sp.edit().putBoolean("Install",true).apply();
//        }


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, lh);
        lh.settLokasjon();

        leggTilStasjonerListView();

        stasjonsListe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stasjon s = (Stasjon) stasjonsListe.getItemAtPosition(position);

                Context con = getApplicationContext();
                Intent i = new Intent(con, EnkelStasjonKart.class);
                i.putExtra("Stasjon", s);
                startActivity(i);


            }
        });

        billigste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Stasjon s = (Stasjon) billigste.getItemAtPosition(position);

                if (s.getLat() == 0.0 || s.getLng() == 0.0) {
                    Toast.makeText(Forside.this, R.string.ingen_stasjon, Toast.LENGTH_SHORT).show();
                    return;
                }
                Context con = getApplicationContext();
                Intent i = new Intent(con, EnkelStasjonKart.class);
                i.putExtra("Stasjon", s);
                startActivity(i);
            }
        });

        // Longclick for å slette stasjon, hvis ønskelig. Custom Dialog
        stasjonsListe.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Stasjon s = (Stasjon) stasjonsListe.getItemAtPosition(position);

                LayoutInflater linf = LayoutInflater.from(Forside.this);
                View v = linf.inflate(R.layout.dialog_slett, null);
                final AlertDialog builder = new AlertDialog.Builder(Forside.this).create();

                slettTilbake = (ImageButton) v.findViewById(R.id.slettTilbake);
                slett = (ImageButton) v.findViewById(R.id.slett);
                t = (TextView) v.findViewById(R.id.slettTekst);

                t.setText(getApplicationContext().getString(R.string.slett_stasjon1) + "\r\n" + s.getNavn() + "?"
                        + "\r\n\r\n" + getApplicationContext().getString(R.string.slett_stasjon2));

                builder.setView(v);
                builder.show();

                slett.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.slettStasjon(s,true);
                        leggTilStasjonerListView();
                        builder.cancel();
                    }
                });
                slettTilbake.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.cancel();
                    }
                });
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_forside, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.innstillinger:

                Log.v("meny inst","Gps "+ lh.getGps_ok());
                boolean gps = lh.getGps_ok();
                Intent i = new Intent(this, Innstillinger.class);
                i.putExtra("gps", gps);
                startActivity(i);

                return true;

            case R.id.visAlleStasjoner:
                Intent k = new Intent(this, Kart.class);
                startActivity(k);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // Legger stasjoer i visning for alle stasjoner og billigste stasjon
    public void leggTilStasjonerListView() {
        stasjonsListe = (ListView) findViewById(R.id.stasjonsVisning);
        billigste = (ListView) findViewById(R.id.billigsteStasjonVisning);
        Location l = lh.getLokasjon();

        List<Stasjon> alleStasjoner = db.finnAlleStasjoner();
        if(alleStasjoner.size() < 0){
            Stasjon s = new Stasjon("" + R.string.default_stasjon,0.0,0.0,0.0,0);
            alleStasjoner.add(s);

            arrayAdapter = new ListViewAdapter(this, R.layout.list_stasjoner, alleStasjoner, l);
            stasjonsListe.setAdapter(arrayAdapter);
        } else {
            arrayAdapter = new ListViewAdapter(this, R.layout.list_stasjoner, alleStasjoner, l);
            stasjonsListe.setAdapter(arrayAdapter);
        }

        List<Stasjon> billigsteStasjon = db.finnBilligste();

        if(billigsteStasjon.size() < 0) {
            Stasjon s = new Stasjon("" + R.string.default_billigste,0.0,0.0,0.0,0);
            billigsteStasjon.add(s);

            arrayAdapter = new ListViewAdapter(this, R.layout.list_billigste,billigsteStasjon, l);
            billigste.setAdapter(arrayAdapter);
        } else {
            arrayAdapter = new ListViewAdapter(this, R.layout.list_billigste,billigsteStasjon, l);
            billigste.setAdapter(arrayAdapter);
        }
    }

    @Override
       protected void onResume() {
        super.onResume();
        leggTilStasjonerListView();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
}