package com.jiahaoliuliu.android.viajerosenelmundo;

import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.Callback;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.ListViajerosProvider;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.OnUrlReceivedListener;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero;

public class WorldMapFragment extends Fragment {

	private static final String LOG_TAG = WorldMapFragment.class.getSimpleName();
	
	private static final int ZOOM_ANIMATION_LEVEL = 5;
	private static final int MOST_ZOOM_LEVEL = 1;

	private static View view;
	private Context context;
	private Activity activity;
	private ListViajerosProvider listViajerosProvider;
	private OnUrlReceivedListener onUrlReceivedListener;
	private FragmentManager supportFragmentManager;

	private GoogleMap googleMap;
	// The callback to wait the google Map to be ready for the first time.
	private Callback googleMapCallback;
	private List<Viajero> viajeros;
	// The callback to wait the list of viajeros to be ready for the first time.
	private Callback listDataCallback;
    private HashMap<Marker, String> urlMaps = new HashMap<Marker, String>();
    private HashMap<LatLng, Marker> markerByLocation = new HashMap<LatLng, Marker>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Check the implementation
		try {
			listViajerosProvider = (ListViajerosProvider) activity;
		} catch (ClassCastException classCastException) {
			Log.e(LOG_TAG, "The attached class has not implemented ListViajerosProvider. ", classCastException);
			throw new ClassCastException(activity.toString() + " must implement ListViajerosProvider.");
		}
		
		try {
			onUrlReceivedListener = (OnUrlReceivedListener) activity;
		} catch (ClassCastException classCastException) {
			Log.e(LOG_TAG, "The attached class has not implemented OnUrlReceivedListener. ", classCastException);
			throw new ClassCastException(activity.toString() + " must implement OnUrlReceivedListener.");
		}

		this.context = activity;
		this.activity = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
	    if (view != null) {
	        ViewGroup parent = (ViewGroup) view.getParent();
	        if (parent != null)
	            parent.removeView(view);
	    }

	    try {
	        view = inflater.inflate(R.layout.world_map_fragment, container, false);
		    int isEnabled = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		    if (isEnabled != ConnectionResult.SUCCESS) {
		        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(isEnabled, activity, 0);
		        if (errorDialog != null) {
	        		errorDialog.show();
		        }
		    } else {
				supportFragmentManager = this.getActivity().getSupportFragmentManager();
				// Get the map
				googleMap = ((SupportMapFragment)supportFragmentManager
						.findFragmentById(R.id.map))
						.getMap();
				// When tilt, the view will be created again and the viajeros will be null
				viajeros = listViajerosProvider.getListViajeros();

				// Draw all the points to the map
				for (Viajero viajeroTmp: viajeros) {
					MarkerOptions markerOptions = new MarkerOptions()
						.position(viajeroTmp.getPosition())
						.snippet(getResources().getString(R.string.marker_instruction));

					String city = viajeroTmp.getCity();
					String country = viajeroTmp.getCountry();
					if (city.equalsIgnoreCase(country)) {
						markerOptions.title(country);
					} else {
						markerOptions.title(city + ", " + country);
					}

					Marker marker = googleMap.addMarker(markerOptions);
					
					// Add the data to the hash map
					urlMaps.put(marker, viajeroTmp.getUrl());
					markerByLocation.put(viajeroTmp.getPosition(), marker);
					// Set the icon
					switch (viajeroTmp.getChannel()) {
					case RTVE:
						marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
						break;
					case CUATRO:
						marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
						break;
					case TELEMADRID:
						// TODO: Set it white
						marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
						break;
					default:
						Log.e(LOG_TAG, "Channel not recognized " + viajeroTmp.getChannel().toString());
					}
				}

				// Show the market selected
				if (listDataCallback != null) {
					listDataCallback.done();
				}

				googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
					
					@Override
					public void onInfoWindowClick(Marker marker) {
						onUrlReceivedListener.onUrlReceived(urlMaps.get(marker));
					}
				});

				// Notify that the google maps is ready.
				if (googleMapCallback != null) {
					googleMapCallback.done();
				}
			}
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    	Log.w(LOG_TAG, "Error inflating the view." + e.getLocalizedMessage());
	    }
	    return view;
	}

	public void selectItem(final int position) {
		if (viajeros == null) {
			Log.w(LOG_TAG, "The list of viajero is not ready yet.");
			listDataCallback = new Callback() {
				
				@Override
				public void done() {
					selectItemWhenListDataIsReady(position);
					listDataCallback = null;
				}
			};
			return;
		}

		selectItemWhenListDataIsReady(position);
	}
	
	private void selectItemWhenListDataIsReady(final int position) {
		if (position < 0 || position > viajeros.size() -1 ) {
			Log.e(LOG_TAG, "The position selected is not correct: " + position);
			return;
		}

		if (googleMap == null) {
			Log.w(LOG_TAG, "The google map is not ready yet");
			googleMapCallback = new Callback() {
				
				@Override
				public void done() {
					selectItemWhenMapsIsReady(position);
					googleMapCallback = null;
				}
			};
			return;
		}

		selectItemWhenMapsIsReady(position);
	}
	
	private void selectItemWhenMapsIsReady(int position) {
		final Viajero viajero = viajeros.get(position);

		int startZoomLevel = viajero.getZoomLevel() - ZOOM_ANIMATION_LEVEL;
		if (startZoomLevel < MOST_ZOOM_LEVEL) {
			startZoomLevel = MOST_ZOOM_LEVEL;
		}

		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viajero.getPosition(), startZoomLevel));
	    // Zoom in, animating the camera.
		Log.v(LOG_TAG, "Going to the zoom level " + viajero.getZoomLevel());
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(viajero.getZoomLevel()), 2000, null);

		// Show the info windows
		if (markerByLocation.containsKey(viajero.getPosition())) {
			Marker marker = markerByLocation.get(viajero.getPosition());
			marker.showInfoWindow();
		}
	}
	
}
