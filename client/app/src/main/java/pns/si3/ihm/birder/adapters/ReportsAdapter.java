package pns.si3.ihm.birder.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import etudes.fr.demoosm.R;
import pns.si3.ihm.birder.models.Report;
import pns.si3.ihm.birder.views.reports.InformationActivity;
import pns.si3.ihm.birder.views.reports.MainActivity;
import pns.si3.ihm.birder.views.reports.ReportActivity;

/**
 * Reports recycler view adapter.
 *
 * Manages a list of reports inside a recycler view.
 */
public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {
	/**
	 * The list of reports.
	 */
	private List<Report> reports;

	/**
	 * The reports activity.
	 */
	private MainActivity context;

	/**
	 * Constructs a reports adapter.
	 */
	public ReportsAdapter(MainActivity context) {
		this.reports = new ArrayList<>();
		this.context = context;
	}

	public ReportsAdapter() {
		this.reports = new ArrayList<>();
	}

	/**
	 * Updates the list of reports.
	 * @param reports The new list of reports.
	 */
	public void setReports(List<Report> reports) {
		this.reports = reports;
		notifyDataSetChanged();
	}

	/**
	 * Returns the time elapsed since a date.
	 * @param date The start date.
	 * @return The time elapsed since the start date.
	 */
	private String getElapsedTime(Date date) {
		Date now = new Date();
		return DateUtils.getRelativeTimeSpanString(
				date.getTime(),
				now.getTime(),
				DateUtils.MINUTE_IN_MILLIS
		).toString();
	}

	@Override
	public ReportsAdapter.ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context context = parent.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		// Inflate the custom layout.
		View reportView = inflater.inflate(R.layout.list_item_report, parent, false);

		// Return a new holder instance.
		return new ReportViewHolder(reportView);
	}

	@Override
	public void onBindViewHolder(ReportsAdapter.ReportViewHolder viewHolder, int position) {
		// Get the report in the list.
		Report report = reports.get(position);

		// Picture.
		String picturePath = report.getPicturePath();
		if (picturePath != null) {
			FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
			StorageReference pictureReference = firebaseStorage.getReference(picturePath);
			ImageView imagePicture = viewHolder.imagePicture;
			Glide
				.with(context)
				.load(pictureReference)
				.into(imagePicture);
		}

		// Species.
		TextView titleText = viewHolder.textSpecies;
		titleText.setText(report.getSpecies());

		// Elapsed time.
		TextView dateText = viewHolder.textDate;
		String elapsedTime = getElapsedTime(report.getDate());
		dateText.setText(elapsedTime);

		// On button selected.
		Button button = viewHolder.selectButton;
		button.setOnClickListener(v -> {
			Intent intent = new Intent(context, InformationActivity.class);
			intent.putExtra("id", report.getId());
			context.startActivity(intent);
		});

	}

	/**
	 * Returns the number of reports.
	 * @return The number of reports.
	 */
	@Override
	public int getItemCount() {
		return reports.size();
	}

	/**
	 * Reports recycler view holder.
	 *
	 * Represents a single report inside the recycler view.
	 */
	static class ReportViewHolder extends RecyclerView.ViewHolder {
		private TextView textSpecies;
		private TextView textDate;
		private ImageView imagePicture;
		private Button selectButton;

		ReportViewHolder(View itemView) {
			super(itemView);
			imagePicture = itemView.findViewById(R.id.image_picture);
			textSpecies = itemView.findViewById(R.id.text_title);
			textDate = itemView.findViewById(R.id.text_date);
			selectButton = itemView.findViewById(R.id.button_select);
		}

	}
}