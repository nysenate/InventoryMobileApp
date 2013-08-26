package gov.nysenate.inventory.model;

import java.io.Serializable;

public class Location implements Serializable {

    private static final long serialVersionUID = 1L;
    private String cdLocType; // location type code
    private String cdLoc; // location code
    private String addressLine1; // Used in summary information about the location throughout the app.

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

    public static void main(String[] args) {
        String a = "A333F-W: THE ALB CENTER FOR DDDEENUTS";
        Location loc = new Location(a);
        System.out.println(loc.getCdLoc());
        System.out.println(loc.getCdLocType());
        System.out.println(loc.getAddressLine1());
    }
}
