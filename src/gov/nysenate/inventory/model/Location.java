package gov.nysenate.inventory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable
{

    private String cdloctype;
    private String cdlocat;
    private String adstreet1;
    private String adcity;
    private String adzipcode;
    private String adstate;

    public Location() {
        cdloctype = "";
        cdlocat = "";
        adstreet1 = "";
    }

    public Location(String summary) {
        String[] tmp = summary.split("-");
        cdlocat = tmp[0];
        tmp = tmp[1].split(":");
        cdloctype = tmp[0];
        adstreet1 = tmp[1].trim();
    }

    // Location is remote if it is outside of albany.
    public boolean isRemote() {
        return adcity.equalsIgnoreCase("Albany") ? false : true;
    }

    public String getAdstreet1() {
        return adstreet1;
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

    public void setAdstreet1(String addressLine1) {
        this.adstreet1 = addressLine1;
    }

    public String getAdcity() {
        return adcity;
    }

    public void setAdcity(String city) {
        this.adcity = city;
    }

    public String getAdzipcode() {
        return adzipcode;
    }

    public void setAdzipcode(String adzipcode) {
        this.adzipcode = adzipcode;
    }

    public String getAdstate() {
        return adstate;
    }

    public void setAdstate(String adstate) {
        this.adstate = adstate;
    }

    public String getLocationSummaryString() {
        return getCdlocat() + "-" + getCdloctype()+ ": " + getAdstreet1();
    }

    public String getFullAddress() {
        return this.getAdstreet1() + " " + this.getAdcity() + ", " + this.getAdstate() + " " + this.getAdzipcode();
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
        dest.writeString(adstreet1);
        dest.writeString(adcity);
        dest.writeString(adzipcode);
    }

    public void readFromParcel(Parcel in) {
        // Read Parcel in same order we wrote it.
        cdlocat = in.readString();
        cdloctype = in.readString();
        adstreet1 = in.readString();
        adcity = in.readString();
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
