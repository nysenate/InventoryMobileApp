package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.util.AppProperties;

public class EditPickup2Activity extends SelectDelivery2 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl();
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        return url;
    }

    @Override
    protected String getPageTitle() {
        return "Please select a pickup to edit.";
    }

    @Override
    protected Class getNextActivity() {
        return EditPickupMenu.class;
    }
}
