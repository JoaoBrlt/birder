package pns.si3.ihm.birder.repositories.interfaces;

import androidx.lifecycle.LiveData;

import java.util.List;

import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.Species;

/**
 * Species repository.
 *
 * Manages the bird species in the database.
 */
public interface SpeciesRepository {
	/**
	 * Returns a species.
	 * @param id The id of the species.
	 * @return The selected species.
	 */
	LiveData<DataTask<Species>> getSpecies(String id);

	/**
	 * Searches for species based on a text query.
	 * @param text The text query.
	 * @return The list of found species.
	 */
	LiveData<DataTask<List<Species>>> searchSpecies(String text);
}
