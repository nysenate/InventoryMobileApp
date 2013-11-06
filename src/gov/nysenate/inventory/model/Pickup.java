package gov.nysenate.inventory.model;


import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class Pickup extends Transaction
{

    private ArrayList<InvItem> pickupItems;
    private String comments;
    private String napickupby;
    private String nareleaseby;
    private String nuxrrelsign;
    private String date;
    private int count;

    public Pickup() {
        super(); // needed for parcelable.
        comments = "";
        napickupby = "";
        nareleaseby = "";
        nuxrrelsign = "";
        pickupItems = new ArrayList<InvItem>();
        origin = new Location();
        destination = new Location();
    }

    public Pickup(Location origin, Location destination) {
        super(origin, destination);
        comments = "";
        napickupby = "";
        nareleaseby = "";
        nuxrrelsign = "";
        pickupItems = new ArrayList<InvItem>();
    }

    public String getDateWithoutTime() {
        String[] splitDate = date.split(" ");
        return splitDate[2] + " " + splitDate[0];
    }

    public ArrayList<InvItem> getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(InvItem[] pickupItems) {
        this.pickupItems = new ArrayList<InvItem>(Arrays.asList(pickupItems));
    }

    public void setPickupItems(String[] items) {
        ArrayList<InvItem> pickupItems = new ArrayList<InvItem>();
        for (String item : items) {
            InvItem invItem = new InvItem();
            invItem.setNusenate(item);
            pickupItems.add(invItem);
        }
        this.pickupItems = pickupItems;
    }

    public void setPickupItemsList(ArrayList<String> items) {
        ArrayList<InvItem> pickupItems = new ArrayList<InvItem>();
        for (String item : items) {
            InvItem invItem = new InvItem();
            invItem.setNusenate(item);
            pickupItems.add(invItem);
        }
        this.pickupItems = pickupItems;
    }

    public String[] getPickupItemsNusenate() {
        String[] nusenates = new String[pickupItems.size()];
        for (int i = 0; i < pickupItems.size(); i++) {
            nusenates[i] = pickupItems.get(i).getNusenate();
        }
        return nusenates;
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

    public String getNapickupby() {
        return napickupby;
    }

    public void setNapickupby(String naPickupBy) {
        this.napickupby = naPickupBy.toUpperCase();
    }

    public String getNareleaseby() {
        return nareleaseby;
    }

    public void setNareleaseby(String naReleaseBy) {
        this.nareleaseby = naReleaseBy.toUpperCase();
    }

    public String getNuxrrelsign() {
        return nuxrrelsign;
    }

    public void setNuxrrelsign(String nuxrRelSign) {
        this.nuxrrelsign = nuxrRelSign;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int cnt) {
        this.count = cnt;
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
        dest.writeString(napickupby);
        dest.writeString(nareleaseby);
        dest.writeString(nuxrrelsign);
        dest.writeString(date);
        dest.writeInt(count);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        pickupItems = new ArrayList(Arrays.asList(in.readParcelableArray(InvItem.class.getClassLoader())));
        comments = in.readString();
        napickupby = in.readString();
        nareleaseby = in.readString();
        nuxrrelsign = in.readString();
        date = in.readString();
        count = in.readInt();
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
