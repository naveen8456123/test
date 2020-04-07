package s172589.cheapfuel;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Halvor on 14.11.2015.
 */
public class ListViewAdapter extends ArrayAdapter<Stasjon> {
    int res;
    LocationHandler lh = new LocationHandler();
    Location l;
    int dist;

    public ListViewAdapter(Context context, int resource, List<Stasjon> stasjonsliste, Location location) {
        super(context, 0, stasjonsliste);
        res = resource;
        l = location;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Stasjon s = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(res, parent, false);
        }

        ImageView stasjonsBilde = (ImageView) convertView.findViewById(R.id.stasjonIcon);
        TextView navnFelt = (TextView) convertView.findViewById(R.id.navnTekst);
        TextView prisFelt = (TextView) convertView.findViewById(R.id.prisTekst);
        TextView distFelt = (TextView) convertView.findViewById(R.id.distTekst);

        if(l != null){
            dist = lh.hentDistanse(s.getLat(),s.getLng(),l.getLatitude(),l.getLongitude());
            if(dist > 2000){
                distFelt.setText("");
            } else {
                distFelt.setText(R.string.listV_dist + dist + " km");
            }
        } else {
            distFelt.setText(R.string.listV_dist_err);
        }
        navnFelt.setText(s.getNavn());
        stasjonsBilde.setImageResource(s.getIcon());
        if (s.getPris()>0) {
            prisFelt.setText(getContext().getString(R.string.listV_pris) + String.format(" %.2f", s.getPris()) + " kr");
        }
        else {
            prisFelt.setText(R.string.listV_pris_err);
        }

        return convertView;
    }
}
