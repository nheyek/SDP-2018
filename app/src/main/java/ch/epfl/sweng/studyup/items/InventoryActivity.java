package ch.epfl.sweng.studyup.items;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;

import ch.epfl.sweng.studyup.R;
import ch.epfl.sweng.studyup.player.Player;
import ch.epfl.sweng.studyup.utils.adapters.ListItemAdapter;
import ch.epfl.sweng.studyup.utils.navigation.NavigationStudent;

import static ch.epfl.sweng.studyup.utils.Constants.INVENTORY_INDEX;
import static ch.epfl.sweng.studyup.utils.Utils.setupToolbar;

public class InventoryActivity extends NavigationStudent {
    private ArrayAdapter<String> adapter;
    private ListItemAdapter listItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        setupToolbar(this);
        navigationSwitcher(InventoryActivity.this, InventoryActivity.class, INVENTORY_INDEX);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupListView();
    }

    private void setupListView() {
        ListView ownedItems = findViewById(R.id.listViewItems);
        HashSet<Items> ownedItemsWithoutDuplicates = new HashSet<>(Player.get().getItems());
        listItemAdapter = new ListItemAdapter(this, new ArrayList<>(ownedItemsWithoutDuplicates),R.layout.shop_list_item_model, false);
        ownedItems.setAdapter(listItemAdapter);
        ownedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Items item = (Items)listItemAdapter.getItem(position);
                startActivity(new Intent(parent.getContext(), DisplayItemActivity.class).putExtra(DisplayItemActivity.class.getName(), item.getName()));
            }
        });
    }
}
