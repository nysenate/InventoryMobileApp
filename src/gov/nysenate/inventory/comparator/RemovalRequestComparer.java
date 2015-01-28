package gov.nysenate.inventory.comparator;

import gov.nysenate.inventory.model.Item;

import java.util.Comparator;

public class RemovalRequestComparer implements Comparator<Item> {
    @Override
    public int compare(Item item1, Item item2) {
        String description1 = "";
        
        try {
            description1 = item1.getCommodity().getDescription();
        }
        catch (Exception e) {
            
        }
        
        String description2 = "";
        try {
            description2 = item2.getCommodity().getDescription();
        }
        catch (Exception e) {
            
        }
        
        int value1 = description1.compareTo(description2);
        if (value1 == 0) {
            int serialNumber1 = 0;
            try {
                serialNumber1 = Integer.parseInt(item1.getSerialNumber());
            }
            catch (Exception e) {
                
            }
            
            int serialNumber2 = 0;
            try {
                serialNumber2 = Integer.parseInt(item2.getSerialNumber());
            }
            catch (Exception e) {
                
            }
            
            int value2 = serialNumber1 - serialNumber2;
            
            return value2;
        }
        return value1;
    }

  }    