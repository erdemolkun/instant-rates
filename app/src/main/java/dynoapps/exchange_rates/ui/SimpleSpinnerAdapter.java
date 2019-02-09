package dynoapps.exchange_rates.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import dynoapps.exchange_rates.R;

/**
 * Created by erdemmac on 25/11/15.
 */
public class SimpleSpinnerAdapter<T> extends ArrayAdapter<T> {

    public SimpleSpinnerAdapter(Context context, List<T> objects) {
        super(context, R.layout.item_spinner_dropdown, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        View vTxt = convertView.findViewById(R.id.tv_item_spinner);
        if (vTxt != null) {
            TextView tv = ((TextView) vTxt);
            T item = getItem(position);
            tv.setText(item.toString());
//            if (position == 0) {
//                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
//            } else {
//                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
//                tv.setTextSize(16);
//            }
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_dropdown, parent, false);
        }
        View vTxt = convertView.findViewById(R.id.tv_item_spinner);
        if (vTxt != null) {
            TextView tv = ((TextView) vTxt);
            T item = getItem(position);
            tv.setText(item.toString());
//            if (position == 0) {
//                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
//            } else {
//                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
//            }
        }
        return convertView;
    }
}
