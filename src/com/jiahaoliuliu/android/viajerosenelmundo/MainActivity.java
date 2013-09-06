package com.jiahaoliuliu.android.viajerosenelmundo;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.view.GravityCompat;

public class MainActivity extends SherlockFragmentActivity {

	// Variables
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private MenuListAdapter mMenuAdapter;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	
	private GoogleMap googleMap;
	private Marker marker;
	private List<Viajero> viajeros;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_main);

		// Get the title
		mTitle = mDrawerTitle = getTitle();

		viajeros = new ArrayList<Viajero>();
		
		addData();

		// Get the map
		googleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// Draw all the points to the map
		for (Viajero viajeroTmp: viajeros) {
			googleMap.addMarker(
					new MarkerOptions()
						.position(viajeroTmp.getPosition())
						.title(viajeroTmp.getCity())
						.snippet(viajeroTmp.getUrl())
						);
		}

		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				// Open a web page
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(marker.getSnippet()));
				startActivity(intent);
			}
		});
		
		// Link the content
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		
		mDrawerList = (ListView)findViewById(R.id.listview_drawer);
		
		// Set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mMenuAdapter = new MenuListAdapter(MainActivity.this, viajeros);
		
		mDrawerList.setAdapter(mMenuAdapter);
		
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// Enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// ActionBarDrawerToggle ties together the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.drawer_open,
				R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}
			
			public void onDrawerOpened(View drawerView) {
				// Set the title on the action when drawer open
				getSupportActionBar().setTitle(mDrawerTitle);
				super.onDrawerOpened(drawerView);
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			if (!viajeros.isEmpty()) {
				selectItem(viajeros.get(0));
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(viajeros.get(position));
		}
	}
	
	private void selectItem(Viajero viajero) {
		
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viajero.getPosition(), 15));
	    // Zoom in, animating the camera.
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		// Get the title followed by the position
		setTitle(viajero.getCity());
		
		// Close drawer
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	private void addData() {
		// Add data
		Viajero viajero = new Viajero();
		
		viajero.setCity("Estocolmo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(59.32893, 18.06491));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--estocolmo/609798/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Berlín");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(52.519171, 13.406091));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-berlin/889792/");
		viajeros.add(viajero);
		
		viajero = new Viajero();
		viajero.setCity("Gabón");
		viajero.setCountry("Gabón");
		viajero.setPosition(new LatLng(-0.803689, 11.609444));
		viajero.setUrl("http://www.rtve.es/m/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-gabon/1927330/?media=tve");
		viajeros.add(viajero);
		
	}
}
