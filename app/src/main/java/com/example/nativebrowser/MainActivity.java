package com.example.nativebrowser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    private WebView webView;
    DbHelper mydb;
    String currTitle;
    BroadcastReceiver onComplete;
    private static int REQUEST_CODE=1;
    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

    void setCurrTitle(String s)
    {
        currTitle=s;
    }

    String getCurrTitle()
    {
        return currTitle;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.addressBar);
        final ImageButton bookmark = (ImageButton) findViewById(R.id.bookmark);
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new CustomWebClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setWebViewClient(new WebViewClient() {
            boolean isRedirected;
            boolean evaluated=false;
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                if (!isRedirected) {
                    //Do something you want when starts loading
                }

                isRedirected = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                try {
//                    webView.loadUrl(url);
                    editText.setText(webView.getUrl());
                    isRedirected=true;
                    if(mydb.inBookmarks(webView.getUrl())) {
                        bookmark.setBackgroundResource(R.drawable.bookf);
                    }
                    else
                    {
                        bookmark.setBackgroundResource(R.drawable.booke);
                    }
                }
                 catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                boolean darkmode = sharedPreferences.getBoolean("dark_mode", false);

                if (darkmode) {
                    webView.loadUrl("javascript:document.body.style.setProperty(\"background-color\",\"black\");");
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                }
                if(!evaluated) {
                    if (!isRedirected) {

                        boolean incognito = sharedPreferences.getBoolean("incognito", false);

                        if (!incognito)
                            mydb.addHistory(view.getUrl());

                    }
                    evaluated = true;
                }
                else{
                    evaluated=false;
                }

                super.onPageFinished(view, url);

            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.requestFocus(View.FOCUS_DOWN);
        WebSettings settings = webView.getSettings();

        boolean desktop = sharedPreferences.getBoolean("desktop", false);
        if (!desktop)
        {
            settings.setUserAgentString(MOBILE_USER_AGENT);
        }
        else
        {
            settings.setUserAgentString(DESKTOP_USER_AGENT);
        }
        Intent i = getIntent();
        if (i.hasExtra("url"))
        {
            webView.loadUrl(i.getStringExtra("url"));
            editText.setText(webView.getUrl());
            if(i.hasExtra("book"))
            {
                bookmark.setBackgroundResource(R.drawable.bookf);
            }
        }
        else if (i.hasExtra("frompreferences"))
        {
            mydb = new DbHelper(this);
            ArrayList<String> historyList = mydb.getHistory();
            int k = mydb.numberOfRowsInHistory();
            String url2;
            if(k==0) {
                url2 = sharedPreferences.getString("homepage", "https://www.google.com");
            }
            else{
                url2=historyList.get(0);
            }
            Log.d("2222222222222222", url2);
            webView.loadUrl(url2);
            editText.setText(webView.getUrl());
        }
        else
        {
            webView.loadUrl(sharedPreferences.getString("homepage", "https://www.google.com"));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setDownloadListener(new DownloadListener()
        {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimeType,
                                        long contentLength) {
                final Uri uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                Log.d("222222", uri.toString());
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading File...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                final long enq = dm.enqueue(request);
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                            DownloadManager.Query query = new DownloadManager.Query();
                            query.setFilterById(enq);
                            DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                            Cursor c = downloadManager.query(query);
                            if (c.moveToFirst()) {
                                int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                                    String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                    //TODO : Use this local uri and launch intent to open file
                                    String uriSub = uriString.substring(7);
                                    mydb.addDownloads(uriSub);
                                    File pdfFile = new File(uriSub);
                                    if (pdfFile.exists()) //Checking if the file exists or not
                                    {
                                        Uri path = Uri.fromFile(pdfFile);
                                        Intent objIntent = new Intent(Intent.ACTION_VIEW);
                                        String type = getMimeType(pdfFile.getPath());
                                        objIntent.setDataAndType(path, type);
                                        objIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(objIntent);//Starting the pdf viewer
                                    } else {

                                        Toast.makeText(MainActivity.this, "The file doesn't exists! ", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }
                        }
                    }
                };
                registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }});

//        String intent1=getIntent().getExtras().getString("url");[
//        if(intent1!=null){
//            webView.loadUrl(intent1);
//        }
        mydb = new DbHelper(this);
        editText.setText(webView.getUrl());
        ImageButton homeButton = (ImageButton) findViewById(R.id.home);
        ImageButton speechButton = (ImageButton) findViewById(R.id.speech);
        final ImageButton menuButton = (ImageButton) findViewById(R.id.menu) ;

        ((EditText)findViewById(R.id.addressBar)).setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {
                                // the user is done typing.
                                String url = editText.getText().toString();
                                if (url.startsWith("https://")) {
                                    webView.setWebChromeClient(new WebChromeClient() {
                                    });
                                    webView.loadUrl(url);
                                    editText.setText(webView.getUrl());
                                }
                                else if (url.startsWith("www.")) {
                                    webView.loadUrl("https://"+url);
                                    editText.setText(webView.getUrl());
                                }
                                else
                                {
                                    webView.loadUrl("https://www.google.com/search?q=" + url);
                                    editText.setText(webView.getUrl());
                                }
                                if(mydb.inBookmarks(webView.getUrl())) {
                                    bookmark.setBackgroundResource(R.drawable.bookf);
                                }
                                else
                                {
                                    bookmark.setBackgroundResource(R.drawable.booke);
                                }
                                return true; // consume.
                            }
                        }
                        return false; // pass on to other listeners.
                    }
                }
        );


        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String homepage = sharedPreferences.getString("homepage", "https://www.google.com");
                webView.loadUrl(homepage);
                editText.setText(webView.getUrl());
                if(mydb.inBookmarks(webView.getUrl())) {
                    bookmark.setBackgroundResource(R.drawable.bookf);
                }
                else
                {
                    bookmark.setBackgroundResource(R.drawable.booke);
                }
            }
        });

        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mydb.inBookmarks(webView.getUrl())) {
                    mydb.addBookmark(webView.getUrl());
                    bookmark.setBackgroundResource(R.drawable.bookf);
                }
                else
                {
                    mydb.removeBookmark(webView.getUrl());
                    bookmark.setBackgroundResource(R.drawable.booke);
                }
            }
        });

        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening...");
                try {
                    startActivityForResult(intent, 100);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, menuButton);
                popup.getMenuInflater().inflate(R.menu.settings_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("History")) {
                            if (mydb.numberOfRowsInHistory() != 0) {
                                Intent i = new Intent(getApplicationContext(), HistoryActivity.class);
                                startActivity(i);
                            } else Toast.makeText(MainActivity.this, "History Not Found"
                                    , Toast.LENGTH_SHORT).show();
                        }

                        if (item.getTitle().equals("Bookmarks")) {
                            if (mydb.numberOfRowsInBookmarks() != 0) {
                                Intent i = new Intent(getApplicationContext(), BookmarkActivity.class);
                                startActivity(i);
                            } else Toast.makeText(MainActivity.this, "Bookmarks List Empty"
                                    , Toast.LENGTH_SHORT).show();
                        }

                        if (item.getTitle().equals("Downloads")) {
                            if (mydb.numberOfRowsInDownloads() != 0) {
                                Intent i = new Intent(getApplicationContext(), DownloadActivity.class);
                                startActivity(i);
                            } else Toast.makeText(MainActivity.this, "Downloads List Empty"
                                    , Toast.LENGTH_SHORT).show();
                        }

                        if (item.getTitle().equals("Refresh")) {
                            String tempUrl=webView.getUrl();
                            webView.loadUrl(tempUrl);
                        }
                        if (item.getTitle().equals("Settings")) {
                            Intent i = new Intent(getApplicationContext(), PreferenceSettings.class);
                            startActivity(i);
                        }
                        if (item.getTitle().equals("Find on page...")) {
                            webView.showFindDialog((String)"hello", true);
                        }
                        if (item.getTitle().equals("Share")) {
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/plain");
                            i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
                            i.putExtra(Intent.EXTRA_TEXT, webView.getUrl().toString());
                            startActivity(Intent.createChooser(i, "Share URL"));
                        }
                        return true;
                    }
                });
                popup.show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    webView.loadUrl("https://www.google.com/search?q=" + result.get(0));
                    editText.setText(webView.getUrl());
                }
                break;
            }
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//                String intent1=getIntent().getExtras().getString("url");
//        if(intent1!=null){
//            webView.loadUrl(intent1);
//        }
//
//    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack() == true) {
            webView.goBack();
        } else {
            MainActivity.super.onBackPressed();
        }
    }

    public class CustomWebClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            view.loadUrl(url);
            editText.setText(url);
            return true;
        }
    }

}