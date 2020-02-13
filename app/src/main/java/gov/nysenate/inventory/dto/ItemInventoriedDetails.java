package gov.nysenate.inventory.dto;

import android.util.Log;

import java.util.Date;

import gov.nysenate.inventory.model.Item;

public class ItemInventoriedDetails {

    private Item item;
    private Date lastInventoried;

    public ItemInventoriedDetails() {
    }

    public ItemInventoriedDetails(Item item, Date lastInventoried) {
        this.item = item;
        this.lastInventoried = lastInventoried;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        Log.i(this.getClass().getName(), "setItem(" + item.getBarcode() + ":" + item.getSerialNumber() + ")");
        this.item = item;
    }

    public void setLastInventoried(Date lastInventoried) {
        this.lastInventoried = lastInventoried;
        Log.i(this.getClass().getName(), "setLastInventoried(" + lastInventoried + ")");
    }

    public Date getLastInventoried() {
        return lastInventoried;
    }


}