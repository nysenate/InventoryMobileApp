package gov.nysenate.inventory.activity;


public class Delivery2 extends SelectDelivery2 {

    @Override
    protected String getPageTitle() {
        return "Please select a pickup to deliver.";
    }

    @Override
    protected Class getNextActivity() {
        return Delivery3.class;
    }
}
