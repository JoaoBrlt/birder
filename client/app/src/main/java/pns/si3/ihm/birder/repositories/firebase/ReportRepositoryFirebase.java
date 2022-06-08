package pns.si3.ihm.birder.repositories.firebase;

import android.icu.text.SimpleDateFormat;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pns.si3.ihm.birder.exceptions.DocumentNotCreatedException;
import pns.si3.ihm.birder.exceptions.DocumentNotFoundException;
import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.repositories.interfaces.ReportRepository;

/**
 * Report repository using Firebase.
 *
 * Implementation of the report repository using Firebase.
 */
public class ReportRepositoryFirebase implements ReportRepository {
	public static final ReportRepositoryFirebase instance = new ReportRepositoryFirebase();

	/**
	 * The tag of the log messages.
	 */
	public static final String TAG = "ReportRepository";

	/**
	 * The firebase firestore instance.
	 */
	private FirebaseFirestore firebaseFirestore;

	/**
	 * The firebase storage instance.
	 */
	private FirebaseStorage firebaseStorage;

	/**
	 * Constructs a report repository.
	 */
	public ReportRepositoryFirebase() {
		firebaseFirestore = FirebaseFirestore.getInstance();
		firebaseStorage = FirebaseStorage.getInstance();
	}

	/**
	 * Returns the list of reports (updated in real time).
	 * @return The list of reports (updated in real time).
	 */
	public LiveData<DataTask<List<Report>>> getReports() {
		MutableLiveData<DataTask<List<Report>>> reportsLiveData = new MutableLiveData<>();

		// Get all reports (in real time).
		firebaseFirestore
			.collection("reports")
			.orderBy("date", Query.Direction.DESCENDING)
			.addSnapshotListener(
				(snapshots, error) -> {
					// Query succeeded.
					if (error == null) {
						// Reports found.
						if (snapshots != null) {
							// Get all reports.
							List<Report> reports = new ArrayList<>();
							for (QueryDocumentSnapshot snapshot : snapshots) {
								Report report = snapshot.toObject(Report.class);
								report.setId(snapshot.getId());
								reports.add(report);
							}

							// Success task.
							DataTask<List<Report>> dataTask = DataTask.success(reports);
							reportsLiveData.setValue(dataTask);
						}

						// Reports not found.
						else {
							// Error task.
							DataTask<List<Report>> dataTask = DataTask.error(new DocumentNotFoundException());
							reportsLiveData.setValue(dataTask);
						}
					}

					// Query failed.
					else {
						// Error task.
						DataTask<List<Report>> dataTask = DataTask.error(error);
						reportsLiveData.setValue(dataTask);
					}
				}
			);

		return reportsLiveData;
	}

	/**
	 * Returns a report (updated in real time).
	 * @param id The id of the report.
	 * @return The report (updated in real time).
	 */
	public LiveData<DataTask<Report>> getReport(String id) {
		MutableLiveData<DataTask<Report>> reportLiveData = new MutableLiveData<>();

		// Get the report (in real time).
		firebaseFirestore
			.collection("reports")
			.document(id)
			.addSnapshotListener(
				(snapshot, error) -> {
					// Query succeeded.
					if (error == null) {
						// Report found.
						if (snapshot != null) {
							Report report = snapshot.toObject(Report.class);
							if (report != null) {
								// Update the report id.
								report.setId(id);

								// Success task.
								DataTask<Report> dataTask = DataTask.success(report);
								reportLiveData.setValue(dataTask);
							}
						}

						// Report not found.
						else {
							// Error task.
							DataTask<Report> dataTask = DataTask.error(new DocumentNotFoundException());
							reportLiveData.setValue(dataTask);
						}
					}

					// Query failed.
					else {
						// Error task.
						DataTask<Report> dataTask = DataTask.error(error);
						reportLiveData.setValue(dataTask);
					}
				}
			);

		return reportLiveData;
	}

	/**
	 * Returns all the created reports (updated in real time).
	 * @return All the created reports (updated in real time).
	 */
	public LiveData<DataTask<List<Report>>> getCreatedReports() {
		MutableLiveData<DataTask<List<Report>>> reportsLiveData = new MutableLiveData<>();

		// Get all created reports (in real time).
		firebaseFirestore
			.collection("reports")
			.orderBy("date", Query.Direction.DESCENDING)
			.addSnapshotListener(
				(snapshots, error) -> {
					// Query succeeded.
					if (error == null) {
						// Reports found.
						if (snapshots != null) {
							// Get all created reports.
							List<Report> createdReports = new ArrayList<>();
							for (DocumentChange documentChange : snapshots.getDocumentChanges()) {
								// Created report.
								if (documentChange.getType() == DocumentChange.Type.ADDED) {
									QueryDocumentSnapshot snapshot = documentChange.getDocument();
									Report report = snapshot.toObject(Report.class);
									report.setId(snapshot.getId());
									createdReports.add(report);
								}
							}

							// Success task.
							DataTask<List<Report>> dataTask = DataTask.success(createdReports);
							reportsLiveData.setValue(dataTask);
						}

						// Reports not found.
						else {
							// Error task.
							DataTask<List<Report>> dataTask = DataTask.error(new DocumentNotFoundException());
							reportsLiveData.setValue(dataTask);
						}
					}

					// Query failed.
					else {
						// Error task.
						DataTask<List<Report>> dataTask = DataTask.error(error);
						reportsLiveData.setValue(dataTask);
					}
				}
			);

		return reportsLiveData;
	}

