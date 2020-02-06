package gov.nysenate.inventory.model;

/*
 * New Serial Item created as a quick way to return less data.
 * This has been created for Search Activity on the mobile client.
 */

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import gov.nysenate.inventory.util.Serializer;

public class InvSerialNumber {
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

    private List<Location> locations;

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

    public void setLocations(JSONArray jsonArray) {
       setLocations((ArrayList<Location>) Serializer.deserialize(jsonArray.toString(), Location.class));
    }

    public void setLocations(List <Location> locations) {
        if (locations == null) {
            this.locations = new ArrayList<>();
        }
        else {
            this.locations = locations;
        }
    }
    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public String toString() {
        return nuserial;
    }
}
