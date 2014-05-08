package gov.nysenate.inventory.listener;

import gov.nysenate.inventory.model.Commodity;

public interface CommodityDialogListener
{

    public void commoditySelected(int rowSelected, Commodity commoditySelected);

}
