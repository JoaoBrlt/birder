package pns.si3.ihm.birder.repositories.interfaces;

import androidx.lifecycle.LiveData;

import pns.si3.ihm.birder.models.Trophy;

public interface TrophyRepository {
    /**
     * Get a trophy from the database.
     * @param id The id of the user.
     * @return The live data of the trophy.
     */
    LiveData<Trophy> getTrophy(String id);

    /**
     * Creates a trophy in the database.
     * @param trophy The user to be created.
     * @return The live data of the created trophy.
     */
    LiveData<Trophy> createTrophy(Trophy trophy);

    /**
     * Returns the live data of the trophy request errors.
     * @return The live data of the trophy request errors.
     */
    LiveData<Exception> getErrors();

    /**
     * Clears the live data of the trophy request errors.
     * This avoids receiving the same error multiple times.
     */
    void clearErrors();
}
