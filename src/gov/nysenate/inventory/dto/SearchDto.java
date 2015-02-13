package gov.nysenate.inventory.dto;

import gov.nysenate.inventory.model.Item;

import java.util.Date;

public class SearchDto {

    private Item item;
    private Date lastInventoried;

    public SearchDto() {}

    public SearchDto(Item item, Date lastInventoried) {
        this.item = item;
        this.lastInventoried = lastInventoried;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setLastInventoried(Date lastInventoried) {
        this.lastInventoried = lastInventoried;
    }

    public Date getLastInventoried() {
        return lastInventoried;
    }
}