	/**
	 * Creates a report.
	 * @param report The report to be created.
	 * @return The created report.
	 */
	@Override
	public LiveData<DataTask<Report>> createReport(Report report) {
		// Report has a picture.
		Uri pictureUri = report.getPictureUri();
		if (pictureUri != null) {
			// Generate the picture name.
			Date now = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
			String timestamp = dateFormat.format(now);
			String pictureName = report.getUserId() + "_" + timestamp;

			// Store the picture.
			return Transformations.switchMap(
				storePicture(pictureName, pictureUri),
				task -> {
					// Picture stored.
					if (task.isSuccessful()) {
						// Get the picture path.
						String picturePath = task.getData();

						// Store the report.
						report.setPicturePath(picturePath);
						return storeReport(report);
					}

					// Picture not stored.
					DataTask<Report> dataTask = DataTask.error(task.getError());
					return new MutableLiveData<>(dataTask);
				}
			);
		}

		// Report has no picture.
		else {
			// Store the report.
			return storeReport(report);
		}
	}

	/**
	 * Stores a report.
	 * @param report The report to be created.
	 * @return The created report.
	 */
	private LiveData<DataTask<Report>> storeReport(Report report) {
		MutableLiveData<DataTask<Report>> reportLiveData = new MutableLiveData<>();

		// Create the report.
		firebaseFirestore
			.collection("reports")
			.add(report)
			.addOnCompleteListener(
				task -> {
					// Query succeeded.
					if (task.isSuccessful()) {
						// Report created.
						DocumentReference reference = task.getResult();
						if (reference != null) {
							// Update the report id.
							report.setId(reference.getId());

							// Success task.
							DataTask<Report> dataTask = DataTask.success(report);
							reportLiveData.setValue(dataTask);
						}

						// Report not created.
						else {
							// Error task.
							DataTask<Report> dataTask = DataTask.error(new DocumentNotCreatedException());
							reportLiveData.setValue(dataTask);
						}
					}

					// Query failed.
					else {
						// Error task.
						DataTask<Report> dataTask = DataTask.error(task.getException());
						reportLiveData.setValue(dataTask);
					}
				}
			);

		return reportLiveData;
	}

	/**
	 * Get the path of the report picture.
	 * @param name The name of the report picture.
	 * @return The path of the report picture.
	 */
	private String getPicturePath(String name) {
		return "reports/images/" + name;
	}

	/**
	 * Stores a report picture.
	 * @param name The name of the report picture.
	 * @param uri The URI of the report picture.
	 * @return The path of the report picture.
	 */
	private LiveData<DataTask<String>> storePicture(String name, Uri uri) {
		MutableLiveData<DataTask<String>> picturePathLiveData = new MutableLiveData<>();

		// Get the picture path.
		String picturePath = getPicturePath(name);

		// Store the picture.
		StorageReference reference = firebaseStorage.getReference(picturePath);
		reference
			.putFile(uri)
			.addOnCompleteListener(
				task -> {
					// Picture uploaded.
					if (task.isSuccessful()) {
						// Success task.
						DataTask<String> dataTask = DataTask.success(picturePath);
						picturePathLiveData.setValue(dataTask);
					}

					// Picture not uploaded.
					else {
						// Error task.
						DataTask<String> dataTask = DataTask.error(task.getException());
						picturePathLiveData.setValue(dataTask);
					}
				}
			);

		return picturePathLiveData;
	}

	/**
	 * Updates a report.
	 * @param report The report to be updated.
	 * @return The updated report.
	 */
	public LiveData<DataTask<Report>> updateReport(Report report) {
		MutableLiveData<DataTask<Report>> reportLiveData = new MutableLiveData<>();

		// Update the report.
		firebaseFirestore
			.collection("reports")
			.document(report.getId())
			.set(report)
			.addOnCompleteListener(
				task -> {
					// Report updated.
					if (task.isSuccessful()) {
						// Success task.
						DataTask<Report> dataTask = DataTask.success(report);
						reportLiveData.setValue(dataTask);
					}

					// Report not updated.
					else {
						// Error task.
						DataTask<Report> dataTask = DataTask.error(task.getException());
						reportLiveData.setValue(dataTask);
					}
				}
			);

		return reportLiveData;
	}

	/**
	 * Deletes a report.
	 * @param report The report to be delete.
	 * @return The deleted report.
	 */
	public LiveData<DataTask<Report>> deleteReport(Report report) {
		MutableLiveData<DataTask<Report>> reportLiveData = new MutableLiveData<>();

		// Delete the report.
		firebaseFirestore
			.collection("reports")
			.document(report.getId())
			.delete()
			.addOnCompleteListener(
				task -> {
					// Report deleted.
					if (task.isSuccessful()) {
						// Success task.
						DataTask<Report> dataTask = DataTask.success(report);
						reportLiveData.setValue(dataTask);
					} else {
						// Error task.
						DataTask<Report> dataTask = DataTask.error(task.getException());
						reportLiveData.setValue(dataTask);
					}
				}
			);

		return reportLiveData;
	}
}
