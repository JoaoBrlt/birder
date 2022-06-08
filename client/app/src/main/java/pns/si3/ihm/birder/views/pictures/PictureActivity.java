package pns.si3.ihm.birder.views.pictures;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.viewmodels.UserViewModel;

/**
 * Picture activity.
 *
 * Allows the user to take a picture, save a picture or load a picture.
 */
public class PictureActivity extends AppCompatActivity {
	public static final int REQUEST_TAKE_PICTURE = 1;
	public static final int REQUEST_LOAD_PICTURE = 2;

	/**
	 * The tag for the log messages.
	 */
	private static final String TAG = "PictureActivity";

	/**
	 * The user view model.
	 */
	UserViewModel userViewModel;

	/**
	 * The activity values.
	 */
	private ImageView picture;
	private Uri pictureUri;
	private boolean pictureCreated;

	/**
	 * The activity buttons.
	 */
	private Button returnButton;
	private Button takePictureButton;
	private Button loadPictureButton;
	private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
		initViewModels();
		initValues();
        initButtons();
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
		userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
	}

	/**
	 * Initializes the activity values.
	 */
	private void initValues() {
		pictureUri = null;
		pictureCreated = false;
		picture = findViewById(R.id.image_picture);
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

		// Take picture button.
		takePictureButton = findViewById(R.id.button_take_picture);
		takePictureButton.setOnClickListener(v -> {
			// Permissions required to take a picture.
			String[] takePicturePermissions = {
				Manifest.permission.CAMERA,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			};

			if (hasPermissions(takePicturePermissions)) {
				takePicture();
			}else {
				ActivityCompat.requestPermissions(this, takePicturePermissions, REQUEST_TAKE_PICTURE);
			}
		});

		// Load picture.
		loadPictureButton = findViewById(R.id.button_load_picture);
		loadPictureButton.setOnClickListener(v -> {
			// Permissions required to load a picture.
			String[] loadPicturePermissions = {
				Manifest.permission.READ_EXTERNAL_STORAGE
			};

			if (hasPermissions(loadPicturePermissions)) {
				loadPicture();
			} else {
				ActivityCompat.requestPermissions(this, loadPicturePermissions, REQUEST_LOAD_PICTURE);
			}
		});

		// Submit button.
		submitButton = findViewById(R.id.button_submit);
		submitButton.setOnClickListener(v -> {
			if (pictureUri != null) {
				Intent intent = new Intent();
				intent.putExtra("pictureURI", pictureUri);
				intent.putExtra("pictureCreated", pictureCreated);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

	/*====================================================================*/
	/*                            PERMISSIONS                             */
	/*====================================================================*/

	/**
	 * Checks if all permissions of a list are granted.
	 * @param permissions The list of permissions to be checked.
	 * @return Whether all the permissions are granted or not.
	 */
	private boolean hasPermissions(String[] permissions) {
		for (String permission : permissions) {
			int grantResult = ContextCompat.checkSelfPermission(this, permission);
			if (grantResult != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method triggered when the user allows or denies a permission request.
	 * @param requestCode The request code.
	 * @param permissions The permission requests.
	 * @param grantResults The permissions results.
	 */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
        	// Take picture request.
            case REQUEST_TAKE_PICTURE: {
				// Granted.
                if (
                	grantResults.length >= 2 &&
					grantResults[0] == PackageManager.PERMISSION_GRANTED &&
					grantResults[1] == PackageManager.PERMISSION_GRANTED
				) {
                   takePicture();
                }

				// Not granted.
                else {
                   	Toast.makeText(
                   		this,
						"L'utilisation de la caméra n'est pas autorisée.",
						Toast.LENGTH_SHORT
					).show();
                }
				break;
            }
        	// Load picture request.
            case REQUEST_LOAD_PICTURE: {
				// Granted.
				if (
					grantResults.length >= 1 &&
					grantResults[0] == PackageManager.PERMISSION_GRANTED
				) {
					loadPicture();
				}

				// Not granted.
				else {
					Toast.makeText(
						this,
						"La lecture de la mémoire n'est pas autorisée.",
						Toast.LENGTH_SHORT
					).show();
                }
				break;
            }
        }
    }

	/*====================================================================*/
	/*                               PICTURE                              */
	/*====================================================================*/

	/**
	 * Creates a picture file.
	 * @return The created picture file.
	 * @throws IOException If the file creation failed.
	 */
	private File createPictureFile() throws IOException {
		// Get the picture folder.
		File storage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

		// Create a timestamp.
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
		String timestamp = dateFormat.format(now);
		String filename = "IMG_" + timestamp + "_";

		// Create the picture file.
		return File.createTempFile(
			filename,
			".jpg",
			storage
		);
	}

	/**
	 * Deletes a picture file by URI.
	 */
	public void deletePictureFile(Uri uri) {
		ContentResolver contentResolver = getContentResolver();
		contentResolver.delete(uri, null, null);
	}

	/**
	 * Deletes the selected picture if it has been created.
	 */
	private void deleteCreatedPicture() {
		if (pictureUri != null && pictureCreated) {
			deletePictureFile(pictureUri);
		}
	}

	/**
	 * Allows the user to take a picture.
	 */
	private void takePicture() {
		// Intent to take a picture.
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// An application to take a picture is available.
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			File pictureFile = null;
			try {
				// Create a picture file.
				pictureFile = createPictureFile();
			} catch (IOException e) {
				// File creation failed.
				Log.e(TAG, e.getMessage());
			}
			if (pictureFile != null) {
				// Get the picture URI.
				pictureUri = FileProvider.getUriForFile(
					this,
					"pns.si3.ihm.birder.fileprovider",
					pictureFile
				);

				// Start the intent to take a picture with the URI.
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
			}
		}
	}

	/**
	 * Make the user load a picture from the gallery.
	 */
	public void loadPicture() {
		// Intent to load a picture.
		Intent loadPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		if (loadPictureIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(loadPictureIntent, REQUEST_LOAD_PICTURE);
		}
	}

	/**
	 * Method triggered when the activity receives a result.
	 * @param requestCode The request code.
	 * @param resultCode The result code.
	 * @param data The result data.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			// Camera request.
			case REQUEST_TAKE_PICTURE: {
				switch (resultCode) {
					// Picture received.
					case RESULT_OK: {
						if (pictureUri != null) {
							pictureCreated = true;
							picture.setImageURI(pictureUri);
						}
						break;
					}
					// Picture canceled.
					case RESULT_CANCELED: {
						Toast.makeText(
							this,
							"Aucune photo n'a été prise.",
							Toast.LENGTH_SHORT
						).show();
						break;
					}
					// Picture failed.
					default: {
						Toast.makeText(
							this,
							"Une erreur est survenue.",
							Toast.LENGTH_SHORT
						).show();
						break;
					}
				}
				break;
			}
			// Pick image request.
			case REQUEST_LOAD_PICTURE: {
				switch (resultCode) {
					// Picture received.
					case RESULT_OK: {
						if (data != null) {
							Uri uri = data.getData();
							if (uri != null) {
								deleteCreatedPicture();
								pictureUri = uri;
								pictureCreated = false;
								picture.setImageURI(pictureUri);
							}
						}
						break;
					}
					// Picture canceled.
					case RESULT_CANCELED: {
						Toast.makeText(
							this,
							"Aucune photo n'a été sélectionnée.",
							Toast.LENGTH_SHORT
						).show();
						break;
					}
					// Picture failed.
					default: {
						Toast.makeText(
							this,
							"Une erreur est survenue.",
							Toast.LENGTH_SHORT
						).show();
						break;
					}
				}
				break;
			}
		}
	}
}
