package pns.si3.ihm.birder.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pns.si3.ihm.birder.enumerations.DataStatus;

/**
 * Data task.
 *
 * Allows the repositories to indicate a status along with the data :
 * Loading, Success, Error.
 *
 * @param <T> The data class.
 */
public class DataTask<T> {
	/**
	 * The data status.
	 */
	@NonNull
	private DataStatus status;

	/**
	 * The data.
	 */
	@Nullable
	private T data;

	/**
	 * The error.
	 */
	@Nullable
	private Throwable error;

	/**
	 * Constructs a state data.
	 */
	private DataTask() {
		this.status = DataStatus.LOADING;
		this.data = null;
		this.error = null;
	}

	/**
	 * Returns whether the data task was successful, or not.
	 * @return Whether the data task was successful, or not.
	 */
	public boolean isSuccessful() {
		return this.status == DataStatus.SUCCESS;
	}

	/**
	 * Returns the data of the task.
	 * @return The data of the task, if the task was successful;
	 * <code>null</code> otherwise.
	 */
	@Nullable
	public T getData() {
		return data;
	}

	/**
	 * Returns the error of the task.
	 * @return The error of the task, if the task was unsuccessful;
	 * <code>null</code> otherwise;
	 */
	@Nullable
	public Throwable getError() {
		return error;
	}

	/**
	 * Returns a successful data task.
	 * @param data The data of the task.
	 * @param <T> The data class.
	 * @return The successful data task.
	 */
	public static <T> DataTask<T> success(T data) {
		DataTask<T> dataTask = new DataTask<>();
		dataTask.status = DataStatus.SUCCESS;
		dataTask.data = data;
		dataTask.error = null;
		return dataTask;
	}

	/**
	 * Returns a successful data task, without data.
	 * @param <T> The data class.
	 * @return The successful data task, without data.
	 */
	public static <T> DataTask<T> success() {
		return success(null);
	}

	/**
	 * Returns an unsuccessful data task.
	 * @param error The error of the data task.
	 * @param <T> The data class.
	 * @return The unsuccessful data task.
	 */
	public static <T> DataTask<T> error(Throwable error) {
		DataTask<T> dataTask = new DataTask<>();
		dataTask.status = DataStatus.ERROR;
		dataTask.data = null;
		dataTask.error = error;
		return dataTask;
	}
}
