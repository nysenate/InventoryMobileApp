package gov.nysenate.inventory.android;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.content.Context;

public class Preferences
{
    private String prefFilename;
    private String os;
    private Cipher ecipher;
    private Cipher dcipher;
    private SecretKey readKey;
    private SecretKey writeKey;
    private String pkyFile;
    private String encFile;
    private FileInputStream in;
    private String dir;
    private Context mcontext;
    boolean readEncrypt = false;
    boolean writeEncrypt = true;
    public boolean written = false;
    boolean readingFile = false;
    private boolean readnow = false, writeKeyFile = false, defaultGoRow = true,
            defaultClearVal = false, syncSeektoCurRow = false, inSeek = false;
    public boolean seekDone = false, allowLockPointer = false,
            pointerLocked = false;
    private byte[] iv = new byte[] { (byte) 0x8E, 0x12, 0x39, (byte) 0x9C,
            0x07, 0x72, 0x6F, 0x5A };

    ArrayList prefName = new ArrayList();
    ArrayList prefValue = new ArrayList();

    private AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
    // Buffer used to transport the bytes from one stream to another
    byte[] buf = new byte[1024];

    final int ADDUPDATE = -101, UPDATEONLY = -102, ADDONLY = -103,
            VIEWONLY = -104;
    int prefMode = ADDUPDATE;

    public Preferences(Context mcontext) {
        this(mcontext, "DEFAULT");
    }

    public Preferences(Context mcontext, String prefFilename) {
        System.out.println("IN PREFERENCES!!!!!!!!!!!!!!!!!!!");
        this.mcontext = mcontext;
        dir = mcontext.getFilesDir().getPath().toString();
        System.out.println("IN PREFERENCES-DIRECTORY:" + dir);
        this.prefFilename = prefFilename;
        os = System.getProperty("os.name", "N/A");

        prefFilename = prefFilename.toUpperCase();
        if (prefFilename.endsWith(".PREF")) {
            prefFilename = prefFilename.substring(0,
                    prefFilename.indexOf(".PREF"));
        }
        startFile();
    }

