package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;


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

    int max=0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        //code from2A
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        System.out.println("This is myPort");
        System.out.println(myPort);

        try
        {
            System.out.println("Server partial");
            System.out.println(SERVER_PORT);
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


        final Button btn=(Button)findViewById(R.id.button4);
        // implement onClickListener and use socket to send msg to all AVDS

        //https://developer.android.com/reference/android/widget/Button
        //code from 2A
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
    public boolean onCreateOptionsMenu(Menu menu) {
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

        HashMap<Integer,String> store =new HashMap<Integer, String>();
        HashMap<Integer,String> CP =new HashMap<Integer, String>();

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

            ServerSocket serverSocket = sockets[0];
            ContentValues c = new ContentValues();
            mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
            int j=0;
            int i=0;
            int fNO=0;
            int count=0;
            int xy=0;
            try
            {
                //basic client server code taken from 2A
                while(true) {
                    Socket s = serverSocket.accept();
                    InputStreamReader isr = new InputStreamReader(s.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    PrintWriter p = new PrintWriter(s.getOutputStream(), true);
                    String msg = br.readLine();
                    count++;

                    int length = msg.length();
                    String portno = msg.substring(0, 5);
                    String msgReceived = msg.substring(6, length);
                    System.out.println("Received is" + msgReceived);

                    //Thread.sleep(1500);
                    String msgNew = Integer.toString(i);
                    System.out.println("Sending proposed " + msgNew);
                    p.println(msgNew);
                    p.flush();
                    //publishProgress(msgReceived);
                    //System.out.println("Sent is "+msgNew);



                    String x = "";
                    //s.setSoTimeout(300);

                    //loop till x is not null
                    //https://stackoverflow.com/questions/36209938/while-loop-with-a-readline
                    //https://stackoverflow.com/questions/13405822/using-bufferedreader-readline-in-a-while-loop-properly
                    while((x = br.readLine()) != null)
                    {
                        try {

                            fNO = Integer.parseInt(x);
                            System.out.println("Final sequence number after consensus is" + fNO);
                            break;
                        }
                        catch (NullPointerException e)
                        {
                            continue;
                        }
                        catch (NumberFormatException e)
                        {
                            continue;
                        }
                    }

                    //take max of proposed and agreed sequence
                    if(fNO>=0)
                    {
                        xy=Math.max(i,fNO);
                    }

                    int temp=i;

                    CP.put(xy,msgReceived);
                    System.out.println(CP);

                    //Thread.sleep(500);
                    //below part for contentvalue is taken from PA2A description
                    c.put("key", Integer.toString(xy));
                    c.put("value", msgReceived);
                    getContentResolver().insert(mUri, c);

                    //update local sequence number
                    i=xy;
                    i++;

                    // Displaying the message
                    //https://stackoverflow.com/questions/10462819/get-keys-from-hashmap-in-java
                    try
                    {

                        store.put(xy, msgReceived);
                        for (int key : store.keySet()) {
                            if (temp >= key) {
                                publishProgress(store.get(key));
                                store.remove(key);
                            }
                        }

                        // when all messages passed and some still left in hashmap
                        List<Integer> hm = new ArrayList<Integer>(store.keySet());
                        if(count>=25 && hm.size()>0)
                        {
                            for ( int key : store.keySet() )
                            {
                                publishProgress(store.get(key));
                                store.remove(key);
                            }
                        }

                    }
                    catch(Exception e)
                    {
                        System.out.println("Error ");
                    }

                    //s.close();

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

    //code taken from PA2A
    private class ClientTask extends AsyncTask<String, Void, Void>
    {

        HashMap<Integer,Integer> fnList =new HashMap<Integer, Integer>();

        @Override
        protected Void doInBackground(String... msgs)
        {
            int seq=0;
            boolean s0=true;
            boolean s1=true;
            boolean s2=true;
            boolean s3=true;
            boolean s4=true;
            int proposedNo0;
            int proposedNo1;
            int proposedNo2;
            int proposedNo3;
            int proposedNo4;
            int maxPro;
            try
            {
                String msgToSend = msgs[0];
                System.out.println(msgToSend);
                //int y=msgToSend.length();
                //String compare=msgToSend.substring(0,y-1);
                //System.out.println(compare);
                //System.out.println("Length of compare is "+compare.length());
                String portNo=msgs[1];
                //https://docs.oracle.com/javase/7/docs/api/java/io/PrintWriter.html
                List<Integer> pro=new ArrayList<Integer>();
                List<Integer> avd=new ArrayList<Integer>();
                pro.add(seq);
                String send = portNo + Integer.toString(seq) + msgToSend;
                //seen.add(compare);


                //Socket 0
                // basic client server code from 2A
                Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 11108);
                PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
                InputStreamReader isr0=new InputStreamReader(socket0.getInputStream());
                BufferedReader br0=new BufferedReader(isr0);

                p0.println(send);
                p0.flush();
                String receivedMsg0="";
                try
                {
                    receivedMsg0=br0.readLine();
                    System.out.println("returned is "+receivedMsg0);
                    proposedNo0=Integer.parseInt(receivedMsg0);
                    pro.add(proposedNo0);
                    avd.add(5554);

                }

                catch(Exception e)
                {
                    //pro.add(fnList.get(0));
                    s0=false;
                    Log.e(TAG, "Null ClientTask 0 socket IOException");
                }




                //Socket 1
                Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 11112);
                PrintWriter p1 = new PrintWriter(socket1.getOutputStream(), true);
                InputStreamReader isr1=new InputStreamReader(socket1.getInputStream());
                BufferedReader br1=new BufferedReader(isr1);
                p1.println(send);
                p1.flush();
                String receivedMsg1="";
                try
                {
                    receivedMsg1=br1.readLine();
                    System.out.println("returned is "+receivedMsg1);
                    proposedNo1=Integer.parseInt(receivedMsg1);
                    pro.add(proposedNo1);
                    avd.add(5556);
                }


                catch(Exception e)
                {
                    //pro.add(fnList.get(1));
                    s1=false;
                    Log.e(TAG, "Null ClientTask 1 socket IOException");
                }






                //Socket 2
                Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 11116);
                PrintWriter p2 = new PrintWriter(socket2.getOutputStream(), true);
                InputStreamReader isr2=new InputStreamReader(socket2.getInputStream());
                BufferedReader br2=new BufferedReader(isr2);
                p2.println(send);
                p2.flush();
                String receivedMsg2="";
                try
                {
                    receivedMsg2=br2.readLine();
                    System.out.println("returned is "+receivedMsg2);
                    proposedNo2=Integer.parseInt(receivedMsg2);
                    pro.add(proposedNo2);
                    avd.add(5558);
                }

                catch(Exception e)
                {
                    //pro.add(fnList.get(2));
                    s2=false;
                    Log.e(TAG, "Null ClientTask 2 socket IOException");

                }



                //Socket 3
                Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 11120);
                PrintWriter p3 = new PrintWriter(socket3.getOutputStream(), true);
                InputStreamReader isr3=new InputStreamReader(socket3.getInputStream());
                BufferedReader br3=new BufferedReader(isr3);
                p3.println(send);
                p3.flush();
                String receivedMsg3="";
                try
                {
                    receivedMsg3=br3.readLine();
                    System.out.println("returned is "+receivedMsg3);
                    proposedNo3=Integer.parseInt(receivedMsg3);
                    pro.add(proposedNo3);
                    avd.add(5560);
                }


                catch(Exception e)
                {
                    //pro.add(fnList.get(3));
                    s3=false;
                    Log.e(TAG, "Null ClientTask 3 socket IOException");
                }




                //Socket 4
                Socket socket4 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 11124);
                PrintWriter p4 = new PrintWriter(socket4.getOutputStream(), true);
                InputStreamReader isr4=new InputStreamReader(socket4.getInputStream());
                BufferedReader br4=new BufferedReader(isr4);
                p4.println(send);
                p4.flush();
                String receivedMsg4="";
                try
                {
                    receivedMsg4=br4.readLine();
                    System.out.println("returned is "+receivedMsg4);
                    proposedNo4=Integer.parseInt(receivedMsg4);
                    pro.add(proposedNo4);
                    avd.add(5562);
                }

                catch(Exception e)
                {
                    //pro.add(fnList.get(4));
                    s4=false;
                    Log.e(TAG, "Null ClientTask 4 socket IOException");
                }

                String maxProposed="";
                seq++;

                // take max of all proposed numbers and multicast
                System.out.println(pro);
                maxPro = Collections.max(pro);
                maxProposed =Integer.toString(maxPro) ;


                try{
                    p0.println(maxProposed);
                }
                catch(Exception e){
                    System.out.println("AVD 0 dead");
                }
                try{
                    p1.println(maxProposed);
                }
                catch(Exception e){
                    System.out.println("AVD 1 dead");
                }
                try{
                    p2.println(maxProposed);
                }
                catch(Exception e){
                    System.out.println("AVD 2 dead");
                }
                try{
                    p3.println(maxProposed);
                }
                catch(Exception e){
                    System.out.println("AVD 3 dead");
                }
                try{
                    p4.println(maxProposed);
                }
                catch(Exception e){
                    System.out.println("AVD 4 dead");
                }

                System.out.println("Final seq Number sent to most or all");
                //Thread.sleep(500);

                //socket0.close();
                //socket1.close();
                //socket2.close();
                //socket3.close();
                //socket4.close();

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
