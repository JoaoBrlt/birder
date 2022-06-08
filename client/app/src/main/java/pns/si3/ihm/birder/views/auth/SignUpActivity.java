package pns.si3.ihm.birder.views.auth;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.viewmodels.UserViewModel;

public class SignUpActivity extends AppCompatActivity {
	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "SignUpActivity";

	/**
	 * The user view model.
	 */
	private UserViewModel userViewModel;

	/**
	 * The activity fields.
	 */
	private EditText editFirstName;
	private EditText editLastName;
	private EditText editEmail;
	private EditText editPassword;
	private EditText editConfirmPassword;

	/**
	 * The activity buttons.
	 */
	private Button returnButton;
	private Button submitButton;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		initViewModel();
		initFields();
		initButtons();
	}

	@Override
	public void onStart() {
		super.onStart();
		// The user is already authenticated.
		if (userViewModel.isAuthenticated()) {
			finish();
		}
	}

	/**
	 * Initializes the authentication view model that holds the data.
	 */
	private void initViewModel() {
		userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
	}

	/**
	 * Initializes the activity fields.
	 */
	private void initFields() {
		editFirstName = findViewById(R.id.edit_first_name);
		editLastName = findViewById(R.id.edit_last_name);
		editEmail = findViewById(R.id.edit_email);
		editPassword = findViewById(R.id.edit_password);
		editConfirmPassword = findViewById(R.id.edit_confirm_password);
	}

	/**
	 * Initializes the activity buttons.
	 */
	private void initButtons() {
		// Return button.
		returnButton = findViewById(R.id.button_return);
		returnButton.setOnClickListener(v -> {
			finish();
		});

		// Submit button.
		submitButton = findViewById(R.id.button_submit);
		submitButton.setOnClickListener(v -> {
			signUp();
		});
	}

	/**
	 * Signs up the user.
	 */
	private void signUp() {
		if (isFormValid()) {
			createUserWithEmailAndPassword();
		}
	}

	/**
	 * Creates a user with an email and password.
	 */
	private void createUserWithEmailAndPassword() {
		// Get values.
		String firstName = editFirstName.getText().toString();
		String lastName = editLastName.getText().toString();
		String email = editEmail.getText().toString();
		String password = editPassword.getText().toString();

		// Initialize the user.
		User user = new User(
			firstName,
			lastName,
			email
		);

		// Create the user.
		userViewModel
			.createUser(user, password)
			.observe(
				this,
				task -> {
					// User created.
					if (task.isSuccessful()) {
						// Reset password.
						editPassword.setText("");
						editConfirmPassword.setText("");

						// Success toast.
						Toast.makeText(
							this,
							"Bonjour " + user.getFirstName() + " !",
							Toast.LENGTH_LONG
						).show();

						// Close the activity.
						finish();
					}

					// User not created.
					else {
						// Reset passwords.
						editPassword.setText("");
						editConfirmPassword.setText("");
						editPassword.requestFocus();

						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
	}

	/**
	 * Validates the form.
	 * @return Whether the form is valid, or not.
	 */
	private boolean isFormValid() {
		// Get form values.
		String firstName = editFirstName.getText().toString();
		String lastName = editLastName.getText().toString();
		String email = editEmail.getText().toString();
		String password = editPassword.getText().toString();
		String confirmPassword = editConfirmPassword.getText().toString();

		// First name is empty.
		if (firstName.isEmpty()) {
			editFirstName.setError("Veuillez saisir un prénom.");
			editFirstName.requestFocus();
			return false;
		}

		// Last name is empty.
		if (lastName.isEmpty()) {
			editLastName.setError("Veuillez saisir un nom.");
			editLastName.requestFocus();
			return false;
		}

		// Email is empty.
		if (email.isEmpty()) {
			editEmail.setError("Veuillez saisir une adresse email.");
			editEmail.requestFocus();
			return false;
		}

		// Email is invalid.
		if (!isEmailValid(email)) {
			editEmail.setError("Veuillez saisir une adresse email valide.");
			editEmail.requestFocus();
			return false;
		}

		// Password is empty.
		if (password.isEmpty()) {
			editPassword.setError("Veuillez saisir un mot de passe.");
			editConfirmPassword.setText("");
			editPassword.requestFocus();
			return false;
		}

		// Confirm password is empty.
		if (confirmPassword.isEmpty()) {
			editConfirmPassword.setError("Veuillez resaisir votre mot de passe.");
			editConfirmPassword.requestFocus();
			return false;
		}

		// Passwords don't match.
		if (!password.equals(confirmPassword)) {
			editConfirmPassword.setError("Les mots de passe ne correspondent pas.");
			editConfirmPassword.setText("");
			editConfirmPassword.requestFocus();
			return false;
		}

		// Password not long enough.
		if (password.length() < 6) {
			editPassword.setError("Votre mot de passe doit comporter au moins 6 caractères.");
			editPassword.setText("");
			editConfirmPassword.setText("");
			editPassword.requestFocus();
			return false;
		}

		return true;
	}

	/**
	 * Checks if an email is valid.
	 * @param email The email to be checked.
	 * @return Whether the email is valid, or not.
	 */
	boolean isEmailValid(String email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
}