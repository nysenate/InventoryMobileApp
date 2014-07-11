package gov.nysenate.inventory.model;

public class AdjustCode
{
    private String code;
    private String description;

    /**
     * An Adjustment Code is the reason for an inventory item to be removed from our database.
     */
    public AdjustCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


}
