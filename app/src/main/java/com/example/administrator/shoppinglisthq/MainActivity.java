package com.example.administrator.shoppinglisthq;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ShoppingMemoDatasource datasource;
    private ListView shoppingMemosListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datasource = new ShoppingMemoDatasource(this);
        initializeShoppingMemosListView();
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

    private void initializeShoppingMemosListView() {
        List<ShoppingMemo> emtyListForInitialisation = new ArrayList<>();

        shoppingMemosListView = findViewById(R.id.listview_shopping_memos);
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter = new ArrayAdapter<ShoppingMemo>(this,
                android.R.layout.simple_list_item_multiple_choice,emtyListForInitialisation){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                ShoppingMemo memo = (ShoppingMemo) shoppingMemosListView.getItemAtPosition(position);
                if (memo.isChecked()){
                    textView.setPaintFlags(textView.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175,175,175));
                }else{
                    textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }
                return view;
            }
        };
        shoppingMemosListView.setAdapter(shoppingMemoArrayAdapter);

        shoppingMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ShoppingMemo memo = (ShoppingMemo) adapterView.getItemAtPosition(i);
                ShoppingMemo updatedShoppingMemo = datasource.updateShioppingMemeo(memo.getId(),
                        memo.getProduct(),memo.getQuantity(),!memo.isChecked());
                showAllListEntries();
            }
        });
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
            int selCount = 0;
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                if (checked){
                    selCount++;
                }else{
                    selCount--;
                }
                String cabTitle = selCount + " " + getString(R.string.cab_checked_string);
                actionMode.setTitle(cabTitle);
                actionMode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_edit);
                if(selCount == 1){
                    item.setVisible(true);
                }else {
                    item.setVisible(false);
                }
                return true;

            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                SparseBooleanArray touchedShoppingMemosPosition = shoppingMemoListView.
                        getCheckedItemPositions();
                switch (menuItem.getItemId()) {

                    case R.id.cab_delete:

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
                    case R.id.cab_edit:
                        for (int i = 0; i < touchedShoppingMemosPosition.size(); i++) {
                            boolean isChecked = touchedShoppingMemosPosition.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedShoppingMemosPosition.keyAt(i);
                                ShoppingMemo memo = (ShoppingMemo) shoppingMemoListView.
                                        getItemAtPosition(positionInListView);
                                Log.d(TAG, "onActionItemClicked: Position in Listview: " + positionInListView +
                                        " Inhalt: " + memo.toString());
                                AlertDialog alertDialog = createEditShoppingMemoDialog(memo);
                                alertDialog.show();
                            }
                        }
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selCount = 0;
            }
        });

    }

    private void showAllListEntries() {
        List<ShoppingMemo> shoppingMemoList = datasource.getAllShoppingMemos();
        ArrayAdapter<ShoppingMemo> shoppingMemoArrayAdapter =
                (ArrayAdapter<ShoppingMemo>) shoppingMemosListView.getAdapter();
        shoppingMemoArrayAdapter.clear();
        shoppingMemoArrayAdapter.addAll(shoppingMemoList);
        shoppingMemoArrayAdapter.notifyDataSetChanged();
    }

    private AlertDialog createEditShoppingMemoDialog(final ShoppingMemo memo){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        final View dialogsView = inflater.inflate(R.layout.dialog_edit_shopping_memo,null);

        final EditText editTextNewQuantity = dialogsView.findViewById(R.id.editText_new_quantity);
        editTextNewQuantity.setText(String.valueOf(memo.getQuantity()));

        final EditText editTextNewProduct = dialogsView.findViewById(R.id.editText_new_product);
        editTextNewProduct.setText(memo.getProduct());

        builder.setView(dialogsView)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String quantity = editTextNewQuantity.getText().toString();
                        String product = editTextNewProduct.getText().toString();

                        if ((TextUtils.isEmpty(quantity))||(TextUtils.isEmpty(product))){
                            Toast.makeText(MainActivity.this,"Felder dürfen nicht leer sein",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int quantity_int = Integer.parseInt(quantity);
                        ShoppingMemo updatedShopingMemo = datasource.updateShioppingMemeo(memo.getId(),
                                product,quantity_int,memo.isChecked());
                        Log.d(TAG, "onClick: alt:" + memo.getId() + " : " + memo.toString());
                        Log.d(TAG, "onClick: neu:" + updatedShopingMemo.getId() + " : " +
                                updatedShopingMemo.toString());

                        showAllListEntries();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        return builder.create();
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
