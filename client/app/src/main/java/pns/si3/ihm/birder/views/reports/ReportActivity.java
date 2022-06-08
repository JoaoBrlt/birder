package pns.si3.ihm.birder.views.reports;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.models.User;
import pns.si3.ihm.birder.viewmodels.ReportViewModel;
import pns.si3.ihm.birder.viewmodels.SpeciesViewModel;
import pns.si3.ihm.birder.viewmodels.UserViewModel;
import pns.si3.ihm.birder.views.species.ChoiceSpeciesActivity;
import pns.si3.ihm.birder.views.notifications.NotificationApp;
import pns.si3.ihm.birder.views.pictures.PictureActivity;
import pns.si3.ihm.birder.views.position.GpsActivity;

import static pns.si3.ihm.birder.views.position.IGPSActivity.REQUEST_CODE;
import static pns.si3.ihm.birder.views.notifications.NotificationApp.CHANNEL_ID;

/**
 * Report activity.
 *
 * Allows the user to create bird reports.
 */
public class ReportActivity
		extends AppCompatActivity
		implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener
{
	public static final int REQUEST_PICTURE = 1;
	public static final int REQUEST_POSITION = 2;
	public static final int REQUEST_SPECIES = 3;
	private int notificationId = 0;
	private  String reportId;

	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "ReportActivity";

	/**
	 * The report view model.
	 */
	private ReportViewModel reportViewModel;

    /**
     * The user view model.
     */
    private UserViewModel userViewModel;

	/**
	 * The activity fields.
	 */
    ImageView picture;
	EditText editNumber;
	EditText editDate;
    EditText editTime;
	EditText editLocation;
	EditText editAge;
	Spinner spinnerGender;
	TextView textAge;
	TextView textGender;
	TextView textViewSpecies;
	String gender;

	/**
	 * The date and time values.
	 */
	int selectedDay;
	int selectedMonth;
	int selectedYear;
	int selectedHour;
	int selectedMinute;

	/**
	 * Fields needed to get the current user's position
	 */
	private Location userLocation;
	private LocationManager locationManager = null;

	/**
	 * The image values.
	 */
	Uri pictureUri;
	boolean pictureCreated;

	/**
	 * The activity buttons.
	 */
	Button returnButton;
	Button editImageButton;
	Button deleteImageButton;
	TextView currentLocationButton;
	TextView chooseLocationButton;
	Button submitButton;
	ImageView searchImage;
	ImageView cancelImage;
	Switch switchAdvanced;

