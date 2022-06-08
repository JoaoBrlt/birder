package pns.si3.ihm.birder.views.account;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import etudes.fr.demoosm.R;

import pns.si3.ihm.birder.viewmodels.UserViewModel;

public class PasswordActivity extends AppCompatActivity {
	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "ParametersActivity";

    /**
     * The authentication view model.
     */
    private UserViewModel userViewModel;

    /**
     * The activity fields.
     */
    private EditText editPassword;
    private EditText editConfirmPassword;

	/**
	 * The activity buttons.
	 */
	private Button buttonReturn;
    private Button buttonChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
		initViewModel();
		initFields();
		initButtons();
    }

	/**
	 * Initializes the activity fields.
	 */
	private void initFields() {
		editPassword = findViewById(R.id.editText_parameters_mdp);
		editConfirmPassword = findViewById(R.id.editText_parameters_confirm_mdp);
	}

	/**
	 * Initializes the activity buttons.
	 */
	private void initButtons(){
        // Return button.
        buttonReturn = findViewById(R.id.button_param);
        buttonReturn.setOnClickListener(v -> {
			finish();
		});

        // Submit button.
		buttonChangePassword = findViewById(R.id.button_parameters_confirm);
		buttonChangePassword.setOnClickListener(v -> {
			submit();
		});
    }

    /**
     * Initializes the authentication view model that holds the data.
     */
    private void initViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

	/**
	 * Submit the password change, if the form is valid.
	 */
	private void submit() {
		if (isFormValid()) {
			updatePassword();
		}
	}

    /**
     * Checks if the form is valid.
     * @return Whether the form is valid, or not.
     */
    private boolean isFormValid() {
    	// Get the values.
		String password = editPassword.getText().toString();
		String confirmPassword = editConfirmPassword.getText().toString();

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
	 * Updates the user password.
	 */
	private void updatePassword() {
		// Get the password.
		String password = editPassword.getText().toString();

		// Update the password.
		userViewModel
			.updatePassword(password)
			.observe(
				this,
				task -> {
					// Password updated.
					if (task.isSuccessful()) {
						// Reset password.
						editPassword.setText("");
						editConfirmPassword.setText("");

						// Success toast.
						Toast.makeText(
							this,
							"Votre mot de passe a bien été modifié.",
							Toast.LENGTH_SHORT
						).show();

						// Close the activity.
						finish();
					}

					// Password not updated.
					else {
						// Error toast.
						Toast.makeText(
							this,
							"Le mot de passe n'a pas pu être modifié !",
							Toast.LENGTH_SHORT
						).show();

						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
	}
}
