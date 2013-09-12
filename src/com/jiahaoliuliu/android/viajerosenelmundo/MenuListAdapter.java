package com.jiahaoliuliu.android.viajerosenelmundo;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuListAdapter extends BaseAdapter {

	// Declare variables
	private Context context;
	private List<Viajero> viajeros;
	private LayoutInflater inflater;
	
	public MenuListAdapter(Context context, List<Viajero> viajeros) {
		this.context = context;
		this.viajeros = viajeros;
	}
	
	@Override
	public int getCount() {
		return viajeros.size();
	}

	@Override
	public Object getItem(int position) {
		return viajeros.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Declare Variables
		
		Viajero viajero = viajeros.get(position);
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View itemView = inflater.inflate(R.layout.drawer_list_item, parent, false);
		
		// Locate the TextViews
		TextView cityTV = (TextView)itemView.findViewById(R.id.city);
		TextView countryTV = (TextView)itemView.findViewById(R.id.country);
		TextView channelTV = (TextView)itemView.findViewById(R.id.channel);
		
		// Set the data
		cityTV.setText(viajero.getCity());
		countryTV.setText(viajero.getCountry());
		channelTV.setText(viajero.getChannel().toString());

		return itemView;
		
	}

}
