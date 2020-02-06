package gov.nysenate.inventory.activity;


import gov.nysenate.inventory.util.AppProperties;

public class EnterRemote2 extends SelectDelivery2 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl(this);
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        url += "&incompleteRemote=true";
        return url;
    }

    @Override
    protected String getPageTitle() {
        return "Please select a delivery.";
    }

    @Override
    protected Class getNextActivity() {
        return EnterRemote3.class;
    }
}