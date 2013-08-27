package gov.nysenate.inventory.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Pickup extends Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> pickupItems;
    private String comments;
    private String naPickupBy;
    private String naReleaseBy;
    private String nuxrRelSign;

    public Pickup() {
        comments = "";
        naPickupBy = "";
        naReleaseBy = "";
        nuxrRelSign = "";
        origin = new Location();
        destination = new Location();
        pickupItems = new ArrayList<String>();
    }

    public ArrayList<String> getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(String[] pickupItems) {
        this.pickupItems = new ArrayList<String>(Arrays.asList(pickupItems));
    }

    public void setPickupItems(ArrayList<String> pickupItems) {
        this.pickupItems = pickupItems;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNaPickupBy() {
        return naPickupBy;
    }

    public void setNaPickupBy(String naPickupBy) {
        this.naPickupBy = naPickupBy.toUpperCase();
    }

    public String getNaReleaseBy() {
        return naReleaseBy;
    }

    public void setNaReleaseBy(String naReleaseBy) {
        this.naReleaseBy = naReleaseBy.toUpperCase();
    }

    public String getNuxrRelSign() {
        return nuxrRelSign;
    }

    public void setNuxrRelSign(String nuxrRelSign) {
        this.nuxrRelSign = nuxrRelSign;
    }

}
