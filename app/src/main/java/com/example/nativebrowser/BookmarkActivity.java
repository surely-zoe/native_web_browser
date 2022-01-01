package com.example.nativebrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;

public class BookmarkActivity extends AppCompatActivity {
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_bookmark);
        Intent intent = getIntent();
        listView = (ListView) findViewById(R.id.bookmark_listView);
        mydb = new DbHelper(this);
        ArrayList<String> bookmarkList = mydb.getBookmark();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.mytextview, bookmarkList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                PopupMenu popupMenu=new PopupMenu(BookmarkActivity.this,adapterView);
                popupMenu.getMenuInflater().inflate(R.menu.history_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        mydb.removeBookmark((String) listView.getItemAtPosition(i));
                        Intent intent1=new Intent(BookmarkActivity.this,BookmarkActivity.class);
                        startActivity(intent1);
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent2 = new Intent(BookmarkActivity.this,MainActivity.class);
                intent2.putExtra("url", (String) listView.getItemAtPosition(position));
                intent2.putExtra("book", (int) 1);
                startActivity(intent2);
            }
        });
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent1 =new Intent(BookmarkActivity.this,MainActivity.class);
//                intent1.putExtra("url",adapterView.getItemAtPosition(i).toString());
//               startActivity(intent1);
//
//            }
//        });
    }
    @Override
    public void onBackPressed() {
        Intent intent=new Intent(BookmarkActivity.this,MainActivity.class);
        intent.putExtra("frompreferences", (int) 1);
        startActivity(intent);
    }
}
