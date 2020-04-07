package s172589.cheapfuel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Halvor on 10.11.2015.
 */
public class DBHandler extends SQLiteOpenHelper {
    static String TABLE_STASJONER = "Stasjoner";
    static String KEY_ID = "ID";
    static String KEY_NAME = "Navn";
    static String KEY_LAT = "Latitude";
    static String KEY_LNG = "Longitude";
    static String KEY_PRIS = "Pris";
    static String KEY_ICON = "Icon";
    static int DATABASE_VERSION = 2;
    static String DATABASE_NAME= "Bensinstasjoner";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private NyStasjonEksternDB sEB = new NyStasjonEksternDB();
    private OppdaterPrisEksternDB oped = new OppdaterPrisEksternDB();
    private SlettStasjonEksternDB ssedb = new SlettStasjonEksternDB();

    @Override
    public void onCreate(SQLiteDatabase db) {
        String LAG_TABELL = "CREATE TABLE " + TABLE_STASJONER +"(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + KEY_NAME + " TEXT," + KEY_LAT + " TEXT," + KEY_LNG + " TEXT, " + KEY_PRIS + " TEXT, " + KEY_ICON + " INT)";
        Log.d("SQL", LAG_TABELL);
        db.execSQL(LAG_TABELL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STASJONER);
        onCreate(db);
    }

    public void leggTilStasjon(Stasjon s,boolean finnes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, s.getNavn());
        values.put(KEY_LAT, s.getLat());
        values.put(KEY_LNG, s.getLng());
        values.put(KEY_PRIS, s.getPris());
        values.put(KEY_ICON, s.getIcon());
        db.insert(TABLE_STASJONER, null, values);

        if(!finnes)
            sEB = new NyStasjonEksternDB();
            sEB.execute(values);

        db.close();
    }

    public boolean oppdaterPris(String stasjon, String pris, boolean først) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_PRIS, pris);

        if(først)
            oped.execute(values,stasjon);

        return db.update(TABLE_STASJONER,values,KEY_NAME + "='" + stasjon + "'",null) > 0;
    }

    public List<Stasjon> finnAlleStasjoner(){
        List<Stasjon> stasjonsListe = new ArrayList<>();
        String selectQuery = "SELECT * FROM " +	TABLE_STASJONER;
        SQLiteDatabase db =	this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if	(cursor.moveToFirst())	{
            do{
                Stasjon	stasjon = new Stasjon();
                stasjon.setNavn(cursor.getString(1));
                stasjon.setLat(cursor.getDouble(2));
                stasjon.setLng(cursor.getDouble(3));
                stasjon.setPris(cursor.getDouble(4));
                stasjon.setIcon(cursor.getInt(5));
                stasjonsListe.add(stasjon);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return stasjonsListe;
    }

    // Sletter stasjon ved navn
    public void slettStasjon(Stasjon s, boolean slett) {
        SQLiteDatabase db = this.getWritableDatabase();

        if(slett) {
            ssedb = new SlettStasjonEksternDB();
            ssedb.execute(s);
        }

        db.delete(TABLE_STASJONER, KEY_NAME + " =?", new String[]{String.valueOf(s.getNavn())});
        db.close();
    }


    public List<Stasjon> finnBilligste(){
        List<Stasjon> alleStasjoner = finnAlleStasjoner();
        List<Stasjon> billigste = new ArrayList<>();

        Collections.sort(alleStasjoner);

        if(alleStasjoner.size() > 0){
            billigste.add(alleStasjoner.get(0));
        } else {
            Stasjon s = new Stasjon("Ingen stasjoner å vise",0.0,0.0,0.0,0);
            billigste.add(s);
        }

        return billigste;
    }

    private class NyStasjonEksternDB extends AsyncTask<ContentValues,Void,String>{
        @Override
        protected String doInBackground(ContentValues... params) {

            ContentValues cv = params[0];
                Log.v("SKRIVTILEKSTERN", "ContentValues: " + cv.get(KEY_NAME));

                try {
                    URL url = new URL("http://student.cs.hioa.no/~s172589/cheapfuel_db_leggTilStasjon.php?");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    OutputStreamWriter skriver = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

                    skriver.write(hentStreng(cv));
                    skriver.flush();
                    skriver.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String s;
                    String utdata = "";
                    while((s = br.readLine()) != null) {
                        utdata = utdata + s;
                    }
                    Log.v("SkrivEksternDB", "Svar fra server: " + utdata);

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            return "Godt jobba";
        }

            public String hentStreng(ContentValues cv) {
                StringBuilder resultat = new StringBuilder();

                resultat.append("navn=");
                resultat.append(cv.getAsString(KEY_NAME).replace(" ","+"));
                resultat.append("&");
                resultat.append("latitude=");
                resultat.append(cv.get(KEY_LAT));
                resultat.append("&");
                resultat.append("longitude=");
                resultat.append(cv.get(KEY_LNG));
                resultat.append("&");
                resultat.append("pris=");
                resultat.append(cv.get(KEY_PRIS));
                resultat.append("&");
                resultat.append("ikon=");
                resultat.append(cv.getAsInteger(KEY_ICON));

                Log.v("hentStreng","RESULTAT " + resultat.toString());
                return resultat.toString();
            }
        }

    private class OppdaterPrisEksternDB extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            ContentValues cv = (ContentValues) params[0];
            String stasjonNavn = (String) params[1];

            Log.v("Oppdater", "ContentValues: " + cv.get(KEY_NAME));

            try {
                URL url = new URL("http://student.cs.hioa.no/~s172589/cheapfuel_db_oppdaterPris.php?");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStreamWriter skriver = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

                skriver.write(hentStreng(cv,stasjonNavn));
                skriver.flush();
                skriver.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String s;
                String utdata = "";
                while((s = br.readLine()) != null) {
                    utdata = utdata + s;
                }
                Log.v("SkrivEksternDB", "Svar fra server: " + utdata);

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Godt jobba";
        }

        public String hentStreng(ContentValues cv, String navn) {

            StringBuilder sb = new StringBuilder();
            sb.append("Navn=");
            sb.append(navn);
            sb.append("&");
            sb.append("Pris=");
            sb.append(cv.get(KEY_PRIS));

            Log.v("OPPDATERPRIS","RESULTAT SB " + sb.toString());

            return sb.toString();
        }
    }

    private class SlettStasjonEksternDB extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            Stasjon s = (Stasjon) params[0];

            Log.v("SlettEksternDB", "Stajson: " + s.getNavn());

            try {
                URL url = new URL("http://student.cs.hioa.no/~s172589/cheapfuel_db_slettStasjon.php?");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStreamWriter skriver = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

                skriver.write(hentStreng(s));
                skriver.flush();
                skriver.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String string;
                String utdata = "";
                while((string = br.readLine()) != null) {
                    utdata = utdata + string;
                }
                Log.v("SlettEksternDB", "Svar fra server: " + utdata);

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Godt jobba";
        }

        public String hentStreng(Stasjon s) {

            StringBuilder sb = new StringBuilder();
            sb.append("Navn=");
            sb.append(s.getNavn());
            Log.v("OPPDATERPRIS","RESULTAT SB " + sb.toString());

            return sb.toString();
        }
    }
}

