package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static java.lang.Math.max;

public class SimpleDhtProvider extends ContentProvider
{

    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    ArrayList<String> nodesAlive=new ArrayList<String>();
    ArrayList<String> allNodes = new ArrayList<String>(Arrays.asList("11108","11112","11116","11120","11124"));
    //ArrayList<String> correctOrdering=new ArrayList<String>(Arrays.asList("11124","11112","11108","11116","11120"));
    static final int SERVER_PORT = 10000;
    private Uri mUri=null;
    String assignedPort="";
    String queryVal="";
    String toSend="";
    //String f="";
    HashMap<String,String> storageHistory=new HashMap<String, String>();
    //https://www.baeldung.com/java-initialize-hashmap
    public static Map<String, String> mapper;
    static {
        mapper = new HashMap<String, String>();
        mapper.put("11108", "5554");
        mapper.put("11112", "5556");
        mapper.put("11116", "5558");
        mapper.put("11120", "5560");
        mapper.put("11124", "5562");
    }
    int no0fAVDS=1;
    String deleteQuery="";
    String starQuery="";
    ArrayList order=new ArrayList();
    ArrayList<String> hp=new ArrayList<String>();
    HashMap<String,String> keyAndValue=new HashMap<String, String>();
    String myPort="";
    String Newmsg="";
    HashMap<String,Integer> aliveAVD=new HashMap<String, Integer>();
    int No=1;
    String predecessor="";
    String successor="";
    int count=0;



    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // Do this
        // TODO Auto-generated method stub
        String filename = selection;
        String ret = "";
        Context context=this.getContext();
        for(int i=0;i<order.size();i++)
        {
            deleteQuery="";
            String port = (String) order.get(i);
            String toSend = "delete,"+selection;
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, port, toSend);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(deleteQuery.equals(selection))
            {
                return 0;
            }

        }
        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        // TODO Auto-generated method stub
        return null;
    }


    public void repartition(String myPort)
    {
        System.out.println("Storage hist is"+storageHistory);
    }

    public void addFile(String key,String value)
    {
        FileOutputStream outputStream=null;
        Context context=this.getContext();
        try {
            System.out.println("Key inside no of avds 1 is " + key);
            outputStream = context.openFileOutput(key, MODE_PRIVATE);
            outputStream.write(value.getBytes());
            outputStream.close();
            hp.add(key);
            keyAndValue.put(key,value);
            System.out.println("Here msg "+key+"should be assigned to " + (myPort));
            //System.out.println("File written at"+myPort);
            Log.v("insert", key.toString());

        } catch (Exception e) {
            Log.e(TAG, "File write failed");
        }
    }

    public boolean deleteFile(String file)
    {
        Context context=this.getContext();
        return context.deleteFile(file);

    }


    public String readFile(String key)
    {
        String ret="";
        Context context=this.getContext();
        if(hp.contains(key))
        {
            try
            {
                InputStream inputStream = context.openFileInput(key);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                ret = bufferedReader.readLine();
            }
            catch(Exception e)
            {
                System.out.println("File not found");
            }
        }
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method
        String value = (String) values.get("value");
        String key = (String) values.get("key");
        //System.out.println(value);
        System.out.println("Key is " + key);
        FileOutputStream outputStream = null;
        Context context = this.getContext();
        try
        {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Port" + myPort);

        if (!myPort.equals("11108"))
        {
            System.out.println("Sending list to " + myPort);
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort, "list");
            try {
                Thread.sleep(300);
            } catch (Exception e) {
                System.out.println("Thread exception");
            }
        }

        //1 mark ka part
        if ((order.size() == 1 && myPort.equals("11108")) || order.size() == 0)
        {
            System.out.println("Order in if is" + order);
            addFile(key, value);
            System.out.println("Shoudl only work 5 times once for" + myPort);
        }

        //for all other marks
        else
            {
                //ArrayList orderOrgcopy=order;
                HashMap<String,String> hashOrgcopy=new HashMap<String, String>();
                ArrayList orderHashed=new ArrayList();
                for(int ji=0;ji<order.size();ji++)
                {
                    try{
                        String x=(String)order.get(ji);
                        String temp=genHash(mapper.get(x));
                        orderHashed.add(temp);
                        hashOrgcopy.put(temp,x);
                    }
                    catch (NoSuchAlgorithmException e)
                    {
                    System.out.println("Error in genHash");
                    }
                }
                Collections.sort(orderHashed);


            System.out.println("For key" + key + "order is" + orderHashed);
            System.out.println("Here Order is after calling client" + orderHashed);

                for (int ii = 0; ii < orderHashed.size(); ii++)
            {
                String a = "";
                String x = "";
                String y = "";
                try
                {
                    a = genHash(key);
                    x = (String)orderHashed.get(ii);
                    //y = genHash(mapper.get(orderHashed.get(ii+1)));

                } catch (NoSuchAlgorithmException e) {
                    System.out.println("Error in genHash");
                }

                //System.out.println("Here msg is" +key+"and i is"+ii);
                //System.out.println("a is" + a + "and x is" + x);
                //System.out.println("For x" + mapper.get(order.get(ii)) + "and" + orderHashed.get(ii));
                assignedPort = (String)hashOrgcopy.get(orderHashed.get(ii)) ;


                if (a.compareTo(x) < 0)
                {
                    System.out.println("Found in loop" + assignedPort);
                    toSend = "-" + key + "," + value;

                    if (assignedPort.equals(myPort))
                    {
                        addFile(key, value);
                        System.out.println("Assigned was " + assignedPort + "and saved in" + myPort + "itself");

                    }
                    else
                    {
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, assignedPort, toSend);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Repartition message Messages sent is " + toSend + "to" + assignedPort);
                    }
                    return uri;
                }
            }

            assignedPort = (String) hashOrgcopy.get(orderHashed.get(0));
            System.out.println("Found outside loop" + assignedPort);
            toSend = "-" + key + "," + value;

            if (assignedPort.equals(myPort))
            {
                addFile(key, value);
                System.out.println("Assigned was " + assignedPort + "and saved in" + myPort + "itself");
            } else
            {
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, assignedPort, toSend);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Repartition message Messages sent is " + toSend + "to" + assignedPort);
            }
            return uri;
        }
        return uri;
    }



    @Override
    public boolean onCreate()
    {

        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        //myPort=f;
        System.out.println("This is myPort"+myPort);

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
            return false;
        }

        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort, "firsttime");

        return true;

    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder)
    {
        //System.out.println("Selection is "+selection);
        String filename = selection;
        String ret = "";
        Context context = this.getContext();
        String[] arr = {"key", "value"};
        //hp.remove("0");
        MatrixCursor c = new MatrixCursor(arr);
        System.out.println("Order in Query is"+order);

        //1 marks wala case
        if ((order.size()==1 && myPort.equals("11108")) || order.size()==0)
        {
            System.out.println("Inside if condition");
            if (selection.equals("*") || selection.equals("@"))
            {
                // TODO Auto-generated method stub
                for (int i = 0; i < hp.size(); i++)
                {
                    String key=hp.get(i);
                    //https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
                    System.out.println("Opening file key " + key);
                    ret=readFile(key);
                    //http://www.zoftino.com/android-matrixcursor-example
                    String[] m = new String[2];
                    m[0] = key;
                    m[1] = ret;
                    c.addRow(m);
                }
                System.out.println("Cursor returned");
                return c;

            }


            else {
                    ret=readFile(filename);
                    //http://www.zoftino.com/android-matrixcursor-example
                    String[] m = new String[2];
                    m[0] = selection;
                    m[1] = ret;
                    c.addRow(m);
                    System.out.println("Cursor returned");
                    return c;

                }

        }


        // other than 1 marks wala case
        else

            {
            System.out.println("Number of avds for query greater than one");

            //if not * or @ ir filename wala
            if(!selection.equals("*") && !selection.equals("@"))
            {
                    //message present locally
                    if(hp.contains(selection))
                  {
                      ret=readFile(selection);
                      String[] m = new String[2];
                      m[0] = selection;
                      m[1] = ret;
                      c.addRow(m);
                      System.out.println("Cursor returned");
                      return c;
                  }

                  //message present in some other avd
                  else
                  {
                      for(int i=0;i<order.size();i++)
                      {
                          //call ClientTask here for all ports so as to get hp
                          String port=(String)order.get(i);
                          String toSend="query,"+selection;
                          new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, port, toSend);
                          try {
                              Thread.sleep(300);
                          } catch (InterruptedException e) {
                              e.printStackTrace();
                          }
                          // write if condition here
                          System.out.println("After client task completion return is"+queryVal);
                          if(queryVal.length()>1)
                          {
                              String[] m = new String[2];
                              m[0] = selection;
                              m[1] = queryVal;
                              c.addRow(m);
                              System.out.println("Cursor returned");
                              Log.v("query", selection);
                              return c;
                          }

                      }
                  }
            }


            //local
            if (selection.equals("@"))
            {
                // TODO Auto-generated method stub
                for (int i = 0; i < hp.size(); i++)
                {
                    String key = hp.get(i);
                    //https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
                    System.out.println("Opening file key " + key);
                    ret=readFile(key);
                    //http://www.zoftino.com/android-matrixcursor-example
                    String[] m = new String[2];
                    m[0] = key;
                    m[1] = ret;
                    c.addRow(m);
                }
                System.out.println("Cursor returned");
                return c;
            }

            //global
            if(selection.equals("*"))
            {
                System.out.println("Global dump");
                for(int i=0;i<order.size();i++)
                {
                    String port=(String)order.get(i);
                    String toSend="StarQuery---------------------";
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, port, toSend);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // write if condition here
                    System.out.println("After client task completion return is"+starQuery);
                    String[] h=starQuery.split(",");
                    for(int j=0;j<h.length;j++)
                    {
                        if(h[j].length()>5 && h[j+1].length()>5) {
                            String[] m = new String[2];
                            m[0] = h[j];
                            m[1] = h[j + 1];
                            c.addRow(m);
                            j++;
                        }

                    }

                }

                System.out.println("Cursor returned");
                Log.v("query", selection);
                return c;

            }


         }
        return null;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException
    {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private Uri buildUri(String scheme, String authority)
    {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

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
            ServerSocket serverSocket = sockets[0];
            mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
            System.out.println("Server Task  created");
            String toSend="";
            //HashMap<String,String> predecessor=new HashMap<String, String>();
            //HashMap<String,String> successor=new HashMap<String, String>();

            try {
                int i = 0;
                while (true)
                {
                    Socket s = serverSocket.accept();
                    //creates object of input stream
                    InputStreamReader isr = new InputStreamReader(s.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String msg = br.readLine();
                    System.out.println("Message from client is " + msg);


                    if(msg.substring(0,1).equals("-"))
                    {
                        String restOfmsg=msg.substring(1);
                        System.out.println("Server Message from new Client task is"+restOfmsg);
                        String[] h=restOfmsg.split(",");
                        System.out.println("Server message sent from client after partitioning is"+h[0]+h[1]);
                        String key=h[0];
                        String value=h[1];
                        addFile(key,value);

                    }


                    if (!order.contains(msg) && msg != null && allNodes.contains(msg) )
                    {
                        order.add(msg);
                    }
                    System.out.println("to confirm order is");

                    if(msg.equals("list"))
                    {
                        Collections.sort(order);
                        toSend = TextUtils.join("", order);
                    }

                    else
                    {
                        toSend=Integer.toString(order.size());
                    }

                    if(msg.length()>6)
                    {
                        System.out.println("Inside first if should go always");
                        if(msg.substring(0,5).equals("query"))
                        {
                            System.out.println("Inside second if");
                            if(hp.contains(msg.substring(6)))
                            {
                                System.out.println("I have"+hp);
                                toSend=readFile(msg.substring(6));
                            }
                            else
                            {
                                toSend="";
                            }
                        }
                    }

                    if(msg.length()>6)
                    {
                        System.out.println("Inside first if should go always");
                        if(msg.substring(0,6).equals("delete"))
                        {
                            String filetoDelete=msg.substring(7);
                            if(hp.contains(filetoDelete))
                            {
                                boolean deleted=deleteFile(filetoDelete);
                                System.out.println("Boolean is"+deleted);
                                toSend=filetoDelete;
                            }
                            else
                            {
                                toSend="Not";
                            }
                        }
                    }



                    if(msg.length()>6)
                    {
                        System.out.println("Inside first if should go always");
                        if(msg.substring(0,4).equals("Star"))
                        {
                            toSend=toSend+",";
                            for(int u=0;u<hp.size();u++)
                            {
                                String file=hp.get(u);
                                String k=readFile(file);
                                toSend=toSend+file+","+k+",";
                            }
                        }
                    }


                    PrintWriter p = new PrintWriter(s.getOutputStream(), true);
                    System.out.println("All for star :" + toSend);
                    p.println(toSend);
                    p.flush();


                    System.out.println("Order is" + order);
                    i++;

                }
            }

            catch (IOException e)
            {
                e.printStackTrace();
            }


            return null;
        }


    }



    //code taken from PA1
    private class ClientTask extends AsyncTask<String, Void, Void>
    {
        int flag=0;
        @Override
        protected Void doInBackground(String... msgs)
        {
            try
            {
                System.out.println("Msgs size is"+msgs.length);
                String msgToSend = msgs[1];
                //myPort=msgs[0];
                System.out.println("Before client task"+msgs[0]);
                System.out.println("Client Task created for port"+msgs[0]);

                if(msgs[1].length()>6)
                {
                    if(msgs[1].substring(0,5).equals("query"))
                    {
                        //write query code here
                        //last mai add return
                        flag=1;
                        Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[0]));
                        PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
                        p0.println(msgs[1]);
                        System.out.println("For query Messages sent is " + msgs[1]);
                        p0.flush();

                        InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        queryVal = br.readLine();
                        System.out.println("After query value returned is"+queryVal);
                        socket0.close();

                        //return null;
                    }

                }

                if(msgs[1].length()>6)
                {
                    if(msgs[1].substring(0,6).equals("delete"))
                    {
                        //write query code here
                        //last mai add return
                        flag=1;
                        Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[0]));
                        PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
                        p0.println(msgs[1]);
                        System.out.println("For delete Messages sent is " + msgs[1]);
                        p0.flush();

                        InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        deleteQuery = br.readLine();
                        System.out.println("After delete query value returned is"+deleteQuery);
                        socket0.close();

                    }

                }


                if(msgs[1].length()>6)
                {
                    if(msgs[1].substring(0,4).equals("Star"))
                    {
                        //write query code here
                        //last mai add return
                        flag=1;
                        Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[0]));
                        PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
                        p0.println(msgs[1]);
                        System.out.println("For * query Messages sent is " + msgs[1]);
                        p0.flush();

                        InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        starQuery = br.readLine();
                        System.out.println("After star query value returned is"+starQuery);
                        socket0.close();

                    }

                }






                if(msgToSend.substring(0,1).equals("-"))
                {
                    String restOfmsg=msgToSend.substring(1);
                    System.out.println("Message from new Client task is "+restOfmsg+"to be sent to"+msgs[0]);
                    //String key=restOfmsg.split(",");
                    //String value=;

                    try
                    {
                        Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[0]));
                        PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
                        p0.println(msgToSend);
                        System.out.println("Messages sent is " + msgToSend);
                        p0.flush();

                        InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        Newmsg = br.readLine();
                        //no0fAVDS=Integer.parseInt(Newmsg);
                        //System.out.println("We have"+no0fAVDS+"AVDS alive");

                        socket0.close();
                    }
                    catch (UnknownHostException e)
                    {
                        Log.e(TAG, "Error while creating for repartition part ClientTask UnknownHostException");
                        //continue ;
                    }
                    catch (IOException e)
                    {
                        Log.e(TAG, "ClientTask socket IOException");
                        //continue ;
                    }
                }

                /*else if(msgs[1].substring(0,5).equals("query"))
                {
                    //write query code here

                }
                */

                else
                    {
                        if(flag==0)
                        {

                    String toSended="";
                    if(msgs[1].equals("firsttime")) {
                        toSended=msgs[0];
                    }
                    else {
                        toSended=msgs[1];
                    }
                    //https:docs.oracle.com/javase/7/docs/api/java/io/PrintWriter.html
                    try
                    {
                        Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt("11108"));
                        System.out.println("Socket successfully created for 11108");
                        System.out.println("My port is"+msgs[0]);
                        PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
                        p0.println(toSended);
                        //p0.println(myPort);
                        System.out.println("Messages sent is " + toSended);
                        p0.flush();

                        InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
                        Newmsg="";
                        int len=0;
                        BufferedReader br = new BufferedReader(isr);
                        while((Newmsg = br.readLine()) != null)
                        {
                            try
                            {
                                System.out.println("Before if else"+Newmsg);
                                len=Newmsg.length();
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
                        if(len%5==0)
                        {
                            int z=0;
                            while(z<len)
                            {
                                String a=Newmsg.substring(z,z+5);
                                if(!order.contains(a)) {
                                    order.add(a);
                                }
                                z+=5;
                            }
                            System.out.println("List after sending to everybody now is"+order);
                        }
                        else
                        {
                            System.out.println("We have "+Newmsg+"AVDS");
                            //no0fAVDS=Integer.parseInt(Newmsg);
                        }

                        //socket0.close();
                    }
                    catch (UnknownHostException e)
                    {
                        Log.e(TAG, "Error in normal running ClientTask UnknownHostException");
                        //continue ;
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException");
                        //continue ;
                    }
                    }
                    }
            }
            catch (Exception e)
            {
                Log.e(TAG, "Error Broke here ClientTask UnknownHostException");
                e.printStackTrace();
            }
            return null;
        }
    }
}
