package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.util.AppProperties;

public class EditPickup1Activity extends SelectDelivery1 {

    protected String getPickupsAPIUrl() {
        String url = AppProperties.getBaseUrl(this);
        url += "GetAllPickups?";
        return url;
    }

    protected String getPickupsParams() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("userFallback=");
        stringBuilder.append(LoginActivity.nauser);
        return stringBuilder.toString();
    }

    @Override
    protected String getPickupsUrl() {
        String url = getPickupsAPIUrl();
        url += getPickupsParams();
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
