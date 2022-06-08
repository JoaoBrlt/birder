package pns.si3.ihm.birder.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.Species;
import pns.si3.ihm.birder.repositories.firebase.SpeciesRepositoryFirebase;
import pns.si3.ihm.birder.repositories.interfaces.SpeciesRepository;

/**
 * Species view model.
 *
 * Holds the data for species views.
 */
public class SpeciesViewModel extends ViewModel {
	/**
	 * The species repository.
	 */
	private SpeciesRepository speciesRepository;

	/**
	 * The list of found species.
	 */
	private LiveData<DataTask<List<Species>>> foundSpeciesLiveData;

	/**
	 * Constructs a species view model.
	 */
	public SpeciesViewModel() {
		super();

		// Initialize the repository.
		speciesRepository = new SpeciesRepositoryFirebase();

		// Initialize the live data.
		foundSpeciesLiveData = speciesRepository.searchSpecies("");
	}

	/**
	 * Returns a species.
	 * @param id The id of the species.
	 * @return The selected species.
	 */
	public LiveData<DataTask<Species>> getSpecies(String id) {
		return speciesRepository.getSpecies(id);
	}

	/**
	 * Searches for species based on a text query.
	 * @param text The text query.
	 */
	public void searchSpecies(String text) {
		foundSpeciesLiveData = speciesRepository.searchSpecies(text);
	}

	/**
	 * Returns the list of found species.
	 * @return The list of found species.
	 */
	public LiveData<DataTask<List<Species>>> getFoundSpeciesLiveData() {
		return foundSpeciesLiveData;
	}
}