	/**
	 * Boolean to know if notification has to be send.
	 */
	private Boolean notificationActivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
		initViewModels();
		initFields();
		initValues();
		initButtons();
		setSpinner();
    }

	@Override
	public void onStart() {
		super.onStart();
		// The user is not authenticated.
		if (!userViewModel.isAuthenticated()) {
			finish();
		}
	}

	/**
	 * Initializes the view models that hold the data.
	 */
	private void initViewModels() {
		reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
		userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
	}

	/**
	 * Initializes the field values.
	 */
	private void initValues() {
		// Date and time values.
		Calendar calendar = Calendar.getInstance();
		selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
		selectedMonth = calendar.get(Calendar.MONTH);
		selectedYear = calendar.get(Calendar.YEAR);
		selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
		selectedMinute = calendar.get(Calendar.MINUTE);
		updateDateField();
		updateTimeField();

		// Picture values.
		pictureUri = null;
		pictureCreated = false;
	}

	/**
	 * Initializes the activity fields.
	 */
    private void initFields() {
    	// Get the fields.
		picture = findViewById(R.id.image_picture);
		textViewSpecies = findViewById(R.id.text_speciesName);
		editNumber = findViewById(R.id.edit_number);
		editLocation = findViewById(R.id.edit_location);
		editAge = findViewById(R.id.edit_age);
		spinnerGender = findViewById(R.id.spinner_gender);
		textAge = findViewById(R.id.text_age);
		textGender = findViewById(R.id.text_gender);

		// Edit date field.
		editDate = findViewById(R.id.edit_date);
		editDate.setOnClickListener(
			v -> {
				DatePickerDialog datePickerDialog = createDatePickerDialog();
				datePickerDialog.show();
			}
		);

		// Edit time field.
		editTime = findViewById(R.id.edit_time);
		editTime.setOnClickListener(
			v -> {
				TimePickerDialog timePickerDialog = createTimePickerDialog();
				timePickerDialog.show();
			}
		);
	}

	/**
	 * Initializes the activity buttons.
	 */
	private void initButtons() {
		// Return button.
		returnButton = findViewById(R.id.button_return);
		returnButton.setOnClickListener(v -> {
			deleteCreatedPicture();
			finish();
		});

		// Edit image button.
		editImageButton = findViewById(R.id.edit_image_button);
		editImageButton.setOnClickListener(v -> {
			Intent intent = new Intent(ReportActivity.this, PictureActivity.class);
			startActivityForResult(intent, REQUEST_PICTURE);
		});

		// Delete image button.
		deleteImageButton = findViewById(R.id.delete_image_button);
		deleteImageButton.setOnClickListener(v -> {
			resetPicture();
		});

		// Current location button.
		currentLocationButton = findViewById(R.id.text_current_location);
		currentLocationButton.setOnClickListener(v -> {
			// Check if permission already granted
			boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
			if (!permissionGranted)
				ActivityCompat.requestPermissions(ReportActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
			else {
				setLocation();
				editLocation.setText(GpsActivity.getPlaceName(userLocation, this));
			}
		});

		// Choose location button.
		chooseLocationButton = findViewById(R.id.text_choose_location);
		chooseLocationButton.setOnClickListener(v -> {
			Intent intent = new Intent(ReportActivity.this, GpsActivity.class);
			startActivityForResult(intent, REQUEST_POSITION);
		});

		// Submit button.
		submitButton = findViewById(R.id.button_submit);
		submitButton.setOnClickListener(v -> {
			submit();
		});

		// Search button.
		searchImage = findViewById(R.id.imageView_report_search);
		searchImage.setOnClickListener(v -> {
			search();
		});

        cancelImage = findViewById(R.id.imageView_report_cancel);
        cancelImage.setVisibility(ImageView.INVISIBLE);
        cancelImage.setOnClickListener(v -> {
			cancelImage.setVisibility(ImageView.INVISIBLE);
			textViewSpecies.setText("Inconnue");
		});

        switchAdvanced = findViewById(R.id.switch_advanced);
        setFieldsOfAdvancedMode(View.GONE);
        switchAdvanced.setOnClickListener(v -> {
			if(switchAdvanced.isChecked()){
				setFieldsOfAdvancedMode(View.VISIBLE);
			}
			else{
				setFieldsOfAdvancedMode(View.GONE);
				editAge.setText("");
				gender = null;
			}
		});
	}

	private void setFieldsOfAdvancedMode(int visibility){
		spinnerGender.setVisibility(visibility);
		textGender.setVisibility(visibility);
		editAge.setVisibility(visibility);
		textAge.setVisibility(visibility);
	}

	/**
	 * Initializes the attribute userLocation
	 */
	private void setLocation() {
		// Check if permission already granted
		boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if (!permissionGranted)
			ActivityCompat.requestPermissions(ReportActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

		String fournisseur = null;
		if (locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Criteria criteres = new Criteria();
			// la précision  : (ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision)
			criteres.setAccuracy(Criteria.ACCURACY_COARSE);
			// l'altitude
			criteres.setAltitudeRequired(false);
			// la direction
			criteres.setBearingRequired(false);
			// la vitesse
			criteres.setSpeedRequired(false);
			// un potentiel coût
			criteres.setCostAllowed(false);
			// la consommation d'énergie demandée
			criteres.setPowerRequirement(Criteria.POWER_MEDIUM);

			fournisseur = locationManager.getBestProvider(criteres, true);
		}

		if (fournisseur != null) {
            LocationListener locationListener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					userLocation = location;
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {

				}

				@Override
				public void onProviderEnabled(String provider) {

				}

				@Override
				public void onProviderDisabled(String provider) {

				}
			};

			// On configure la mise à jour automatique : immédiate et en permanence
			locationManager.requestLocationUpdates(fournisseur, 0, 0, locationListener);
			do {
				userLocation = locationManager.getLastKnownLocation(fournisseur);
			} while (userLocation == null);

			locationListener.onLocationChanged(userLocation);

			if (locationManager != null) {
				locationManager.removeUpdates(locationListener);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// Check if permission already granted
		boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if (!permissionGranted)
			ActivityCompat.requestPermissions(ReportActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
		else
			setLocation();
	}

	/**
	 * Submits the report, if the form is valid.
	 */
	public void submit() {
    	if (isFormValid()) {
    		createReport();
		}
	}

	/**
	 * Search a species.
	 */
	public void search(){
		startActivityForResult(new Intent(ReportActivity.this, ChoiceSpeciesActivity.class), REQUEST_SPECIES);
	}

	/**
	 * Creates the report.
	 */
    public void createReport() {
		// Initialize the report.
		Report report = getReport();

		// Create the report.
		reportViewModel
			.createReport(report)
			.observe(
				this,
				task -> {
					// Report created.
					if (task.isSuccessful()) {
						// Get the created report.
						Report createdReport = task.getData();

						// Success toast.
						Toast.makeText(
							ReportActivity.this,
							"Votre signalement a été envoyé.",
							Toast.LENGTH_SHORT
						).show();

						// Get the user.
						userViewModel
							.getUser(createdReport.getUserId())
							.observe(
								this,
								secondTask -> {
									// User found.
									if (secondTask.isSuccessful()) {
										// Get the user.
										User user = secondTask.getData();

										// Send Notification
										setNotificationActivated(createdReport.getSpecies());

										// Add data to user
										boolean added = false;
										for (String reportId : user.getIdOfReports()){
											if (createdReport.getId().equals(reportId)) {
												added = true;
												break;
											}
										}

										if (!added) {
											ArrayList<String> newList = user.getIdOfReports();
											newList.add(createdReport.getId());
											user.setIdOfReports(newList);

											// Update number of pictures shared.
											if(createdReport.getPicturePath() != null){
												int sharedPicture = user.getNumberPictureShared();
												user.setNumberPictureShared(sharedPicture + 1);
											}

											// Update number of birds reported.
											int bird = user.getNumberOfBirdShared();
											user.setNumberOfBirdShared(bird + createdReport.getNumber());
										}

										// Update the user.
										userViewModel.updateUser(user)
											.observe(this,
												thirdTask -> {
													// User updated
													if (thirdTask.isSuccessful()){
														Log.i(TAG, "User nb photo = " + user.getNumberPictureShared());
														Log.i(TAG, "User nb oiseaux = " + user.getNumberOfBirdShared());

														// Close the activity.
														finish();
													}

													// User not updated.
													else {
														// Error logs.
														Throwable thirdError = thirdTask.getError();
														Log.e(TAG, thirdError.getMessage());
													}
												});
									}

									// User not found.
									else {
										// Error logs.
										Throwable secondError = secondTask.getError();
										Log.e(TAG, secondError.getMessage());
									}
								}
							);

					}

					// Report not created.
					else {
						// Error logs.
						Throwable error = task.getError();
						Log.e(TAG, error.getMessage());
					}
				}
			);
	}

	/**
	 * Returns the report created from the field values.
	 * @return The report created from the field values.
	 */
	private Report getReport() {
		// Get the values.
		String species = textViewSpecies.getText().toString();
		int number = Integer.parseInt(editNumber.getText().toString());
		String userId = userViewModel.getAuthenticationId();
		Date date = getSelectedDate();
		String age = editAge.getText().toString();
		Double latitude = userLocation.getLatitude();
		Double longitude = userLocation.getLongitude();

		// Init the report.
		return new Report(
			null,
			userId,
			species,
			number,
			pictureUri,
			date,
			gender,
			age,
			latitude,
			longitude
		);
	}

	/**
	 * Checks if the form is valid.
	 * @return Whether the form is valid, or not.
	 */
	private boolean isFormValid() {
		// Get values.
		String species = textViewSpecies.getText().toString();
		String number = editNumber.getText().toString();
		String age = editAge.getText().toString();

		// Species is empty.
		if (species.equals("Inconnue")) {
			if(picture.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.gallery).getConstantState()) {
				Toast.makeText(this, "Veuillez choisir une espèce si vous ne renseignez pas d'image.", Toast.LENGTH_SHORT).show();
				return false;
			}
		}

		// Number is empty.
		if (number.isEmpty()) {
			editNumber.setError("Veuillez saisir un nombre d'individus.");
			editNumber.requestFocus();
			return false;
		}

		// Number is invalid.
		if (!isNumeric(number)) {
			editNumber.setError("Veuillez saisir un nombre valide d'individus.");
			editNumber.requestFocus();
			return false;
		}

		// Age is invalid.
		if (!age.isEmpty()) {
            if (!isNumeric(age)) {
                editAge.setError("Veuillez saisir un nombre valide pour l'âge.");
                editAge.requestFocus();
                return false;
            }
		}

		// Location is invalid.
		if (userLocation == null) {
			editLocation.setError("Veuillez préciser une position.");
			editLocation.requestFocus();
			return false;
		}

		return true;
	}

	/**
	 * Checks if a string is numeric.
	 * @param string The string to be checked.
	 * @return Whether the string is numeric, or not.
	 */
	public static boolean isNumeric(String string) {
		return string.matches("\\d+");
	}

	/**
	 * Deletes a picture file by URI.
	 */
	public void deletePictureFile(Uri uri) {
		ContentResolver contentResolver = getContentResolver();
		contentResolver.delete(uri, null, null);
	}

	/**
	 * Deletes the picture if it has been created.
	 */
	private void deleteCreatedPicture() {
		if (pictureUri != null && pictureCreated) {
			deletePictureFile(pictureUri);
		}
	}

	/**
	 * Sets the picture.
	 * @param uri The new picture URI.
	 * @param created Whether the image has been created, or not.
	 */
	private void setPicture(Uri uri, boolean created) {
		pictureUri = uri;
		pictureCreated = created;
		picture.setImageURI(pictureUri);
	}

	/**
	 * Resets the picture.
	 */
	private void resetPicture() {
		// Delete the selected picture if created.
		deleteCreatedPicture();

		// Reset attributes.
		pictureUri = null;
		pictureCreated = false;
		picture.setImageResource(R.drawable.gallery);
	}

	/**
	 * Method triggered when the activity receives a result.
	 * @param requestCode The request code.
	 * @param resultCode The result code.
	 * @param data The result data.
	 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("GPS", "onActivityResult");
        if (requestCode == REQUEST_PICTURE && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            if (bundle != null) {
				// Reset the picture.
				resetPicture();

				// Set the new picture.
				setPicture(
					(Uri) bundle.get("pictureURI"),
					(boolean) bundle.get("pictureCreated")
				);
			}
        } else if (requestCode == REQUEST_POSITION && resultCode == RESULT_OK) {
        	Bundle bundle = data.getExtras();
        	if (bundle != null) {
        		Location location = (Location) bundle.get("location");
        		userLocation = location;
        		editLocation.setText(GpsActivity.getPlaceName(location, this));
			}
		} else if (requestCode == REQUEST_SPECIES && resultCode == RESULT_OK){
        	Bundle bundle = data.getExtras();
        	if(bundle != null){
				String speciesChoosed = (String) bundle.get("name");
                cancelImage.setVisibility(ImageView.VISIBLE);
				textViewSpecies.setText(speciesChoosed);
			}
		}
    }

	/*====================================================================*/
	/*                            DATE AND TIME                           */
	/*====================================================================*/

	/**
	 * Creates a date picker dialog.
	 * @return A date picker dialog.
	 */
    private DatePickerDialog createDatePickerDialog() {
    	// Create date picker dialog.
		DatePickerDialog datePickerDialog = new DatePickerDialog(
			this,
			this,
			selectedYear,
			selectedMonth,
			selectedDay
		);

		// Get date interval.
		Date minDate = getMinDate();
		Date now = new Date();

		// Set date interval.
		DatePicker datePicker = datePickerDialog.getDatePicker();
		datePicker.setMinDate(minDate.getTime());
		datePicker.setMaxDate(now.getTime());

		return datePickerDialog;
	}

	/**
	 * Creates a time picker dialog.
	 * @return A time picker dialog.
	 */
	private TimePickerDialog createTimePickerDialog() {
		return new TimePickerDialog(
			this,
			this,
			selectedHour,
			selectedMinute,
			true
		);
	}

	/**
	 * Returns the date created from the field values.
	 * @return The date created from the field values.
	 */
	private Date getSelectedDate() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(
			selectedYear,
			selectedMonth,
			selectedDay,
			selectedHour,
			selectedMinute
		);
		return calendar.getTime();

	}

	/**
	 * Returns the minimal date a user can select.
	 * @return The minimal date a user can select.
	 */
	private Date getMinDate() {
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.MONTH, -2);
		return calendar.getTime();
	}

	/**
	 * Updates the date field with the current date values.
	 */
	private void updateDateField() {
		editDate.setText(
			String.format(
				Locale.getDefault(),
				"%02d/%02d/%02d",
				selectedDay,
				selectedMonth + 1,
				selectedYear
			)
		);
	}

	/**
	 * Updates the time field with the current time values.
	 */
	private void updateTimeField() {
		editTime.setText(
			String.format(
				Locale.getDefault(),
				"%02d:%02d",
				selectedHour,
				selectedMinute
			)
		);
	}

	/**
	 * Method triggered when the user selects a date on the date picker dialog.
	 * @param view The current view.
	 * @param year The selected year.
	 * @param month The selected month (starting at zero).
	 * @param dayOfMonth The selected day of the month.
	 */
	@Override
	public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
		selectedDay = dayOfMonth;
		selectedMonth = month;
		selectedYear = year;
		updateDateField();
	}

	/**
	 * Method triggered when the user selects a time on the time picker dialog.
	 * @param view The current view.
	 * @param hourOfDay The selected hour.
	 * @param minute The selected minute.
	 */
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		selectedHour = hourOfDay;
		selectedMinute = minute;
		updateTimeField();
	}

	/*====================================================================*/
	/*                            NOTIFICATIONS                           */
	/*====================================================================*/

    public void sendNotification( String nameBird){
        Bitmap bitmap = null;
        try {
            if(android.os.Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), pictureUri);
                bitmap = ImageDecoder.decodeBitmap(source);
                }
        }
        catch (Exception e) {
            bitmap = null;
        }
        if(nameBird.equals("Inconnue")) nameBird = "Un oiseau vient d'être signalé !";
        else nameBird = "L'oiseau " + nameBird.toLowerCase() +" vient d\'être signalé !";
		NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
				.setSmallIcon(R.drawable.bird)
				.setLargeIcon(bitmap)
				.setContentTitle("Nouveau signalement")
				.setContentText(nameBird)
				.setTimeoutAfter(3600000)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		NotificationApp.getNotificationManager().notify(++notificationId, notification.build());
	}

	public void setNotificationActivated(String nameSpecies){
    	notificationActivate = false;
		userViewModel
			.getUser()
			.observe(
				this,
				task -> {
					// User found.
					if (task.isSuccessful()) {
						// Get the user.
						User user = task.getData();

						// Get the notifications.
						ArrayList<String> notifications = user.getSpeciesNotifications();
						if (notifications != null) {
							// User wants notification for this species.
							for (String name : user.getSpeciesNotifications()){
								if (name.equals(nameSpecies)){
									notificationActivate = true;
									break;
								}
							}

							// User wants all notifications.
							if (user.getAllNotificationActivate()) {
								notificationActivate = true;
							}

							// Send notification.
							if (notificationActivate){
								sendNotification(nameSpecies);
							}
						}
					}
				}
			);
	}

	/*====================================================================*/
	/*                               SPINNER                              */
	/*====================================================================*/

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch(position) {
			// Female
			case 0:
				gender = "Femelle";
				break;
			// Male
			case 1:
				gender = "Male";
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public void setSpinner(){
		final Spinner spinner = findViewById(R.id.spinner_gender);
		spinner.setAdapter(null);
		spinner.setOnItemSelectedListener(this);
		List<String> list = new ArrayList<>();
		list.add("Femelle");
		list.add("Male");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
	}

}
