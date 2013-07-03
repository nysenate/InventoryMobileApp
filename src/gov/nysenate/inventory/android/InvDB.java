package gov.nysenate.inventory.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class InvDB
{

    private InvSQLiteHelper dbHelper;

    private SQLiteDatabase database;

    /**
     * 
     * @param context
     */
    public InvDB(Context context) {
        dbHelper = new InvSQLiteHelper(context);
        database = dbHelper.getWritableDatabase();
    }


    public Long insert(String table, String columnFieldList, String columnValueList) throws Exception {
         return insert(table, columnFieldList, columnValueList, "\\|");
    }

    public Long insert(String table, String columnFieldList, String columnValueList, String delimeter) throws Exception {
        long result = 0;
        String[] columnFields = columnFieldList.split(delimeter);
        String[] columnValues = columnValueList.split(delimeter);
        ContentValues insertValues = new ContentValues();
        int totalFields = columnFields.length;
        if (columnFields.length!=columnValues.length) {
            throw new Exception("!!ERROR: Column Values has to have the same number of values as Column Fields.");
        }
        for (int x=0;x<totalFields;x++) {
            insertValues.put(columnFields[x], columnValues[x]);
        }
        result = database.insert(table, null, insertValues);
        return result;
    }
    
    public Long update(String table, String columnFieldList, String columnValueList, String whereClause) throws Exception {
        return update(table, columnFieldList, whereClause, columnValueList, "\\|");
    }
  
    public Long update(String table, String columnFieldList, String columnValueList, String whereClause, String delimeter) throws Exception {
        long result = 0;
        String[] columnFields = columnFieldList.split(delimeter);
        String[] columnValues = columnValueList.split(delimeter);
        ContentValues updateValues = new ContentValues();
        int totalFields = columnFields.length;
        if (columnFields.length!=columnValues.length) {
            throw new Exception("!!ERROR: Column Values has to have the same number of values as Column Fields.");
        }
        result = database.update(table, updateValues, whereClause, null); 
        return result;
    }
        
    
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        Cursor mCursor = database.rawQuery(sql, selectionArgs);
        if (mCursor != null) {
            mCursor.moveToFirst();

        }
        return mCursor; // iterate to get each value.
    }
    
    public void truncateTable(String table) {
        delete(table, null);
    }

    public void delete(String table, String whereClause) {
        delete (table, whereClause, null);
    }

    public void delete(String table, String whereClause, String[] whereArgs ) {
        database.delete(table,whereClause, whereArgs);
    }
    
    
    public void execSQL (String sql) {
        database.execSQL(sql);
    }
    
    public void resetDB () {
        dbHelper.resetDatabase(database);
    }
    
}