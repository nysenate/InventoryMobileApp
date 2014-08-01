package gov.nysenate.inventory.model;

public class Item
{
    private int id;
    private String barcode;
    private String serialNumber;
    private Commodity commodity;
    private Location location;
    private ItemStatus status;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (id != item.id) return false;
        if (barcode != null ? !barcode.equals(item.barcode) : item.barcode != null) return false;
        if (commodity != null ? !commodity.equals(item.commodity) : item.commodity != null) return false;
        if (location != null ? !location.equals(item.location) : item.location != null) return false;
        if (serialNumber != null ? !serialNumber.equals(item.serialNumber) : item.serialNumber != null) return false;
        if (status != item.status) return false;

        return true;
    }
}
