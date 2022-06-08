package pns.si3.ihm.birder.repositories.interfaces;

import androidx.lifecycle.LiveData;

import java.util.List;

import pns.si3.ihm.birder.models.DataTask;
import pns.si3.ihm.birder.models.Report;

/**
 * Report repository.
 *
 * Manages the bird reports in the database.
 */
public interface ReportRepository {
	/**
	 * Returns the list of reports (updated in real time).
	 * @return The list of reports (updated in real time).
	 */
	LiveData<DataTask<List<Report>>> getReports();

	/**
	 * Returns the list of created reports (updated in real time).
	 * @return The list of created reports (updated in real time).
	 */
	LiveData<DataTask<List<Report>>>  getCreatedReports();

	/**
	 * Returns a report (updated in real time).
	 * @param id The id of the report.
	 * @return The selected report (updated in real time).
	 */
	LiveData<DataTask<Report>> getReport(String id);

	/**
	 * Creates a report.
	 * @param report The report to be created.
	 * @return The created report.
	 */
	LiveData<DataTask<Report>> createReport(Report report);

	/**
	 * Updates a report.
	 * @param report The report to be updated.
	 * @return The updated report.
	 */
	LiveData<DataTask<Report>> updateReport(Report report);

	/**
	 * Deletes a report.
	 * @param report The report to be deleted.
	 * @return The deleted report.
	 */
	LiveData<DataTask<Report>> deleteReport(Report report);
}
