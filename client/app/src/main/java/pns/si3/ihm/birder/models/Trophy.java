package pns.si3.ihm.birder.models;

import android.os.Parcel;
import android.os.Parcelable;

import pns.si3.ihm.birder.enumerations.TrophyEnum;

public class Trophy implements Parcelable {
    public String id;
    private TrophyEnum trophyEnum;
    private String description;
    private String name;
    private String image;

    public Trophy(String id, TrophyEnum trophyEnum){
        this.id = id;
        this.trophyEnum = trophyEnum;
        this.description = trophyEnum.getDescription();
        this.name = trophyEnum.getName();
        this.image = trophyEnum.getImage();
    }

    protected Trophy(Parcel in) {
        id = in.readString();
        description = in.readString();
        name = in.readString();
        image = in.readString();
    }

    public static final Creator<Trophy> CREATOR = new Creator<Trophy>() {
        @Override
        public Trophy createFromParcel(Parcel in) {
            return new Trophy(in);
        }

        @Override
        public Trophy[] newArray(int size) {
            return new Trophy[size];
        }
    };

    public String getId() {
        return id;
    }

    public TrophyEnum getTrophyEnum() {
        return trophyEnum;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(description);
        parcel.writeString(name);
        parcel.writeString(image);
    }
}
