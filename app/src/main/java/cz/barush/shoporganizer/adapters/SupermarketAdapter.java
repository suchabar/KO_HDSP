package cz.barush.shoporganizer.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.barush.shoporganizer.R;
import cz.barush.shoporganizer.persistance.entity.Supermarket;

/**
 * Created by Barbora on 09-Apr-17.
 */

public class SupermarketAdapter extends ArrayAdapter<Supermarket>
{
    Context context;
    int layoutResourceId;
    List<Supermarket> data = null;

    public SupermarketAdapter(Context context, int layoutResourceId, List<Supermarket> data)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        SupermarketHolder holder = null;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SupermarketHolder();
            holder.name = (TextView) row.findViewById(R.id.name);
            holder.distance = (TextView) row.findViewById(R.id.distance);

            row.setTag(holder);
        }
        else
        {
            holder = (SupermarketHolder) row.getTag();
        }
        Supermarket supermarket = data.get(position);
        holder.name.setText(supermarket.getName());
        holder.distance.setText(supermarket.getDistance() + " m");

        return row;
    }

    static class SupermarketHolder
    {
        TextView name;
        TextView distance;
    }
}
