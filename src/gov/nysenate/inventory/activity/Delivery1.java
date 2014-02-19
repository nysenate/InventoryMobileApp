package gov.nysenate.inventory.activity;

import android.os.Build;

import android.os.AsyncTask;
import gov.nysenate.inventory.util.AppProperties;

public class Delivery1 extends SelectDelivery1 {

    @Override
    protected String getPickupsUrl() {
        String url = AppProperties.getBaseUrl(this);
        url += "GetAllPickups?";
        url += "userFallback=" + LoginActivity.nauser;
        return url;
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
