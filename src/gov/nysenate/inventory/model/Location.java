package gov.nysenate.inventory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable
{

    private String cdloctype;
    private String cdlocat;
    private String addressLine1;
    private String city;
    private String adzipcode;

    public Location() {
        cdloctype = "";
        cdlocat = "";
        addressLine1 = "";
    }

    public Location(String summary) {
        String[] tmp = summary.split("-");
        cdlocat = tmp[0];
        tmp = tmp[1].split(":");
        cdloctype = tmp[0];
        addressLine1 = tmp[1].trim();
    }

    public String getLocSummary() {
        return cdlocat + "-" + cdloctype + ": " + addressLine1;
    }

    public String getCdloctype() {
        return cdloctype;
    }

    public void setCdloctype(String cdloctype) {
        this.cdloctype = cdloctype;
    }

    public String getCdlocat() {
        return cdlocat;
    }

    public void setCdlocat(String cdlocat) {
        this.cdlocat = cdlocat;
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
        return adzipcode;
    }

    public void setZip(String adzipcode) {
        this.adzipcode = adzipcode;
    }

    public String getLocationSummaryString() {
        return getCdlocat() + "-" + getCdloctype()+ ": " + getAddressLine1();
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
        dest.writeString(cdlocat);
        dest.writeString(cdloctype);
        dest.writeString(addressLine1);
        dest.writeString(city);
        dest.writeString(adzipcode);
    }

    public void readFromParcel(Parcel in) {
        // Read Parcel in same order we wrote it.
        cdlocat = in.readString();
        cdloctype = in.readString();
        addressLine1 = in.readString();
        city = in.readString();
        adzipcode = in.readString();
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
