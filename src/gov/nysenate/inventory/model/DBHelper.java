package gov.nysenate.inventory.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DBHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "invApp.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String TABLE_VERIFYINV_CREATE = "CREATE TABLE ad12verinv "
            + "( nuxrissue INTEGER PRIMARY KEY AUTOINCREMENT, nusenate TEXT NOT NULL, "
            + " cdcond TEXT NOT NULL,  cdcategory TEXT NOT NULL, cdintransit TEXT NOT NULL, "
            + "   nuxrpickup INTEGER NOT NULL, decommodityf TEXT NOT NULL, cdlocatfrm TEXT NOT NULL, "
            + " dttxnorigin TEXT NOT NULL, natxnorguser TEXT NOT NULL, "
            + " dttxnupdate TEXT NOT NULL, natxnupduser TEXT NOT NULL" + " );";


    private static final String TABLE_SERIALINV_CREATE = "CREATE TABLE ad12serial "
            + "( nuxrserial INTEGER PRIMARY KEY AUTOINCREMENT,  nusenate TEXT NOT NULL, "
            + " nuserial TEXT NOT NULL,  decommodityf TEXT NOT NULL," +
            " dttxnorigin TEXT NOT NULL, natxnorguser TEXT NOT NULL, "
            + " dttxnupdate TEXT NOT NULL, natxnupduser TEXT NOT NULL" + " );";

    
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.w(DBHelper.class.getName(), "Creating database");
        // this.executeSQLScript(database, "create.sql");
        database.execSQL(TABLE_VERIFYINV_CREATE);
        Log.w(DBHelper.class.getName(), TABLE_SERIALINV_CREATE);
        database.execSQL(TABLE_SERIALINV_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS ad12verinv");
        onCreate(db);
    }

    public void resetDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS ad12verinv");
        onCreate(db);
    }

    private void executeSQLScript(SQLiteDatabase database, String dbname) {
        Log.w(DBHelper.class.getName(), "Executing database script");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        Context context = null;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open(dbname);
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();

            String[] createScript = outputStream.toString().split(";");
            for (int i = 0; i < createScript.length; i++) {
                Log.w(DBHelper.class.getName(), "Check script line:"
                        + createScript[i]);
                String sqlStatement = createScript[i].trim();
                Log.w(DBHelper.class.getName(),
                        "Executing database script line:" + sqlStatement);

                // TODO You may want to parse out comments here
                if (sqlStatement.length() > 0) {
                    database.execSQL(sqlStatement + ";");
                }
            }
        } catch (IOException e) {
            // TODO Handle Script Failed to Load
        } catch (SQLException e) {
            // TODO Handle Script Failed to Execute
        }
    }

}