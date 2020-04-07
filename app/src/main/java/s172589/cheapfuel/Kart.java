package s172589.cheapfuel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Halvor on 10.11.2015.
 */
public class Kart extends FragmentActivity implements OnMapReadyCallback, Serializable {

    DBHandler db = new DBHandler(this);
    CameraUpdate cu;
    ImageButton tilbake;
    EditText prisFelt;
    String informasjon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kart);
        SupportMapFragment kartFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.kart);
        kartFragment.getMapAsync(this);


        tilbake = (ImageButton)findViewById(R.id.tilbake);

        tilbake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean info = sp.getBoolean(informasjon, false);

        Log.v("info", "" + info);
        if (!info) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(Kart.this);
            builder2.setTitle(getApplicationContext().getString(R.string.info_tittel));
            builder2.setMessage(getApplicationContext().getString(R.string.info_meld1) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.info_meld2) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.info_meld3) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.info_meld4) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.info_meld5));
            builder2.setCancelable(false);
            builder2.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sp.edit().putBoolean(informasjon, true).apply();
                    dialog.cancel();
                }
            });

            AlertDialog a = builder2.create();
            a.show();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        // Leser og legger inn alle stasjoner, legger ut pinpoints på kartet.

        map.getUiSettings().setRotateGesturesEnabled(false);

        final List<Stasjon> sList = db.finnAlleStasjoner();

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {

                AlertDialog.Builder builder2 = new AlertDialog.Builder(Kart.this);
                builder2.setTitle(R.string.endre_tittel);
                builder2.setMessage(getApplicationContext().getString(R.string.endre_meld));
                builder2.setCancelable(true);
                builder2.setNegativeButton(R.string.nei, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder2.setPositiveButton(R.string.ja, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Kart.this);

                        LayoutInflater linf = LayoutInflater.from(Kart.this);
                        View v = linf.inflate(R.layout.dialog_nypris, null);

                        builder.setTitle(R.string.endre_ny_pris);
                        builder.setView(v);
                        builder.setCancelable(true);
                        builder.setNegativeButton(R.string.nei, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setPositiveButton(R.string.ja, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Context c = getApplicationContext();
                                db = new DBHandler(c);
                                Dialog d = (Dialog) dialog;
                                prisFelt = (EditText) d.findViewById(R.id.endrePris);

                                if (prisFelt.getText().toString().equals("")) {
                                    Toast.makeText(Kart.this, R.string.endre_tomt_felt, Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }

                                db.oppdaterPris(marker.getTitle(), prisFelt.getText().toString(),true);
                                Toast.makeText(Kart.this, R.string.endre_suksess + marker.getTitle(), Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        });

                        AlertDialog a = builder.create();
                        a.show();

                    }
                });
                AlertDialog b = builder2.create();
                b.show();
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.v("Kart Longclick", "Koordinatene er " + latLng.latitude + " | " + latLng.longitude);

                final LatLng ltLg = latLng;

                AlertDialog.Builder builder = new AlertDialog.Builder(Kart.this);
                builder.setTitle(R.string.lagre_tittel);
                builder.setMessage(R.string.lagre_meld);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.nei, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(R.string.ja, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = ltLg.latitude;
                        double lng = ltLg.longitude;
                        Bundle b = new Bundle();

                        b.putDouble("Lat",lat);
                        b.putDouble("Lng",lng);

                        Context con = getApplicationContext();
                        Intent i = new Intent(con, LeggTilStasjon.class);
                        i.putExtras(b);
                        startActivity(i);
                        finish();
                    }
                });

                AlertDialog a = builder.create();
                a.show();
            }
        });

        // Utføres når kartets layout har blitt lastet
        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (sList.size() > 1) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (int i = 0; i < sList.size(); i++) {
                        Stasjon s = sList.get(i);

                        LatLng plass = new LatLng(s.getLat(), s.getLng());
                        map.addMarker(new MarkerOptions()
                                .position(plass)
                                .title(s.getNavn())
                                .snippet(R.string.stasj_snippet + String.format(" %.2f", s.getPris()) + " kr.")
                                .icon(BitmapDescriptorFactory.fromResource(s.getIcon())));

                        // Sender med stasjonens posisjon til LatLngBounds
                        builder.include(plass);
                    }

                    final LatLngBounds bounds = builder.build();
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, 70);
                    map.animateCamera(cu);

                } else if (sList.size() == 1) {
                    for (int i = 0; i < sList.size(); i++) {
                        Stasjon s = sList.get(i);

                        LatLng plass = new LatLng(s.getLat(), s.getLng());
                        map.addMarker(new MarkerOptions()
                                .position(plass)
                                .title(s.getNavn())
                                .snippet(getApplicationContext().getString(R.string.stasj_snippet) + String.format(" %.2f", s.getPris()) + " kr.")
                                .icon(BitmapDescriptorFactory.fromResource(s.getIcon())));

                        Log.v("En stasjon", "JEG HER ER JEG");
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(plass, 13));
                    }
                } else {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    // Setter default maps på Oslo hvis sList er lik 0
                    LatLng plass1 = new LatLng(59.8548935, 10.6518331);
                    LatLng plass2 = new LatLng(59.9567692, 10.83621);
                    builder.include(plass1);
                    builder.include(plass2);

                    final LatLngBounds bounds = builder.build();
                    cu = CameraUpdateFactory.newLatLngBounds(bounds, 70);
                    map.animateCamera(cu);
                }
            }
        });
    }
}