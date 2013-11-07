package gov.nysenate.inventory.model;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Transaction implements Parcelable
{

    protected int nuxrpd;
    protected Location origin;
    protected Location destination;
    private int shipId;
    private String shipType;
    private String shipComments;
    private int verificationId;
    private String verificationMethod;
    private String verificationComments;
    private int employeeId;
    private String helpReferenceNum;

    // shipType must exists for all remote transactions.
    public boolean isRemote() {
        if (shipType != null) {
            return true;
        }
        return false;
    }

    protected Transaction() {
        origin = new Location();
        destination = new Location();
    }

    protected Transaction(Location origin, Location destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public String getOriginCdLoc() {
        return origin.getCdlocat();
    }

    public String getOriginCdLocType() {
        return origin.getCdloctype();
    }

    public String getOriginAddressLine1() {
        return origin.getAdstreet1();
    }

    public String getDestinationCdLoc() {
        return destination.getCdlocat();
    }

    public String getDestinationCdLocType() {
        return destination.getCdloctype();
    }

    public String getDestinationAddressLine1() {
        return destination.getAdstreet1();
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

    public Location getOrigin() {
        return origin;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }

    public String getOriginSummaryString() {
        return origin.getLocationSummaryString();
    }

    public String getDestinationSummaryString() {
        return destination.getLocationSummaryString();
    }

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public String getShipComments() {
        return shipComments;
    }

    public void setShipComments(String shipComments) {
        this.shipComments = shipComments;
    }

    public int getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(int verificationId) {
        this.verificationId = verificationId;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getVerificationComments() {
        return verificationComments;
    }

    public void setVerificationComments(String verificationComments) {
        this.verificationComments = verificationComments;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getHelpReferenceNum() {
        return helpReferenceNum;
    }

    public void setHelpReferenceNum(String helpReferenceNum) {
        this.helpReferenceNum = helpReferenceNum;
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
        dest.writeInt(shipId);
        dest.writeString(shipType);
        dest.writeString(shipComments);
        dest.writeInt(verificationId);
        dest.writeString(verificationMethod);
        dest.writeString(verificationComments);
        dest.writeInt(employeeId);
        dest.writeString(helpReferenceNum);
    }

    public void readFromParcel(Parcel in) {
        nuxrpd = in.readInt();
        origin = in.readParcelable(Location.class.getClassLoader());
        destination = in.readParcelable(Location.class.getClassLoader());
        shipId = in.readInt();
        shipType = in.readString();
        shipComments = in.readString();
        verificationId = in.readInt();
        verificationMethod = in.readString();
        verificationComments = in.readString();
        employeeId = in.readInt();
        helpReferenceNum = in.readString();
    }
}
