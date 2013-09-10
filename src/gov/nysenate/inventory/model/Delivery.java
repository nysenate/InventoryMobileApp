package gov.nysenate.inventory.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Delivery extends Transaction implements Serializable
{

    private static final long serialVersionUID = 1L;
    private ArrayList<String> allItems;
    private ArrayList<String> checkedItems;
    private ArrayList<String> notCheckedItems;
    private String comments;
    private String naDeliverBy;
    private String naAcceptBy;
    private String nuxrAccptSign;

    public Delivery() {
        comments = "";
        naDeliverBy = "";
        naAcceptBy = "";
        nuxrAccptSign = "";
        origin = new Location();
        destination = new Location();
        allItems = new ArrayList<String>();
        checkedItems = new ArrayList<String>();
        notCheckedItems = new ArrayList<String>();
    }

    public void generateNotCheckedItems() {
        notCheckedItems = (ArrayList<String>) allItems.clone();
        for (String item : checkedItems) {
            notCheckedItems.remove(item);
        }
    }

    public ArrayList<String> getAllItems() {
        return allItems;
    }

    public void setAllItems(ArrayList<String> allItems) {
        this.allItems = allItems;
    }

    public void setAllItems(String[] allItems) {
        this.allItems = new ArrayList<String>(Arrays.asList(allItems));
    }

    public ArrayList<String> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(String[] checkedItems) {
        this.checkedItems = new ArrayList<String>(Arrays.asList(checkedItems));
    }

    public ArrayList<String> getNotCheckedItems() {
        return notCheckedItems;
    }

    public void setNotCheckedItems(String[] notCheckedItems) {
        this.notCheckedItems = new ArrayList<String>(
                Arrays.asList(notCheckedItems));
    }

    public void setNotCheckedItems(ArrayList<String> notCheckedItems) {
        this.notCheckedItems = notCheckedItems;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNaDeliverBy() {
        return naDeliverBy;
    }

    public void setNaDeliverBy(String naDeliverBy) {
        this.naDeliverBy = naDeliverBy.toUpperCase();
    }

    public String getNaAcceptBy() {
        return naAcceptBy;
    }

    public void setNaAcceptBy(String naAcceptBy) {
        this.naAcceptBy = naAcceptBy.toUpperCase();
    }

    public String getNuxrAccptSign() {
        return nuxrAccptSign;
    }

    public void setNuxrAccptSign(String nuxrAccptSign) {
        this.nuxrAccptSign = nuxrAccptSign;
    }

}
