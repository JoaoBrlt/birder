package pns.si3.ihm.birder.enumerations;

public enum TrophyEnum {
    UNEPHOTO("Prendre 1 photo", "Vous avez photographié 1 oiseau", "photo"),
    CINQPHOTOS ("Prendre 5 photos","Vous avez photographié 5 oiseaux", "photo"),
    CINQUANTEPHOTOS ("Prendre 50 photos", "Vous avez photographié 50 oiseaux", "photo"),
    CENTPHOTOS ("Prendre 100 photos", "Vous avez photographié 100 oiseaux", "photo");

    private String name;
    private String description;
    private String image;

    TrophyEnum(String name, String description, String image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }
}
