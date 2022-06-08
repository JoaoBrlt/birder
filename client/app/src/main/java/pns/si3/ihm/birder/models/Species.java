package pns.si3.ihm.birder.models;

import com.google.firebase.firestore.Exclude;

import java.util.HashMap;

/**
 * Bird species.
 *
 * Represents a bird species with all its information.
 */
public class Species {
	/**
	 * The id of the species.
	 */
	private String id;

	/**
	 * The taxon of the species.
	 */
	private String taxon;

	/**
	 * The order of the species.
	 */
	private String order;

	/**
	 * The family of the species.
	 */
	private String family;

	/**
	 * The genus of the species.
	 */
	private String genus;

	/**
	 * The species.
	 */
	private String species;

	/**
	 * The scientific name of the species.
	 */
	private String name;

	/**
	 * The common names of the species.
	 * Key : Language.
	 * Value : Name.
	 */
	private HashMap<String, String> commonNames;

	/**
	 * Whether the species is extinct, or not.
	 */
	private boolean extinct;

	/**
	 * The breeding region of the species.
	 */
	private String breedingRegion;

	/**
	 * The breeding sub-region of the species.
	 */
	private String breedingSubregion;

	/**
	 * The non-breeding sub-region of the species.
	 */
	private String nonbreedingSubregion;

	/**
	 * Default constructor.
	 */
	public Species() {}

	@Exclude
	public String getId() {
		return id;
	}

	@Exclude
	public void setId(String id) {
		this.id = id;
	}

	public String getTaxon() {
		return taxon;
	}

	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, String> getCommonNames() {
		return commonNames;
	}

	@Exclude
	public String getFrenchCommonName() {
		return commonNames.get("French");
	}

	@Exclude
	public String getEnglishCommonName() {
		return commonNames.get("English");
	}

	@Exclude
	public String getSpanishCommonName() {
		return commonNames.get("Spanish");
	}

	@Exclude
	public String getItalianCommonName() {
		return commonNames.get("Italian");
	}

	@Exclude
	public String getGermanCommonName() {
		return commonNames.get("German");
	}

	@Exclude
	public String getPortugueseCommonName() {
		String commonName = commonNames.get("Portuguese");
		// Fix a bug of the IOC list.
		if (commonName != null && !commonName.isEmpty()) {
			commonName = commonName.substring(0, 1).toUpperCase() + commonName.substring(1);
		}
		return commonName;
	}

	public void setCommonNames(HashMap<String, String> commonNames) {
		this.commonNames = commonNames;
	}

	public boolean isExtinct() {
		return extinct;
	}

	public void setExtinct(boolean extinct) {
		this.extinct = extinct;
	}

	public String getBreedingRegion() {
		return breedingRegion;
	}

	public void setBreedingRegion(String breedingRegion) {
		this.breedingRegion = breedingRegion;
	}

	public String getBreedingSubregion() {
		return breedingSubregion;
	}

	public void setBreedingSubregion(String breedingSubregion) {
		this.breedingSubregion = breedingSubregion;
	}

	public String getNonbreedingSubregion() {
		return nonbreedingSubregion;
	}

	public void setNonbreedingSubregion(String nonbreedingSubregion) {
		this.nonbreedingSubregion = nonbreedingSubregion;
	}
}
