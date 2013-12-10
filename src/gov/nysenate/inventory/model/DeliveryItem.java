package gov.nysenate.inventory.model;

public class DeliveryItem
{
    private String text;
    private boolean isChecked;

    DeliveryItem(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }

    public boolean getIsChecked() {
        return isChecked;
    }
}
