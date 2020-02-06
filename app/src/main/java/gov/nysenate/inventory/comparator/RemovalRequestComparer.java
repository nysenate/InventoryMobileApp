package gov.nysenate.inventory.comparator;

import java.util.Comparator;

import gov.nysenate.inventory.model.Item;

public class RemovalRequestComparer implements Comparator<Item> {
    @Override
    public int compare(Item item1, Item item2) {
        String description1 = "";

        try {
            description1 = item1.getCommodity().getDescription();
        } catch (Exception e) {

        }

        String description2 = "";
        try {
            description2 = item2.getCommodity().getDescription();
        } catch (Exception e) {

        }

        int value1 = description1.compareTo(description2);
        if (value1 == 0) {
            int barcode1 = 0;
            try {
                barcode1 = Integer.parseInt(item1.getBarcode());
            } catch (Exception e) {

            }

            int barcode2 = 0;
            try {
                barcode2 = Integer.parseInt(item2.getBarcode());
            } catch (Exception e) {

            }

            int value2 = barcode1 - barcode2;

            return value2;
        }
        return value1;
    }

}