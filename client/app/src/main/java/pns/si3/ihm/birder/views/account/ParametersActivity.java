package pns.si3.ihm.birder.views.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.reports.MainActivity;

public class ParametersActivity extends AppCompatActivity {

    /**
     * The TAG of the activity.
     */
    private final String TAG = "Parameters";

    /**
     * The user view model.
     */
    private UserViewModel userViewModel;

    /**
     * The buttons of the activity.
     */
    private Button buttonChangePassword;
    private Button buttonDeleteAccount;
    private Button buttonReturn;

    /**
     * The fields of the activity.
     */
    private TextView textUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        initViewModels();
        initButtonsAndFields();
    }

    /**
     * Initializes the view models that hold the data.
     */
    private void initViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void initButtonsAndFields(){
        buttonChangePassword = findViewById(R.id.change_password);
        buttonDeleteAccount = findViewById(R.id.delete_account);
        buttonReturn = findViewById(R.id.buttonParamsRetour);
        textUser = findViewById(R.id.textUser);

        buttonDeleteAccount.setOnClickListener(v -> {
            dialogBox();
        });

        buttonChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(ParametersActivity.this, PasswordActivity.class));
        });

        buttonReturn.setOnClickListener(v -> {
            finish();
        });

        // Get the user.
        userViewModel
			.getUser()
        	.observe(
        		this,
				task -> {
        			// User found.
                    if (task.isSuccessful()) {
                    	// Get the user.
						User user = task.getData();
						String text = "Connecté, "+ user.getFirstName() + " " + user.getLastName();
                        textUser.setText(text);
                    }
        		});
    }

    private void dialogBox() {
        new AlertDialog.Builder(this)
                .setTitle("Suppression du compte")
                .setMessage("Voulez-vous vraiment supprimer votre compte ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Oui", (dialog, whichButton) -> delete())
                .setNegativeButton("Non", null).show();
    }

    private void delete(){
        deleteAccount();
    }

    private void deleteAccount(){
    	// Delete the user.
        userViewModel
			.deleteUser()
			.observe(
				this,
				task -> {
				if (task.isSuccessful()) {
					// Success toast.
					Toast.makeText(
						this,
						"Compte supprimé !",
						Toast.LENGTH_SHORT
					).show();
                    startActivity(new Intent(ParametersActivity.this, MainActivity.class).putExtra("userDeleted", "True"));
                }
                // Account not deleted.
                else {
                    // Error toast.
                    Toast.makeText(
                            this,
                            "Le compte n'a pas pu être supprimé !",
                            Toast.LENGTH_SHORT
                    ).show();
                    // Error logs.
                    Throwable error = task.getError();
                    Log.e(TAG,error.getMessage());
                }
                });
    }


}
