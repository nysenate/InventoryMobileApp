package gov.nysenate.inventory.model;

import gov.nysenate.inventory.android.InvItem;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class Pickup extends Transaction
{

    private ArrayList<InvItem> pickupItems;
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
        pickupItems = new ArrayList<InvItem>();
    }

    public Pickup(Location origin, Location destination) {
        super(origin, destination);
        comments = "";
        naPickupBy = "";
        naReleaseBy = "";
        nuxrRelSign = "";
        pickupItems = new ArrayList<InvItem>();
    }

    public ArrayList<InvItem> getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(InvItem[] pickupItems) {
        this.pickupItems = new ArrayList<InvItem>(Arrays.asList(pickupItems));
    }

    public void setPickupItems(ArrayList<InvItem> pickupItems) {
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
        pickupItems = new ArrayList<InvItem>();
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelableArray(pickupItems.toArray(new InvItem[pickupItems.size()]), flags);
        dest.writeString(comments);
        dest.writeString(naPickupBy);
        dest.writeString(naReleaseBy);
        dest.writeString(nuxrRelSign);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        pickupItems = (ArrayList) Arrays.asList(in.readParcelableArray(InvItem.class.getClassLoader()));
        comments = in.readString();
        naPickupBy = in.readString();
        naReleaseBy = in.readString();
        nuxrRelSign = in.readString();
    }

    public static final Parcelable.Creator<Pickup> CREATOR = new Parcelable.Creator<Pickup>()
    {
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
