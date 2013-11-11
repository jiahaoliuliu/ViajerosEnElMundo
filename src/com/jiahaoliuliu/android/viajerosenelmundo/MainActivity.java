package com.jiahaoliuliu.android.viajerosenelmundo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.android.gms.maps.model.LatLng;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.ListViajerosProvider;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.OnUrlReceivedListener;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.ProgressBarShowListener;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.onErrorReceivedListener;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero.ChannelId;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.GravityCompat;

public class MainActivity extends SherlockFragmentActivity implements
	ListView.OnScrollListener,
	onErrorReceivedListener, ListViajerosProvider, OnUrlReceivedListener, 
	ProgressBarShowListener {

	// Variables
	private static final String LOG_TAG = MainActivity.class.getSimpleName();

	private static final int MENU_BUTTON_RANDOM_ID = 10000;
	private static final int MENU_BUTTON_ABOUT_ME_ID = 10001;

	private Context context;
	private FragmentManager fragmentManager;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private MenuListAdapter mMenuAdapter;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	private WorldMapFragment worldMapFragment;
	private WebViewFragment webViewFragment;

	private List<Viajero> viajeros;

    private RemoveWindow mRemoveWindow = new RemoveWindow();
    Handler mHandler = new Handler();
    private boolean mHandlerPosted = false;
    private WindowManager mWindowManager;
    private boolean viewAddedToTheWindows = false;
    private TextView mDialogText;
    private boolean mShowing;
    private boolean mReady;
    private char mPrevLetter = Character.MIN_VALUE;
    private AlertDialog closeAppAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  

		setContentView(R.layout.drawer_main);

		context = this;

		// Get the title
		mTitle = mDrawerTitle = getTitle();

		viajeros = new ArrayList<Viajero>();

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        fragmentManager = getSupportFragmentManager();

		addData();

		printCities();

		// Link the content
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

		mDrawerList = (ListView)findViewById(R.id.listview_drawer);

		mMenuAdapter = new MenuListAdapter(MainActivity.this, viajeros);
		
		mDrawerList.setAdapter(mMenuAdapter);

		// If there is not drawer because it is a tablet
		if (mDrawerLayout != null){
			// Set a custom shadow that overlays the main content when the drawer opens
			mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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
					mDialogText.setVisibility(View.INVISIBLE);
					mShowing = false;
				}
				
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					// Set the title on the action when drawer open
					getSupportActionBar().setTitle(mDrawerTitle);
					createBigIndex();
				}
			};
			
			mDrawerLayout.setDrawerListener(mDrawerToggle);
			mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		} else {
			createBigIndex();
			mDrawerList.setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					selectItem(position, true);
				}
			});
		}

		mDrawerList.setOnScrollListener(this);

		// Creates the web view fragment
		webViewFragment = new WebViewFragment();

		// Attach the worldmap
		worldMapFragment = new WorldMapFragment();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.content_frame, worldMapFragment, WorldMapFragment.class.toString());
		ft.commit();
		
		// Go to the random city
		if (savedInstanceState == null) {
			if (!viajeros.isEmpty()) {
				int randomPosition = randomPositionGenerator();
				if (randomPosition > 0) {
					selectItem(randomPosition, false);
				} else {
					Log.e(LOG_TAG, "Error selecting random city. The position is " + randomPosition);
				}
			}
		}

        LayoutInflater inflate = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mDialogText = (TextView) inflate.inflate(R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        
	}

	//======================================= Menu ==============================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Random
    	// About
        menu.add(
        		Menu.NONE,
        		MENU_BUTTON_RANDOM_ID,
        		Menu.NONE,
        		"Aleatorio")
        	.setIcon(R.drawable.ic_random)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

    	// About
        menu.add(
        		Menu.NONE,
        		MENU_BUTTON_ABOUT_ME_ID,
        		Menu.NONE,
        		"Acerca de")
        	.setIcon(R.drawable.ic_about)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout != null) {
				if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
					mDrawerLayout.closeDrawer(mDrawerList);
				} else {
					mDrawerLayout.openDrawer(mDrawerList);
				}
			}
			return true;
		} else if (item.getItemId() == MENU_BUTTON_RANDOM_ID) {
			if (!viajeros.isEmpty()) {
				int randomPosition = randomPositionGenerator();
				if (randomPosition > 0 ) {
					selectItem(randomPosition, false);
				} else {
					Log.e(LOG_TAG, "Error getting the random city. The position is " + randomPosition);
				}
			}
			return true;
		} else if (item.getItemId() == MENU_BUTTON_ABOUT_ME_ID) {
			// Show alert message
			showDialog(0);
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(this)
        .setTitle("Acerce de")
        .setMessage("Esta aplicación pretende reunir los programas de la televisión sobre viajes y ubicarlas en la mapa para facilitar la navegación.\n\n" +
        		"Contribuye su mejora poniendolo en un comentario y vontandola.\n\n" +
        		"Para cualquier otras cuestiones, enviad un correo a jiahaoliuliu@gmail.com \n\n")
        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked OK so do some stuff */
            }
        })
        .create();
    }

	//======================================= Drawer ==============================================

	private class DrawerItemClickListener implements ListView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position, true);
		}
	}

	/**
	 * Select an item and show it on the map.
	 * @param position The position of the list
	 * @param fromList If the selection was done from the list or not.
	 *                 If yes, then nothing.
	 *                 If not, show it on the list
	 */
	private void selectItem (int position, boolean fromList) {
		if (position < 0 || position > viajeros.size() -1 ) {
			Log.e(LOG_TAG, "The position selected is not correct: " + position);
			return;
		}

		if (!fromList) {
			mDrawerList.setSelection(position);
		}

		if (mDrawerLayout != null) {
			// Close drawer
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		
		// Get the title followed by the position
		Viajero viajero = viajeros.get(position);
		setTitle(viajero.getCity());

		// Change to the right fragment if needed
		Fragment fragmentShown = fragmentManager.findFragmentById(R.id.content_frame);
		if (fragmentShown != null && !fragmentShown.getTag().equals(WorldMapFragment.class.toString())) {
			Log.v(LOG_TAG, "The content frame does not contains the world map fragment. Replacing it");
			Log.v(LOG_TAG, "Tag: " + fragmentShown.getTag());
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.replace(R.id.content_frame, worldMapFragment, WorldMapFragment.class.toString());
			ft.commit();
		}

		// Select the viajero in the map
		worldMapFragment.selectItem(position);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerLayout != null) {
			mDrawerToggle.syncState();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerLayout != null) {
			// Pass any configuration change to the drawer toggles
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

    private int randomPositionGenerator() {
    	if (viajeros.size() > 0) {
			return (int)(Math.random() * viajeros.size());
    	} else {
    		return -1;
    	}
    }
    
    // ======================================== Interfaces =======================================
	public void onErrorReceived(int errorCode, String errorMessage) {
		Log.e(LOG_TAG, "Error received with code: " + errorCode + ", and message: " + errorMessage);
		Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
	}

	public List<Viajero> getListViajeros() {
		return viajeros;
	}

	public void onUrlReceived(String url) {
		Log.v(LOG_TAG, "New url received: " + url);
		// Try to load the web page
		webViewFragment.setUrl(url);
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.content_frame, webViewFragment, WebViewFragment.class.toString());
		ft.commit();
	}

	public void showProgressBar() {
		setProgressBarIndeterminateVisibility(true);
	}

	public void hideProgressBar() {
		setProgressBarIndeterminateVisibility(false);
	}
	
	// ================================================= Others =====================================
    // Handle back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (closeAppAlertDialog == null) {
        		closeAppAlertDialog = createCloseAlertDialog();
        	}
        	
        	closeAppAlertDialog.show();
            return true; // To finish here and say the key has been handled
        }
        return super.onKeyDown(keyCode, event);
    }
	
    private AlertDialog createCloseAlertDialog() {
    	return new AlertDialog.Builder(
                context)
        .setTitle("Salir de la aplicación")
        .setMessage("¿Estás seguro de que quieres salir de la aplicación?")
        .setPositiveButton("Sí",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                    	finish();
                    }
                })
        .setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                    }
                })
        .create();
    }

	private void printCities() {
    	// The list of the cities
    	ArrayList<String> countriesList = new ArrayList<String>();
    	HashMap<String, ArrayList<String>> allCities = new HashMap<String, ArrayList<String>>();
    	// The list of the programs
    	HashMap<ChannelId, Integer> programs = new HashMap<ChannelId, Integer>();

    	for (Viajero viajero: viajeros) {
    		// Fill the cities
    		ArrayList<String> cities = allCities.get(viajero.getCountry());
    		if (cities == null) {
    			cities = new ArrayList<String>();
    			allCities.put(viajero.getCountry(), cities);
    			countriesList.add(viajero.getCountry());
    		}
    		
    		if (!cities.contains(viajero.getCity())) {
    			cities.add(viajero.getCity());
    		}
    		
    		// Fill the programs
    		Integer programCount = programs.get(viajero.getChannel());
    		if (programCount == null) {
    			programCount = 1;
    		}
    		
    		programCount++;
    		programs.put(viajero.getChannel(), programCount);
    	}
    	// Print the programs
    	Log.v(LOG_TAG, "Programas:");
    	Set<ChannelId> channels = programs.keySet();
    	for (ChannelId channelId: channels) {
    		
    		Integer programCount = programs.get(channelId);
    		switch (channelId) {
    			case CUATRO:
    				Log.v(LOG_TAG, "\tCuatro - Callejeros viajeros: " + programCount + " programas.");
    			break;
    			case RTVE:
    				Log.v(LOG_TAG, "\tRTVE - Españoles en el mundo: " + programCount + " programas.");
    			break;
    			case TELEMADRID:
    				Log.v(LOG_TAG, "\tTelemadrid - Madrileños por el mundo: " + programCount + " programas.");
    			break;
    		}
    	}

    	// Sort the countries 
    	Collections.sort(countriesList);
    	
    	// Sort the cities
    	Set<String> countries = allCities.keySet();
    	for (String country: countries) {
    		ArrayList<String> cities = allCities.get(country);
    		Collections.sort(cities);
    	}

    	// Print the cities
    	Log.v(LOG_TAG, "Ciudades: ");
    	for (String country: countriesList) {
    		ArrayList<String> cities = allCities.get(country);
    		if (cities.size() == 1 && cities.get(0).equals(country)) {
    			Log.v(LOG_TAG, "\t" + country);
    			continue;
    		} else {
        		Log.v(LOG_TAG, "\t" + country + ":");
        		StringBuilder sb = new StringBuilder("\t\t");
	    		for (String city : cities) {
	    			sb.append(" " + city + ",");
	    		}
	    		// Remove the last character
	    		sb.deleteCharAt(sb.length()-1);
	    		Log.v(LOG_TAG, sb.toString());
    		}
    	}
    }

	private void addData() {
		// Add data

		// Arcola
		Viajero viajero = new Viajero();
		viajero.setCity("Arcola");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(39.684755, -88.306437));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/temporadas/temporada-05/t05xp15-amish-un-viaje-en-tiempo/Callejeros-Viajeros-integrarse-Amish-Chicago_0_1648500447.html");
		viajeros.add(viajero);

		// Alaska
		viajero = new Viajero();
		viajero.setCity("Alaska");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(64.200841, -149.493673));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--alaska/581364/");
		viajeros.add(viajero);

		// Andamán
		viajero = new Viajero();
		viajero.setCity("Andamán");
		viajero.setCountry("Tailandia");
		viajero.setPosition(new LatLng(11.287888, 95.729982));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Callejeros_Viajeros-Tailandia-Koh_Lipe-Paraiso-Andaman_2_1634880081.html");
		viajeros.add(viajero);

		// Amsterdam
		viajero = new Viajero();
		viajero.setCity("Ámsterdam");
		viajero.setCountry("Países Bajos");
		viajero.setPosition(new LatLng(52.370216, 4.895168));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo--amsterdam/572762/");
		viajeros.add(viajero);
		
		// Amsterdam
		viajero = new Viajero();
		viajero.setCity("Ámsterdam");
		viajero.setCountry("Países Bajos");
		viajero.setPosition(new LatLng(52.470216, 4.995168));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-amsterdam");
		viajeros.add(viajero);
		
		// Antártida
		viajero = new Viajero();
		viajero.setCity("Antártida");
		viajero.setCountry("Antártida");
		viajero.setPosition(new LatLng(-82.862752, -135));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-antartida");
		viajeros.add(viajero);
		
		// Antillas Francesas
		viajero = new Viajero();
		viajero.setCity("Antillas Francesas");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(16.201516, -62.211739));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-antillas-francesas");
		viajeros.add(viajero);
		
		// Atenas
		viajero = new Viajero();
		viajero.setCity("Atenas");
		viajero.setCountry("Grecia");
		viajero.setPosition(new LatLng(37.983716, 23.72931));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo--atenas/604343/");
		viajeros.add(viajero);

		// Bali
		viajero = new Viajero();
		viajero.setCity("Bali");
		viajero.setCountry("Indonesia");
		viajero.setPosition(new LatLng(-8.409518, 115.188916));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-bali/630775/");
		viajeros.add(viajero);

		// Bangalore
		viajero = new Viajero();
		viajero.setCity("Bangalore");
		viajero.setCountry("India");
		viajero.setPosition(new LatLng(12.971599, 77.594563));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-bangalore");
		viajeros.add(viajero);

		// Bangkok
		viajero = new Viajero();
		viajero.setCity("Bangkok");
		viajero.setCountry("Tailandia");
		viajero.setPosition(new LatLng(13.727896, 100.524123));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--bangkok/703174/");
		viajeros.add(viajero);

		// Barbados
		viajero = new Viajero();
		viajero.setCity("Barbados");
		viajero.setCountry("Barbados");
		viajero.setPosition(new LatLng(13.193887, -59.543198));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/temporadas/temporada-05/paraisos/barbados/Barbados-el_lujo_del_Caribe-Paraiso-turismo-polo-Caribe-Atlantico-St_James_2_1639605043.html");
		viajeros.add(viajero);

		// Bahamas
		viajero = new Viajero();
		viajero.setCity("Bahamas");
		viajero.setCountry("Bahamas");
		viajero.setPosition(new LatLng(24.215341, -77.863199));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-bahamas");
		viajeros.add(viajero);

		// Beirut
		viajero = new Viajero();
		viajero.setCity("Beirut");
		viajero.setCountry("Líbano");
		viajero.setPosition(new LatLng(33.888629, 35.495479));
		viajero.setZoomLevel(11);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-beirut");
		viajeros.add(viajero);

		// Belfast
		viajero = new Viajero();
		viajero.setCity("Belfast");
		viajero.setCountry("Reinos Unidos");
		viajero.setPosition(new LatLng(54.597285, -5.93012));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130506/ciudad-marcada-historia/656946.shtml");
		viajeros.add(viajero);

		// Bélgica
		viajero = new Viajero();
		viajero.setCity("Bélgica");
		viajero.setCountry("Bélgica");
		viajero.setPosition(new LatLng(50.503887, 4.469936));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101118/espanoles-mundo-viaja-hasta-belgica-cuna-del-art-nouveau/372481.shtml");
		viajeros.add(viajero);

		// Berlín
		viajero = new Viajero();
		viajero.setCity("Berlín");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(52.519171, 13.406091));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-berlin/889792/");
		viajeros.add(viajero);

		// Bogotá
		viajero = new Viajero();
		viajero.setCity("Bogotá");
		viajero.setCountry("Colombia");
		viajero.setPosition(new LatLng(4.598056, -74.075833));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--bogota/721731/");
		viajeros.add(viajero);

		// Bolonia
		viajero = new Viajero();
		viajero.setCity("Bolonia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(44.494887, 11.342616));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-bolonia");
		viajeros.add(viajero);

		// Bombay
		viajero = new Viajero();
		viajero.setCity("Bombay");
		viajero.setCountry("India");
		viajero.setPosition(new LatLng(19.075984, 72.877656));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-bombay/775637/");
		viajeros.add(viajero);

		// Boston
		viajero = new Viajero();
		viajero.setCity("Boston");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(42.358431, -71.059773));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111209/boston-cuna-eeuu/480914.shtml");
		viajeros.add(viajero);

		// Brasil
		viajero = new Viajero();
		viajero.setCity("Brasil");
		viajero.setCountry("Brasil");
		viajero.setPosition(new LatLng(-14.235004, -51.92528));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/Brasil-aire-puro-carta_2_1623330205.html");
		viajeros.add(viajero);

		// Bretaña
		viajero = new Viajero();
		viajero.setCity("Bretaña");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(48.202047, -2.932644));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-bretana/1529562/");
		viajeros.add(viajero);

		// Budapest
		viajero = new Viajero();
		viajero.setCity("Budapest");
		viajero.setCountry("Hungría");
		viajero.setPosition(new LatLng(47.497912, 19.040235));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101129/377030.shtml");
		viajeros.add(viajero);

		// Buenos Aires
		viajero = new Viajero();
		viajero.setCity("Buenos Aires");
		viajero.setCountry("Argentina");
		viajero.setPosition(new LatLng(-34.603723, -58.381593));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100426/espanoles-mundo-visita-buenos-aires-ciudad-del-tango-mate/329079.shtml");
		viajeros.add(viajero);

		// Calgary
		viajero = new Viajero();
		viajero.setCity("Calgary");
		viajero.setCountry("Canadá");
		viajero.setPosition(new LatLng(51.045325, -114.058101));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-calgary/1559764/");
		viajeros.add(viajero);

		// Camboya
		viajero = new Viajero();
		viajero.setCity("Camboya");
		viajero.setCountry("Camboya");
		viajero.setPosition(new LatLng(12.565679, 104.990963));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111021/camboya-secreto-del-sudeste-asiatico/469823.shtml");
		viajeros.add(viajero);

		// Camerún
		viajero = new Viajero();
		viajero.setCity("Camerún");
		viajero.setCountry("Camerún");
		viajero.setPosition(new LatLng(4.923121, 12.310777));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--camerun-02-03-10/709492/");
		viajeros.add(viajero);

		// Capo Verde
		viajero = new Viajero();
		viajero.setCity("Capo Verde");
		viajero.setCountry("Capo Verde");
		viajero.setPosition(new LatLng(15.9342, -23.977607));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--cabo-verde/476672/");
		viajeros.add(viajero);

		// Caracas
		viajero = new Viajero();
		viajero.setCity("Caracas");
		viajero.setCountry("Venezuela");
		viajero.setPosition(new LatLng(10.491016, -66.902061));
		viajero.setZoomLevel(11);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-caracas");
		viajeros.add(viajero);

		// Caribe Holandés
		viajero = new Viajero();
		viajero.setCity("Caribe Holandés");
		viajero.setCountry("Caribe Holandés");
		viajero.setPosition(new LatLng(12.178361, -68.238534));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-caribe-holandes/1578852/");
		viajeros.add(viajero);

		// Carolina del Norte
		viajero = new Viajero();
		viajero.setCity("Carolina del Norte");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(35.759573, -79.0193));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-carolina-del-norte");
		viajeros.add(viajero);

		// Cartagena de Indias
		viajero = new Viajero();
		viajero.setCity("Cartagena de Indias");
		viajero.setCountry("Colombia");
		viajero.setPosition(new LatLng(10.41373, -75.533577));
		viajero.setZoomLevel(12);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120105/cartagena-indias-mas-bellas-ciudades-colombia/487343.shtml");
		viajeros.add(viajero);

		// Cartagena de Indias
		viajero = new Viajero();
		viajero.setCity("Cartagena de Indias");
		viajero.setCountry("Colombia");
		viajero.setPosition(new LatLng(10.31373, -75.433577));
			// The zoom level should be 12, but because the position has been
			// shifted, so the zoom level must be bigger.
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-cartagena-de-indias");
		viajeros.add(viajero);

		// Catar
		viajero = new Viajero();
		viajero.setCity("Catar");
		viajero.setCountry("Catar");
		viajero.setPosition(new LatLng(25.354826, 51.183884));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120224/catar-emirato-del-lujo-modernidad/500865.shtml");
		viajeros.add(viajero);

		// Chiang Mai
		viajero = new Viajero();
		viajero.setCity("Chiang Mai");
		viajero.setCountry("Tailandia");
		viajero.setPosition(new LatLng(18.783431, 98.991813));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110617/chiang-mai-esencia-tailandia-este-semana-espanoles-mundo/440970.shtml");
		viajeros.add(viajero);

		// Chicago
		viajero = new Viajero();
		viajero.setCity("Chicago");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(41.878114, -87.629798));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-chicago/544531/");
		viajeros.add(viajero);

		// Chile
		viajero = new Viajero();
		viajero.setCity("Chile");
		viajero.setCountry("Chile");
		viajero.setPosition(new LatLng(-35.675147, -71.542969));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110131/espanoles-mundo-aterriza-chile-pais-mas-largo-del-mundo/399839.shtml");
		viajeros.add(viajero);

		// Chile (Norte)
		viajero = new Viajero();
		viajero.setCity("Chile(Norte)");
		viajero.setCountry("Chile(Norte)");
		viajero.setPosition(new LatLng(-27.410785, -70.3125));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20121105/espanoles-mundo-visita-norte-chile/572934.shtml");
		viajeros.add(viajero);

		// Chipre
		viajero = new Viajero();
		viajero.setCity("Chipre");
		viajero.setCountry("Chipre");
		viajero.setPosition(new LatLng(34.8915, 33.124473));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--chipre/569001/");
		viajeros.add(viajero);

		// Chipre
		viajero = new Viajero();
		viajero.setCity("Chipre");
		viajero.setCountry("Chipre");
		viajero.setPosition(new LatLng(34.7915, 33.024473));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-chipre");
		viajeros.add(viajero);

		// Ciudad del cabo
		viajero = new Viajero();
		viajero.setCity("Ciudad del cabo");
		viajero.setCountry("Sudáfrica");
		viajero.setPosition(new LatLng(-33.924868, 18.424055));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setZoomLevel(9);
		viajero.setUrl("http://www.rtve.es/television/20111121/ciudad-del-cabo-ciudad-aun-mantiene-enormes-diferencias-sociales/477021.shtml");
		viajeros.add(viajero);

		// Colorado
		viajero = new Viajero();
		viajero.setCity("Colorado");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(39.550051, -105.782067));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110408/colorado-esqui-montanas-rocosas-rodeo-espanoles-mundo/423070.shtml");
		viajeros.add(viajero);

		// Copenhague
		viajero = new Viajero();
		viajero.setCity("Copenhague");
		viajero.setCountry("Dinamarca");
		viajero.setPosition(new LatLng(55.676097, 12.568337));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-copenhague-0");
		viajeros.add(viajero);

		// Corea del Sur
		viajero = new Viajero();
		viajero.setCity("Corea del Sur");
		viajero.setCountry("Corea del Sur");
		viajero.setPosition(new LatLng(36.381595, 128.176839));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-corea-del-sur");
		viajeros.add(viajero);

		// Costa Azul
		viajero = new Viajero();
		viajero.setCity("Costa Azul");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(43.695348, 7.255213));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo--costa-azul/539888/");
		viajeros.add(viajero);

		// Costa Rica
		viajero = new Viajero();
		viajero.setCity("Costa Rica");
		viajero.setCountry("Costa Rica");
		viajero.setPosition(new LatLng(9.748917, -83.753428));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110318/costa-rica-naciones-mas-felices-del-planeta-espanoles-mundo/417820.shtml");
		viajeros.add(viajero);

		// Cracovia
		viajero = new Viajero();
		viajero.setCity("Cracovia");
		viajero.setCountry("Polonia");
		viajero.setPosition(new LatLng(50.06465, 19.94498));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111011/espanoles-mundo-viaja-cracovia-alma-polonia/467534.shtml");
		viajeros.add(viajero);

		// Creta
		viajero = new Viajero();
		viajero.setCity("Creta");
		viajero.setCountry("Grecia");
		viajero.setPosition(new LatLng(35.240117, 24.809269));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-creta");
		viajeros.add(viajero);

		// Croacia
		viajero = new Viajero();
		viajero.setCity("Croacia");
		viajero.setCountry("Croacia");
		viajero.setPosition(new LatLng(45.1, 15.2));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110204/espanoles-mundo-viaja-croacia-pais-con-historia-naturaleza-playa/401174.shtml");
		viajeros.add(viajero);

		// Dallas
		viajero = new Viajero();
		viajero.setCity("Dallas");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(32.78014, -96.800451));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-dallas-avance/1562497/");
		viajeros.add(viajero);

		// Delhi
		viajero = new Viajero();
		viajero.setCity("Delhi");
		viajero.setCountry("India");
		viajero.setPosition(new LatLng(28.635308, 77.22496));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130517/ciudad-enamora/665391.shtml");
		viajeros.add(viajero);

		// Dinamarca
		viajero = new Viajero();
		viajero.setCity("Dinamarca");
		viajero.setCountry("Dinamarca");
		viajero.setPosition(new LatLng(56.26392, 9.501785));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--dinamarca/530386/");
		viajeros.add(viajero);

		// Dubái
		viajero = new Viajero();
		viajero.setCity("Dubái");
		viajero.setCountry("Emiratos Árabes Unidos");
		viajero.setPosition(new LatLng(25.271139, 55.307485));
		viajero.setZoomLevel(9);
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Callejeros_Viajeros-Dubai-lujo-caballos-arabes-Burj_Khalifa_2_1583430176.html");
		viajeros.add(viajero);

		// Dubái
		viajero = new Viajero();
		viajero.setCity("Dubái");
		viajero.setCountry("Emiratos Árabes Unidos");
		viajero.setPosition(new LatLng(25.371139, 55.407485));
		viajero.setZoomLevel(9);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110519/dubai-capital-mundial-del-lujo-exageracion-espanoles-mundo/433533.shtml");
		viajeros.add(viajero);

		// Dubái
		viajero = new Viajero();
		viajero.setCity("Dubái");
		viajero.setCountry("Emiratos Árabes Unidos");
		viajero.setPosition(new LatLng(25.171139, 55.207485));
		viajero.setZoomLevel(9);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-dubai");
		viajeros.add(viajero);

		// Dublín
		viajero = new Viajero();
		viajero.setCity("Dublín");
		viajero.setCountry("Irlanda");
		viajero.setPosition(new LatLng(53.349805, -6.26031));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101014/espanoles-mundo-visita-dublin-ciudad-cerveza-negra/361923.shtml");
		viajeros.add(viajero);

		// Durban
		viajero = new Viajero();
		viajero.setCity("Durban");
		viajero.setCountry("Sudáfrica");
		viajero.setPosition(new LatLng(-29.857876, 31.027581));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-durban/800993/");
		viajeros.add(viajero);

		// Edimburgo
		viajero = new Viajero();
		viajero.setCity("Edimburgo");
		viajero.setCountry("Escocia");
		viajero.setPosition(new LatLng(55.916588, -3.202392));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-edimburgo/600422/");
		viajeros.add(viajero);

		// Egipto
		viajero = new Viajero();
		viajero.setCity("Egipto");
		viajero.setCountry("Egipto");
		viajero.setPosition(new LatLng(26.820553, 30.802498));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-egipto");
		viajeros.add(viajero);

		// El Cairo
		viajero = new Viajero();
		viajero.setCity("El Cairo");
		viajero.setCountry("Egipto");
		viajero.setPosition(new LatLng(30.04442, 31.235712));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-cairo/808655/");
		viajeros.add(viajero);

		// Esmirna
		viajero = new Viajero();
		viajero.setCity("Esmirna");
		viajero.setCountry("Turquía");
		viajero.setPosition(new LatLng(38.41885, 27.12872));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/esmirna/");
		viajeros.add(viajero);

		// Estambul
		viajero = new Viajero();
		viajero.setCity("Estambul");
		viajero.setCountry("Turquía");
		viajero.setPosition(new LatLng(41.00527,28.97696));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-estambul/423017/");
		viajeros.add(viajero);

		// Estocolmo
		viajero = new Viajero();
		viajero.setCity("Estocolmo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(59.32893, 18.06491));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--estocolmo/609798/");
		viajeros.add(viajero);

		// Etiopía
		viajero = new Viajero();
		viajero.setCity("Etiopía");
		viajero.setCountry("Etiopía");
		viajero.setPosition(new LatLng(8.6182, 39.680667));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--etiopia/621017/");
		viajeros.add(viajero);

		// Filadelfia
		viajero = new Viajero();
		viajero.setCity("Filadelfia");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(39.952335, -75.163789));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120504/filadelfia-ciudad-abierta-tolerante-multicultural/521757.shtml");
		viajeros.add(viajero);

		// Filipinas
		viajero = new Viajero();
		viajero.setCity("Filipinas");
		viajero.setCountry("Filipinas");
		viajero.setPosition(new LatLng(12.300728, 123.070404));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110325/filipinas-pais-asiatico-aire-latino-espanoles-mundo/419624.shtml");
		viajeros.add(viajero);

		// Finlandia
		viajero = new Viajero();
		viajero.setCity("Finlandia");
		viajero.setCountry("Finlandia");
		viajero.setPosition(new LatLng(61.92411, 25.748151));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120727/finlandia-pais-mil-lagos/550822.shtml");
		viajeros.add(viajero);

		// Fiordos
		viajero = new Viajero();
		viajero.setCity("Fiordos");
		viajero.setCountry("Noruega");
		viajero.setPosition(new LatLng(63.434584, 10.398412));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110916/espectaculo-naturaleza-fiordos-noruegos-espanoles-mundo/461910.shtml");
		viajeros.add(viajero);

		// Flandes
		viajero = new Viajero();
		viajero.setCity("Flandes");
		viajero.setCountry("Bélgica");
		viajero.setPosition(new LatLng(51.095024, 4.447781));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-flandes");
		viajeros.add(viajero);

		// Florencia
		viajero = new Viajero();
		viajero.setCity("Florencia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(43.771033, 11.248001));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20121130/florencia-museo-aire-libre-espanoles-mundo/579881.shtml");
		viajeros.add(viajero);

		// Florencia
		viajero = new Viajero();
		viajero.setCity("Florencia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(43.871033, 11.348001));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Florencia-Callejeros_viajeros-la_mas_bella-Renacimiento_2_1661655016.html");
		viajeros.add(viajero);

		// Florida
		viajero = new Viajero();
		viajero.setCity("Florida");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(27.664827, -81.515754));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20121205/espanoles-visita-florida-playas-sol-permanentes/582314.shtml");
		viajeros.add(viajero);

		// Gabón
		viajero = new Viajero();
		viajero.setCity("Gabón");
		viajero.setCountry("Gabón");
		viajero.setPosition(new LatLng(-0.803689, 11.609444));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/gabon/");
		viajeros.add(viajero);

		// Gambia
		viajero = new Viajero();
		viajero.setCity("Gambia");
		viajero.setCountry("Gambia");
		viajero.setPosition(new LatLng(13.443182, -15.310139));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130115/gambia-pequena-jamaica/601480.shtml");
		viajeros.add(viajero);

		// Gambia
		viajero = new Viajero();
		viajero.setCity("Gambia");
		viajero.setCountry("Gambia");
		viajero.setPosition(new LatLng(13.543182, -15.410139));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-gambia");
		viajeros.add(viajero);

		// Ghana
		viajero = new Viajero();
		viajero.setCity("Ghana");
		viajero.setCountry("Ghana");
		viajero.setPosition(new LatLng(7.946527, -1.023194));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110420/ghana-gente-hospitalaria-naturaleza-salvaje-espanoles-mundo/426211.shtml");
		viajeros.add(viajero);

		// Ginebra
		viajero = new Viajero();
		viajero.setCity("Ginebra");
		viajero.setCountry("Suiza");
		viajero.setPosition(new LatLng(46.198392, 6.142296));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120525/ginebra-calidad-vida-crisol-nacionalidades/531564.shtml");
		viajeros.add(viajero);

		// Goa
		viajero = new Viajero();
		viajero.setCity("Goa");
		viajero.setCountry("India");
		viajero.setPosition(new LatLng(15.299327, 74.123996));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-goa-india/1526223/");
		viajeros.add(viajero);

		// Gotemburgo
		viajero = new Viajero();
		viajero.setCity("Gotemburgo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(57.70887, 11.97456));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-gotemburgo/1536918/");
		viajeros.add(viajero);

		// Gotemburgo
		viajero = new Viajero();
		viajero.setCity("Gotemburgo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(57.60887, 11.87456));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-gotemburgo");
		viajeros.add(viajero);

		// Groenlandia
		viajero = new Viajero();
		viajero.setCity("Groenlandia");
		viajero.setCountry("Dinamarca");
		viajero.setPosition(new LatLng(71.706936, -42.604303));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101021/espanoles-mundo-vista-uno-los-lugares-mas-reconditos-del-planeta-groenlandia/363880.shtml");
		viajeros.add(viajero);

		// Guatemala
		viajero = new Viajero();
		viajero.setCity("Guatemala");
		viajero.setCountry("Guatemala");
		viajero.setPosition(new LatLng(15.783471, -90.230759));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--guatemala/672608/");
		viajeros.add(viajero);

		// Guayana Francesa
		viajero = new Viajero();
		viajero.setCity("Guayana Francesa");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(3.933889, -53.125782));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120302/guayana-francesa-bosque-tropical-tecnologia-punta/507806.shtml");
		viajeros.add(viajero);

		// Guayaquil
		viajero = new Viajero();
		viajero.setCity("Guayaquil");
		viajero.setCountry("Ecuador");
		viajero.setPosition(new LatLng(-2.203816, -79.897453));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-guayaquil");
		viajeros.add(viajero);

		// Guayaquil
		viajero = new Viajero();
		viajero.setCity("Guayaquil");
		viajero.setCountry("Ecuador");
		viajero.setPosition(new LatLng(-2.303816, -79.997453));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110217/espanoles-mundo-viaja-hasta-guayaquil-corazon-economico-social-ecuador/408799.shtml");
		viajeros.add(viajero);

		// Guinea Ecuatorial
		viajero = new Viajero();
		viajero.setCity("Guinea Ecuatorial");
		viajero.setCountry("Guinea Ecuatorial");
		viajero.setPosition(new LatLng(1.650801, 10.267895));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-guinea-ecuatorial/503034/");
		viajeros.add(viajero);

		// Haití
		viajero = new Viajero();
		viajero.setCity("Haití");
		viajero.setCountry("Haití");
		viajero.setPosition(new LatLng(18.971187, -72.285215));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--haiti/450209/");
		viajeros.add(viajero);

		// Hanói
		viajero = new Viajero();
		viajero.setCity("Hanói");
		viajero.setCountry("Vietnam");
		viajero.setPosition(new LatLng(21.033333, 105.85));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130311/espanoles-hanoi/615584.shtml");
		viajeros.add(viajero);

		// Hawái
		viajero = new Viajero();
		viajero.setCity("Hawái");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(19.896766, -155.582782));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120216/hawai-paraiso-surfista/498461.shtml");
		viajeros.add(viajero);

		// Honduras
		viajero = new Viajero();
		viajero.setCity("Honduras");
		viajero.setCountry("Honduras");
		viajero.setPosition(new LatLng(15.199999, -86.241905));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130408/pueblos-ancestrales-caribe/633560.shtml");
		viajeros.add(viajero);

		// Hong Kong
		viajero = new Viajero();
		viajero.setCity("Hong Kong");
		viajero.setCountry("China");
		viajero.setPosition(new LatLng(22.396428, 114.109497));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120203/tradicion-modernidad-se-dan-mano-hong-kong/495200.shtml");
		viajeros.add(viajero);

		// Houston
		viajero = new Viajero();
		viajero.setCity("Houston");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(29.760193, -95.36939));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100506/espanoles-mundo-viaja-a-houston-plena-fiesta-vaquera-del-famoso-rodeo/330400.shtml");
		viajeros.add(viajero);

		// India
		viajero = new Viajero();
		viajero.setCity("India");
		viajero.setCountry("India");
		viajero.setPosition(new LatLng(20.593684, 78.96288));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-sudeste-de-india");
		viajeros.add(viajero);

		// Iquitos
		viajero = new Viajero();
		viajero.setCity("Iquitos");
		viajero.setCountry("Perú");
		viajero.setPosition(new LatLng(-3.75, -73.25));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130606/biodiversidad-leyendas/681442.shtml");
		viajeros.add(viajero);

		// Islandia
		viajero = new Viajero();
		viajero.setCity("Islandia");
		viajero.setCountry("Islandia");
		viajero.setPosition(new LatLng(64.963051, -19.020835));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--islandia/548756/");
		viajeros.add(viajero);

		// Islandia
		viajero = new Viajero();
		viajero.setCity("Islandia");
		viajero.setCountry("Islandia");
		viajero.setPosition(new LatLng(64.863051, -18.920835));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-islandia");
		viajeros.add(viajero);

		// Islas Caimán
		viajero = new Viajero();
		viajero.setCity("Islas Caimán");
		viajero.setCountry("Reinos Unidos");
		viajero.setPosition(new LatLng(19.3133, -81.2546));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--islas-caiman/683737/");
		viajeros.add(viajero);

		// Islas Griegas
		viajero = new Viajero();
		viajero.setCity("Islas Griegas");
		viajero.setCountry("Islas Griegas");
		viajero.setPosition(new LatLng(39.074208, 21.824312));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111216/navegando-islas-griegas/482447.shtml");
		viajeros.add(viajero);

		// Jalisco
		viajero = new Viajero();
		viajero.setCity("Jalisco");
		viajero.setCountry("México");
		viajero.setPosition(new LatLng(20.15402, -103.914399));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-jalisco/678126/");
		viajeros.add(viajero);

		// Jalisco
		viajero = new Viajero();
		viajero.setCity("Jalisco");
		viajero.setCountry("México");
		viajero.setPosition(new LatLng(20.25402, -104.014399));
		viajero.setZoomLevel(7);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-jalisco");
		viajeros.add(viajero);

		// Jamaica
		viajero = new Viajero();
		viajero.setCity("Jamaica");
		viajero.setCountry("Jamaica");
		viajero.setPosition(new LatLng(18.109581, -77.297508));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-jamaica");
		viajeros.add(viajero);

		// Jamaica
		viajero = new Viajero();
		viajero.setCity("Jamaica");
		viajero.setCountry("Jamaica");
		viajero.setPosition(new LatLng(18.209581, -77.397508));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101223/espanoles-mundo-aterriza-jamaica-una-selva-gigante-pleno-caribe/389392.shtml");
		viajeros.add(viajero);

		// Java
		viajero = new Viajero();
		viajero.setCity("Java");
		viajero.setCountry("Indonesia");
		viajero.setPosition(new LatLng(-7.150975, 110.140259));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-java-avance/1522150/");
		viajeros.add(viajero);

		// Jerusalén
		viajero = new Viajero();
		viajero.setCity("Jerusalén");
		viajero.setCountry("Israel");
		viajero.setPosition(new LatLng(31.768319, 35.21371));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-jerusalen/955754/");
		viajeros.add(viajero);

		// Jordania
		viajero = new Viajero();
		viajero.setCity("Jordania");
		viajero.setCountry("Jordania");
		viajero.setPosition(new LatLng(31.062659, 36.024095));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-jordania");
		viajeros.add(viajero);

		// Jordania
		viajero = new Viajero();
		viajero.setCity("Jordania");
		viajero.setCountry("Jordania");
		viajero.setPosition(new LatLng(31.162659, 36.124095));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--jordania/626040/");
		viajeros.add(viajero);

		// Kazajistán
		viajero = new Viajero();
		viajero.setCity("Kazajistán");
		viajero.setCountry("Kazajistán");
		viajero.setPosition(new LatLng(48.019573, 66.923684));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20121207/kazajistan-enorme-pais-corazon-asia-central/583320.shtml");
		viajeros.add(viajero);

		// Kenia
		viajero = new Viajero();
		viajero.setCity("Kenia");
		viajero.setCountry("Kenia");
		viajero.setPosition(new LatLng(-0.023559, 37.906193));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110608/espanoles-mundo-se-traslada-pais-pelicula-kenia/438188.shtml");
		viajeros.add(viajero);

		// Kyoto
		viajero = new Viajero();
		viajero.setCity("Kyoto");
		viajero.setCountry("Japón");
		viajero.setPosition(new LatLng(35.011636, 135.768029));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--kyoto/497809/");
		viajeros.add(viajero);

		// Kuala Lampur
		viajero = new Viajero();
		viajero.setCity("Kuala Lampur");
		viajero.setCountry("Malasia");
		viajero.setPosition(new LatLng(3.139003, 101.686855));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-kuala-lumpur/666885/");
		viajeros.add(viajero);

		// La Habana
		viajero = new Viajero();
		viajero.setCity("La Habana");
		viajero.setCountry("Cuba");
		viajero.setPosition(new LatLng(23.1168, -82.388557));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-habana/1046370/");
		viajeros.add(viajero);

		// Las Vegas
		viajero = new Viajero();
		viajero.setCity("Las Vegas");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(36.114646, -115.172816));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-vegas/926482/");
		viajeros.add(viajero);

		// Libia
		viajero = new Viajero();
		viajero.setCity("Libia");
		viajero.setCountry("Libia");
		viajero.setPosition(new LatLng(27.209615, 17.813945));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--libia-30-03-10/733696/");
		viajeros.add(viajero);

		// Lisboa
		viajero = new Viajero();
		viajero.setCity("Lisboa");
		viajero.setCountry("Portugal");
		viajero.setPosition(new LatLng(38.723733, -9.139547));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo/565339/");
		viajeros.add(viajero);

		// Lituania
		viajero = new Viajero();
		viajero.setCity("Lituania");
		viajero.setCountry("Lituania");
		viajero.setPosition(new LatLng(55.169438, 23.881275));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-lituania/483570/");
		viajeros.add(viajero);

		// Liverpool
		viajero = new Viajero();
		viajero.setCity("Liverpool");
		viajero.setCountry("Reinos Unidos");
		viajero.setPosition(new LatLng(53.408371, -2.991573));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120113/liverpool-ciudad-caracter/489224.shtml");
		viajeros.add(viajero);

		// Londres
		viajero = new Viajero();
		viajero.setCity("Londres");
		viajero.setCountry("Reinos Unidos");
		viajero.setPosition(new LatLng(51.511214, -0.119824));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101111/espanoes-mundo-viaja-londres-ciudad-cosmopolita/369956.shtml");
		viajeros.add(viajero);

		// Los Ángeles (Florida)
		viajero = new Viajero();
		viajero.setCity("Los Ángeles");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(34.052234, -118.243685));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-los-angeles");
		viajeros.add(viajero);

		// Los Ángeles (Florida)
		viajero = new Viajero();
		viajero.setCity("Los Ángeles");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(34.152234, -118.343685));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-angeles/1033993/");
		viajeros.add(viajero);

		// Los Cayos (Florida)
		viajero = new Viajero();
		viajero.setCity("Los Cayos");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(24.666944, -81.544167));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Los_cayos_de_Florida-Everglades-Caiman-Cayo_Huesos-Cayo_Marathon-Cayo_Largo_2_1642230078.html");
		viajeros.add(viajero);

		// Madagascar
		viajero = new Viajero();
		viajero.setCity("Madagascar");
		viajero.setCountry("Madagascar");
		viajero.setPosition(new LatLng(-18.766947, 46.869107));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/temporadas/temporada-05/paraisos/madagascar/Madagascar-Paraiso-Callejeros_Viajeros-Tsingy-Lemur_2_1631205097.html");
		viajeros.add(viajero);

		// Madagascar
		viajero = new Viajero();
		viajero.setCity("Madagascar");
		viajero.setCountry("Madagascar");
		viajero.setPosition(new LatLng(-18.866947, 46.969107));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-madagascar/900931/");
		viajeros.add(viajero);

		// Madeira
		viajero = new Viajero();
		viajero.setCity("Madeira");
		viajero.setCountry("Portugal");
		viajero.setPosition(new LatLng(32.760707, -16.959472));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120302/madeira-jardin-del-atlantico/503518.shtml");
		viajeros.add(viajero);

		// Maldivas
		viajero = new Viajero();
		viajero.setCity("Maldivas");
		viajero.setCountry("Maldivas");
		viajero.setPosition(new LatLng(-0.673106, 73.186768));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/temporadas/temporada-05/paraisos/maldivas/Maldivas-Paraiso-Callejeros_Viajeros-Coral-Atolon-Verano_2_1628580064.html");
		viajeros.add(viajero);

		// Maldivas
		viajero = new Viajero();
		viajero.setCity("Maldivas");
		viajero.setCountry("Maldivas");
		viajero.setPosition(new LatLng(-0.773106, 73.286768));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130527/meca-del-relax-lujo/673201.shtml");
		viajeros.add(viajero);

		// Malmo
		viajero = new Viajero();
		viajero.setCity("Malmo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(55.604981, 13.003822));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130513/ciudad-sostenible-sede-del-festival-eurovision-2013/662382.shtml");
		viajeros.add(viajero);

		// Malta
		viajero = new Viajero();
		viajero.setCity("Malta");
		viajero.setCountry("Malta");
		viajero.setPosition(new LatLng(35.854114, 14.48328));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110601/espanoles-mundo-visita-malta-paraiso-piedra-mar/435078.shtml");
		viajeros.add(viajero);

		// Manaos
		viajero = new Viajero();
		viajero.setCity("Manaos");
		viajero.setCountry("Brasil");
		viajero.setPosition(new LatLng(-3.106409, -60.02643));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111223/manaos-paris-del-amazonas/484395.shtml");
		viajeros.add(viajero);

		// Marrakech
		viajero = new Viajero();
		viajero.setCity("Marrakech");
		viajero.setCountry("Marruecos");
		viajero.setPosition(new LatLng(31.633333, -8));
		viajero.setZoomLevel(11);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110211/espanoles-mundo-viaja-marraketch-ciudad-zocos-artesanos-palacios/404499.shtml");
		viajeros.add(viajero);

		// Marruecos
		viajero = new Viajero();
		viajero.setCity("Marruecos");
		viajero.setCountry("Marruecos");
		viajero.setPosition(new LatLng(31.791702, -7.09262));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130125/rabat-casablanca-fez-se-abren-par-par/604850.shtml");
		viajeros.add(viajero);

		// Marruecos(Sur)
		viajero = new Viajero();
		viajero.setCity("Marruecos(Sur)");
		viajero.setCountry("Marruecos");
		viajero.setPosition(new LatLng(25.471065, -13.860198));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-sahara/443513/");
		viajeros.add(viajero);

		// Medellín
		viajero = new Viajero();
		viajero.setCity("Medellín");
		viajero.setCountry("Colombia");
		viajero.setPosition(new LatLng(6.235925, -75.575137));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120713/medellin-tierra-paisas/546139.shtml");
		viajeros.add(viajero);

		// Melbourne
		viajero = new Viajero();
		viajero.setCity("Meldourne");
		viajero.setCountry("Australia");
		viajero.setPosition(new LatLng(-37.814107, 144.96328));
		viajero.setZoomLevel(9);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-melbourne/1078863/");
		viajeros.add(viajero);

		// Mendoza
		viajero = new Viajero();
		viajero.setCity("Mendoza");
		viajero.setCountry("Argentina");
		viajero.setPosition(new LatLng(-32.890183, -68.84405));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130121/turismo-enologico-argentina/604120.shtml");
		viajeros.add(viajero);

		// México
		viajero = new Viajero();
		viajero.setCity("México");
		viajero.setCountry("México");
		viajero.setPosition(new LatLng(23.634501, -102.552784));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-caribe-mexicano");
		viajeros.add(viajero);

		// México
		viajero = new Viajero();
		viajero.setCity("México");
		viajero.setCountry("México");
		viajero.setPosition(new LatLng(23.734501, -102.652784));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20121122/mexico-lindo-querido-tambien-para-espanoles/576441.shtml");
		viajeros.add(viajero);

		// Miami
		viajero = new Viajero();
		viajero.setCity("Miami");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(25.788969, -80.226439));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-miami/1065853/");
		viajeros.add(viajero);

		// Milán
		viajero = new Viajero();
		viajero.setCity("Milán");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(45.458626, 9.181873));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130429/capital-mundial-del-diseno/652522.shtml");
		viajeros.add(viajero);

		// Minnesota
		viajero = new Viajero();
		viajero.setCity("Minnesota");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(46.729553, -94.6859));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-minnesota");
		viajeros.add(viajero);

		// Montevideo
		viajero = new Viajero();
		viajero.setCity("Montevideo");
		viajero.setCountry("Uruguay");
		viajero.setPosition(new LatLng(-34.884106, -56.167969));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-montevideo");
		viajeros.add(viajero);

		// Montreal
		viajero = new Viajero();
		viajero.setCity("Montreal");
		viajero.setCountry("Canadá");
		viajero.setPosition(new LatLng(45.50867, -73.553992));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo/595581/");
		viajeros.add(viajero);

		// Moscú
		viajero = new Viajero();
		viajero.setCity("Moscú");
		viajero.setCountry("Rusia");
		viajero.setPosition(new LatLng(55.755826, 37.6173));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100914/espanoles-mundo-viaja-moscu-ciudad-mas-poblada-europa/354053.shtml");
		viajeros.add(viajero);

		// Mozambique
		viajero = new Viajero();
		viajero.setCity("Mozambique");
		viajero.setCountry("Mozambique");
		viajero.setPosition(new LatLng(-18.665695, 35.529562));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120427/mozambique-tierra-buena-gente/519227.shtml");
		viajeros.add(viajero);

		// Múnich
		viajero = new Viajero();
		viajero.setCity("Múnich");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(48.13672, 11.576754));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110120/espanoles-mundo-aterriza-munich-capital-mundial-cerveza/396495.shtml");
		viajeros.add(viajero);

		// Namibia
		viajero = new Viajero();
		viajero.setCity("Namibia");
		viajero.setCountry("Namibia");
		viajero.setPosition(new LatLng(-22.95764, 18.49041));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--namibia/636123/");
		viajeros.add(viajero);

		// Nápoles
		viajero = new Viajero();
		viajero.setCity("Nápoles");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(40.851775, 14.268124));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110506/napoles-esencia-mediterranea-pies-del-vesubio/517911.shtml");
		viajeros.add(viajero);

		// Nepal
		viajero = new Viajero();
		viajero.setCity("Nepal");
		viajero.setCountry("Nepal");
		viajero.setPosition(new LatLng(28.394857, 84.124008));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100418/espanoles-mundo-viaja-a-nepal-pais-casi-legendario-situado-entre-tibet-india/328077.shtml");
		viajeros.add(viajero);

		// Nicaragua
		viajero = new Viajero();
		viajero.setCity("Nicaragua");
		viajero.setCountry("Nicaragua");
		viajero.setPosition(new LatLng(12.865416, -85.207229));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-nicaragua/1417683/");
		viajeros.add(viajero);

		// Nueva Orleans
		viajero = new Viajero();
		viajero.setCity("Nueva Orleans");
		viajero.setCountry("Nicaragua");
		viajero.setPosition(new LatLng(29.951066, -90.071532));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--nueva-orleans/456110/");
		viajeros.add(viajero);

		// Nueva York
		viajero = new Viajero();
		viajero.setCity("Nueva York");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(40.714353, -74.005973));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/temporadas/temporada-05/t05xp17-nueva-york-en-alturas/Nueva-York-alturas-on-line_2_1655880176.html");
		viajeros.add(viajero);

		// Nueva York
		viajero = new Viajero();
		viajero.setCity("Nueva York");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(40.814353, -74.105973));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--nueva-york/650404/");
		viajeros.add(viajero);

		// Nueva York(Navidad)
		viajero = new Viajero();
		viajero.setCity("Nueva York(Navidad)");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(40.614353, -73.905973));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/ny-navidad/");
		viajeros.add(viajero);

		// Nueva Zelanda
		viajero = new Viajero();
		viajero.setCity("Nueva Zelanda");
		viajero.setCountry("Nueva Zelanda");
		viajero.setPosition(new LatLng(-41.155988, 173.688667));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-nueva-zelanda-isla-norte");
		viajeros.add(viajero);

		// Nueva Zelanda
		viajero = new Viajero();
		viajero.setCity("Nueva Zelanda");
		viajero.setCountry("Nueva Zelanda");
		viajero.setPosition(new LatLng(-41.255988, 173.788667));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--nueva-zelanda/470312/");
		viajeros.add(viajero);

		// Oporto
		viajero = new Viajero();
		viajero.setCity("Oporto");
		viajero.setCountry("Portugal");
		viajero.setPosition(new LatLng(41.156689, -8.623925));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101216/espanoles-mundo-visita-oporto-ciudad-los-puentes-dobre-duero/387786.shtml");
		viajeros.add(viajero);

		// Oslo
		viajero = new Viajero();
		viajero.setCity("Oslo");
		viajero.setCountry("Noruega");
		viajero.setPosition(new LatLng(59.91304138427136, 10.765228271484375));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110105/oslo-ciudad-protege-espanoles-mundo/392759.shtml");
		viajeros.add(viajero);

		// Ottawa
		viajero = new Viajero();
		viajero.setCity("Ottawa");
		viajero.setCountry("Canadá");
		viajero.setPosition(new LatLng(45.42153, -75.697193));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130204/apariencia-fria-calida-acogida/606497.shtml");
		viajeros.add(viajero);

		// Panamá
		viajero = new Viajero();
		viajero.setCity("Panamá");
		viajero.setCountry("Panamá");
		viajero.setPosition(new LatLng(8.994269, -79.518792));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--panama/522013/");
		viajeros.add(viajero);

		// Paraguay
		viajero = new Viajero();
		viajero.setCity("Paraguay");
		viajero.setCountry("Paraguay");
		viajero.setPosition(new LatLng(-23.442503, -58.443832));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120210/uno-paises-mas-desconocidos/497238.shtml");
		viajeros.add(viajero);

		// París
		viajero = new Viajero();
		viajero.setCity("París");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(48.856614, 2.352222));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-paris/1165819/");
		viajeros.add(viajero);

		// Patagonia
		viajero = new Viajero();
		viajero.setCity("Patagonia");
		viajero.setCountry("Argentina y Chile");
		viajero.setPosition(new LatLng(-41.810147, -68.906269));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--patagonia/645394/");
		viajeros.add(viajero);

		// Pekín
		viajero = new Viajero();
		viajero.setCity("Pekín");
		viajero.setCountry("China");
		viajero.setPosition(new LatLng(39.90403, 116.407526));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Pekin-Callejeros_Viajeros-Muralla_China-Contaminacion_2_1661655076.html");
		viajeros.add(viajero);

		// Pekín
		viajero = new Viajero();
		viajero.setCity("Pekín");
		viajero.setCountry("China");
		viajero.setPosition(new LatLng(39.80403, 116.307526));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-pekin");
		viajeros.add(viajero);

		// Pekín
		viajero = new Viajero();
		viajero.setCity("Pekín");
		viajero.setCountry("China");
		viajero.setPosition(new LatLng(40.00403, 116.507526));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110303/espanoles-mundo-aterriza-pekin-modernidad-misterio-cultura-milenaria/413125.shtml");
		viajeros.add(viajero);

		// Perth
		viajero = new Viajero();
		viajero.setCity("Perth");
		viajero.setCountry("Australia");
		viajero.setPosition(new LatLng(-31.953004, 115.857469));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130416/vamos-antipodas/641101.shtml");
		viajeros.add(viajero);

		// Perú
		viajero = new Viajero();
		viajero.setCity("Perú");
		viajero.setCountry("Perú");
		viajero.setPosition(new LatLng(-9.189967, -75.015152));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-peru");
		viajeros.add(viajero);

		// Perú
		viajero = new Viajero();
		viajero.setCity("Perú");
		viajero.setCountry("Perú");
		viajero.setPosition(new LatLng(-9.289967, -75.115152));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--peru/507919/");
		viajeros.add(viajero);

		// Polinesia
		viajero = new Viajero();
		viajero.setCity("Polinesia");
		viajero.setCountry("Polinesia");
		viajero.setPosition(new LatLng(-17.663122, -149.46483));
		viajero.setZoomLevel(9);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--polinesia/640984/");
		viajeros.add(viajero);

		// Praga
		viajero = new Viajero();
		viajero.setCity("Praga");
		viajero.setCountry("República Checa");
		viajero.setPosition(new LatLng(50.075538, 14.4378));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100908/espanoles-mundo-viaja-madre-las-capitales-europa-praga/352809.shtml");
		viajeros.add(viajero);

		// Provenza
		viajero = new Viajero();
		viajero.setCity("Provenza");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(47.5471239, 3.954664));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/provenza/");
		viajeros.add(viajero);

		// Puerto Rico
		viajero = new Viajero();
		viajero.setCity("Puerto Rico");
		viajero.setCountry("Puerto Rico");
		viajero.setPosition(new LatLng(18.220833, -66.590149));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-puerto-rico/762470/");
		viajeros.add(viajero);

		// Quebec
		viajero = new Viajero();
		viajero.setCity("Quebec");
		viajero.setCountry("Canadá");
		viajero.setPosition(new LatLng(52.939916, -73.549136));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120302/quebec-ciudad-mas-antigua-norte-mexico/513591.shtml");
		viajeros.add(viajero);

		/* TODO:http://www.rtve.es/television/espanoles-en-el-mundo/republica-dominicana/
		// República Dominicana
		viajero = new Viajero();
		viajero.setCity("República Dominicana");
		viajero.setCountry("República Dominicana");
		viajero.setPosition(new LatLng(18.735693, -70.162651));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120302/quebec-ciudad-mas-antigua-norte-mexico/513591.shtml");
		viajeros.add(viajero);
		*/

		// Río de Janeiro
		viajero = new Viajero();
		viajero.setCity("Río de Janeiro");
		viajero.setCountry("Brasil");
		viajero.setPosition(new LatLng(-22.903539, -43.209587));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120511/rio-janeiro-ciudad-maravillosa/524650.shtml");
		viajeros.add(viajero);

		// Roma
		viajero = new Viajero();
		viajero.setCity("Roma");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(41.892916, 12.48252));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120608/roma-siete-caminos-para-llegar/534051.shtml");
		viajeros.add(viajero);

		// Roma
		viajero = new Viajero();
		viajero.setCity("Roma");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(41.792916, 12.38252));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-roma");
		viajeros.add(viajero);

		// Róterdam
		viajero = new Viajero();
		viajero.setCity("Róterdam");
		viajero.setCountry("Países Bajos");
		viajero.setPosition(new LatLng(51.924216, 4.481776));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130225/espanoles-roterdam/610357.shtml");
		viajeros.add(viajero);

		// Salvador de Bahía
		viajero = new Viajero();
		viajero.setCity("Salvador de Bahía");
		viajero.setCountry("Brasil");
		viajero.setPosition(new LatLng(-12.970382, -38.512382));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--salvador-bahia/715090/");
		viajeros.add(viajero);

		// Salzburgo
		viajero = new Viajero();
		viajero.setCity("Salzburgo");
		viajero.setCountry("Austria");
		viajero.setPosition(new LatLng(47.80949, 13.05501));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--salzburgo/585930/");
		viajeros.add(viajero);

		// San Diego
		viajero = new Viajero();
		viajero.setCity("San Diego");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(32.715329, -117.157255));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130211/ciudad-finest-estados-unidos/607708.shtml");
		viajeros.add(viajero);

		// San Diego
		viajero = new Viajero();
		viajero.setCity("San Diego");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(32.815329, -117.257255));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-san-diego");
		viajeros.add(viajero);

		// San Francisco
		viajero = new Viajero();
		viajero.setCity("San Francisco");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(37.77493, -122.419416));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Callejeros_viajeros-San_Francisco-la_mas_libre-hippie-Generacion_Beat-Silicon_Valley-Gay-Harvey_Milk_2_1594455102.html");
		viajeros.add(viajero);

		// San Francisco
		viajero = new Viajero();
		viajero.setCity("San Francisco");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(37.87493, -122.519416));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--san-francisco/696174/");
		viajeros.add(viajero);

		// San Petesburgo
		viajero = new Viajero();
		viajero.setCity("San Petesburgo");
		viajero.setCountry("Rusia");
		viajero.setPosition(new LatLng(59.93428, 30.335099));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--san-petersburgo/561800/");
		viajeros.add(viajero);

		// San Petesburgo
		viajero = new Viajero();
		viajero.setCity("San Petesburgo");
		viajero.setCountry("Rusia");
		viajero.setPosition(new LatLng(59.83428, 30.235099));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-san-petesburgo");
		viajeros.add(viajero);

		// San Tomé y Príncipe
		viajero = new Viajero();
		viajero.setCity("San Tomé y Príncipe");
		viajero.setCountry("San Tomé y Príncipe");
		viajero.setPosition(new LatLng(0.18636, 6.613081));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120629/santo-tome-principe-pequeno-pais/540539.shtml");
		viajeros.add(viajero);

		// Sao Paulo
		viajero = new Viajero();
		viajero.setCity("Sao Paulo");
		viajero.setCountry("Brasil");
		viajero.setPosition(new LatLng(-23.548943, -46.638818));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-sao-paulo");
		viajeros.add(viajero);

		// Sao Paulo
		viajero = new Viajero();
		viajero.setCity("Sao Paulo");
		viajero.setCountry("Brasil");
		viajero.setPosition(new LatLng(-23.648943, -46.738818));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-sao-paulo/1196467/");
		viajeros.add(viajero);

		// Seattle
		viajero = new Viajero();
		viajero.setCity("Seattle");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(47.60621, -122.332071));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-seattle/964256/");
		viajeros.add(viajero);

		// Selva Negra
		viajero = new Viajero();
		viajero.setCity("Selva Negra");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(48.325885, 8.191226));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/selvanegra/");
		viajeros.add(viajero);

		// Senegal
		viajero = new Viajero();
		viajero.setCity("Senegal");
		viajero.setCountry("Senegal");
		viajero.setPosition(new LatLng(14.497401, -14.452362));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120120/gran-pais-mil-matices/491079.shtml");
		viajeros.add(viajero);

		// Serbia
		viajero = new Viajero();
		viajero.setCity("Serbia");
		viajero.setCountry("Serbia");
		viajero.setPosition(new LatLng(44.016521, 21.005859));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--serbia/512643/");
		viajeros.add(viajero);

		// Seúl
		viajero = new Viajero();
		viajero.setCity("Seúl");
		viajero.setCountry("Corea del Sur");
		viajero.setPosition(new LatLng(37.566535, 126.977969));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-seul/591012/");
		viajeros.add(viajero);

		// Seúl
		viajero = new Viajero();
		viajero.setCity("Seúl");
		viajero.setCountry("Corea del Sur");
		viajero.setPosition(new LatLng(37.666535, 127.077969));
		viajero.setZoomLevel(10);
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-seul");
		viajeros.add(viajero);

		// Shanghai
		viajero = new Viajero();
		viajero.setCity("Shanghai");
		viajero.setCountry("China");
		viajero.setPosition(new LatLng(31.230393, 121.473704));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--shanghai/434709/");
		viajeros.add(viajero);

		// Sicilia
		viajero = new Viajero();
		viajero.setCity("Sicilia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(37.599994, 14.015356));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110506/sicilia-region-volcanes-espanole-mundo/430526.shtml");
		viajeros.add(viajero);

		// Singapur
		viajero = new Viajero();
		viajero.setCity("Singapur");
		viajero.setCountry("Singapur");
		viajero.setPosition(new LatLng(1.280095, 103.850949));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110729/singapur-isla-ciudad-estado-mas-prospera-del-mundo/450965.shtml");
		viajeros.add(viajero);

		// Siria
		viajero = new Viajero();
		viajero.setCity("Siria");
		viajero.setCountry("Siria");
		viajero.setPosition(new LatLng(34.802075, 38.996815));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110116/espanoles-mundo-llega-siria-pais-rico-historia-belleza/395444.shtml");
		viajeros.add(viajero);

		// Sri Lanka
		viajero = new Viajero();
		viajero.setCity("Sri Lanka");
		viajero.setCountry("Sri Lanka");
		viajero.setPosition(new LatLng(7.873054, 80.771797));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120615/sri-lanka-isla-mil-nombres/536182.shtml");
		viajeros.add(viajero);

		// Suiza
		viajero = new Viajero();
		viajero.setCity("Suiza");
		viajero.setCountry("Suiza");
		viajero.setPosition(new LatLng(46.818188, 8.227512));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20101028/espoles-mundo-viaja-suiza-pais-neutral-por-excelencia/365656.shtml");
		viajeros.add(viajero);

		// Sydney
		viajero = new Viajero();
		viajero.setCity("Sydney");
		viajero.setCountry("Australia");
		viajero.setPosition(new LatLng(-33.867487, 151.20699));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--sidney-23-03-10/727359/");
		viajeros.add(viajero);

		// Tanzania
		viajero = new Viajero();
		viajero.setCity("Tanzania");
		viajero.setCountry("Tanzania");
		viajero.setPosition(new LatLng(-6.369028, 34.888822));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/temporadas/temporada-05/t05xp14-una-aventura-en-tanzania/Tanzania-Arusha-Safari-Seregueti-Ngorongoro-Sabana-Callejeros_Viajeros-Masai_2_1646955045.html");
		viajeros.add(viajero);

		// Tanzania
		viajero = new Viajero();
		viajero.setCity("Tanzania");
		viajero.setCountry("Tanzania");
		viajero.setPosition(new LatLng(-6.469028, 34.988822));
		viajero.setZoomLevel(5);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--tanzania/534692/");
		viajeros.add(viajero);

		// Tánger
		viajero = new Viajero();
		viajero.setCity("Tánger");
		viajero.setCountry("Marruecos");
		viajero.setPosition(new LatLng(35.766667, -5.8));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-tanger");
		viajeros.add(viajero);

		// Taiwán
		viajero = new Viajero();
		viajero.setCity("Taiwán");
		viajero.setCountry("Taiwán");
		viajero.setPosition(new LatLng(23.69781, 120.960515));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-taiwan/1536144/");
		viajeros.add(viajero);

		// Tirol
		viajero = new Viajero();
		viajero.setCity("Tirol");
		viajero.setCountry("Austria");
		viajero.setPosition(new LatLng(47.253741, 11.601487));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20110915/vamos-pais-heidi-visitamos-tirol/461698.shtml");
		viajeros.add(viajero);

		// Tokio
		viajero = new Viajero();
		viajero.setCity("Tokio");
		viajero.setCountry("Japón");
		viajero.setPosition(new LatLng(35.692437, 139.703522));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/programas/asia/Callejeros-Viajeros-T05xP10-Tokio-poblada_2_1586580079.html");
		viajeros.add(viajero);

		// Tokio
		viajero = new Viajero();
		viajero.setCity("Tokio");
		viajero.setCountry("Japón");
		viajero.setPosition(new LatLng(35.702437, 139.803522));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100527/espanoles-mundo-viaja-a-tokio-capital-del-sol-naciente/333147.shtml");
		viajeros.add(viajero);

		// Toronto
		viajero = new Viajero();
		viajero.setCity("Toronto");
		viajero.setCountry("Canadá");
		viajero.setPosition(new LatLng(43.653226, -79.383184));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--toronto/490709/");
		viajeros.add(viajero);

		// Transilvania
		viajero = new Viajero();
		viajero.setCity("Transilvania");
		viajero.setCountry("Rumania");
		viajero.setPosition(new LatLng(46.756337, 23.410231));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111028/bienvenidos-transilvania-corazon-rumania/471639.shtml");
		viajeros.add(viajero);

		// Trinidad y Tobago
		viajero = new Viajero();
		viajero.setCity("Trinidad y Tobago");
		viajero.setCountry("Trinidad y Tobago");
		viajero.setPosition(new LatLng(10.691803, -61.222503));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-trinidad-tobago/738247/");
		viajeros.add(viajero);

		// Túnez
		viajero = new Viajero();
		viajero.setCity("Túnez");
		viajero.setCountry("Túnez");
		viajero.setPosition(new LatLng(33.886917, 9.537499));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120622/tunez-joya-africa/539082.shtml");
		viajeros.add(viajero);

		// Turín
		viajero = new Viajero();
		viajero.setCity("Turín");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(45.063299, 7.669289));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20100627/espanoles-mundo-viaja-a-turin-grandes-desconocidas-italia/337370.shtml");
		viajeros.add(viajero);

		// Ucrania
		viajero = new Viajero();
		viajero.setCity("Ucrania");
		viajero.setCountry("Ucrania");
		viajero.setPosition(new LatLng(48.379433, 31.16558));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111118/segundo-pais-mas-grande-europa-sigue-siendo-tierra-desconocida/476327.shtml");
		viajeros.add(viajero);

		// Uganda
		viajero = new Viajero();
		viajero.setCity("Uganda");
		viajero.setCountry("Uganda");
		viajero.setPosition(new LatLng(1.373333, 32.290275));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-uganda/744211/");
		viajeros.add(viajero);

		// Uruguay
		viajero = new Viajero();
		viajero.setCity("Uruguay");
		viajero.setCountry("Uruguay");
		viajero.setPosition(new LatLng(-32.522779, -55.765835));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-uruguay/1090678/");
		viajeros.add(viajero);

		// Utah
		viajero = new Viajero();
		viajero.setCity("Utah");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(39.32098, -111.093731));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20130219/mormones-republicanos-mejor-cine-independiente/609216.shtml");
		viajeros.add(viajero);

		// Vancouver
		viajero = new Viajero();
		viajero.setCity("Vancouver");
		viajero.setCountry("Canadá");
		viajero.setPosition(new LatLng(49.261226, -123.113927));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-vancouver");
		viajeros.add(viajero);

		// Valle de la muerte
		viajero = new Viajero();
		viajero.setCity("Valle de la muerte");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(36.505389, -117.079408));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/ultimo-programa/Valle-muerte-T05xP16-on-line_2_1652205148.html");
		viajeros.add(viajero);

		// Venecia
		viajero = new Viajero();
		viajero.setCity("Venecia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(45.440847, 12.315515));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120126/lugar-enamora-inspira-cuenta/492866.shtml");
		viajeros.add(viajero);

		// Venezuela
		viajero = new Viajero();
		viajero.setCity("Venezuela");
		viajero.setCountry("Venezuela");
		viajero.setPosition(new LatLng(6.42375, -66.58973));
		viajero.setChannel(ChannelId.CUATRO);
		viajero.setUrl("http://www.cuatro.com/callejeros-viajeros/programas/america/Callejeros-Viajeros-T05xP11-Venezuela-guapos_2_1590255127.html");
		viajeros.add(viajero);

		// Viena
		viajero = new Viajero();
		viajero.setCity("Viena");
		viajero.setCountry("Austria");
		viajero.setPosition(new LatLng(48.208174, 16.373819));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20111007/viena-mas-clasica-capitales-europeas/466703.shtml");
		viajeros.add(viajero);

		// Vietnam
		viajero = new Viajero();
		viajero.setCity("Vietnam");
		viajero.setCountry("Vietnam");
		viajero.setPosition(new LatLng(14.058324, 108.277199));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-vietnam/602715/");
		viajeros.add(viajero);

		// Washington D.C.
		viajero = new Viajero();
		viajero.setCity("Washington D.C.");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(38.907231, -77.036464));
		viajero.setChannel(ChannelId.TELEMADRID);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-washington-dc");
		viajeros.add(viajero);

		/* TODO: http://www.rtve.es/television/espanoles-en-el-mundo/washington/
		// Washington D.C.
		viajero = new Viajero();
		viajero.setCity("Washington D.C.");
		viajero.setCountry("Estados Unidos");
		viajero.setPosition(new LatLng(39.107231, -77.136464));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.telemadrid.es/programas/madrilenos-por-el-mundo/madrilenos-por-el-mundo-washington-dc");
		viajeros.add(viajero);
		*/

		// Yucatán
		viajero = new Viajero();
		viajero.setCity("Yucatán");
		viajero.setCountry("México");
		viajero.setPosition(new LatLng(20.709879, -89.094338));
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-yucatan/1140689/");
		viajeros.add(viajero);

		// Zimbabue
		viajero = new Viajero();
		viajero.setCity("Zimbabue");
		viajero.setCountry("Zimbabue");
		viajero.setPosition(new LatLng(-19.015438, 29.154857));
		viajero.setZoomLevel(6);
		viajero.setChannel(ChannelId.RTVE);
		viajero.setUrl("http://www.rtve.es/television/20120601/zimbabue-africa-mas-salvaje/532734.shtml");
		viajeros.add(viajero);
	}
	
	
	//========================================== Big index ============================================
	// Special screen text
    private final class RemoveWindow implements Runnable {
        public void run() {
            removeWindow();
        }
    }

	private void createBigIndex() {
		if (!mHandlerPosted) {
	        mHandler.post(new Runnable() {

	            public void run() {
	                mReady = true;
	                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
	                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
	                        WindowManager.LayoutParams.TYPE_APPLICATION,
	                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
	                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
	                        PixelFormat.TRANSLUCENT);
	                mWindowManager.addView(mDialogText, lp);
	                viewAddedToTheWindows = true;
	            }});
	        mHandlerPosted = true;
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        mReady = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeWindow();
        mReady = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewAddedToTheWindows) {
        	mWindowManager.removeView(mDialogText);
        }
        mReady = false;
    }

    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (mReady) {
            char firstLetter = viajeros.get(firstVisibleItem).getCity().charAt(0);
            
            if (!mShowing && !equalLetters(firstLetter, mPrevLetter)) {

                mShowing = true;
                mDialogText.setVisibility(View.VISIBLE);
            }
            
            // Special case for "Á"
            if (firstLetter == 'Á') {
            	firstLetter = 'A';
            }

            mDialogText.setText(((Character)firstLetter).toString());
            mHandler.removeCallbacks(mRemoveWindow);
            mHandler.postDelayed(mRemoveWindow, 3000);
            mPrevLetter = firstLetter;
        }
    }

    // Set the special case for "A" and "Á"
    private boolean equalLetters(char firstLetter, char prevLetter) {
        if (firstLetter == 'A' && prevLetter == 'Á') {
        	return true;
        } else {
        	return firstLetter == prevLetter;
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    private void removeWindow() {
        if (mShowing) {
            mShowing = false;
            mDialogText.setVisibility(View.INVISIBLE);
        }
    }

	
}