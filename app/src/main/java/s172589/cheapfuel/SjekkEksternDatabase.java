package s172589.cheapfuel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Halvor on 23.11.2015.
 */
public class SjekkEksternDatabase extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Context c = getApplicationContext();
        SjekkEksternDatabaseAsync sedba = new SjekkEksternDatabaseAsync(c);
        sedba.execute(new String[] {"http://student.cs.hioa.no/~s172589/cheapfuel_db_finnAlle.php"});
        Toast.makeText(this.getApplicationContext(),"SjekkEksternDatabase.java",Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }
    private class SjekkEksternDatabaseAsync extends AsyncTask<String, Void, String>{

        Context context;
        DBHandler db;
        public SjekkEksternDatabaseAsync(Context c){
            this.context = c.getApplicationContext();
            db = new DBHandler(context);
        }

        @Override
        protected String doInBackground(String... urls) {

            String s = "";
            String utdata = "";

            for(String url : urls) {
                try{
                    URL urlen = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection) urlen.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept","application/json");

                    if(conn.getResponseCode() != 200) {
                        Toast.makeText(context.getApplicationContext(),R.string.sjekkDB_koble_til,Toast.LENGTH_SHORT).show();
                        throw new RuntimeException("Failed! HTTP errorcode: " + conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    Log.v("SJEKKEKSTERNDB.java", "Kommer fra server: ");

                    while((s = br.readLine()) != null) {
                        utdata = utdata + s;
                    }

                    conn.disconnect();
                    return utdata;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return utdata;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.v("SJEKKEKSTERNDB", "onPostExecute " + s);
            String melding= "";

            if(s.contains("null")){
                Toast.makeText(context, R.string.sjekkDB_ingen_stasjoner, Toast.LENGTH_SHORT).show();
                return;
            }

            Stasjon stasjon;
            List<Stasjon> dbStasjonListe = db.finnAlleStasjoner();
            List<Stasjon> jsonStasjonListe = new ArrayList<>();
            List<Stasjon> stasjonLagtTilEksternt = new ArrayList<>(); // Lagt til stasjon eksternt
            List<Stasjon> stasjonSlettetEksternt = new ArrayList<>(); // Slettet stasjon eksternt
            List<Stasjon> stasjonDyrere = new ArrayList<>(); // Pris endret opp
            List<Stasjon> stasjonBilligere = new ArrayList<>(); // Pris endret ned

            String navn;
            Double lat,lng,pris;
            int ikon;

            JSONObject jsonStasjon;
            try {
                JSONArray jsonArray = new JSONArray(s);
                for(int i = 0; i < jsonArray.length(); i++){
                    Log.v("SJEKKEKSTERNDB json", "Jeg løper igjennom " + i + " ganger");
                    jsonStasjon = jsonArray.getJSONObject(i);
                    Log.v("SJEKKEKSTERNDB jsonObje", "Navn er: " + jsonStasjon.optString("Navn"));

                    navn = jsonStasjon.optString("Navn");
                    lat = jsonStasjon.optDouble("Latitude");
                    lng = jsonStasjon.optDouble("Longitude");
                    pris = jsonStasjon.optDouble("Pris");
                    ikon = jsonStasjon.optInt("Ikon");

                    stasjon = new Stasjon(navn,lat,lng,pris,ikon);
                    jsonStasjonListe.add(stasjon);
                }

                Log.v("JSONArrayList","Arraylist JSON" + jsonStasjonListe.toString());
                Log.v("JSONArrayList","Arraylist JSON størrelse " + jsonStasjonListe.size());
                Log.v("DbArrayList","Arraylist DB" + dbStasjonListe.toString());
                Log.v("DbArrayList","Arraylist DB størrelse " + dbStasjonListe.size());


                // Hvis det har blitt lagt til Stasjon i den eksterne Databasen
                if(jsonStasjonListe.size() > dbStasjonListe.size()){
                    melding += R.string.meld_leggTil_tittel+ "\r\n";
                    for(Stasjon s1 : jsonStasjonListe){
                        boolean funnet = false;
                        for(Stasjon s2 : dbStasjonListe){
                            if(s2.getNavn().equals(s1.getNavn())){
                                funnet = true;
                            }
                        }
                        if(!funnet){
                            Log.v("jsonFor", "Navn på stasjon " + s1.getNavn());
                            stasjonLagtTilEksternt.add(s1);

                            for(Stasjon stasjonFor : stasjonLagtTilEksternt){
                                melding += stasjonFor.getNavn() + R.string.meld_leggTil_pris + stasjonFor.getPris()+"\r\n";
                            }

                            db.leggTilStasjon(s1, false);

                        }
                        melding += "\r\n";
                    }

                    Log.v("forskjellArray", "Arraylist forskjell" + stasjonLagtTilEksternt.toString());

                    // Hvis det har blitt slettet en stasjon i den eksterne databasen, slett på den interne
                }
                if(jsonStasjonListe.size() < dbStasjonListe.size()) {
                    melding += R.string.meld_slett_tittel;
                    for(Stasjon s1 : dbStasjonListe){
                        boolean funnet = false;
                        for(Stasjon s2 : jsonStasjonListe){
                            if(s2.getNavn().equals(s1.getNavn())){
                                funnet = true;
                            }
                        }
                        if(!funnet){
                            Log.v("DBfor", "Fant denne: " + s1.getNavn());
                            stasjonSlettetEksternt.add(s1);

                            for(Stasjon slettet : stasjonSlettetEksternt){
                                melding += slettet.getNavn() + "\r\n";
                            }

                            db.slettStasjon(s1, false);
                        }
                        melding += "\r\n";
                    }
                    Log.v("forskjellArray2", "Arraylist forskjell2 " + stasjonSlettetEksternt.toString());
                }
                if(jsonStasjonListe.size() == dbStasjonListe.size()){
                    Log.v("forskjellArray3", "INGEN FORSKJELL PÅ STØRRELSE jsonarray og d'arcy");
                    Log.v("forskjellArray3", "Arraylist forskjell3 " + stasjonDyrere.toString());

                    Log.v("PRISENDRING", "Prisendring hvis Json er høyere enn Native DB");

                    // Hvis prisen er endret OPP på ekstern
                    for(Stasjon s1 : jsonStasjonListe){
                        boolean endring = false;
                        for(Stasjon s2 : dbStasjonListe){
                            if(s1.getNavn().equals(s2.getNavn())){
                                Log.v("PRISENDRING", "JsonArrayNavn: " + s1.getNavn() + " | DbArrayNavn: " + s2.getNavn());
                                Log.v("PRISENDRING", "JsonArrayPris: " + s1.getPris() + " | DbArrayPris: " + s2.getPris());
                                if(s1.getPris() > s2.getPris()){
                                    Log.v("PRISENDRING", "JsonArrayPris: " + s1.getPris() + " | DbArrayPris: " + s2.getPris());
                                    endring = true;
                                }
                            }
                        }
                        if(endring) {
                            Log.v("PRISENDRING", "Pris skal endres på: " + s1.getNavn());

                            stasjonDyrere.add(s1);
                            String nyPris = ""+s1.getPris();
                            db.oppdaterPris(s1.getNavn(), nyPris, false);
                        }
                    }

                    if(stasjonDyrere.size() >0 ){
                        melding = R.string.meld_dyrere_tittel + "\r\n\r\n";

                        for(Stasjon dyrere : stasjonDyrere){
                            melding += dyrere.getNavn() + "\r\n";
                        }
                        melding += "\r\n";
                    }

                    Log.v("PRISENDRING", "Prisendring hvis Json er lavere enn Native DB");
                    for(Stasjon s1 : jsonStasjonListe){
                        boolean endring = false;
                        for(Stasjon s2 : dbStasjonListe){
                            if(s1.getNavn().equals(s2.getNavn())){
                                Log.v("PRISENDRING", "JsonArrayNavn: " + s1.getNavn() + " | DbArrayNavn: " + s2.getNavn());
                                Log.v("PRISENDRING", "JsonArrayPris: " + s1.getPris() + " | DbArrayPris: " + s2.getPris());
                                if(s1.getPris() < s2.getPris()){
                                    Log.v("PRISENDRING", "JsonArrayPris: " + s1.getPris() + " | DbArrayPris: " + s2.getPris());
                                    endring = true;
                                }
                            }
                        }
                        if(endring) {
                            Log.v("PRISENDRING", "Pris skal endres på: " + s1.getNavn());
                            stasjonBilligere.add(s1);
                            String nyPris = ""+s1.getPris();
                            db.oppdaterPris(s1.getNavn(),nyPris,false);

                        }
                    }

                    if(stasjonBilligere.size() > 0){
                        melding = R.string.meld_billigere_pris + "\r\n\r\n";

                        for(Stasjon billigere : stasjonBilligere){
                            melding += billigere.getNavn() + "\r\n";
                        }
                        melding += "\r\n";
                    }
                }

                if(!melding.equals("")) {
                    // Notifikasjon til bruker
                    NotificationManager nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    Intent intent = new Intent(context, Forside.class);
                    PendingIntent pI = PendingIntent.getActivity(context, 0, intent, 0);

                    Notification.Builder build = new Notification.Builder(context);
                    build.setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(melding)
                            .setSmallIcon(R.drawable.ic_cheapfuel_meny_ikon)
                            .setContentIntent(pI).build();

                    Notification storTekst = new Notification.BigTextStyle(build)
                            .bigText(melding).build();

                    storTekst.flags |= Notification.FLAG_AUTO_CANCEL;
                    nM.notify(0, storTekst);
                }
            } catch (Exception e) {
                Toast.makeText(context,R.string.error_internett,Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("SjekkEktern", "onDestroy");
    }
}
