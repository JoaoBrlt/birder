package pns.si3.ihm.birder.views.species;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Species;
import pns.si3.ihm.birder.viewmodels.SpeciesViewModel;

public class InformationOneSpeciesActivity extends AppCompatActivity {
    /**
     * The tag for the log messages.
     */
    private static final String TAG = "InformationOneSpecies";

    /**
     * The activity buttons and fields.
     */
	private Button buttonReturn;
    private TextView textSpecies;
    private ListView listView;
    private ArrayList<String> listItems;
    private ArrayAdapter<String> adapter;

	/**
	 * The activity values.
	 */
	private Species species;

    /**
     * The species view model.
     */
    private SpeciesViewModel speciesViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_one_species);
		initButtonsAndFields();
        initViewModel();
        loadSpecies();
    }

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModel() {
		speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
	}

	/**
	 * Initializes the activity buttons and fields.
	 */
	private void initButtonsAndFields(){
		// Return button.
		buttonReturn = findViewById(R.id.buttonOneSpeciesReturn);
		buttonReturn.setOnClickListener(v -> {
			finish();
		});

		// Species title.
        textSpecies = findViewById(R.id.textOneSpecies);

        // Species details.
        listView = findViewById(R.id.listOneSpecies);
        listItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        listView.setAdapter(adapter);
    }

    /**
     * Loads the report.
     */
    private void loadSpecies() {
        // Get the species id.
        Intent intent = getIntent();
        String speciesId = intent.getStringExtra("speciesId");

        // Get the species.
        speciesViewModel
			.getSpecies(speciesId)
			.observe(
				this,
				task -> {
					// Species found.
					if (task.isSuccessful()) {
						// Update the data.
						species = task.getData();
						updateSpecies();
					}

					// Species not found.
					else {
						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
    }

	/**
	 * Update the species fields.
	 */
	private void updateSpecies(){
        if (species != null) {
            textSpecies.setText(species.getFrenchCommonName());
            adapter.add("Nom scientifique : " + species.getName());
            adapter.add("Taxon : " + species.getTaxon());
            adapter.add("Ordre : " + species.getOrder());
            adapter.add("Famille : " + species.getFamily());
            adapter.add("Genre : " + species.getGenus());
            adapter.add("Espèce éteinte : " + (species.isExtinct() ? "Oui" : "Non"));
            adapter.add("Région de reproduction : " + species.getBreedingRegion());
            adapter.add("Sous-région de reproduction : " + species.getBreedingSubregion());
            adapter.add(
            	"Sous-région non reproductrice : " +
				(
					species.getNonbreedingSubregion().equals("None") ?
                	"Aucune" : species.getNonbreedingSubregion()
				)
			);
        }
	}
}
