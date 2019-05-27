package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;


import static android.content.Context.MODE_PRIVATE;
import static edu.buffalo.cse.cse486586.groupmessenger1.GroupMessengerActivity.TAG;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 *
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */


public class GroupMessengerProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        // You do not need to implement this.
        return null;
    }



    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        //https://developer.android.com/training/data-storage/files#java

        //System.out.println("Below is values");

        String value=(String)values.get("value");
        String key=(String)values.get("key");
        //System.out.println(value);
        //System.out.println(key);

        FileOutputStream outputStream=null;
        Context context=this.getContext();

        try
        {
            outputStream = context.openFileOutput(key, MODE_PRIVATE);
            outputStream.write(value.getBytes());
            outputStream.close();
            System.out.println("File written");
        }
        catch (Exception e)
        {
            Log.e(TAG, "File write failed");
        }

        //System.out.println("End");

        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */
        Log.v("insert", values.toString());
        return uri;
    }



    @Override
    public boolean onCreate()
    {
        // If you need to perform any one-time initialization task, please do it here.



        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {

        String filename = selection;
        //System.out.println("This is selection");
        //System.out.println(filename);
        String ret = "";
        Context context=this.getContext();

        //https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android

        try {

            InputStream inputStream = context.openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            ret=bufferedReader.readLine();

            String[] arr = {"key", "value"};

            //http://www.zoftino.com/android-matrixcursor-example
            MatrixCursor c=new MatrixCursor(arr);
            String[] m=new String[2];
            m[0]=selection;
            m[1]=ret;
            c.addRow(m);

            System.out.println("Cursor returned");
            return c;

        }

        catch (FileNotFoundException e)
        {
            Log.e("login activity", "File not found: " + e.toString());
        }
        catch (IOException e)
        {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        System.out.println(ret);
        System.out.println("This is what is returned");


        Log.v("query", selection);
        return null;

    }
}