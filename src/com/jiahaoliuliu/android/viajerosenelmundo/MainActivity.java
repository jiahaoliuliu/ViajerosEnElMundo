package com.jiahaoliuliu.android.viajerosenelmundo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
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

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.view.GravityCompat;

public class MainActivity extends SherlockFragmentActivity {

	// Variables
	private static final int MENU_BUTTON_ABOUT_ME_ID = 10000;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(
        		Menu.NONE,
        		MENU_BUTTON_ABOUT_ME_ID,
        		Menu.NONE,
        		"Acerca de")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		} else if (item.getItemId() == MENU_BUTTON_ABOUT_ME_ID) {
			// Show alert message
			showDialog(0);
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

	private void addData() {
		// Add data
		// Europa
		Viajero viajero = new Viajero();
		viajero.setCity("Ámsterdam");
		viajero.setCountry("Países bajos");
		viajero.setPosition(new LatLng(52.370216, 4.895168));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo--amsterdam/572762/");
		viajeros.add(viajero);
		
		viajero = new Viajero();
		viajero.setCity("Atenas");
		viajero.setCountry("Grecia");
		viajero.setPosition(new LatLng(37.983716, 23.72931));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo--atenas/604343/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Belfast");
		viajero.setCountry("Reinos Unidos");
		viajero.setPosition(new LatLng(54.597285, -5.93012));
		viajero.setUrl("http://www.rtve.es/television/20130506/ciudad-marcada-historia/656946.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Bélgica");
		viajero.setCountry("Bélgica");
		viajero.setPosition(new LatLng(50.503887, 4.469936));
		viajero.setUrl("http://www.rtve.es/television/20101118/espanoles-mundo-viaja-hasta-belgica-cuna-del-art-nouveau/372481.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Berlín");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(52.519171, 13.406091));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-berlin/889792/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Bretaña");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(48.202047, -2.932644));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-bretana/1529562/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Budapest");
		viajero.setCountry("Hungría");
		viajero.setPosition(new LatLng(47.497912, 19.040235));
		viajero.setUrl("http://www.rtve.es/television/20101129/377030.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Costa Azul");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(43.695348, 7.255213));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo--costa-azul/539888/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Cracovia");
		viajero.setCountry("Polonia");
		viajero.setPosition(new LatLng(50.06465, 19.94498));
		viajero.setUrl("http://www.rtve.es/television/20111011/espanoles-mundo-viaja-cracovia-alma-polonia/467534.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Croacia");
		viajero.setCountry("Croacia");
		viajero.setPosition(new LatLng(45.1, 15.2));
		viajero.setUrl("http://www.rtve.es/television/20110204/espanoles-mundo-viaja-croacia-pais-con-historia-naturaleza-playa/401174.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Chipre");
		viajero.setCountry("Chipre");
		viajero.setPosition(new LatLng(35.126413, 33.429859));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--chipre/569001/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Dinamarca");
		viajero.setCountry("Dinamarca");
		viajero.setPosition(new LatLng(56.26392, 9.501785));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--dinamarca/530386/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Dublín");
		viajero.setCountry("Irlanda");
		viajero.setPosition(new LatLng(53.349805, -6.26031));
		viajero.setUrl("http://www.rtve.es/television/20101014/espanoles-mundo-visita-dublin-ciudad-cerveza-negra/361923.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Edimburgo");
		viajero.setCountry("Escocia");
		viajero.setPosition(new LatLng(55.916588, -3.202392));
		viajero.setUrl("http://rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-edimburgo/600422/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Esmirna");
		viajero.setCountry("Turquía");
		viajero.setPosition(new LatLng(38.41885, 27.12872));
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/esmirna/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Estocolmo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(59.32893, 18.06491));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--estocolmo/609798/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Finlandia");
		viajero.setCountry("Finlandia");
		viajero.setPosition(new LatLng(61.92411, 25.748151));
		viajero.setUrl("http://www.rtve.es/television/20120727/finlandia-pais-mil-lagos/550822.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Fiordos");
		viajero.setCountry("Noruega");
		viajero.setPosition(new LatLng(63.434584, 10.398412));
		viajero.setUrl("http://www.rtve.es/television/20110916/espectaculo-naturaleza-fiordos-noruegos-espanoles-mundo/461910.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Florencia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(43.771033, 11.248001));
		viajero.setUrl("http://www.rtve.es/television/20121130/florencia-museo-aire-libre-espanoles-mundo/579881.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Ginebra");
		viajero.setCountry("Suiza");
		viajero.setPosition(new LatLng(46.198392, 6.142296));
		viajero.setUrl("http://www.rtve.es/television/20120525/ginebra-calidad-vida-crisol-nacionalidades/531564.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Gotemburgo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(57.70887, 11.97456));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/espanoles-en-el-mundo/espanoles-mundo-gotemburgo/1536918/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Islandia");
		viajero.setCountry("Islandia");
		viajero.setPosition(new LatLng(64.963051, -19.020835));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--islandia/548756/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Islas Griegas");
		viajero.setCountry("Islas Griegas");
		viajero.setPosition(new LatLng(39.074208, 21.824312));
		viajero.setUrl("http://www.rtve.es/television/20111216/navegando-islas-griegas/482447.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Lisboa");
		viajero.setCountry("Portugal");
		viajero.setPosition(new LatLng(38.723733, -9.139547));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo/565339/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Lituania");
		viajero.setCountry("Lituania");
		viajero.setPosition(new LatLng(55.169438, 23.881275));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo-lituania/483570/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Liverpool");
		viajero.setCountry("Reino Unido");
		viajero.setPosition(new LatLng(53.408371, -2.991573));
		viajero.setUrl("http://www.rtve.es/television/20120113/liverpool-ciudad-caracter/489224.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Londres");
		viajero.setCountry("Reino Unido");
		viajero.setPosition(new LatLng(51.511214, -0.119824));
		viajero.setUrl("http://www.rtve.es/television/20101111/espanoes-mundo-viaja-londres-ciudad-cosmopolita/369956.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Madeira");
		viajero.setCountry("Portugal");
		viajero.setPosition(new LatLng(32.760707, -16.959472));
		viajero.setUrl("http://www.rtve.es/television/20120302/madeira-jardin-del-atlantico/503518.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Malmo");
		viajero.setCountry("Suecia");
		viajero.setPosition(new LatLng(55.604981, 13.003822));
		viajero.setUrl("http://www.rtve.es/television/20130513/ciudad-sostenible-sede-del-festival-eurovision-2013/662382.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Malta");
		viajero.setCountry("Malta");
		viajero.setPosition(new LatLng(35.854114, 14.48328));
		viajero.setUrl("http://www.rtve.es/television/20110601/espanoles-mundo-visita-malta-paraiso-piedra-mar/435078.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Milán");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(45.458626, 9.181873));
		viajero.setUrl("http://www.rtve.es/television/20130429/capital-mundial-del-diseno/652522.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Moscú");
		viajero.setCountry("Rusia");
		viajero.setPosition(new LatLng(55.755826, 37.6173));
		viajero.setUrl("http://www.rtve.es/television/20100914/espanoles-mundo-viaja-moscu-ciudad-mas-poblada-europa/354053.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Múnich");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(48.13672, 11.576754));
		viajero.setUrl("http://www.rtve.es/television/20110120/espanoles-mundo-aterriza-munich-capital-mundial-cerveza/396495.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Nápoles");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(40.851775, 14.268124));
		viajero.setUrl("http://www.rtve.es/television/20110506/napoles-esencia-mediterranea-pies-del-vesubio/517911.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Oporto");
		viajero.setCountry("Portugal");
		viajero.setPosition(new LatLng(41.156689, -8.623925));
		viajero.setUrl("http://www.rtve.es/television/20101216/espanoles-mundo-visita-oporto-ciudad-los-puentes-dobre-duero/387786.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Oslo");
		viajero.setCountry("Noruega");
		viajero.setPosition(new LatLng(59.91304138427136, 10.765228271484375));
		viajero.setUrl("http://www.rtve.es/television/20110105/oslo-ciudad-protege-espanoles-mundo/392759.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("París");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(48.856614, 2.352222));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/programa/espanoles-mundo-paris/1165819/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Praga");
		viajero.setCountry("República Checa");
		viajero.setPosition(new LatLng(50.075538, 14.4378));
		viajero.setUrl("http://www.rtve.es/television/20100908/espanoles-mundo-viaja-madre-las-capitales-europa-praga/352809.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Provenza");
		viajero.setCountry("Francia");
		viajero.setPosition(new LatLng(47.5471239, 3.954664));
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/provenza/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Roma");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(41.892916, 12.48252));
		viajero.setUrl("http://www.rtve.es/television/20120608/roma-siete-caminos-para-llegar/534051.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Róterdam");
		viajero.setCountry("Paises Bajos");
		viajero.setPosition(new LatLng(51.924216, 4.481776));
		viajero.setUrl("http://www.rtve.es/television/20130225/espanoles-roterdam/610357.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Salzburgo");
		viajero.setCountry("Austria");
		viajero.setPosition(new LatLng(47.80949, 13.05501));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--salzburgo/585930/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("San Petesburgo");
		viajero.setCountry("Rusia");
		viajero.setPosition(new LatLng(59.93428, 30.335099));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--san-petersburgo/561800/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Selva Negra");
		viajero.setCountry("Alemania");
		viajero.setPosition(new LatLng(48.325885, 8.191226));
		viajero.setUrl("http://www.rtve.es/television/espanoles-en-el-mundo/selvanegra/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Serbia");
		viajero.setCountry("Serbia");
		viajero.setPosition(new LatLng(44.016521, 21.005859));
		viajero.setUrl("http://www.rtve.es/alacarta/videos/television/espanoles-mundo--serbia/512643/");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Sicilia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(37.599994, 14.015356));
		viajero.setUrl("http://www.rtve.es/television/20110506/sicilia-region-volcanes-espanole-mundo/430526.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Suiza");
		viajero.setCountry("Suiza");
		viajero.setPosition(new LatLng(46.818188, 8.227512));
		viajero.setUrl("http://www.rtve.es/television/20101028/espoles-mundo-viaja-suiza-pais-neutral-por-excelencia/365656.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Tirol");
		viajero.setCountry("Austria");
		viajero.setPosition(new LatLng(47.253741, 11.601487));
		viajero.setUrl("http://www.rtve.es/television/20110915/vamos-pais-heidi-visitamos-tirol/461698.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Transilvania");
		viajero.setCountry("Rumania");
		viajero.setPosition(new LatLng(46.756337, 23.410231));
		viajero.setUrl("http://www.rtve.es/television/20111028/bienvenidos-transilvania-corazon-rumania/471639.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Turín");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(45.063299, 7.669289));
		viajero.setUrl("http://www.rtve.es/television/20100627/espanoles-mundo-viaja-a-turin-grandes-desconocidas-italia/337370.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Ucrania");
		viajero.setCountry("Ucrania");
		viajero.setPosition(new LatLng(48.379433, 31.16558));
		viajero.setUrl("http://www.rtve.es/television/20111118/segundo-pais-mas-grande-europa-sigue-siendo-tierra-desconocida/476327.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Venecia");
		viajero.setCountry("Italia");
		viajero.setPosition(new LatLng(45.440847, 12.315515));
		viajero.setUrl("http://www.rtve.es/television/20120126/lugar-enamora-inspira-cuenta/492866.shtml");
		viajeros.add(viajero);

		viajero = new Viajero();
		viajero.setCity("Viena");
		viajero.setCountry("Austria");
		viajero.setPosition(new LatLng(48.208174, 16.373819));
		viajero.setUrl("http://www.rtve.es/television/20111007/viena-mas-clasica-capitales-europeas/466703.shtml");
		viajeros.add(viajero);

	}
}
