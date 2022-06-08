package pns.si3.ihm.birder.repositories.firebase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.algolia.search.saas.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pns.si3.ihm.birder.exceptions.DocumentNotFoundException;
import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.Species;
import pns.si3.ihm.birder.repositories.interfaces.SpeciesRepository;


public class SpeciesRepositoryFirebase implements SpeciesRepository {
	/**
	 * The tag of the log messages.
	 */
	static final String TAG = "SpeciesRepository";

	/**
	 * The firebase firestore instance.
	 */
	private FirebaseFirestore firebaseFirestore;


	/**
	 * The firebase authentication instance.
	 */
	private FirebaseAuth firebaseAuth;

	/**
	 * The algolia client instance.
	 */
	private Client algoliaClient;

	/**
	 * The algolia species index.
	 * This is used to make searches.
	 */
	private Index algoliaIndex;

	/**
	 * Constructs a species repository.
	 */
	public SpeciesRepositoryFirebase() {
		// Initialize Firebase.
		firebaseFirestore = FirebaseFirestore.getInstance();
		firebaseAuth = FirebaseAuth.getInstance();

		// Initialize Algolia.
		algoliaClient = new Client("AH93SYAOZE", "5b67feb8945d27389980251e9c6f4d65");
		algoliaIndex = algoliaClient.getIndex("species");
	}


	/**
	 * Returns the id of the authenticated user.
	 * @return The id of the user, if the user is authenticated;
	 * <code>anonymous</code> otherwise.
	 */
	private String getAuthenticationId() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			return firebaseUser.getUid();
		}
		return "anonymous";
	}

	/**
	 * Returns a species.
	 * @param id The id of the species.
	 * @return The selected species.
	 */
	@Override
	public LiveData<DataTask<Species>> getSpecies(String id) {
		MutableLiveData<DataTask<Species>> speciesLiveData = new MutableLiveData<>();

		// Get the species.
		firebaseFirestore
			.collection("species")
			.document(id)
			.get()
			.addOnCompleteListener(
				task -> {
					// Query succeeded.
					if (task.isSuccessful()) {
						// Species found.
						DocumentSnapshot snapshot = task.getResult();
						if (snapshot != null && snapshot.exists()) {
							Species species = snapshot.toObject(Species.class);
							if (species != null) {
								// Update the species id.
								species.setId(id);

								// Success task.
								DataTask<Species> dataTask = DataTask.success(species);
								speciesLiveData.setValue(dataTask);
							}
						} else {
							// Error task.
							DataTask<Species> dataTask = DataTask.error(new DocumentNotFoundException());
							speciesLiveData.setValue(dataTask);
						}
					}

					// Query failed.
					else {
						// Error task.
						DataTask<Species> dataTask = DataTask.error(task.getException());
						speciesLiveData.setValue(dataTask);
					}
				}
			);

		return speciesLiveData;
	}

	/**
	 * Returns a list of species.
	 * @param speciesIds The list of species ids.
	 * @return The selected list of species.
	 */
	private LiveData<DataTask<List<Species>>> getSpecies(List<String> speciesIds) {
		MutableLiveData<DataTask<List<Species>>> speciesLiveData = new MutableLiveData<>();

		// Get species from list of ids.
		firebaseFirestore
			.collection("species")
			.whereIn(FieldPath.documentId(), speciesIds)
			.get()
			.addOnCompleteListener(
				task -> {
					// Query succeeded.
					if (task.isSuccessful()) {
						// Species found.
						QuerySnapshot snapshots = task.getResult();
						if (snapshots != null) {
							// Get all species.
							List<Species> foundSpecies = new ArrayList<>();
							for (QueryDocumentSnapshot snapshot : snapshots) {
								Species species = snapshot.toObject(Species.class);
								species.setId(snapshot.getId());
								foundSpecies.add(species);
							}

							// Success task.
							DataTask<List<Species>> dataTask = DataTask.success(foundSpecies);
							speciesLiveData.setValue(dataTask);
						} else {
							// Error task.
							DataTask<List<Species>> dataTask = DataTask.error(new DocumentNotFoundException());
							speciesLiveData.setValue(dataTask);
						}
					} else {
						// Error task.
						DataTask<List<Species>> dataTask = DataTask.error(new DocumentNotFoundException());
						speciesLiveData.setValue(dataTask);
					}
				}
			);

		return speciesLiveData;
	}

	/**
	 * Searches for species ids based on a text query.
	 * @param text The text query.
	 * @return The list of found species ids.
	 */
	private LiveData<DataTask<List<String>>> searchSpeciesIds(String text) {
		MutableLiveData<DataTask<List<String>>> speciesIdsLiveData = new MutableLiveData<>();

		// Set the request options for analytics.
		String userId = getAuthenticationId();
		RequestOptions requestOptions = new RequestOptions();
		requestOptions.setHeader("X-Algolia-UserToken", userId);

		// Create the query.
		Query query = new Query(text)
			.setAttributesToRetrieve("objectID")
			.setHitsPerPage(5);

		// Search the species.
		algoliaIndex.searchAsync(
			query,
			requestOptions,
			(content, error) -> {
				// Search succeeded.
				if (error == null) {
					try {
						// Species found.
						if (content != null) {
							// Get all found species.
							List<String> speciesIds = new ArrayList<>();
							JSONArray hits = content.getJSONArray("hits");
							for (int i = 0; i < hits.length(); i++) {
								JSONObject hit = hits.getJSONObject(i);
								String id = (String) hit.get("objectID");
								speciesIds.add(id);
							}

							// Success task.
							DataTask<List<String>> dataTask = DataTask.success(speciesIds);
							speciesIdsLiveData.setValue(dataTask);
						}
					} catch (Exception otherError) {
						// Error task.
						DataTask<List<String>> dataTask = DataTask.error(otherError);
						speciesIdsLiveData.setValue(dataTask);
					}
				}

				// Search failed.
				else {
					// Error task.
					DataTask<List<String>> dataTask = DataTask.error(error);
					speciesIdsLiveData.setValue(dataTask);
				}
			}
		);

		return speciesIdsLiveData;
	}

	/**
	 * Searches for species based on a text query.
	 * @param text The text query.
	 * @return The list of ids of the found species.
	 */
	public LiveData<DataTask<List<Species>>> searchSpecies(String text) {
		// Search species ids.
		return Transformations.switchMap(
			searchSpeciesIds(text),
			task -> {
				// Species found.
				if (task.isSuccessful()) {
					// Get the species ids.
					List<String> speciesIds = task.getData();

					// Get the species.
					return getSpecies(speciesIds);
				}

				// Species not found.
				DataTask<List<Species>> dataTask = DataTask.error(task.getError());
				return new MutableLiveData<>(dataTask);
			}
		);
	}

}
