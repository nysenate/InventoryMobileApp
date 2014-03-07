package gov.nysenate.inventory.activity;


public class EditPickup2Activity extends SelectDelivery2 {

    @Override
    protected String getPageTitle() {
        return "Please select a pickup to edit.";
    }

    @Override
    protected Class getNextActivity() {
        return EditPickupMenu.class;
    }
}
