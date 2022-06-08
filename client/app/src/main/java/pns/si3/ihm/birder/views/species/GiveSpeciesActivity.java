package pns.si3.ihm.birder.views.species;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.osmdroid.config.Configuration;

import java.util.ArrayList;
import java.util.List;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.models.Species;
import pns.si3.ihm.birder.viewmodels.ReportViewModel;
import pns.si3.ihm.birder.viewmodels.SpeciesViewModel;

public class GiveSpeciesActivity extends AppCompatActivity {
    /**
     * The tag for the log messages.
     */
    private static final String TAG = "GiveSpeciesActivity";

    /**
     * The activity fields and buttons.
     */
    private Button buttonRetour;
    private ImageView imageOiseau;
    private ListView listView;
    private EditText editSpeciesName;
    private ImageView searchButton;
    private TextView textInformation;

    /**
     * The others elements.
     */
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private String chosenSpecies;
    private String reportId;
    private String picturePath;

    /**
     * The report view model.
     */
    private ReportViewModel reportViewModel;

    /**
     * The species view model.
     */
    private SpeciesViewModel speciesViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_give_species);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            picturePath = (String) bundle.get("picturePath");
            reportId = (String) bundle.get("reportId");
        }

		initViewModels();
        initFieldsAndButtons();
        updateImageView();
    }

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModels(){
		reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
		speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
	}

	/**
	 * Initializes the activity fields and buttons.
	 */
    private void initFieldsAndButtons(){
        imageOiseau = findViewById(R.id.imageOiseau);
		textInformation = findViewById(R.id.tv_choiceSpecies);

		// Return button.
		buttonRetour = findViewById(R.id.buttonRetour);
        buttonRetour.setOnClickListener(v -> {
        	finish();
		});

        // Species list.
		listView = findViewById(R.id.list_choicesBird_choice);
		listItems = new ArrayList<>();
		adapter = new ArrayAdapter<>(this,
			android.R.layout.simple_list_item_1,
			listItems);
		listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
			chosenSpecies = adapter.getItem(position);
			alertDialog();
		});

        // Species name.
		editSpeciesName = findViewById(R.id.edit_speciesname_choice);
        editSpeciesName.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                searchButton.performClick();
                InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return true;
            }
            return false;
        });

        // Search button.
		searchButton = findViewById(R.id.imageView_choice_search);
        searchButton.setOnClickListener(v -> {
            if (editSpeciesName.getText().toString().isEmpty()){
                editSpeciesName.setError("Veuillez saisir une espèce.");
            } else {
                adapter.clear();
                adapter.notifyDataSetChanged();
                searchSpecies();
            }
        });

    }

	/**
	 * Updates the image view.
	 */
	private void updateImageView() {
        if (picturePath != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference pictureReference = firebaseStorage.getReference(picturePath);
            Glide
				.with(this)
				.load(pictureReference)
				.into(imageOiseau);
        }
    }

	/**
	 * Searches species based on user input.
	 */
	private void searchSpecies() {
    	// Search species based on user input.
        speciesViewModel.searchSpecies(editSpeciesName.getText().toString());

        // Get the found species.
        speciesViewModel
			.getFoundSpeciesLiveData()
			.observe(
				this,
				task -> {
					// Species found.
					if (task.isSuccessful()) {
						// Get found species.
						List<Species> foundSpecies = task.getData();

						// Update the list.
						for (Species species : foundSpecies) {
							adapter.add(species.getFrenchCommonName());
							textInformation.setText("Veuillez choisir une espèce :");
						}
					}

					// Species not found.
					else {
						// Error message.
						editSpeciesName.setError("L'espèce que vous avez saisie est invalide.");

						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
    }

    /**
     * Updates the species of the report.
     */
    private void updateReport() {
    	// Get the report.
        reportViewModel
			.getReport(reportId)
			.observe(this,
				task -> {
					// Report found.
					if (task.isSuccessful()) {
						// Get the report.
						Report report = task.getData();
						report.setSpecies(chosenSpecies);

						// Update the report
						reportViewModel
							.updateReport(report)
							.observe(
								this,
								secondTask -> {
									// User updated.
									if (secondTask.isSuccessful()) {
										Log.i(TAG, "User updated.");

										// Send result.
										Intent returnInformationActivity = new Intent();
										setResult(RESULT_OK, returnInformationActivity);
										finish();
									}

									// User not updated.
									else {
										// Error logs.
										Throwable error = secondTask.getError();
										Log.e(TAG, error.getMessage());
									}
								}
							);
					}
				});
    }


	/**
	 * Alerts the user to validate the chosen species.
	 */
	private void alertDialog(){
        new AlertDialog.Builder(this)
			.setTitle("Changement de l'espèce")
			.setMessage("Voulez-vous associé l'espèce " + chosenSpecies + " à l'image ?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> updateReport())
			.setNegativeButton(android.R.string.no, null).show();
    }
}
