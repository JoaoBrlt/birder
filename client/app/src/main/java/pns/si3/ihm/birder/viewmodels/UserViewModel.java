package pns.si3.ihm.birder.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.repositories.interfaces.UserRepository;
import pns.si3.ihm.birder.repositories.firebase.UserRepositoryFirebase;

/**
 * User view model.
 *
 * Holds the data for the views that use the user data.
 */
public class UserViewModel extends ViewModel {
	/**
	 * The user repository.
	 */
	private UserRepository userRepository;

	/**
	 * Constructs a user view model.
	 */
	public UserViewModel() {
		super();

		// Initialize the repositories.
		userRepository = new UserRepositoryFirebase();
	}

	/**
	 * Returns whether the user is authenticated, or not.
	 * @return Whether the user is authenticated, or not.
	 */
	public boolean isAuthenticated() {
		return userRepository.isAuthenticated();
	}

	/**
	 * Returns the id of the authenticated user.
	 * @return The id of the user, if the user is authenticated;
	 * <code>null</code> otherwise.
	 */
	public String getAuthenticationId() {
		return userRepository.getAuthenticationId();
	}

	/**
	 * Returns the authenticated user (updated in real time).
	 * @return The authenticated user (updated in real time).
	 */
	public LiveData<DataTask<User>> getUser() {
		return userRepository.getUser();
	}

	/**
	 * Returns a user by id (updated in real time).
	 * @param id The id of the user.
	 * @return The selected user (updated in real time).
	 */
	public LiveData<DataTask<User>> getUser(String id) {
		return userRepository.getUser(id);
	}

	/**
	 * Signs in the user with an email and password.
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return The authenticated user (updated in real time).
	 */
	public LiveData<DataTask<User>> signIn(String email, String password) {
		return userRepository.signIn(email, password);
	}

	/**
	 * Signs out the user.
	 */
	public void signOut() {
		userRepository.signOut();
	}

	/**
	 * Creates a user.
	 * @param user The user to be created.
	 * @param password The password of the user.
	 * @return The created user.
	 */
	public LiveData<DataTask<User>> createUser(User user, String password) {
		return userRepository.createUser(user, password);
	}

	/**
	 * Updates the authenticated user.
	 * @param user The user to be updated.
	 * @return The updated user.
	 */
	public LiveData<DataTask<User>> updateUser(User user) {
		return userRepository.updateUser(user);
	}

	/**
	 * Updates the password of the authenticated user.
	 * @param newPassword The new password of the user.
	 * @return Whether the password has been updated, or not.
	 */
	public LiveData<DataTask<Void>> updatePassword(String newPassword) {
		return userRepository.updatePassword(newPassword);
	}

	/**
	 * Deletes the authenticated user.
	 * @return Whether the user has been deleted, or not.
	 */
	public LiveData<DataTask<Void>> deleteUser() {
		return userRepository.deleteUser();
	}
}
