package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.util.AppProperties;

public class EditPickup1Activity extends SelectDelivery1 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl(this);
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        return url;
    }

    @Override
    protected int getInitialSearchByParam() {
        return PICKUP_LOCATION_INDEX;
    }

    @Override
    protected Class getNextActivity() {
        return EditPickup2Activity.class;
    }

}
