package s172589.cheapfuel;

import java.io.Serializable;

/**
 * Created by Halvor on 14.11.2015.
 */
public class Stasjon implements Serializable, Comparable<Stasjon>{
    String navn;
    Double latitude;
    Double longitude;
    Double pris;
    int icon;

    public Stasjon(String n, Double lt, Double lg, Double p, int ic) {
        navn = n;
        latitude = lt;
        longitude = lg;
        pris = p;
        icon = ic;
    }

    public Stasjon(){}

    // Get- og set-metoder
    public Double getLat() {
        return latitude;
    }

    public void setLat(Double lat) {
        this.latitude = lat;
    }

    public double getPris() {
        return pris;
    }

    public void setPris(double pris) {
        this.pris = pris;
    }

    public Double getLng() {
        return longitude;
    }

    public void setLng(Double lng) {
        this.longitude = lng;
    }

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public int getIcon() { return icon; }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public int compareTo(Stasjon annen) {
        return pris.compareTo(annen.pris);
    }
}
