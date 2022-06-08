package pns.si3.ihm.birder.repositories.firebase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import pns.si3.ihm.birder.exceptions.DocumentNotFoundException;
import pns.si3.ihm.birder.exceptions.UserNotAuthenticatedException;
import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.repositories.interfaces.UserRepository;

/**
 * User repository using Firebase.
 *
 * Implementation of the user repository using Firebase.
 */
public class UserRepositoryFirebase implements UserRepository {
	/**
	 * The tag of the log messages.
	 */
	private static final String TAG = "UserRepository";

	/**
	 * The firebase authentication instance.
	 */
	private FirebaseAuth firebaseAuth;

	/**
	 * The firebase firestore instance.
	 */
	private FirebaseFirestore firebaseFirestore;

	/**
	 * Constructs a user repository.
	 */
	public UserRepositoryFirebase() {
		firebaseAuth = FirebaseAuth.getInstance();
		firebaseFirestore = FirebaseFirestore.getInstance();
	}

	/*====================================================================*/
	/*                         AUTHENTICATION ONLY                        */
	/*====================================================================*/

	/**
	 * Returns whether the user is authenticated, or not.
	 * @return Whether the user is authenticated, or not.
	 */
	@Override
	public boolean isAuthenticated() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		return firebaseUser != null;
	}

	/**
	 * Returns the id of the authenticated user.
	 * @return The id of the user, if the user is authenticated;
	 * <code>null</code> otherwise.
	 */
	public String getAuthenticationId() {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		return (firebaseUser != null) ? firebaseUser.getUid() : null;
	}

	/**
	 * Signs in the user (authentication only).
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return The id of the authenticated user.
	 */
	private LiveData<DataTask<String>> signInAuthenticationOnly(String email, String password) {
		MutableLiveData<DataTask<String>> userIdLiveData = new MutableLiveData<>();

		// Sign in the user.
		firebaseAuth
			.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(
				task -> {
					// Sign in succeeded.
					if (task.isSuccessful()) {
						// Get the user id.
						String userId = getAuthenticationId();

						// Success task.
						DataTask<String> dataTask = DataTask.success(userId);
						userIdLiveData.setValue(dataTask);
					}

					// Sign in failed.
					else {
						// Error task.
						DataTask<String> dataTask = DataTask.error(task.getException());
						userIdLiveData.setValue(dataTask);
					}
				}
			);

		return userIdLiveData;
	}

	/**
	 * Creates a user (authentication only).
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return The id of the created user.
	 */
	private LiveData<DataTask<String>> createUserAuthenticationOnly(String email, String password) {
		MutableLiveData<DataTask<String>> userIdLiveData = new MutableLiveData<>();

		// Create the user.
		firebaseAuth
			.createUserWithEmailAndPassword(email, password)
			.addOnCompleteListener(
				task -> {
					// User created.
					if (task.isSuccessful()) {
						// Get the user id.
						String userId = getAuthenticationId();

						// Success task.
						DataTask<String> dataTask = DataTask.success(userId);
						userIdLiveData.setValue(dataTask);
					} else {
						// Error task.
						DataTask<String> dataTask = DataTask.error(task.getException());
						userIdLiveData.setValue(dataTask);
					}
				}
			);

		return userIdLiveData;
	}

	/**
	 * Updates the email of the authenticated user.
	 * @param email The email of the user.
	 * @return Whether the email has been updated, or not.
	 */
	private LiveData<DataTask<Void>> updateEmail(String email) {
		MutableLiveData<DataTask<Void>> emailUpdatedLiveData = new MutableLiveData<>();

		// User authenticated.
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			// Update the email.
			firebaseUser
				.updateEmail(email)
				.addOnCompleteListener(
					task -> {
						// Email updated.
						if (task.isSuccessful()) {
							// Success task.
							DataTask<Void> dataTask = DataTask.success();
							emailUpdatedLiveData.setValue(dataTask);
						}

						// Email not updated.
						else {
							// Error task.
							DataTask<Void> dataTask = DataTask.error(task.getException());
							emailUpdatedLiveData.setValue(dataTask);
						}
					}
				);
		}

		// User not authenticated.
		else {
			// Error task.
			DataTask<Void> dataTask = DataTask.error(new UserNotAuthenticatedException());
			emailUpdatedLiveData.setValue(dataTask);
		}

		return emailUpdatedLiveData;
	}

	/**
	 * Updates the password of the authenticated user.
	 * @param newPassword The new password of the user.
	 * @return Whether the password has been updated, or not.
	 */
	public LiveData<DataTask<Void>> updatePassword(String newPassword) {
		MutableLiveData<DataTask<Void>> passwordUpdatedLiveData = new MutableLiveData<>();

		// User authenticated.
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			// Update the password.
			firebaseUser
				.updatePassword(newPassword)
				.addOnCompleteListener(
					task -> {
						// Password updated.
						if (task.isSuccessful()) {
							// Success task.
							DataTask<Void> dataTask = DataTask.success();
							passwordUpdatedLiveData.setValue(dataTask);
						}

						// Password not updated.
						else {
							// Error task.
							DataTask<Void> dataTask = DataTask.error(task.getException());
							passwordUpdatedLiveData.setValue(dataTask);
						}
					}
				);
		}

		// User not authenticated.
		else {
			// Error task.
			DataTask<Void> dataTask = DataTask.error(new UserNotAuthenticatedException());
			passwordUpdatedLiveData.setValue(dataTask);
		}

		return passwordUpdatedLiveData;
	}

	/**
	 * Deletes the authenticated user.
	 * @return Whether the user has been deleted, or not.
	 */
	private LiveData<DataTask<Void>> deleteUserAuthenticationOnly() {
		MutableLiveData<DataTask<Void>> deletedUserLiveData = new MutableLiveData<>();

		// User authenticated.
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			// Update the password.
			firebaseUser
				.delete()
				.addOnCompleteListener(
					task -> {
						// User deleted.
						if (task.isSuccessful()) {
							// Success task.
							DataTask<Void> dataTask = DataTask.success();
							deletedUserLiveData.setValue(dataTask);
						}

						// User not deleted.
						else {
							// Error task.
							DataTask<Void> dataTask = DataTask.error(task.getException());
							deletedUserLiveData.setValue(dataTask);
						}
					}
				);

		}

		// User not authenticated.
		else {
			// Error task.
			DataTask<Void> dataTask = DataTask.error(new UserNotAuthenticatedException());
			deletedUserLiveData.setValue(dataTask);
		}

		return deletedUserLiveData;
	}

	/*====================================================================*/
	/*                            DATABASE ONLY                           */
	/*====================================================================*/

	/**
	 * Returns a user by id (updated in real time).
	 * @param id The id of the user.
	 * @return The selected user (updated in real time).
	 */
	public LiveData<DataTask<User>> getUser(String id) {
		MutableLiveData<DataTask<User>> userLiveData = new MutableLiveData<>();

		// Get the user (in real time).
		firebaseFirestore
			.collection("users")
			.document(id)
			.addSnapshotListener(
				(snapshot, error) -> {
					// Query succeeded.
					if (error == null) {
						// User found.
						if (snapshot != null && snapshot.exists()) {
							User user = snapshot.toObject(User.class);
							if (user != null) {
								// Update the user id.
								user.setId(id);

								// Success task.
								DataTask<User> dataTask = DataTask.success(user);
								userLiveData.setValue(dataTask);
							}
						}

						// User not found.
						else {
							// Error task.
							DataTask<User> dataTask = DataTask.error(new DocumentNotFoundException());
							userLiveData.setValue(dataTask);
						}
					}

					// Query failed.
					else {
						// Error task.
						DataTask<User> dataTask = DataTask.error(error);
						userLiveData.setValue(dataTask);
					}
				}
			);

		return userLiveData;
	}

	/**
	 * Creates or updates a user (database only).
	 * @param user The user to be created.
	 * @return The created user.
	 */
	private LiveData<DataTask<User>> insertUserDatabaseOnly(User user) {
		MutableLiveData<DataTask<User>> createdUserLiveData = new MutableLiveData<>();

		// Insert the user.
		firebaseFirestore
			.collection("users")
			.document(user.getId())
			.set(user)
			.addOnCompleteListener(
				task -> {
					// User inserted.
					if (task.isSuccessful()) {
						// Success task.
						DataTask<User> dataTask = DataTask.success(user);
						createdUserLiveData.setValue(dataTask);
					}

					// User not inserted.
					else {
						// Error task.
						DataTask<User> dataTask = DataTask.error(task.getException());
						createdUserLiveData.setValue(dataTask);
					}
				}
			);

		return createdUserLiveData;
	}

	/**
	 * Deletes the authenticated user (database only).
	 * @param id The id of the user.
	 * @return Whether the user has been deleted, or not.
	 */
	private LiveData<DataTask<Void>> deleteUserDatabaseOnly(String id) {
		MutableLiveData<DataTask<Void>> userDeletedLiveData = new MutableLiveData<>();

		// Delete the user.
		firebaseFirestore
			.collection("users")
			.document(id)
			.delete()
			.addOnCompleteListener(
				task -> {
					// User deleted.
					if (task.isSuccessful()) {
						// Success task.
						DataTask<Void> dataTask = DataTask.success();
						userDeletedLiveData.setValue(dataTask);
					}

					// User not deleted.
					else {
						// Error task.
						DataTask<Void> dataTask = DataTask.error(task.getException());
						userDeletedLiveData.setValue(dataTask);
					}
				}
			);

		return userDeletedLiveData;
	}

	/*====================================================================*/
	/*                   BOTH AUTHENTICATION AND DATABASE                 */
	/*====================================================================*/


	/**
	 * Returns the authenticated user.
	 * @return The authenticated user.
	 */
	@Override
	public LiveData<DataTask<User>> getUser() {
		// User authenticated.
		String userId = getAuthenticationId();
		if (userId != null) {
			return getUser(userId);
		}

		// User not authenticated.
		DataTask<User> dataTask = DataTask.error(new UserNotAuthenticatedException());
		return new MutableLiveData<>(dataTask);
	}

	/**
	 * Signs in the user.
	 * @param email The email of the user.
	 * @param password The password of the user.
	 * @return The authenticated user (updated in real time).
	 */
	@Override
	public LiveData<DataTask<User>> signIn(String email, String password) {
		return Transformations.switchMap(
			// Sign in the user.
			signInAuthenticationOnly(email, password),
			signInUser -> {
				// User authenticated.
				if (signInUser.isSuccessful()) {
					// Get the user.
					String userId = signInUser.getData();
					return getUser(userId);
				}

				// User not authenticated.
				DataTask<User> dataTask = DataTask.error(signInUser.getError());
				return new MutableLiveData<>(dataTask);
			}
		);
	}

	/**
	 * Signs out the user.
	 */
	@Override
	public void signOut() {
		firebaseAuth.signOut();
	}

	/**
	 * Creates a user.
	 * @param user The user to be created.
	 * @return The created user.
	 */
	@Override
	public LiveData<DataTask<User>> createUser(User user, String password) {
		return Transformations.switchMap(
			// Create the authentication user.
			createUserAuthenticationOnly(user.getEmail(), password),
			createUser -> {
				// User created.
				if (createUser.isSuccessful()) {
					// Create the database user.
					String userId = createUser.getData();
					user.setId(userId);
					return insertUserDatabaseOnly(user);
				}

				// User not created.
				DataTask<User> dataTask = DataTask.error(createUser.getError());
				return new MutableLiveData<>(dataTask);
			}
		);
	}

	/**
	 * Returns whether the email of the user has changed, or not.
	 * @param user The user to be checked.
	 * @return Whether the email of the user has changed, or not.
	 */
	private boolean emailChanged(User user) {
		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if (firebaseUser != null) {
			// User authenticated.
			String authEmail = firebaseUser.getEmail();
			String userEmail = user.getEmail();
			return (authEmail != null && !authEmail.equals(userEmail));
		}
		return false;
	}

	/**
	 * Updates the authenticated user.
	 * @param user The user to be updated.
	 * @return The updated user.
	 */
	@Override
	public LiveData<DataTask<User>> updateUser(User user) {
		if (emailChanged(user)) {
			return Transformations.switchMap(
				// Update the authentication email.
				updateEmail(user.getEmail()),
				updateEmail -> {
					// Email updated.
					if (updateEmail.isSuccessful()) {
						// Update the database user.
						return insertUserDatabaseOnly(user);
					}

					// Email not updated.
					DataTask<User> dataTask = DataTask.error(updateEmail.getError());
					return new MutableLiveData<>(dataTask);
				}
			);
		} else {
			// Update the database user.
			return insertUserDatabaseOnly(user);
		}
	}

	/**
	 * Deletes the authenticated user.
	 * @return The deleted user.
	 */
	@Override
	public LiveData<DataTask<Void>> deleteUser() {
		// Get the id of the authenticated user.
		String userId = getAuthenticationId();

		return Transformations.switchMap(
			// Delete the authentication user.
			deleteUserAuthenticationOnly(),
			deleteUser -> {
				// User deleted.
				if (deleteUser.isSuccessful()) {
					// Delete the database user.
					return deleteUserDatabaseOnly(userId);
				}

				// User not deleted.
				return new MutableLiveData<>(deleteUser);
			}
		);
	}
}
