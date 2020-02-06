package gov.nysenate.inventory.model;

/*
 * New Serial Item created as a quick way to return less data.
 * This has been created for Search Activity on the mobile client.
 */

public class InvSerialNumber
{
    String nuserial = "";
    String nusenate = "";
    String nuxrefsn = "";
    String cdcommodity = "";
    String decommodityf = "";    
   
    final int NUSERIAL = -101;
    final int NUSENATE = -103;
    final int NUXERFSN = -104;
    final int CDCOMMODITY = -105;
    final int DECOMMODITYF = -106;
    
    public InvSerialNumber() {
    }

    public String getNuserial() {
        return nuserial;
    }

    public void setNuserial(String nuserial) {
        this.nuserial = nuserial;
    }

    public String getNusenate() {
        return nusenate;
    }

    public void setNusenate(String nusenate) {
        this.nusenate = nusenate;
    }

    public String getNuxrefsn() {
        return nuxrefsn;
    }

    public void setNuxrefsn(String nuxrefsn) {
        this.nuxrefsn = nuxrefsn;
    }
    
    public String getCdcommodty() {
        return cdcommodity;
    }

    public void setCdcommodity(String cdcommodity) {
        this.cdcommodity = cdcommodity;
    }    
    
    public String getDecommodityf() {
        return decommodityf;
    }

    public void setDecommodityf(String decommodityf) {
        this.decommodityf = decommodityf;
    }        

    @Override
    public String toString() {
        return nuserial;
    }
}
