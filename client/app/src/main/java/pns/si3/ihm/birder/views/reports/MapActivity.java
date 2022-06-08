package pns.si3.ihm.birder.views.reports;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.adapters.ReportsAdapter;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.viewmodels.ReportViewModel;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.account.AccountActivity;
import pns.si3.ihm.birder.views.species.ChoiceSpeciesActivity;
import pns.si3.ihm.birder.views.auth.SignInActivity;

import static pns.si3.ihm.birder.views.position.IGPSActivity.REQUEST_CODE;

public class MapActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "MapActivity";

	private MapView map;
	private ReportViewModel reportViewModel;
	private UserViewModel userViewModel;
	private ReportsAdapter reportsAdapter;
	private List<Report> reports;
	private IMapController mapController;
	/**
	 * Fields needed to get the current user's position
	 */
	private Location userLocation;
	private LocationManager locationManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Configuration.getInstance().load(getApplicationContext(),
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
		setContentView(R.layout.activity_map);
		initViewModels();
		setSpinner();
		init();
		observeReports();
	}

	/**
	 * Initializes reports.
	 */
	private void init() {
		reportsAdapter = new ReportsAdapter();
	}

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModels() {
		userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
		reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
	}

	private void observeReports() {
		reportViewModel
			.getReports()
			.observe(
				this,
					task -> {
						// Reports found.
						if (task.isSuccessful()) {
							// Get the reports.
							reports = task.getData();

							map = findViewById(R.id.map);
							map.setTileSource(TileSourceFactory.MAPNIK);
							map.setBuiltInZoomControls(true);
							map.setMultiTouchControls(true);
							mapController = map.getController();
							mapController.setZoom(8);
							GeoPoint startPoint;
							ArrayList<OverlayItem> items = new ArrayList<OverlayItem>(); // future liste de nos signalisations
							boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
							if (permissionGranted){
								LocationManager locationManager;
								locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
								if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
									createGpsDisabledAlert();
									startPoint = new GeoPoint(43.65020, 7.00517);
								}
								else {
									setLocation();
									startPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
									items.add(new OverlayItem("Vous êtes ici", "",new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude())));

								}
							}
							else{
								startPoint = new GeoPoint(43.65020, 7.00517);
							}
							mapController.setCenter(startPoint);

						mapController.setCenter(startPoint);

						// Update the reports.
						for(Report report : reports){
							if(report.getLatitude() != null && report.getLongitude() != null) {
								items.add(new OverlayItem(report.getSpecies(), "nombre : " + report.getNumber(), new GeoPoint(report.getLatitude(), report.getLongitude())));
							}
						}
						ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, items,
							new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
								@Override
								public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
									//do something
									return true;
								}

								@Override
								public boolean onItemLongPress(final int index, final OverlayItem item) {
									Intent intent = new Intent(getApplicationContext(),InformationActivity.class);
									intent.putExtra("id",reports.get(index).getId());
									startActivity(intent);
									return false;
								}
							});
						mOverlay.setFocusItemsOnTap(true);
						map.getOverlays().add(mOverlay);
					}

					// Reports not found.
					else {
						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch(position){
			case 0:break;
			case 1: //Dernières signalisations
				{
				Intent intent = new Intent(MapActivity.this, MainActivity.class);
				startActivity(intent);
			}break;
			case 2://Liste des oiseaux
			{
				Intent intent = new Intent(MapActivity.this, ChoiceSpeciesActivity.class);
				intent.putExtra("want", "allSpecies");
				startActivity(intent);
			}
			break;
			case 3: //Compte (connecté) / Se connecter (déconnecté)
			{
				if (userViewModel.isAuthenticated()) {
					Intent intent = new Intent(MapActivity.this, AccountActivity.class);
					startActivity(intent);
				}
				else {
					Intent intent = new Intent(MapActivity.this, SignInActivity.class);
					startActivity(intent);
				}
			}break;
			case 4:// Déconnexion (connecté)
			{
				// The user is connected.
				if (userViewModel.isAuthenticated()) {
					// Sign out the user.
					userViewModel.signOut();

					// Success toast.
					Toast.makeText(
							MapActivity.this,
							"Vous avez été déconnecté !",
							Toast.LENGTH_SHORT
					).show();

					setSpinner();
				}
				// The user is not connected.
				else {
					// Navigate to sign in.
					Intent intent = new Intent(MapActivity.this, SignInActivity.class);
					startActivity(intent);
				}
			}break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void setLocation() {
		// Check if permission already granted
		boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if (!permissionGranted)
			ActivityCompat.requestPermissions(MapActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

		String fournisseur = null;
		if (locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteres = new Criteria();
			// la précision  : (ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision)
			criteres.setAccuracy(Criteria.ACCURACY_COARSE);
			// l'altitude
			criteres.setAltitudeRequired(false);
			// la direction
			criteres.setBearingRequired(false);
			// la vitesse
			criteres.setSpeedRequired(false);
			// un potentiel coût
			criteres.setCostAllowed(false);
			// la consommation d'énergie demandée
			criteres.setPowerRequirement(Criteria.POWER_MEDIUM);

			fournisseur = locationManager.getBestProvider(criteres, true);
		}

		if (fournisseur != null) {
			LocationListener locationListener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					userLocation = location;
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {

				}

				@Override
				public void onProviderEnabled(String provider) {

				}

				@Override
				public void onProviderDisabled(String provider) {

				}
			};

			// On configure la mise à jour automatique : immédiate et en permanence
			locationManager.requestLocationUpdates(fournisseur, 0, 0, locationListener);
			do {
				userLocation = locationManager.getLastKnownLocation(fournisseur);
			} while (userLocation == null);

			locationListener.onLocationChanged(userLocation);

			if (locationManager != null) {
				locationManager.removeUpdates(locationListener);
			}
		}
	}

	private void createGpsDisabledAlert() {
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
		localBuilder
				.setMessage("Le GPS est désactivé, voulez-vous l'activer ?")
				.setCancelable(false)
				.setPositiveButton("Activer le GPS ",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface paramDialogInterface, int paramInt) {
								startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
								finish();
							}
						}
				);
		localBuilder.setNegativeButton("Ne pas activer le GPS",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					}
				}
		);
		localBuilder.create().show();

	}

	public void setSpinner(){
		final Spinner spinner = (Spinner) findViewById(R.id.spinner_map);
		spinner.setOnItemSelectedListener(this);
		List<String> list = new ArrayList<>();
		list.add("Menu");
		list.add("Dernières signalisations");
		list.add("Liste des oiseaux");
		// The user is connected.
		if (userViewModel.isAuthenticated()) {
			list.add("Compte");
			list.add("Se déconnecter");
		}
		// The user is not connected.
		else {
			list.add("Se connecter");
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
	}
}
