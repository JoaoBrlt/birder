package pns.si3.ihm.birder.views.position;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.views.reports.ReportActivity;

import static pns.si3.ihm.birder.views.position.IGPSActivity.REQUEST_CODE;

public class GpsActivity extends AppCompatActivity {

    private MapView mapView;
    private MapController mapController;
    private Location userLocation;
    private LocationManager locationManager = null;
    private String fournisseur;
    private LocationListener locationListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_gps);

        // Return button
        Button returnButton = findViewById(R.id.buttonGPSCancel);
        returnButton.setOnClickListener(v -> {
            arreterLocalisation();
            finish();
        });

        // Valid button
        Button validButton = findViewById(R.id.buttonGPSValid);
        validButton.setOnClickListener(v -> {
            arreterLocalisation();

            Intent returnReportActivity = new Intent(GpsActivity.this, ReportActivity.class);
            returnReportActivity.putExtra("location", userLocation);
            setResult(RESULT_OK, returnReportActivity);
            finish();
        });

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);
        mapController = (MapController) mapView.getController();
        mapController.setZoom(13);

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                mapView.getOverlays().clear();

                mapController.setCenter(p);

                userLocation.setLatitude(p.getLatitude());
                userLocation.setLongitude(p.getLongitude());
                addIcon(userLocation);

                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay overlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        mapView.getOverlays().add(overlayEvents);

        // Check if permission already granted
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted)
            ActivityCompat.requestPermissions(GpsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        else {
            initialiserLocalisation();

            GeoPoint currentLocation = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
            mapController.setCenter(currentLocation);

            addIcon(userLocation); // Places an icon on 'location'
        }
    }

    /**
     * Set 'userLocation' on user's position
     */
    public void initialiserLocalisation() {
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

        // Check if permission already granted
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted)
            ActivityCompat.requestPermissions(GpsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

        if (fournisseur != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    userLocation = location;
                    mapController.setCenter(new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude()));
                    Log.d("GPS", "Lat : " + userLocation.getLatitude() + " & Lon : " + userLocation.getLongitude());
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

            // Mise à jour auto : 15 secondes - 10 mètres
            locationManager.requestLocationUpdates(fournisseur, 15000, 10, locationListener);
        }
    }

    /**
     * Places an icon on 'location'
     */
    void addIcon(Location location) {
        ArrayList<OverlayItem> items = new ArrayList<>();
        OverlayItem report;
        report = new OverlayItem("Signalisation", getPlaceName(location, this), new GeoPoint(location.getLatitude(), location.getLongitude()));
        items.add(report);

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<>(this, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                });

        // On recentre le marqueur lorsqu'on appuie dessus
        mOverlay.setFocusItemsOnTap(true);

        mapView.getOverlays().add(mOverlay);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Check if permission already granted
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!permissionGranted)
            ActivityCompat.requestPermissions(GpsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        else {
            initialiserLocalisation();

            GeoPoint currentLocation = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
            mapController.setCenter(currentLocation);

            addIcon(userLocation); // Places an icon on 'location'
        }
    }

    private void arreterLocalisation() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    /**
     * Method to get the name of the place of the report
     */
    public static String getPlaceName(Location location, Context context) {
        if (location != null) {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                return addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("GPS", "Erreur lors de la récupération de l'adresse");
                return "Lieu inconnu";
            }
        } else
            return "Lieu inconnu";
    }
}
