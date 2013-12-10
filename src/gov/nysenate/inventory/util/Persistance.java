package gov.nysenate.inventory.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

public class Persistance {
    private int length;
    private Context context;
    private String locationData;
    private String verificationData;

    public Persistance(Context context) {
        this.context = context;
        length = 0;
        locationData = "locationData";
        verificationData = "verificationData";
    }

    public void append() {

    }

    // TODO: Specify encoding when writing.
    public void saveLoc(String location) throws IOException {
        byte[] bytes = location.getBytes();
        FileOutputStream out = context.openFileOutput(locationData, 0);
        out.write(bytes, length, bytes.length);
        length += bytes.length;
        out.close();
    }

    public String loadLoc() throws IOException {
        FileInputStream out = context.openFileInput(locationData);
        byte[] data = new byte[5000];
        out.read(data);
        return new String(data, "US-ASCII");
    }
}
