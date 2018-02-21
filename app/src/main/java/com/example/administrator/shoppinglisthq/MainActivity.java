package com.example.administrator.shoppinglisthq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
        activateAddButton();
        initializeContextualActionBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Die Datenquelle wird geschlossen");
        datasource.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Die Datenquelle wird geöffnet");
        datasource.open();
        Log.d(TAG, "folgende Einträge sind in der DB vorhanden: ");
        showAllListEntries();
    }

    private void activateAddButton() {
        Button button = findViewById(R.id.button_add_product);
        final EditText editTextQuantity = findViewById(R.id.editText_quantity);
        final EditText editTextProduct = findViewById(R.id.editText_product);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantity = editTextQuantity.getText().toString();
                String product = editTextProduct.getText().toString();

                if (TextUtils.isEmpty(quantity)){
                    editTextQuantity.setError(getString(R.string.editText_errorMessage));
                    editTextQuantity.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(product)){
                    editTextProduct.setError(getString(R.string.editText_errorMessage));
                    editTextProduct.requestFocus();
                    return;
                }
                int quantity_int = Integer.parseInt(quantity);
                editTextProduct.setText("");
                editTextQuantity.setText("");

                datasource.createShoppingMemo(product, quantity_int);

                InputMethodManager inputMethodManager =
                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null){
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                }
                editTextQuantity.requestFocus();

                showAllListEntries();
            }
        });
    }

    private void initializeContextualActionBar() {
        final ListView shoppingMemoListView = findViewById(R.id.listview_shopping_memos);
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {}

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) { return false; }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.cab_delete:
                        SparseBooleanArray touchedShoppingMemosPosition = shoppingMemoListView.
                                getCheckedItemPositions();
                        for (int i = 0; i < touchedShoppingMemosPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo memo = (ShoppingMemo) shoppingMemoListView.
                                        getItemAtPosition(positionInListView);
                                Log.d(TAG, "onActionItemClicked: Position in Listview: " + positionInListView +
                                        " Inhalt: " + memo.toString());
                                datasource.deleteShoppingMemo(memo);
                            }
                        }
                        showAllListEntries();
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {}
        });

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
