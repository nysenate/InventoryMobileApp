package gov.nysenate.inventory.activity;

import org.json.JSONObject;

import gov.nysenate.inventory.util.AppProperties;

public class EnterRemote1 extends SelectDelivery1 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl(this);
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        url += "&incompleteRemote=true";
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
        return EnterRemote2.class;
    }
}