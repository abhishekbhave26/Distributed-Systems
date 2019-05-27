package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */


public class GroupMessengerActivity extends Activity
{
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    private Uri mUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //code taken from PA1

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        System.out.println("This is myPort");
        System.out.println(myPort);

        try
        {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.

             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        TextView tv = (TextView) findViewById(R.id.local_text_display);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final Button btn=(Button)findViewById(R.id.button4);
        // implement onClickListener and use socket to send msg to all AVDS

        //https://developer.android.com/reference/android/widget/Button
        btn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses button
                EditText editText=(EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
                editText.setText("");// This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.local_text_display);
                localTextView.append("\t" + msg); // This is one way to display a string.


                TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
                remoteTextView.append("\n");

                //System.out.println("Button works");

                /*
                 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
                 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does. To understand
                 * the difference, please take a look at
                 * http://developer.android.com/reference/android/os/AsyncTask.html
                 */
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

            }

            });

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }






    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */

    //code taken from PA1
    private class ServerTask extends AsyncTask<ServerSocket, String, Void>
    {

        //code for below taken from OnPTestClickListener
        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }


        @Override
        protected Void doInBackground(ServerSocket... sockets)
        {
            //System.out.println("Below is sockets");
            //System.out.println(sockets);
            ServerSocket serverSocket = sockets[0];

            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");

            try
            {
                int i=0;

                while(true)
                {
                    Socket s = serverSocket.accept();

                    //creates object of input stream
                    InputStreamReader isr = new InputStreamReader(s.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String msg = br.readLine();
                    System.out.println("Message from client is " + msg);

                    //below part for contentvalue is taken from PA2A description
                    ContentValues c=new ContentValues();
                    c.put("key",Integer.toString(i));
                    c.put("value",msg);
                    getContentResolver().insert(mUri,c);
                    //https://developer.android.com/reference/android/os/AsyncTask
                    publishProgress(msg);
                    i++;
                }


            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


            return null;
        }

        protected void onProgressUpdate(String...strings)
        {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.local_text_display);
            localTextView.append("\n");



            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */

    //code taken from PA1
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs)
        {
            //System.out.println(msgs[0]);
            //System.out.println(msgs[1]);

            try
            {

                String msgToSend = msgs[0];

                //https://docs.oracle.com/javase/7/docs/api/java/io/PrintWriter.html

                Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT0));
                PrintWriter p0=new PrintWriter(socket0.getOutputStream(),true);
                p0.println(msgToSend);
                p0.flush();
                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT1));
                PrintWriter p1=new PrintWriter(socket1.getOutputStream(),true);
                p1.println(msgToSend);
                p1.flush();
                Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT2));
                PrintWriter p2=new PrintWriter(socket2.getOutputStream(),true);
                p2.println(msgToSend);
                p2.flush();
                Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT3));
                PrintWriter p3=new PrintWriter(socket3.getOutputStream(),true);
                p3.println(msgToSend);
                p3.flush();
                Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT4));
                PrintWriter p4=new PrintWriter(socket4.getOutputStream(),true);
                p4.println(msgToSend);
                p4.flush();


            }
            catch (UnknownHostException e)
            {
                Log.e(TAG, "ClientTask UnknownHostException");
            }
            catch (IOException e)
            {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

}









