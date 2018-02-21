package com.example.administrator.shoppinglisthq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 20.02.2018.
 */

public class ShoppingMemoDatasource {

    private static final String TAG = ShoppingMemoDatasource.class.getSimpleName();

    private SQLiteDatabase database;
    private ShoppingMemoDbHelper dbHelper;

    private String[] columns = {
            ShoppingMemoDbHelper.COLUMN_ID,
            ShoppingMemoDbHelper.COLUMN_PRODUCT,
            ShoppingMemoDbHelper.COLUMN_QUANTITY
    };

    public ShoppingMemoDatasource(Context context){
        Log.d(TAG, "ShoppingMemoDatasource erzeugt den dbHelper");
        dbHelper = new ShoppingMemoDbHelper(context);
    }

    public void open(){
        Log.d(TAG, "open: Eine Referenz auf die Datenbank wird angefragt");
        database = dbHelper.getWritableDatabase();
        Log.d(TAG, "open: Referenz erhalten, Pfad zur DB: " + database.getPath());
    }

    public void close(){
        dbHelper.close();
        Log.d(TAG, "close: Datenbank wurde geschlossen");
    }

    public ShoppingMemo createShoppingMemo(String product, int quantity){
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT,product);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY,quantity);

        long insertId = database.insert(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,null,values);

        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,columns,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + insertId,
                null,null,null,null);
        cursor.moveToFirst();
        ShoppingMemo shoppingMemo = cursorToShoppingMemo(cursor);
        cursor.close();
        return shoppingMemo;
    }

    public void deleteShoppingMemo(ShoppingMemo memo){
        long id = memo.getId();

        database.delete(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id, null);
        Log.d(TAG, "deleteShoppingMemo: Eintrag gelöscht" + id + " " + memo.toString());
    }

    public ShoppingMemo updateShioppingMemeo(long id, String newProduct, int newQuantity){
        ContentValues values = new ContentValues();
        values.put(ShoppingMemoDbHelper.COLUMN_PRODUCT,newProduct);
        values.put(ShoppingMemoDbHelper.COLUMN_QUANTITY,newQuantity);

        database.update(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,values,
                ShoppingMemoDbHelper.COLUMN_ID + "=" + id,null);
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,
                columns,ShoppingMemoDbHelper.COLUMN_ID + "=" + id,
                null,null,null,null);
        cursor.moveToFirst();
        ShoppingMemo memo = cursorToShoppingMemo(cursor);
        cursor.close();
        return memo;
    }

    private ShoppingMemo cursorToShoppingMemo(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_ID);
        int idPrduct = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_PRODUCT);
        int idQuanrity = cursor.getColumnIndex(ShoppingMemoDbHelper.COLUMN_QUANTITY);

        String product = cursor.getString(idPrduct);
        int quantity = cursor.getInt(idQuanrity);
        long id = cursor.getLong(idIndex);
        ShoppingMemo memo = new ShoppingMemo(product,quantity,id);
        return memo;
    }

    public List<ShoppingMemo> getAllShoppingMemos(){
        List<ShoppingMemo> shoppingMemoList = new ArrayList<>();
        Cursor cursor = database.query(ShoppingMemoDbHelper.TABLE_SHOPPING_LIST,columns,
                null,null,null,null,null);
        cursor.moveToFirst();
        ShoppingMemo memo;
        while (!cursor.isAfterLast()){
            memo = cursorToShoppingMemo(cursor);
            shoppingMemoList.add(memo);
            Log.d(TAG, "ID: " + memo.getId() + " Inhalt " + memo.toString());
            cursor.moveToNext();
        }
        cursor.close();
        return  shoppingMemoList;
    }
}
