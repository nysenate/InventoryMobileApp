package gov.nysenate.inventory.model;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class Pickup extends Transaction {

    private ArrayList<String> pickupItems;
    private String comments;
    private String naPickupBy;
    private String naReleaseBy;
    private String nuxrRelSign;

    public Pickup() {
        super();
        comments = "";
        naPickupBy = "";
        naReleaseBy = "";
        nuxrRelSign = "";
        pickupItems = new ArrayList<String>();
    }

    public Pickup(Location origin, Location destination) {
        super(origin, destination);
        comments = "";
        naPickupBy = "";
        naReleaseBy = "";
        nuxrRelSign = "";
        pickupItems = new ArrayList<String>();
    }

    public ArrayList<String> getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(String[] pickupItems) {
        this.pickupItems = new ArrayList<String>(Arrays.asList(pickupItems));
    }

    public void setPickupItems(ArrayList<String> pickupItems) {
        this.pickupItems = pickupItems;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNaPickupBy() {
        return naPickupBy;
    }

    public void setNaPickupBy(String naPickupBy) {
        this.naPickupBy = naPickupBy.toUpperCase();
    }

    public String getNaReleaseBy() {
        return naReleaseBy;
    }

    public void setNaReleaseBy(String naReleaseBy) {
        this.naReleaseBy = naReleaseBy.toUpperCase();
    }

    public String getNuxrRelSign() {
        return nuxrRelSign;
    }

    public void setNuxrRelSign(String nuxrRelSign) {
        this.nuxrRelSign = nuxrRelSign;
    }

    // ---------- Code for Parcelable interface ----------

    public Pickup(Parcel in) {
        pickupItems = new ArrayList<String>();
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeStringList(pickupItems);
        dest.writeString(comments);
        dest.writeString(naPickupBy);
        dest.writeString(naReleaseBy);
        dest.writeString(nuxrRelSign);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        in.readStringList(pickupItems);
        comments = in.readString();
        naPickupBy = in.readString();
        naReleaseBy = in.readString();
        nuxrRelSign = in.readString();
    }

    public static final Parcelable.Creator<Pickup> CREATOR =
            new Parcelable.Creator<Pickup>() {
                @Override
                public Pickup createFromParcel(Parcel in) {
                    return new Pickup(in);
                }

                @Override
                public Pickup[] newArray(int size) {
                    return new Pickup[size];
                }
            };
}
