package pns.si3.ihm.birder.views.reports;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.models.Species;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.viewmodels.ReportViewModel;
import pns.si3.ihm.birder.viewmodels.SpeciesViewModel;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.species.GiveSpeciesActivity;
import pns.si3.ihm.birder.views.species.InformationOneSpeciesActivity;

public class InformationActivity extends AppCompatActivity {

	/**
	 * The tag of the log messages.
	 */
	static final String TAG = "InformationActivity";

	/**
	 * The activity buttons.
	 */
    private Button buttonInfoRetour;
    private Button shareSignal;

	/**
	 * The request code
	 */
	public static final int REQUEST_INFORM_SPECIES = 0;


	/**
	 * The activity fields.
	 */
	private ImageView imageInfo;
	private ImageView imageQuestion;
	private ListView listView;
    private Species species;
	private MapView map;
	private IMapController mapController;
	private ArrayList<String> listItems;
	private ArrayAdapter<String> adapter;


	/**
	 * The report view model.
	 */
	private ReportViewModel reportViewModel;

	/**
	 * The user view model.
	 */
    private UserViewModel userViewModel;

	/**
	 * The species view model.
	 */
	private SpeciesViewModel speciesViewModel;

	/**
	 * The selected report.
	 */
	private Report report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Configuration.getInstance().load(   getApplicationContext(),
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) );
        setContentView(R.layout.activity_information);
		initViewModels();
		initFields();
		initButtons();
        loadReport();
    }

    private void initMap(){
		map = findViewById(R.id.map);
		map.setTileSource(TileSourceFactory.MAPNIK);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		mapController = map.getController();
		mapController.setZoom(12);
		GeoPoint startPoint = new GeoPoint(report.getLatitude(), report.getLongitude());
		ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		items.add(new OverlayItem(report.getSpecies(), "nombre : " + report.getNumber(), new GeoPoint(report.getLatitude(), report.getLongitude())));
		mapController.setCenter(startPoint);
		ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, items,
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
		mOverlay.setFocusItemsOnTap(true);
		map.getOverlays().add(mOverlay);
	}

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModels() {
		userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
		reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
		speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
	}

	/**
	 * Initializes the activity buttons.
	 */
	private void initButtons(){
        buttonInfoRetour = findViewById(R.id.buttonInfoRetour);
        buttonInfoRetour.setOnClickListener(v -> {
			Intent intent = new Intent(InformationActivity.this, MainActivity.class);
			startActivity(intent);
		});

		shareSignal = findViewById(R.id.buttonSignalShare);
		shareSignal.setOnClickListener(v -> {
			if (report != null) {
				String shareBody = "L'oiseau d'espèce \"" + report.getSpecies() + "\" a été observé!";
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "oiseau");
				sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, "Partager via"));
			}
		});

		imageQuestion = findViewById(R.id.imageView_question);
		imageQuestion.setOnClickListener(v -> {
			//Get first element of listView
			if(adapter.getItem(0).equals("Espèce non renseignée")) {
				Intent intent = new Intent(InformationActivity.this, GiveSpeciesActivity.class);
				intent.putExtra("picturePath", report.getPicturePath());
				intent.putExtra("reportId", report.getId());
				startActivityForResult(intent, REQUEST_INFORM_SPECIES);
			}
			else {
				Intent intent = new Intent(InformationActivity.this, InformationOneSpeciesActivity.class);
				intent.putExtra("speciesId", species.getId());
				startActivity(intent);
			}
		});
    }

	/**
	 * Initializes the activity fields.
	 */
	private void initFields(){
        imageInfo = findViewById(R.id.imageInfo);
        listView = findViewById(R.id.listInfo);
        imageQuestion = findViewById(R.id.imageView_question);
		listItems = new ArrayList<>();
		adapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1,
				listItems);
		listView.setAdapter(adapter);
    }

	/**
	 * Loads the report.
	 */
	private void loadReport() {
		// Get the report id.
		Intent intent = getIntent();
		String id = intent.getStringExtra("id");

		// Get the report.
    	reportViewModel
			.getReport(id)
			.observe(
				this,
				task -> {
					// Report found.
					if (task.isSuccessful()) {
						// Get the report.
						report = task.getData();

						// Update the fields.
						updateReport();
						loadPicture();
						loadUser();
						initMap();
					}

					// Report not found.
					else {
						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
	}

	/**
	 * Update the report values.
	 */
	private void updateReport() {
		if (!report.getSpecies().equals("Inconnue")){
			adapter.add("Espèce : " + report.getSpecies());
			// Search the species on the database (based on user input).
			speciesViewModel.searchSpecies(report.getSpecies());

			// Query succeeded.
			speciesViewModel
				.getFoundSpeciesLiveData()
				.observe(
					this,
					task -> {
						// Species found.
						if (task.isSuccessful()) {
							List<Species> foundSpecies = task.getData();
							if (foundSpecies != null && foundSpecies.size() > 0) {
								// Get the best matching species.
								Species bestMatch = foundSpecies.get(0);
								species = bestMatch;
								adapter.add("Nom scientifique : " + bestMatch.getName());
							}
						}

						// Species not found.
						else {
							// Error logs.
							Throwable error = task.getError();
							Log.e(TAG, error.getMessage());
						}

					}
				);
		} else {
			adapter.add("Espèce non renseignée");
			imageQuestion.setVisibility(View.VISIBLE);
		}

		adapter.add("Nombre : " + report.getNumber());
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd-MM-yyyy ");
		String date = formatter.format(report.getDate());
		adapter.add("Date : " + date);


		if(report.getAge() != null && !report.getAge().equals("")) {
			if (Integer.valueOf(report.getAge()) > 1) {
				adapter.add("Age : environ " + report.getAge() + " ans");
			} else adapter.add("Age : environ " + report.getAge() + " an");
		}
		if(report.getGender() != null) adapter.add("Genre : " + report.getGender());

	}

	/**
	 * Loads the report user.
	 */
	private void loadUser() {
		// Get the user.
		userViewModel
			.getUser(report.getUserId())
			.observe(
				this,
				task -> {
					// User found.
					if (task.isSuccessful()) {
						// Get the user.
						User user = task.getData();
						String userDisplayName = "Par : " + user.getFirstName() + " " + user.getLastName();
						adapter.add(userDisplayName);
					}
				}
			);
	}

	/**
	 * Loads the report picture.
	 */
    private void loadPicture() {
		String picturePath = report.getPicturePath();
		if (picturePath != null) {
			FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
			StorageReference pictureReference = firebaseStorage.getReference(picturePath);
			Glide
				.with(this)
				.load(pictureReference)
				.into(imageInfo);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_INFORM_SPECIES && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				String speciesChoosed = (String) bundle.get("name");

			}
		}
	}

}
