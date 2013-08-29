package gov.nysenate.inventory.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Transaction implements Parcelable {

    protected int nuxrpd;
    protected Location origin;
    protected Location destination;

    protected Transaction() {
        origin = new Location();
        destination = new Location();
    }

    protected Transaction(Location origin, Location destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public String getOriginCdLoc() {
        return origin.getCdLoc();
    }

    public String getOriginCdLocType() {
        return origin.getCdLocType();
    }

    public String getOriginAddressLine1() {
        return origin.getAddressLine1();
    }

    public String getDestinationCdLoc() {
        return destination.getCdLoc();
    }

    public String getDestinationCdLocType() {
        return destination.getCdLocType();
    }

    public String getDestinationAddressLine1() {
        return destination.getAddressLine1();
    }

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    // ---------- Code for Parcelable interface ----------

    protected Transaction(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nuxrpd);
        dest.writeParcelable(origin, flags);
        dest.writeParcelable(destination, flags);
    }

    public void readFromParcel(Parcel in) {
        nuxrpd = in.readInt();
        origin = in.readParcelable(Location.class.getClassLoader());
        destination = in.readParcelable(Location.class.getClassLoader());
    }
}
