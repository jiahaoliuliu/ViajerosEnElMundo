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
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.OnFullScreenRequestListener;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.OnUrlReceivedListener;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.ProgressBarShowListener;
import com.jiahaoliuliu.android.viajerosenelmundo.interfaces.onErrorReceivedListener;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero;
import com.jiahaoliuliu.android.viajerosenelmundo.model.Viajero.ChannelId;

import android.media.AudioRecord.OnRecordPositionUpdateListener;
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
	ProgressBarShowListener,
	OnFullScreenRequestListener
	{

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.drawer_main);

		context = this;

		// Get the title
		mTitle = mDrawerTitle = getTitle();

		viajeros = new ArrayList<Viajero>();

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        fragmentManager = getSupportFragmentManager();

        viajeros = CreateContents.CreateListViajeros();

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
				if (randomPosition >= 0) {
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
				if (randomPosition >= 0 ) {
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
        .setTitle(getResources().getString(R.string.alert_about_title))
        .setMessage(getResources().getString(R.string.alert_about_message))
        .setPositiveButton(getResources().getString(R.string.alert_about_button), new DialogInterface.OnClickListener() {
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

		// Remove all the backstack
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

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
		// If it was not map view (for example, web view), come back to map view
		Fragment fragmentShown = fragmentManager.findFragmentById(R.id.content_frame);
		if (fragmentShown != null && !fragmentShown.getTag().equals(WorldMapFragment.class.toString())) {
			fragmentManager.popBackStack();
		}
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
		ft.addToBackStack(WebViewFragment.class.toString());
		ft.commit();
	}

	public void showProgressBar() {
		setProgressBarIndeterminateVisibility(true);
	}

	public void hideProgressBar() {
		setProgressBarIndeterminateVisibility(false);
	}
	
	public void requestGoToFullScreen() {
		getSupportActionBar().hide();
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
		
		mDrawerList.setVisibility(View.GONE);
	}
	
	public void requestHideFullScreen() {
		getSupportActionBar().show();
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		}

		mDrawerList.setVisibility(View.VISIBLE);
	}
	// ================================================= Others =====================================
    // Handle back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	// If it is showing the webViewFragment, show the mapFragment
    		Fragment fragmentShown = fragmentManager.findFragmentById(R.id.content_frame);
    		if (fragmentShown != null && !fragmentShown.getTag().equals(WorldMapFragment.class.toString())) {
    			// Check if it is the web view
    			if (fragmentShown != null && fragmentShown.getTag().equals(WebViewFragment.class.toString())) {
    				if (webViewFragment != null) {
    					webViewFragment.goesBack();
    				}
    			}
    			
    			fragmentManager.popBackStack();
    		} else {
    			// If the app was already on the map fragment, show the alert dialog
    			if (closeAppAlertDialog == null) {
    				closeAppAlertDialog = createCloseAlertDialog();
    			}
    			closeAppAlertDialog.show();
    		}
            return true; // To finish here and say the key has been handled
        }
        return super.onKeyDown(keyCode, event);
    }
	
    private AlertDialog createCloseAlertDialog() {
    	return new AlertDialog.Builder(
                context)
        .setTitle(getResources().getString(R.string.alert_exit_title))
        .setMessage(getResources().getString(R.string.alert_exit_message))
        .setPositiveButton(getResources().getString(R.string.alert_exit_positive_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                    	finish();
                    }
                })
        .setNegativeButton(getResources().getString(R.string.alert_exit_negative_button),
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
    				Log.v(LOG_TAG, "\tRTVE - Espa√±oles en el mundo: " + programCount + " programas.");
    			break;
    			case TELEMADRID:
    				Log.v(LOG_TAG, "\tTelemadrid - Madrile√±os por el mundo: " + programCount + " programas.");
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

    // Set the special case for "A" and "√Å"
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
