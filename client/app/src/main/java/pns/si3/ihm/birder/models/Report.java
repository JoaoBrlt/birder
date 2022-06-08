package pns.si3.ihm.birder.models;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

/**
 * Bird report.
 *
 * Represents a bird report created by a user.
 */
public class Report {
	/**
	 * The id of the report.
	 */
	@Exclude
	private String id;

	/**
	 * The id of the user.
	 */
	private String userId;

	/**
	 * The species of the report.
	 */
	private String species;

	/**
	 * The number of birds.
	 */
	private int number;

	/**
	 * The URI of the report picture.
	 * Only used to upload the picture at report creation.
	 */
	private Uri pictureUri;

	/**
	 * The path of the report picture.
	 */
	private String picturePath;

	/**
	 * The date of the report.
	 */
	private Date date;

	/**
	 * The gender of the bird reported.
	 */
	private String gender;


	/**
	 * The age of the bird reported.
	 */
	private String age;

	/**
	 * The latitude of the bird reported
	 */
	private Double latitude;

	/**
	 * The longitude of the bird reported
	 */
	private Double longitude;

	/**
	 * Default constructor.
	 */
	public Report() {}

	public Report(
		String id,
		String userId,
		String species,
		int number,
		Uri pictureUri,
		Date date,
		String gender,
		String age,
		Double latitude,
		Double longitude
	) {
		this.id = id;
		this.userId = userId;
		this.species = species;
		this.number = number;
		this.pictureUri = pictureUri;
		this.picturePath = null;
		this.date = date;
		this.gender = gender;
		this.age = age;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Exclude
	public String getId() {
		return id;
	}

	@Exclude
	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Exclude
	public Uri getPictureUri() {
		return pictureUri;
	}

	@Exclude
	public void setPictureUri(Uri pictureUri) {
		this.pictureUri = pictureUri;
	}

	public String getPicturePath() {
		return picturePath;
	}

	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}
