package gov.nysenate.inventory.activity;


import gov.nysenate.inventory.util.AppProperties;

public class Delivery2 extends SelectDelivery2 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl(this);
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        return url;
    }

    @Override
    protected String getPageTitle() {
        return "Please select a pickup to deliver.";
    }

    @Override
    protected Class getNextActivity() {
        return Delivery3.class;
    }
}
