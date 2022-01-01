package com.example.nativebrowser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class DownloadActivity extends AppCompatActivity {
    ListView listView;
    DbHelper mydb;
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
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
        setContentView(R.layout.activity_download);
        Intent intent = getIntent();
        listView = (ListView) findViewById(R.id.download_listView);
        mydb = new DbHelper(this);
        final ArrayList<String> downloadList = mydb.getDownloads();
        final ArrayList<String> downloadListForView = mydb.getDownloadsForList();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.mytextview, downloadListForView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                PopupMenu popupMenu=new PopupMenu(DownloadActivity.this,adapterView);
                popupMenu.getMenuInflater().inflate(R.menu.history_item_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String fname = downloadList.get(i);
                        File fFile = new File(fname);
                        mydb.removeDownload((String) downloadList.get(i));
                        if (fFile.exists()) {
                            if (fFile.delete()) {
                                Toast.makeText(DownloadActivity.this, "File deleted successfully! ", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DownloadActivity.this, "Error! ", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Intent intent1=new Intent(DownloadActivity.this,DownloadActivity.class);
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
                String fname = downloadList.get(position);
                File fFile = new File(fname);
                if (fFile.exists()) //Checking if the file exists or not
                {
                    String type = getMimeType(fFile.getPath());
                    Uri path = Uri.fromFile(fFile);
                    Intent objIntent = new Intent(Intent.ACTION_VIEW);
                    objIntent.setDataAndType(path, type);
                    objIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(objIntent);//Starting the viewer
                } else {

                    Toast.makeText(DownloadActivity.this, "The file doesn't exists! ", Toast.LENGTH_SHORT).show();

                }
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
        Intent intent=new Intent(DownloadActivity.this,MainActivity.class);
        intent.putExtra("frompreferences", (int) 1);
        startActivity(intent);
    }
}
