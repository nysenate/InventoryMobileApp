package gov.nysenate.inventory.android;

public class PrefItem
{
    String decommodityf = "";
    String type = "";
    String nusenate = "";
    String cdcategory = "";

    public PrefItem(String nusenate, String cdcategory, String type,
            String decommodityf) {
        this.nusenate = nusenate;
        this.cdcategory = cdcategory;
        this.type = type;
        this.decommodityf = decommodityf;
    }

    public String getDecommodityf() {
        return decommodityf;
    }

    public void setDecommodityf(String decommodityf) {
        this.decommodityf = decommodityf;
    }

    public String getNusenate() {
        return decommodityf;
    }

    public void setNusenate(String nusenate) {
        this.nusenate = nusenate;
    }

    public String getCdcategory() {
        return cdcategory;
    }

    public void setCdcategory(String cdcategory) {
        this.cdcategory = cdcategory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return decommodityf;
    }

}
