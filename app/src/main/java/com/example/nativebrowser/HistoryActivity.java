package com.example.nativebrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    ListView listView;
    DbHelper mydb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
        setContentView(R.layout.activity_history);
        getSupportActionBar().hide(); //hide the title bar
        Intent intent = getIntent();
        listView = (ListView) findViewById(R.id.history_listView);

        mydb = new DbHelper(this);
        ArrayList<String> historyList = mydb.getHistory();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.mytextview, historyList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent2 = new Intent(HistoryActivity.this,MainActivity.class);
                intent2.putExtra("url", (String) listView.getItemAtPosition(position));
                startActivity(intent2);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                PopupMenu popupMenu=new PopupMenu(HistoryActivity.this,adapterView);
                popupMenu.getMenuInflater().inflate(R.menu.history_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        mydb.removeHistory((String) listView.getItemAtPosition(i));
                        Intent intent2=new Intent(HistoryActivity.this,HistoryActivity.class);
                        startActivity(intent2);
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent intent=new Intent(HistoryActivity.this,MainActivity.class);
        intent.putExtra("frompreferences", (int) 1);
        startActivity(intent);
    }
}
