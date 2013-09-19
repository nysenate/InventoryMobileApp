package gov.nysenate.inventory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable
{

    private String cdLocType;
    private String cdLoc;
    private String addressLine1;
    private String city;
    private String zip;

    public Location() {
        cdLocType = "";
        cdLoc = "";
        addressLine1 = "";
    }

    public Location(String summary) {
        String[] tmp = summary.split("-");
        cdLoc = tmp[0];
        tmp = tmp[1].split(":");
        cdLocType = tmp[0];
        addressLine1 = tmp[1].trim();
    }

    public String getLocSummary() {
        return cdLoc + "-" + cdLocType + ": " + addressLine1;
    }

    public String getCdLocType() {
        return cdLocType;
    }

    public void setCdLocType(String cdLocType) {
        this.cdLocType = cdLocType;
    }

    public String getCdLoc() {
        return cdLoc;
    }

    public void setCdLoc(String cdLoc) {
        this.cdLoc = cdLoc;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }


    // ---------- Code for Parcelable interface --------------

    public Location(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cdLoc);
        dest.writeString(cdLocType);
        dest.writeString(addressLine1);
        dest.writeString(city);
        dest.writeString(zip);
    }

    public void readFromParcel(Parcel in) {
        // Read Parcel in same order we wrote it.
        cdLoc = in.readString();
        cdLocType = in.readString();
        addressLine1 = in.readString();
        city = in.readString();
        zip = in.readString();
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>()
    {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
}
