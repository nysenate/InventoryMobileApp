package gov.nysenate.inventory.android;

public class Nvl {

    public String value(String val, String returnIfNull) {
        if (val == null) {
            return returnIfNull;
        } else {
            return val;

        }
    }

    public static String staticValue(String val, String returnIfNull) {
        if (val == null) {
            return returnIfNull;
        } else {
            return val;

        }
    }


}
