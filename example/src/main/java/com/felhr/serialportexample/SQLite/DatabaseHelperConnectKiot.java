package com.felhr.serialportexample.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ho Dong Trieu on 09/20/2018
 */
public class DatabaseHelperConnectKiot extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "VENDING_MACHINE";
    private static final String TABLE_NAME = "KiotViet";

    //create value of database
    private static final String KEY_INDEX = "id_index";
    private static final String KEY_PRODUCT_ID = "productID";
    private static final String KEY_PRODUCT_CODE = "productCode";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_BRANCHID = "branchId";

    public DatabaseHelperConnectKiot(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_INDEX + " INTEGER PRIMARY KEY,"
                + KEY_PRODUCT_ID + " LONG,"
                + KEY_PRODUCT_CODE + " TEXT,"
                + KEY_QUANTITY + " INTEGER,"
                + KEY_BRANCHID + " LONG"
                + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE_TRANSACTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void AddCatelogy(KiotVietModel kiotVietModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_ID,kiotVietModel.getProductID());
        values.put(KEY_PRODUCT_CODE,kiotVietModel.getProductCode());
        values.put(KEY_QUANTITY,kiotVietModel.getQuantity());
        values.put(KEY_BRANCHID,kiotVietModel.getBranchId());
        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public KiotVietModel GetIndex(int index){
        KiotVietModel kiotVietModel = new KiotVietModel();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,new String[] { KEY_INDEX ,
                KEY_PRODUCT_ID ,
                KEY_PRODUCT_CODE ,
                KEY_QUANTITY ,
                KEY_BRANCHID },KEY_INDEX + "=?", new String[] { String.valueOf(index) },null,null,null,null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                kiotVietModel.setId_index(cursor.getInt(0));
                kiotVietModel.setProductID(cursor.getLong(1));
                kiotVietModel.setProductCode(cursor.getString(2));
                kiotVietModel.setQuantity(cursor.getInt(3));
                kiotVietModel.setBranchId(cursor.getLong(4));
            } while (cursor.moveToNext());
        }
        db.close();
        return  kiotVietModel;
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.execSQL(clearDBQuery);
    }
}
