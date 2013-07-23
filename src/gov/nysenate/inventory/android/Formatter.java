package gov.nysenate.inventory.android;

import java.util.ArrayList;
import java.util.List;

public class Formatter {

    /**
     * @param parameterName
     *            Name of get request parameter associated with this array.
     * @param values
     *            String array to convert into get request form.
     * @return string array formatted for a get request. example:
     *         &parameterName=
     *         value1&parameterName=value2&parameterName=value3...
     */
    public static String generateGetArray(String parameterName, String[] values) {
        String getString = "";
        for (String value : values) {
            getString += "&" + parameterName + "=" + value;
        }
        return getString;
    }

    public static String generateGetArray(String parameterName, List<InvItem> invItems) {
        ArrayList<String> nusenateStrings = new ArrayList<String>();
        for (InvItem aValue : invItems) {
            nusenateStrings.add(aValue.getNusenate());
        }
        String[] s = nusenateStrings.toArray(new String[nusenateStrings.size()]);
        return Formatter.generateGetArray(parameterName, s);
    }
}
