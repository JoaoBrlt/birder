package pns.si3.ihm.birder.views.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.viewmodels.UserViewModel;

public class SignInActivity extends AppCompatActivity {
	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "SignInActivity";

	/**
	 * The user view model.
	 */
	private UserViewModel userViewModel;

	/**
	 * The activity fields.
	 */
	private EditText editEmail;
	private EditText editPassword;

	/**
	 * The activity buttons.
	 */
	private Button returnButton;
	private Button submitButton;
	private TextView signUpButton;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
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
		editEmail = findViewById(R.id.edit_email);
		editPassword = findViewById(R.id.edit_password);
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
			signIn();
		});

		// Sign up button.
		signUpButton = findViewById(R.id.text_sign_up);
		signUpButton.setOnClickListener(v -> {
			Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
			startActivity(intent);
		});
	}

	/**
	 * Signs in the user.
	 */
	private void signIn() {
		if (isFormValid()) {
			signInWithEmailAndPassword();
		}
	}

	/**
	 * Signs in the user with an email and password.
	 */
	private void signInWithEmailAndPassword() {
		// Get values.
		String email = editEmail.getText().toString();
		String password = editPassword.getText().toString();

		// Request the sign in.
		userViewModel
			.signIn(email, password)
			.observe(
				this,
				task -> {
					// Authentication succeeded.
					if (task.isSuccessful()) {
						// Reset password.
						editPassword.setText("");

						// Success toast.
						User user = task.getData();
						Toast.makeText(
							this,
							"Bonjour " + user.getFirstName() + " !",
							Toast.LENGTH_LONG
						).show();

						// Close the activity.
						finish();
					}

					// Authentication failed.
					else {
						// Reset password.
						editPassword.setText("");
						editPassword.requestFocus();

						// Error messages.
						editPassword.setError("Les identifiants de connexion sont incorrects.");

						// Error toast.
						Toast.makeText(
							SignInActivity.this,
							"La connexion a échouée.",
							Toast.LENGTH_SHORT
						).show();

						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
	}

	/**
	 * Checks if the form is valid.
	 * @return Whether the form is valid, or not.
	 */
	private boolean isFormValid() {
		// Get values.
		String email = editEmail.getText().toString();
		String password = editPassword.getText().toString();

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
