package com.example.mushroom;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private static final String TAG = "MainActivity";
    private static final Object Mat = "";
    String path = null;
    static String imageName = null;
    static String pathOfSelected = null;
  //  public String loadUrl = "http://agritech.tnau.ac.in/farm_enterprises/Farm%20enterprises_%20Mushroom_Disease.html";
    public String loadUrl = "https://www.mushroomexpert.com/coprinopsis_atramentaria.html";
    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        WebView browser = (WebView) findViewById(R.id.webview);
        browser.setWebViewClient(new MyBrowser());
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.setHorizontalScrollBarEnabled(false);
        browser.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }
        });
        browser.loadUrl(loadUrl);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                showFileChooser();
            }
        });
    }

    public void open(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if(result == R.string.Disease1){
            alertDialogBuilder.setMessage("Attention : Found "+ getString(R.string.Disease1));
        }else if(result == R.string.Disease2){
            alertDialogBuilder.setMessage("Attention : Found "+ getString(R.string.Disease2));
        }else if(result == R.string.Disease3){
            alertDialogBuilder.setMessage("Attention : Found "+ getString(R.string.Disease3));
        }else if(result == R.string.Disease4){
            alertDialogBuilder.setMessage("Attention : Found "+ getString(R.string.Disease4));
        }else{
            alertDialogBuilder.setMessage(R.string.NoDisease);
        }
        alertDialogBuilder.setPositiveButton("OK",
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
               // Toast.makeText(MainActivity.this,"You clicked yes  button",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void loading(){
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait!!");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        try {
            Thread.sleep(10000);
            dialog.dismiss();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());

                    String[] projection = { MediaStore.Images.Media.DATA };
                    Cursor cursor = null;

                    if (Build.VERSION.SDK_INT > 19) {
                        // Will return "image:x*"
                        String wholeID = DocumentsContract.getDocumentId(uri);
                        // Split at colon, use second item in the array
                        String id = wholeID.split(":")[1];
                        // where id is equal to
                        String sel = MediaStore.Images.Media._ID + "=?";

                        cursor = getBaseContext().getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                projection, sel, new String[] { id }, null);
                    }
                    String path = null;
                    try {
                        int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        pathOfSelected = cursor.getString(column_index).toString();

                        cursor.close();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        path = getPath(this, uri);
                        //open();
                        new ImageProcessing().execute();
                       /* Intent intent = new Intent(this, DisplayActivity.class);
                        startActivity(intent);*/
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error Retrieving Path", Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, "File Path: " + path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        String  p = uri.getLastPathSegment();
        File f = new File(p);
        imageName = f.getName();
        Log.e("dddd", imageName);

        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        String type = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{ split[1] };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return type;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ImageProcessing extends AsyncTask<String, Integer, String> {
        ProgressDialog p;
        @Override
        protected void onPreExecute() {
            p = new ProgressDialog(MainActivity.this);
            p.setMessage("Please wait...Detecting Image/Disease");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... params) {

            // Add code here for calling library
            @SuppressLint("ResourceType") InputStream inputStream = getResources().openRawResource(R.string.csvFilePath);
            CSVFile csvFile = new CSVFile(inputStream);
            List diseasesList = csvFile.read();

            result = process(pathOfSelected, diseasesList);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            p.dismiss();
            open();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public int process(String img, List list){
        //ImageProcessing.process(img, list);
        return 0;
    }
}
