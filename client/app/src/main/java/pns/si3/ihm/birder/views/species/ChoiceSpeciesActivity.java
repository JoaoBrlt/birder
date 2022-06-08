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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.osmdroid.config.Configuration;

import java.util.ArrayList;
import java.util.List;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Species;
import pns.si3.ihm.birder.viewmodels.SpeciesViewModel;

public class ChoiceSpeciesActivity extends AppCompatActivity {
	/**
	 * The tag for the log messages.
	 */
	private String TAG = "ChoiceSpecies";

	/**
	 * The species view model.
	 */
	private SpeciesViewModel speciesViewModel;

    /**
     * The activity fields and buttons.
     */
    private Button returnButton;
    private EditText editText;
    private ImageView imageViewSearch;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;
    private ListView listView;
    private TextView textView;

	/**
	 * The activity values.
	 */
	private String speciesId;
    private String want;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_choicespecies);
		initViewModels();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            want = getIntent().getStringExtra("want");
        }
		initButtonsAndFields();
    }

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModels(){
		speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
	}

	/**
	 * Initializes activity buttons and fields.
	 */
    private void initButtonsAndFields(){
		textView = findViewById(R.id.tv_choice);

    	// Return button.
        returnButton = findViewById(R.id.button_return_choicespecies);
		returnButton.setOnClickListener(v -> {
			finish();
		});

        // Species list.
        listView = findViewById(R.id.list_choicesBird);
        listItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (want == null){
                Intent returnReportActivity = new Intent();
                Log.i(TAG,adapter.getItem(position));
                returnReportActivity.putExtra("name", adapter.getItem(position));
                setResult(RESULT_OK, returnReportActivity);
                finish();
            }
            else if (want.equals("allSpecies")) {
                Intent showInformation = new Intent(ChoiceSpeciesActivity.this, InformationOneSpeciesActivity.class);
                Log.i(TAG, adapter.getItem(position));
                setSpeciesId(adapter.getItem(position));
                showInformation.putExtra("speciesId", speciesId);
                startActivity(showInformation);
            }
        });

        // Search button.
		imageViewSearch = findViewById(R.id.imageView_choice_search);
        imageViewSearch.setOnClickListener(v -> {
            if(editText.getText().toString().isEmpty()){
                editText.setError("Veuillez saisir une espèce.");
            }
            else {
                adapter.clear();
                adapter.notifyDataSetChanged();
                searchSpecies();
            }
        });

		// Search field.
		editText = findViewById(R.id.edit_speciesname);
        editText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                imageViewSearch.performClick();
                InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return true;
            }
            return false;
        });
    }

	/**
	 * Sets the species id based on user selection.
	 * @param frenchCommonName The french common name of the selected species.
	 */
	private void setSpeciesId(String frenchCommonName) {
		// Get found species.
        speciesViewModel
			.getFoundSpeciesLiveData()
			.observe(this,
				task -> {
					// Species found.
					if (task.isSuccessful()) {
						// Get the found species.
						List<Species> foundSpecies = task.getData();

						// Find the right species.
						for (Species species : foundSpecies) {
							if (species.getFrenchCommonName().equals(frenchCommonName)){
								speciesId = species.getId();
							}
						}
					}
				});
    }

	/**
	 * Searches species based on user input.
	 */
	private void searchSpecies() {
    	// Search species based on user input.
        speciesViewModel.searchSpecies(editText.getText().toString());

        // Get found species.
        speciesViewModel
			.getFoundSpeciesLiveData()
			.observe(
				this,
				task -> {
					// Species found.
					if (task.isSuccessful()) {
						// Get the found species.
						List<Species> foundSpecies = task.getData();

						// Update the list.
						for (Species species : foundSpecies) {
							adapter.add(species.getFrenchCommonName());
							textView.setText("Veuillez choisir une espèce :");
						}
					}

					// Species not found.
					else {
						// Error message.
						editText.setError("L'espèce que vous avez saisie est invalide.");

						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
    }
}
