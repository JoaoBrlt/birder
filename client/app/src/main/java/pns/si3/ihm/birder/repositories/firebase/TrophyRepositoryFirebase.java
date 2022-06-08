package pns.si3.ihm.birder.repositories.firebase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import pns.si3.ihm.birder.exceptions.DocumentNotFoundException;
import pns.si3.ihm.birder.models.Trophy;

public class TrophyRepositoryFirebase {
    /**
     * The tag of the log messages.
     */
    private static final String TAG = "TrophyRepository";

    /**
     * The firebase firestore instance.
     */
    private FirebaseFirestore firebaseFirestore;

    /**
     * The live data of the trophy request errors.
     */
    private MutableLiveData<Exception> errorLiveData;

    /**
     * Constructs a trophy repository.
     */
    public TrophyRepositoryFirebase() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        errorLiveData = new MutableLiveData<>();
    }

    /**
     * Gets a trophy from the database in real time.
     * @param id The id of the trophy.
     * @return The live data of the trophy.
     */
    public LiveData<Trophy> getTrophy(String id) {
        MutableLiveData<Trophy> trophyLiveData = new MutableLiveData<>();

        // Get the user.
        firebaseFirestore
			.collection("trophys")
			.document(id)
			.addSnapshotListener(
				(trophySnapshot, error) -> {
					if (error == null) {
						// Query succeeded.
						if (trophySnapshot != null) {
							// User found.
							Trophy trophy = trophySnapshot.toObject(Trophy.class);
							trophyLiveData.setValue(trophy);
						} else {
							// User not found.
							errorLiveData.setValue(new DocumentNotFoundException());
						}
					} else {
						// Query failed.
						errorLiveData.setValue(error);
					}
				}
			);

        return trophyLiveData;
    }

    /**
     * Creates a trophy in the database.
     * @param trophy The trophy to be created.
     * @return The live data of the created trophy.
     */
    public LiveData<Trophy> createTrophy(Trophy trophy) {
        MutableLiveData<Trophy> trophyLiveData = new MutableLiveData<>();

        // Create the user.
        firebaseFirestore
			.collection("trophys")
			.document(trophy.id)
			.set(trophy)
			.addOnCompleteListener(
				trophyTask -> {
					if (trophyTask.isSuccessful()) {
						// Query succeeded.
						trophyLiveData.setValue(trophy);
					} else {
						// Query failed.
						errorLiveData.setValue(trophyTask.getException());
					}
				}
			);

        return trophyLiveData;
    }

    /**
     * Returns the live data of the user request errors.
     * @return The live data of the user request errors.
     */
    public LiveData<Exception> getErrors() {
        return errorLiveData;
    }

    /**
     * Clears the live data of the user request errors.
     * This avoids receiving the same error multiple times.
     */
    public void clearErrors() {
        errorLiveData.setValue(null);
    };
}
