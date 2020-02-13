package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.util.AppProperties;

public class Delivery1 extends SelectDelivery1 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl();
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        return url;
    }

    @Override
    protected String getPickupsAPIUrl() {
        return getPickupsUrl();
    }

    @Override
    protected String getPickupsParams() {
        return null;
    }

    @Override
    protected int getInitialSearchByParam() {
        return DELIVERY_LOCATION_INDEX;
    }

    @Override
    protected Class getNextActivity() {
        return Delivery2.class;
    }


}
