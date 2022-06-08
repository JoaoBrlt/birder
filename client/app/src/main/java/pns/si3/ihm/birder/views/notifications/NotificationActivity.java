package pns.si3.ihm.birder.views.notifications;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.viewmodels.SpeciesViewModel;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.account.AccountActivity;
import pns.si3.ihm.birder.views.species.ChoiceSpeciesActivity;
import pns.si3.ihm.birder.views.reports.MainActivity;
import pns.si3.ihm.birder.views.reports.MapActivity;
import pns.si3.ihm.birder.views.auth.SignInActivity;

public class NotificationActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {
	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "NotificationActivity";

    private ListView listView;
    private CheckBox checkBox;
    private ImageView imageView;
    private Boolean allNotification = true;
    private UserViewModel userViewModel;
    private SpeciesViewModel speciesViewModel;
    public static final int REQUEST_SPECIES = 1;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
		initViewModel();
        init();


        setListAndCheckBoxInInit();
        checkBox.setOnClickListener(v -> {
            setAllNotification(!getAllNotification());
            changeBooleanAllNotification();
            Log.i("Notif", "Notif bool = " + getAllNotification());
        });
        imageView.setOnClickListener(v -> startActivityForResult(new Intent(
                NotificationActivity.this, ChoiceSpeciesActivity.class), REQUEST_SPECIES));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPECIES && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if(bundle != null){
                String speciesChoosed = (String) bundle.get("name");
                addItemNotificationForUser(speciesChoosed);
                adapter.notifyDataSetChanged();
            }
        }
    }



    void init(){
        listView = findViewById(R.id.listview_notification);
        imageView = findViewById(R.id.imageview_add_notification);
        checkBox = findViewById(R.id.checkbox_all_notif);
        setSpinner();

        listView.setOnItemClickListener((parent, view, position, id) -> {
			if(!listView.getItemAtPosition(position).toString().isEmpty()){
				dialogBoxDelete(listView.getItemAtPosition(position).toString());
			}
		});
    }

    private void initViewModel(){
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        speciesViewModel = new ViewModelProvider(this).get(SpeciesViewModel.class);
    }


    private void setListAndCheckBoxInInit() {
        userViewModel
			.getUser()
			.observe(
				this,
				task -> {
					// User found.
					if (task.isSuccessful()) {
						User user = task.getData();
						setAllNotification(user.getAllNotificationActivate());
						Log.i("Notif", "User bool activate = " + user.getAllNotificationActivate());
						adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, user.getSpeciesNotifications());
						listView.setAdapter(adapter);
						checkBox.setChecked(user.getAllNotificationActivate());
					}
				}
			);
    }

    void changeBooleanAllNotification(){
        userViewModel
			.getUser()
			.observe(
				this,
				task -> {
					// User found.
					if (task.isSuccessful()) {
						// Get the user.
						User user = task.getData();

						// Update the user notifications.
						user.setAllNotificationActivate(getAllNotification());
                        userViewModel
							.updateUser(user)
							.observe(
								this,
								secondTask -> {
									// User updated.
									if (secondTask.isSuccessful()) {
										Log.i(TAG, "User updated.");
									}

									// User not updated.
									else {
										Throwable error = task.getError();
										Log.e(TAG, error.getMessage());
									}
								}
							);
					}

					// User not found.
					else {
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
    }

    private void addItemNotificationForUser(String speciesChoosed) {
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

						// Update the user (locally).
						ArrayList<String> notif = user.getSpeciesNotifications();
						if (!notif.contains(speciesChoosed)) {
							notif.add(speciesChoosed);
							user.setSpeciesNotifications(notif);

							// Update the user (online).
							userViewModel
								.updateUser(user)
								.observe(
									this,
									secondTask -> {
										// User updated.
										if (secondTask.isSuccessful()) {
											Log.i(TAG, "User updated!");
										}

										// User not updated.
										else {
											Throwable error = task.getError();
											Log.e(TAG, error.getMessage());
										}
									}
								);
						}
						else{
							Toast.makeText(this, "L'espèce choisi est déjà dans la liste.", Toast.LENGTH_SHORT).show();
						}
					}

					// User not found.
					else {
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(position){
            case 0:break;
            case 1: // Liste signalisation
            {
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
            break;
            case 2: // Map
            {
                Intent intent = new Intent(NotificationActivity.this, MapActivity.class);
                startActivity(intent);
            }break;
            case 3: //Compte (connecté) / Se connecter (déconnecté)
            {
                if (userViewModel.isAuthenticated()) {
                    Intent intent = new Intent(NotificationActivity.this,AccountActivity.class);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(NotificationActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }break;
            case 4:// Déconnexion (connecté)
            {
                // The user is connected.
                if (userViewModel.isAuthenticated()) {
                    // Sign out the user.
					userViewModel.signOut();

                    // Success toast.
                    Toast.makeText(
                            NotificationActivity.this,
                            "Vous avez été déconnecté !",
                            Toast.LENGTH_SHORT
                    ).show();
                    Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                // The user is not connected.
                else {
                    // Navigate to sign in.
                    Intent intent = new Intent(NotificationActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setSpinner(){
        final Spinner spinner = findViewById(R.id.spinner_notification);
        spinner.setAdapter(null);
        spinner.setOnItemSelectedListener(this);
        List<String> list = new ArrayList<>();
        list.add("Menu");
        list.add("Dernières signalisations");
        list.add("Voir Carte");
        // The user is connected.
        if (userViewModel.isAuthenticated()) {
            list.add("Compte");
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

    private void setAllNotification(Boolean value){
        this.allNotification = value;
    }

    private Boolean getAllNotification(){
        return allNotification;
    }


    void deleteBird(String speciesName){
        userViewModel
			.getUser()
			.observe(
				this,
				task -> {
					// User found.
					if (task.isSuccessful()) {
						// Get the user.
						User user = task.getData();

						// Update the user (locally).
						ArrayList<String> notif = user.getSpeciesNotifications();
						notif.remove(speciesName);
						user.setSpeciesNotifications(notif);

						// Update the user (online).
						userViewModel
							.updateUser(user)
							.observe(
								this,
								secondTask -> {
									// User updated.
									if (secondTask.isSuccessful()) {
										Log.i(TAG, "User updated!");
									}

									// User not updated.
									else {
										Throwable error = task.getError();
										Log.e(TAG, error.getMessage());
									}
								}
							);
					}

					// User not found.
					else {
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
        }

        private void dialogBoxDelete(String speciesName){
            new AlertDialog.Builder(this)
                    .setTitle("Suppression d'une notification")
                    .setMessage("Voulez-vous vraiment ne plus recevoir de notification pour l'espèce " + speciesName + " ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(NotificationActivity.this, "Notification pour l'oiseau " + speciesName + " est supprimée.", Toast.LENGTH_SHORT).show();
                            deleteBird(speciesName);
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }

}
