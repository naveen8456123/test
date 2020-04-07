package s172589.cheapfuel;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Halvor on 22.11.2015.
 */
public class LeggTilStasjon extends AppCompatActivity {

    DBHandler db = new DBHandler(this);
    Stasjon s;

    String navn;
    Double lat, lng;
    Double pris;
    EditText navnFelt, prisFelt, latFelt, lngFelt;
    Button lagreKnapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legg_til);
        setTitle(R.string.leggTil_tittel);

        navnFelt = (EditText)findViewById(R.id.stasjonNavnFelt);
        prisFelt = (EditText)findViewById(R.id.stasjonPrisFelt);
        latFelt = (EditText)findViewById(R.id.stasjonLatFelt);
        lngFelt = (EditText)findViewById(R.id.stasjonLngFelt);
        lagreKnapp = (Button)findViewById(R.id.lagreStasjon);

        lagreKnapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             leggTilStasjon();
            }
        });

        // Henter lengdegrad og breddegrad fra kart og legger til i tekstfelt
        Bundle b = getIntent().getExtras();
        latFelt.setText("" + b.getDouble("Lat"));
        lngFelt.setText("" + b.getDouble("Lng"));
    }

    public void leggTilStasjon() {

        // Feilhåndtering hvis bruker trykker uten å ha skrevet inn noe
        if(navnFelt.getText().toString().equals("")){
            Toast.makeText(LeggTilStasjon.this, R.string.leggTil_navn_err, Toast.LENGTH_SHORT).show();
            return;
        }
        if(prisFelt.getText().toString().equals("")){
            Toast.makeText(LeggTilStasjon.this, R.string.leggTil_pris_err, Toast.LENGTH_SHORT).show();
            return;
        }

        navn = navnFelt.getText().toString();
        lat = Double.parseDouble(latFelt.getText().toString());
        lng = Double.parseDouble(lngFelt.getText().toString());
        pris = Double.parseDouble(prisFelt.getText().toString());
        if(pris <= 0.0){
            Toast.makeText(LeggTilStasjon.this, R.string.leggTil_pris_err_lavt, Toast.LENGTH_SHORT).show();
        }

        // Setter inn stasjonslogo hvis enten Statoil, Shell eller Esso er nevnt i navn
        if(navn.contains("Statoil") || navn.contains("statoil")){
            Log.v("LeggTilStasjon()", "Statoil i navnet");
            s = new Stasjon(navn,lat,lng,pris,R.drawable.ic_statoil);
        } else if(navn.contains("Shell") || navn.contains("shell")){
            Log.v("LeggTilStasjon()", "Shell i navnet");
            s = new Stasjon(navn,lat,lng,pris,R.drawable.ic_shell);
        } else if(navn.contains("Esso") || navn.contains("esso")) {
            Log.v("LeggTilStasjon()", "Esso i navnet");
            s = new Stasjon(navn,lat,lng,pris,R.drawable.ic_esso);
        } else {
            Log.v("LeggTilStasjon()", "Default ikon");
            s = new Stasjon(navn,lat,lng,pris,R.drawable.ic_cheapfuel_meny_ikon);
        }

        List<Stasjon> finnes = db.finnAlleStasjoner();

        for(Stasjon s : finnes){
            if(s.getNavn().equals(navn)){
                Toast.makeText(getBaseContext(),s.getNavn() + R.string.leggTil_stasj_finnes,Toast.LENGTH_SHORT).show();
                return;
            }
        }
        db.leggTilStasjon(s,false);
        finish();
    }
}