package gov.nysenate.inventory.activity;

import android.app.Activity;
import android.os.Bundle;

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