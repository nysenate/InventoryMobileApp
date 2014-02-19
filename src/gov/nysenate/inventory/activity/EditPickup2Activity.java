package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.activity.SelectDelivery1.SearchByParam;
import gov.nysenate.inventory.adapter.PickupSearchList;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.TransactionParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EditPickup2Activity extends SelectDelivery2 {

    @Override
    protected String getPageTitle() {
        return "Please select a pickup to edit.";
    }

    @Override
    protected Class getNextActivity() {
        return EditPickupMenu.class;
    }
}
