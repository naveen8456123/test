package s172589.cheapfuel;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Halvor on 18.11.2015.
 */
public class LocationHandler implements LocationListener {

    public LocationManager lm;
    public Location l;
    public String locProvider;

    public Double minPosLat;
    public Double minPosLng;
    public Context context;

    boolean gps_ok;

    public LocationHandler(){
    }

    public LocationHandler(Context c) {
        this.context = c;
    }

    public void settLokasjon() {
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locProvider = LocationManager.GPS_PROVIDER;

        try {
            setGps_ok(lm.isProviderEnabled(locProvider));
            l = lm.getLastKnownLocation(locProvider);

            Log.v("LocationHandler", "Inne i settLokasjon try/Catch, gps " + getGps_ok());
            oppdaterLok(l);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    public void oppdaterLok(Location l){

        if(l != null) {
            Log.v("OPPDATER", "l.getLatitude() " + l.getLatitude() + " | l.getLongitude()" + l.getLongitude());
            setMinPosLat(l.getLatitude());
            setMinPosLng(l.getLongitude());
            setLokasjon(l);
        } else
            Log.v("OPPDATER","Lokasjon var null");
    }

    public int hentDistanse(Double sLat,Double sLng,Double mLat,Double mLng) {
        int dist;
        float[] res = new float[1];
        Location.distanceBetween(sLat, sLng, mLat, mLng, res);

        float distFloat = res[0];
        dist = Math.round(distFloat) / 1000;

        return dist;
    }

    // Metoder for LocationListener
    @Override
    public void onLocationChanged(Location location) {
        oppdaterLok(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        oppdaterLok(l);
    }

    @Override
    public void onProviderEnabled(String provider) {
        oppdaterLok(l);
    }

    @Override
    public void onProviderDisabled(String provider) {
        oppdaterLok(l);
    }

    //Get- og set-metoder for brukerens posisjon
    public void setMinPosLat(double lat){
        minPosLat = lat;
    }

    public void setMinPosLng(double lng) {
        minPosLng = lng;
    }

    public Double getMinPosLat() {
        return minPosLat;
    }

    public Double getMinPosLng() {
        return minPosLng;
    }

    public boolean getGps_ok() {
        return gps_ok;
    }

    public void setGps_ok(boolean gps){
        gps_ok = gps;
    }

    public void setLokasjon(Location location) {
        l = location;
    }

    public Location getLokasjon() {
        return l;
    }
}