    private void startFile() {
        try {
            in = new FileInputStream(dir + "/" + prefFilename + ".pref");
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        }
        try {
            ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");

        } catch (javax.crypto.NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        }

        try {
            readEncryptMode(true);
        } catch (FileNotFoundException e1) {
            try {
                writeEncryptMode();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (writeEncrypt || readEncrypt) {
            try {
                writeKey = readKey();
                readKey = writeKey;
                ecipher.init(Cipher.ENCRYPT_MODE, writeKey, paramSpec);
                dcipher.init(Cipher.DECRYPT_MODE, readKey, paramSpec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        read();
    }

    public SecretKey readKey() throws IOException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidKeySpecException {
        return readKey(true);
    }

    public SecretKey readKey(boolean allowDefault) throws IOException,
            NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException {
        byte[] rawkey;
        if (pkyFile == null) {
            pkyFile = this.prefFilename;

        }
        try {
            File f = new File(dir + "/" + pkyFile + ".ppky");
            // Read the raw bytes from the keyfile
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            rawkey = new byte[(int) f.length()];
            in.readFully(rawkey);
            in.close();
        } catch (FileNotFoundException e)

        {
            if (allowDefault) {
                pkyFile = "DEFAULT";
                File f = new File(dir + "/" + pkyFile + ".ppky");
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                rawkey = new byte[(int) f.length()];
                in.readFully(rawkey);
                in.close();
            } else {
                throw new FileNotFoundException();
            }
        }

        // Convert the raw bytes to a secret key like this
        // DESedeKeySpec keyspec = new DESedeKeySpec(rawkey);
        DESKeySpec keyspec = new DESKeySpec(rawkey);
        // SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");

        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DES");
        // /ECB/PKCS5Padding

        SecretKey key = keyfactory.generateSecret(keyspec);

        return key;
    }

    public void generateNewKey() throws NoSuchAlgorithmException,
            InvalidKeySpecException, IOException {

        if (writeEncrypt) {
            // Get a key generator for Triple DES (a.k.a DESede)
            // KeyGenerator keygen = KeyGenerator.getInstance("DESede");

            KeyGenerator keygen = KeyGenerator.getInstance("DES");
            // /ECB/PKCS5Padding
            // Use it to generate a key
            writeKey = keygen.generateKey();

            try {
                ecipher.init(Cipher.ENCRYPT_MODE, writeKey, paramSpec);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
        } else {
            writeKey = null;
        }
        ;
        writeKeyFile = true;
    }

    public synchronized void writeEncryptMode() throws IOException {
        OutputStream fout = new FileOutputStream(dir + "/" + encFile + ".penc");
        OutputStream bout = new BufferedOutputStream(fout);
        OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");
        if (writeEncrypt) {
            out.write("TRUE");
        } else {
            out.write("FALSE");
        }
        ;
        out.flush();
        out.close();
    }

    private void read() {
        read(in);
    }

    private void read(InputStream in) {

        try {
            try {
                readEncryptMode(false);
            } catch (FileNotFoundException e1) {
                try {
                    writeEncryptMode();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }

            if (readEncrypt) {
                // Bytes read from in will be decrypted
                in = new CipherInputStream(in, dcipher);
                try {
                    writeKey = readKey();
                    ecipher.init(Cipher.ENCRYPT_MODE, writeKey, paramSpec);
                    dcipher.init(Cipher.DECRYPT_MODE, readKey, paramSpec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            BufferedReader inr = new BufferedReader(new InputStreamReader(in));
            int lineCntr = -1;
            // Read in the decrypted bytes and write the cleartext to out
            String c = new String();
            while (!(c = inr.readLine()).equals(null)) {
                // System.out.println("READ:"+c);
                lineCntr++;
                this.storePrefData(c);
            }

        } catch (java.lang.NullPointerException e) {
        }

        catch (java.io.IOException e) {
        } catch (Exception e) {
        }

    }

    public boolean readEncrypted() {
        return readEncrypt;
    }

    public boolean writeEncrypted() {
        return writeEncrypt;
    }

    public boolean encrypted() {
        if (readEncrypt && writeEncrypt) {
            return true;
        } else {
            return false;
        }
    }

    public String getPref(String prefName) {
        int x;
        try {
            x = this.findPref(prefName);
        } catch (Exception e) {
            x = -1;
        }
        if (x == -1) {
            System.out.println("PREF:" + prefName + " not found.");
        }
        if (x == -1) {
            return null;
        } else {
            return (String) this.prefValue.get(x);
        }
    }

    public int findPref(String prefName) {
        if (this.prefName.size() <= 0 || this.prefName == null) {
            return -1;
        }
        for (int x = 0; x < this.prefName.size(); x++) {
            if (prefName.equalsIgnoreCase((String) this.prefName.get(x))) {
                return x;
            }
        }
        return -1;
    }

    public void setPref(String prefName, String prefValue) throws Exception {
        int foundAt = this.findPref(prefName);
        if (foundAt == -1) {
            if (!this.readingFile && this.prefMode == UPDATEONLY) {
                throw new Exception("You cannot add " + prefName
                        + " because the prefMode is UPDATEONLY.");
            } else if (!this.readingFile && this.prefMode == VIEWONLY) {
                throw new Exception("You cannot add " + prefName
                        + " because the prefMode is VIEWONLY.");
            }
            this.prefName.add(prefName);
            this.prefValue.add(prefValue);
        } else {
            if (!this.readingFile && this.prefMode == ADDONLY) {
                throw new Exception("You cannot add " + prefName
                        + " because the prefMode is ADDONLY.");
            } else if (!this.readingFile && this.prefMode == VIEWONLY) {
                throw new Exception("You cannot add " + prefName
                        + " because the prefMode is VIEWONLY.");
            }
            this.prefValue.set(foundAt, prefValue);
        }
    }

    public void clearPrefs() {
        this.prefName = new ArrayList();
        this.prefValue = new ArrayList();
    }

    public boolean prefExists(String prefName) {
        if (this.findPref(prefName) > -1) {
            return true;
        } else {
            return false;
        }
    }

    public void readEncryptMode(boolean allowDefault)
            throws FileNotFoundException {
        if (encFile == null) {
            // System.out.println(xmlFile+" ENC FILE SET FOR THE FIRST TIME");
            encFile = prefFilename;
        }
        try {
            File fin = new File(dir + "/" + encFile + ".penc");
            FileReader in = new FileReader(fin);
            BufferedReader br = new BufferedReader(in);
            String c = null;
            c = br.readLine();
            if (c.indexOf("FALSE") != -1) {
                readEncrypt = false;
                writeEncrypt = false;
            } else {
                readEncrypt = true;
                writeEncrypt = true;
            }

            in.close();
        } catch (FileNotFoundException e) {
            if (allowDefault) {
                try {
                    encFile = "DEFAULT";
                    File fin = new File(dir + "/" + encFile + ".penc");
                    FileReader in = new FileReader(fin);
                    BufferedReader br = new BufferedReader(in);
                    String c = null;
                    c = br.readLine();
                    if (c.indexOf("FALSE") != -1) {
                        readEncrypt = false;
                        writeEncrypt = false;
                    } else {
                        readEncrypt = true;
                        writeEncrypt = true;
                    }

                    in.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                throw new FileNotFoundException();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEncryptMode(boolean myencrypt) throws IOException {
        writeEncrypt = myencrypt;
        writeEncryptMode();
        if (myencrypt) {
            try {
                // System.out.println("***********************************"+xmlFile+" reakKey 4");
                writeKey = readKey(false);
            } catch (Exception e) {
                try {
                    generateNewKey();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public int getPrefCount() {
        return prefName.size();
    }

    public void removeAllPreferences() {
        boolean deleted = false;
        deleted = new File(dir + "/" + prefFilename + ".pref").delete();
        clearPrefs();
    }

    public void setToDefaultSettings() {
        boolean deleted = false;
        deleted = new File(dir + "/" + prefFilename + ".penc").delete();
        deleted = new File(dir + "/" + prefFilename + ".ppky").delete();
        pkyFile = "DEFAULT";
        encFile = "DEFAULT";
        try {
            readEncryptMode(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            // System.out.println("***********************************"+xmlFile+" reakKey 5");
            writeKey = readKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write() {
        // System.out.println("PREFERENCES INITIAL WRITE");
        try {
            write(new FileOutputStream(dir + "/" + prefFilename + ".pref"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(OutputStream out) {
        // System.out.println("PREFERENCES WRITE");
        try {
            // readEncryptMode();
            if (writeEncrypt) {
                // Bytes written to out will be encrypted
                out = new CipherOutputStream(out, ecipher);
                try {

                    // key = readKey();
                    ecipher.init(Cipher.ENCRYPT_MODE, writeKey, paramSpec);
                    // dcipher.init(Cipher.DECRYPT_MODE, readKey, paramSpec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
            byte[] bCurRow;
            // System.out.println("WRITE "+prefName.size()+" LINES");
            for (int x = 0; x < prefName.size(); x++) {
                // System.out.println("BEFORE WRITING LINE:"+x);
                bCurRow = this.getPrefLine(x).getBytes();
                // System.out.println("WRITING:"+x+" "+this.getPrefLine(x));
                out.write(bCurRow, 0, bCurRow.length);
            }
            out.flush();
            out.close();
            written = true;
            if (writeKeyFile) {
                try {
                    writeKey();
                    readKey = writeKey;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            readEncrypt = writeEncrypt;

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeKey() throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        if (writeEncrypt) {
            // Convert the secret key to an array of bytes like this
            // SecretKeyFactory keyfactory =
            // SecretKeyFactory.getInstance("DESede");
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DES");
            // /ECB/PKCS5Padding
            // DESKeySpec keyspec = (DESKeySpec) keyfactory.getKeySpec(key,
            // DESKeySpec.class);
            // DESedeKeySpec keyspec = (DESedeKeySpec)
            // keyfactory.getKeySpec(key, DESedeKeySpec.class);
            DESKeySpec keyspec = (DESKeySpec) keyfactory.getKeySpec(writeKey,
                    DESKeySpec.class);
            byte[] rawkey = keyspec.getKey();

            // Write the raw key to the file
            FileOutputStream out = new FileOutputStream(dir + "/" + pkyFile
                    + ".ppky");
            out.write(rawkey);
            out.close();

        } else {
        }
        ;
    }

    public String getPrefLine(int row) {
        StringBuffer returnString = new StringBuffer();
        returnString.append((String) this.prefName.get(row));
        returnString.append(":");
        returnString.append((String) this.prefValue.get(row));
        returnString.append("\r\n");
        return returnString.toString();
    }

    public void storePrefData(String prefLine) throws Exception {
        int prefDelimiter = prefLine.indexOf(":");
        String prefName = prefLine.substring(0, prefDelimiter);
        String prevValue = prefLine.substring(prefDelimiter + 1);
        this.setPref(prefName, prevValue);
    }

    public boolean removePref(String prefName) {
        int x = this.findPref(prefName);
        if (x == -1) {
            return false;
        } else {
            this.prefName.remove(x);
            this.prefValue.remove(x);
            return true;
        }
    }

    public String getFile() {
        return prefFilename;
    }

    public SecretKey getKey() {
        return writeKey;
    }

    public SecretKey getReadKey() {
        return readKey;
    }

    public SecretKey getWriteKey() {
        return writeKey;
    }

    public ArrayList getPrefNames() {
        return this.prefName;
    }

}