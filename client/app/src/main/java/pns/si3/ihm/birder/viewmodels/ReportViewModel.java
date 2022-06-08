package pns.si3.ihm.birder.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.repositories.firebase.ReportRepositoryFirebase;
import pns.si3.ihm.birder.repositories.interfaces.ReportRepository;

/**
 * Report view model.
 *
 * Holds the data for report views.
 */
public class ReportViewModel extends ViewModel {
	/**
	 * The report repository.
	 */
	private ReportRepository reportRepository;

	/**
	 * The list of reports (updated in real time).
	 */
	private LiveData<DataTask<List<Report>>> reportsLiveData;

	/**
	 * The list of created reports (updated in real time).
	 */
	private LiveData<DataTask<List<Report>>> createdReportsLiveData;

	/**
	 * Constructs a report view model.
	 */
	public ReportViewModel() {
		super();

		// Initialize the repository.
		reportRepository = new ReportRepositoryFirebase();

		// Initialize the live data.
		reportsLiveData = reportRepository.getReports();
		createdReportsLiveData = reportRepository.getCreatedReports();
	}

	/**
	 * Returns the list of reports (updated in real time).
	 * @return The list of reports (updated in real time).
	 */
	public LiveData<DataTask<List<Report>>> getReports() {
		return reportsLiveData;
	}

	/**
	 * Returns the list of created reports (updated in real time).
	 * @return The list of created reports (updated in real time).
	 */
	public LiveData<DataTask<List<Report>>> getCreatedReports() {
		return createdReportsLiveData;
	}

	/**
	 * Returns a report (updated in real time).
	 * @param id The id of the report.
	 * @return The selected report (updated in real time).
	 */
	public LiveData<DataTask<Report>> getReport(String id) {
		return reportRepository.getReport(id);
	}

	/**
	 * Creates a report.
	 * @param report The report to be created.
	 * @return The created report.
	 */
	public LiveData<DataTask<Report>> createReport(Report report) {
		return reportRepository.createReport(report);
	}

	/**
	 * Requests the update of a report.
	 * @param report The report to be updated.
	 */
	public LiveData<DataTask<Report>> updateReport(Report report) {
		return reportRepository.updateReport(report);
	}

	/**
	 * Requests the deletion of a report.
	 * @param report The report to be deleted.
	 */
	public LiveData<DataTask<Report>> deleteReport(Report report) {
		return reportRepository.deleteReport(report);
	}
}
