package s172589.cheapfuel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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

/**
 * Created by Halvor on 16.11.2015.
 */
public class EnkelStasjonKart extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, Serializable {

    Location minLok;
    LocationHandler locHandler = new LocationHandler(this);
    Stasjon s;
    CameraUpdate cu;
    EditText prisFelt;
    ImageButton tilbake;
    String informasjon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kart);
        SupportMapFragment kartFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.kart);
        kartFragment.getMapAsync(this);

        prisFelt = (EditText)findViewById(R.id.endrePris);

        locHandler.settLokasjon();

        // Henter stasjon fra Forside.java
        Intent i = getIntent();
        s = (Stasjon)i.getSerializableExtra("Stasjon");
        if(s != null){
            Log.v("ENKELSTASJONKART", "Sendt med " + s.getNavn());
        }
        Log.v("ENKELSTASJONKART", "Inne i enkelKart");

        tilbake = (ImageButton)findViewById(R.id.tilbake);

        tilbake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(this);
        int info = sp1.getInt("info", 0);

        Log.v("info", "" + info);
        if (info == 0) {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(EnkelStasjonKart.this);
            builder2.setTitle(R.string.enkel_info_tittel);
            builder2.setMessage(getApplicationContext().getString(R.string.enkel_info_meld1) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.enkel_info_meld2) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.enkel_info_meld3) + "\r\n" +
                    getApplicationContext().getString(R.string.enkel_info_meld4) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.enkel_info_meld5) + "\r\n\r\n" +
                    getApplicationContext().getString(R.string.enkel_info_meld6));
            builder2.setCancelable(false);
            builder2.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sp1.edit().putInt("info", 1).apply();
                    dialog.cancel();
                }
            });

            AlertDialog a = builder2.create();
            a.show();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        map.setOnMarkerClickListener(this);
        map.getUiSettings().setRotateGesturesEnabled(false);

        LatLng plass = new LatLng(s.getLat(),s.getLng());
        final Marker stasjonMark = map.addMarker(new MarkerOptions()
                .position(plass)
                .title(s.getNavn())
                .snippet(String.format(getApplicationContext().getString(R.string.listV_pris) + " %.2f", s.getPris()) + " kr.")
                .icon(BitmapDescriptorFactory.fromResource(s.getIcon())));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(plass, 13));

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {

                if (marker.getTitle().equals(getApplicationContext().getString(R.string.enkel_dest_minPos))) {
                    Toast.makeText(EnkelStasjonKart.this, R.string.enkel_dest_err, Toast.LENGTH_SHORT).show();
                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(EnkelStasjonKart.this);

                    builder.setTitle(R.string.enkel_dest_tittel);
                    builder.setMessage(getApplicationContext().getString(R.string.enkel_dest_meld1) + "\r\n" + getApplicationContext().getString(R.string.enkel_dest_meld2) + "\r\n\r\n" + s.getNavn() + "?");
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
                            float[] res = new float[1];
                            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                            LatLng stasjonPos = new LatLng(s.getLat(), s.getLng());

                            if (locHandler.getMinPosLat() != null || locHandler.getMinPosLng() != null) {
                                Log.v("StasjonPos", "Lat: " + s.getLat() + " | Long: " + s.getLng());
                                Log.v("MinPos", "Lat: " + locHandler.getMinPosLat() + " | Long: " + locHandler.getMinPosLng());

                                minLok = new Location(getApplicationContext().getString(R.string.enkel_dest_minPos));
                                minLok.setLatitude(locHandler.getMinPosLat());
                                minLok.setLongitude(locHandler.getMinPosLng());

                                LatLng mPos = new LatLng(locHandler.getMinPosLat(), locHandler.getMinPosLng());

                                Location.distanceBetween(stasjonPos.latitude, stasjonPos.longitude, mPos.latitude, mPos.longitude, res);

                                float distFloat = res[0];
                                int dist = Math.round(distFloat) / 1000;

                                // Legger til markør på kartet
                                Marker minMark = map.addMarker(new MarkerOptions()
                                        .position(mPos)
                                        .title(getApplicationContext().getString(R.string.enkel_dest_minPos))
                                        .snippet("Ca " + dist + getApplicationContext().getString(R.string.enkel_dest_avstand)));
                                stasjonMark.hideInfoWindow();
                                minMark.showInfoWindow();

                                latLngBuilder.include(stasjonPos);
                                latLngBuilder.include(mPos);

                                LatLngBounds bounds = latLngBuilder.build();

                                cu = CameraUpdateFactory.newLatLngBounds(bounds, 160);
                                map.animateCamera(cu);

                                Toast.makeText(EnkelStasjonKart.this, R.string.enkel_dest_posOK, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.v("MinPos", "Ingen posisjon registrert");
                                Log.v("MinPos", " GPS " + locHandler.getGps_ok());

                                Toast.makeText(EnkelStasjonKart.this, R.string.enkel_dest_GPS, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    AlertDialog a = builder.create();
                    a.show();
                }
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Toast.makeText(EnkelStasjonKart.this, R.string.enkel_leggTil, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker m) {
        m.showInfoWindow();
        return true;
    }
}