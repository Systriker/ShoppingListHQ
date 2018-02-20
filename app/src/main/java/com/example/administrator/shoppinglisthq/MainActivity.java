package com.example.administrator.shoppinglisthq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ShoppingMemoDatasource datasource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShoppingMemo testMemo = new ShoppingMemo("Birnen",5,102);
        Log.d(TAG, "onCreate: Inhalt der Testmemo: " + testMemo.toString());
        datasource = new ShoppingMemoDatasource(this);
        Log.d(TAG, "onCreate: Die Datenquelle wird geöffnet");
        datasource.open();

        ShoppingMemo shoppingMemo = datasource.createShoppingMemo("Testprodukt", 2);
        Log.d(TAG, "onCreate: Folgendes wurde in DB eingetragen");
        Log.d(TAG, "ID: " + shoppingMemo.getId() + " Inhalt: " + shoppingMemo.toString());

        Log.d(TAG, "folgende Einträge sind in der DB vorhanden: ");
        showAllListEntries();

        Log.d(TAG, "onCreate: Die Datenquelle wird geschlossen");
        datasource.close();
    }

    private void showAllListEntries() {
        List<ShoppingMemo> shoppingMemoList = datasource.getAllShoppingMemos();
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                shoppingMemoList
        );
        ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);
        shoppingMemoListView.setAdapter(shoppingMemoArrayAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
