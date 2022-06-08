package pns.si3.ihm.birder.repositories.interfaces;

import androidx.lifecycle.LiveData;

import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.User;

/**
 * User repository.
 *
 * Manages the users in the database.
 */
public interface UserRepository {
	/**
	 * Returns whether the user is authenticated, or not.
	 * @return Whether the user is authenticated, or not.
	 */
	boolean isAuthenticated();

	/**
	 * Returns the id of the authenticated user.
	 * @return The id of the user, if the user is authenticated;
	 * <code>null</code> otherwise.
	 */
	String getAuthenticationId();

	/**
	 * Returns the authenticated user (updated in real time).
	 * @return The authenticated user (updated in real time).
	 */
	LiveData<DataTask<User>> getUser();

	/**
	 * Returns a user by id (updated in real time).
	 * @param id The id of the user.
	 * @return The selected user (updated in real time).
	 */
	LiveData<DataTask<User>> getUser(String id);

	/**
	 * Signs in the user with an email and password.
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return The authenticated user (updated in real time).
	 */
	LiveData<DataTask<User>> signIn(String email, String password);

	/**
	 * Signs out the user.
	 */
	void signOut();

	/**
	 * Creates a user.
	 * @param user The user to be created.
	 * @param password The password of the user.
	 * @return The created user.
	 */
	LiveData<DataTask<User>> createUser(User user, String password);

	/**
	 * Updates the authenticated user.
	 * @param user The user to be updated.
	 * @return The updated user.
	 */
	LiveData<DataTask<User>> updateUser(User user);

	/**
	 * Updates the password of the authenticated user.
	 * @param newPassword The new password of the user.
	 * @return Whether the password has been updated, or not.
	 */
	LiveData<DataTask<Void>> updatePassword(String newPassword);

	/**
	 * Deletes the authenticated user.
	 * @return Whether the user has been deleted, or not.
	 */
	LiveData<DataTask<Void>> deleteUser();
}
