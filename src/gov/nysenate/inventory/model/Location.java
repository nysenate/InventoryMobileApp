package gov.nysenate.inventory.model;

import java.util.List;

public class Location
{
    private String cdloctype;
    private String cdlocat;
    private String adstreet1;
    private String street2;
    private String adcity;
    private String adzipcode;
    private String adstate;
    private String description;
    private String department;
    private List<Item> inventory;

    public Location() {
        cdloctype = "";
        cdlocat = "";
        adstreet1 = "";
    }

    // TODO: make into builder pattern.
    public Location(String cdlocat, String cdloctype, String adstreet1, String adcity,
                    String adzipcode, String adstate, String description, String department) {
        this.cdlocat = cdlocat;
        this.cdloctype = cdloctype;
        this.adstreet1 = adstreet1;
        this.adcity = adcity;
        this.adzipcode = adzipcode;
        this.adstate = adstate;
        this.description = description;
        this.department = department;
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

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLocationSummaryString() {
        return getCdlocat() + "-" + getCdloctype()+ ": " + getAdstreet1();
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void setInventory(List<Item> inventory) {
        this.inventory = inventory;
    }

    public String getLocationSummaryStringRemoteAppended() {
        String remoteTag = " [" + "<font color='#ff0000'>R</font>" + "]";
        return this.isRemote() ? getLocationSummaryString() + remoteTag : getLocationSummaryString();
    }

    public String getFullAddress() {
        return this.getAdstreet1() + " " + this.getAdcity() + ", " + this.getAdstate() + " " + this.getAdzipcode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Location location = (Location) o;

        if (adcity != null ? !adcity.equals(location.adcity) : location.adcity != null) return false;
        if (adstate != null ? !adstate.equals(location.adstate) : location.adstate != null) return false;
        if (adstreet1 != null ? !adstreet1.equals(location.adstreet1) : location.adstreet1 != null) return false;
        if (adzipcode != null ? !adzipcode.equals(location.adzipcode) : location.adzipcode != null) return false;
        if (cdlocat != null ? !cdlocat.equals(location.cdlocat) : location.cdlocat != null) return false;
        if (cdloctype != null ? !cdloctype.equals(location.cdloctype) : location.cdloctype != null) return false;
        if (department != null ? !department.equals(location.department) : location.department != null) return false;
        if (description != null ? !description.equals(location.description) : location.description != null)
            return false;
        if (inventory != null ? !inventory.equals(location.inventory) : location.inventory != null) return false;
        if (street2 != null ? !street2.equals(location.street2) : location.street2 != null) return false;

        return true;
    }
}
