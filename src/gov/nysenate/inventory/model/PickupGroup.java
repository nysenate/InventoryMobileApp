package gov.nysenate.inventory.model;

import gov.nysenate.inventory.android.Nvl;

public class PickupGroup
{
    String pickupDateTime = "";
    String pickupFrom = "";
    String pickupLocat = "";
    String pickupRelBy = "";
    String pickupAdstreet1 = "";
    String pickupAdcity = "";
    String pickupAdstate = "";
    String pickupAdzipcode = "";
    int pickupItemCount = 0;
    int nuxrpd = 0;
    int shipId;

    public PickupGroup(int nuxrpd, String pickupDateTime, String pickupFrom,
            String pickupRelBy, String pickupLocat, String pickupAdstreet1,
            String pickupAdcity, String pickupAdstate, String pickupAdzipcode,
            int pickupItemCount, int shipId) {
        Nvl nvl = new Nvl();
        this.nuxrpd = nuxrpd;
        this.pickupDateTime = nvl.value(pickupDateTime, "N/A");
        this.pickupFrom = nvl.value(pickupFrom, "N/A");
        this.pickupRelBy = nvl.value(pickupRelBy, "N/A");
        this.pickupLocat = nvl.value(pickupLocat, "N/A");
        this.pickupAdstreet1 = nvl.value(pickupAdstreet1, "N/A");
        this.pickupAdcity = nvl.value(pickupAdcity, "N/A");
        this.pickupAdstate = nvl.value(pickupAdstate, "N/A");
        this.pickupAdzipcode = nvl.value(pickupAdzipcode, "N/A");
        this.pickupItemCount = pickupItemCount;
        this.shipId = shipId;
    }

    public String getPickupLocat() {
        return pickupLocat;
    }

    public void setPickupLocat(String pickupLocat) {
        this.pickupLocat = pickupLocat;
    }

    public String getPickupAdstreet1() {
        return pickupAdstreet1;
    }

    public void setPickupAdstreet1(String pickupAdstreet1) {
        this.pickupAdstreet1 = pickupAdstreet1;
    }

    public String getPickupAdcity() {
        return pickupAdcity;
    }

    public String getPickupAdstate() {
        return pickupAdstate;
    }

    public void setPickupAdstate(String pickupAdstate) {
        this.pickupAdstate = pickupAdstate;
    }

    public void setPickupAdcity(String pickupAdcity) {
        this.pickupAdcity = pickupAdcity;
    }

    public String getPickupAdzipcode() {
        return pickupAdzipcode;
    }

    public void setPickupAdzipcode(String pickupAdzipcode) {
        this.pickupAdzipcode = pickupAdzipcode;
    }

    public String getPickupFrom() {
        return pickupFrom;
    }

    public void setPickupFrom(String pickupFrom) {
        this.pickupFrom = pickupFrom;
    }

    public String getPickupRelBy() {
        return pickupRelBy;
    }

    public void setPickupRelBy(String pickupRelBy) {
        this.pickupRelBy = pickupRelBy;
    }

    public String getPickupDateTime() {
        return this.pickupDateTime;
    }

    public void setPickupDateTime(String pickupDateTime) {
        this.pickupDateTime = pickupDateTime;
    }

    public int getPickupItemCount() {
        return pickupItemCount;
    }

    public void setPickupItemCount(int pickupItemCount) {
        this.pickupItemCount = pickupItemCount;
    }

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
    }

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

}
