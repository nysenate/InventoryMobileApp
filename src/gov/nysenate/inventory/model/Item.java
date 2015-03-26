package gov.nysenate.inventory.model;

public class Item
{
    /** The unique identifier used by the data store */
    private int id;
    private String barcode;
    private String serialNumber;
    private Commodity commodity;
    private Location location;
    private ItemStatus status;

    /** The reason for an item to be inactivated. */
    private AdjustCode adjustCode;

    public Item() { }

    public Item(int id, String barcode) {
        this.id = id;
        this.barcode = barcode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public void setCommodity(Commodity commodity) {
        this.commodity = commodity;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public AdjustCode getAdjustCode() {
        return adjustCode;
    }

    public void setAdjustCode(AdjustCode adjustCode) {
        this.adjustCode = adjustCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (id != item.id) return false;
        if (adjustCode != null ? !adjustCode.equals(item.adjustCode) : item.adjustCode != null) return false;
        if (barcode != null ? !barcode.equals(item.barcode) : item.barcode != null) return false;
        if (commodity != null ? !commodity.equals(item.commodity) : item.commodity != null) return false;
        if (location != null ? !location.equals(item.location) : item.location != null) return false;
        if (serialNumber != null ? !serialNumber.equals(item.serialNumber) : item.serialNumber != null) return false;
        if (status != item.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
        result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
        result = 31 * result + (commodity != null ? commodity.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (adjustCode != null ? adjustCode.hashCode() : 0);
        return result;
    }
}
