package pns.si3.ihm.birder.views.account;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.trophies.StatisticsActivity;
import pns.si3.ihm.birder.views.auth.SignInActivity;
import pns.si3.ihm.birder.views.notifications.NotificationActivity;
import pns.si3.ihm.birder.views.reports.MainActivity;
import pns.si3.ihm.birder.views.reports.MapActivity;
import pns.si3.ihm.birder.views.species.ChoiceSpeciesActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
	/**
	 * The user view model.
	 */
	private UserViewModel userViewModel;

	/**
	 * The activity buttons.
	 */
    private Button buttonNotification;
    private Button buttonStat;
    private Button buttonParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
		initViewModel();
		initButtons();
		setSpinner();
    }

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModel() {
    	userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
	}

	/**
	 * Initializes the activity buttons.
	 */
    private void initButtons() {
		buttonNotification = findViewById(R.id.button_gestion_notification);
		buttonNotification.setOnClickListener(v -> {
			Intent intent = new Intent(AccountActivity.this, NotificationActivity.class);
			startActivity(intent);
		});

		buttonStat = findViewById(R.id.button_stats);
		buttonStat.setOnClickListener(v -> {
			Intent intent = new Intent(AccountActivity.this, StatisticsActivity.class);
			startActivity(intent);
		});

		buttonParams = findViewById(R.id.button_parametre_compte);
		buttonParams.setOnClickListener(v -> {
			Intent intent = new Intent(AccountActivity.this, ParametersActivity.class);
			startActivity(intent);
		});
	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(position){
            case 0:break;
            case 1: // Liste signalisation
            {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
            }
            break;
            case 2://Liste des oiseaux
            {
                Intent intent = new Intent(AccountActivity.this, ChoiceSpeciesActivity.class);
                intent.putExtra("want", "allSpecies");
                startActivity(intent);
            }
            break;
            case 3: // Map
            {
                Intent intent = new Intent(AccountActivity.this, MapActivity.class);
                startActivity(intent);
            }break;
            case 4: //Compte (connecté) / Se connecter (déconnecté)
            {
                if (userViewModel.isAuthenticated()) {
                    // Signs out the user.
					userViewModel.signOut();

                    // Success toast.
                    Toast.makeText(
                            AccountActivity.this,
                            "Vous avez été déconnecté !",
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void setSpinner(){
        final Spinner spinner = findViewById(R.id.spinner_account);
        spinner.setAdapter(null);
        spinner.setOnItemSelectedListener(this);
        List<String> list = new ArrayList<>();
        list.add("Menu");
        list.add("Dernières signalisations");
        list.add("Liste des oiseaux");
        list.add("Voir Carte");
        // The user is connected.
        if (userViewModel.isAuthenticated()) {
            list.add("Se déconnecter");
        }
        // The user is not connected.
        else {
            list.add("Se connecter");
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }
}
