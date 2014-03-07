package gov.nysenate.inventory.activity;


public class EnterRemote2 extends SelectDelivery2 {

    @Override
    protected String getPageTitle() {
        return "Please select a delivery.";
    }

    @Override
    protected Class getNextActivity() {
        return EnterRemote3.class;
    }
}