package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
import java.util.concurrent.ConcurrentHashMap;

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



public class SimpleDynamoProvider extends ContentProvider
{


	static final String TAG = SimpleDynamoProvider.class.getSimpleName();
	static final int SERVER_PORT = 10000;
	private Uri mUri=null;
	String assignedPort="";
	String rep1="";
	String rep2="";
	String queryVal="";
	String toSend="";
	String repMsg="";

	ConcurrentHashMap<String,String> cmap=new ConcurrentHashMap<String, String>();
	HashMap<String,ArrayList<String>> recoveryMap = new HashMap<String,ArrayList<String>>();
	HashMap<String,String> storageHistory=new HashMap<String, String>();
	//https://www.baeldung.com/java-initialize-hashmap
	String RecoveryQuery="";
	public static Map<String, String> mapper;
	static {
		mapper = new HashMap<String, String>();
		mapper.put("11108", "5554");
		mapper.put("11112", "5556");
		mapper.put("11116", "5558");
		mapper.put("11120", "5560");
		mapper.put("11124", "5562");
	}
	String deleteQuery="";
	String starQuery="";
	String repStore="";
	ArrayList<String> order = new ArrayList<String>(Arrays.asList("11108","11112","11116","11120","11124"));
	ArrayList<String> chord = new ArrayList<String>(Arrays.asList("11124","11112","11108","11116","11120"));
	ArrayList<String> hp=new ArrayList<String>();
	HashMap<String,String> keyAndValue=new HashMap<String, String>();
	String myPort="";
	String Newmsg="";
	HashMap<String,Integer> aliveAVD=new HashMap<String, Integer>();

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		// Do this
		// TODO Auto-generated method stub
		String filename = selection;
		String ret = "";
		Context context=this.getContext();

		deleteQuery="";
		//String port = (String) order.get(i);
		String toSend = "delete,"+selection;
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort, toSend);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e)
		{
			System.out.println("Error in InterruptedException thread");
			e.printStackTrace();
		}

		return 0;
		// TODO Auto-generated method stub
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}


	public void addFile(String key,String value)
	{
		FileOutputStream outputStream=null;
		Context context=this.getContext();
		try
		{
			//System.out.println("Key inside no of avds 1 is " + key);
			outputStream = context.openFileOutput(key, MODE_PRIVATE);
			outputStream.write(value.getBytes());
			outputStream.close();
			hp.add(key);
			keyAndValue.put(key,value);
			cmap.put(key,value);
			System.out.println("Added "+key+"in"+myPort);
			//System.out.println("Here msg "+key+"should be assigned to " + (myPort));
			Log.v("insert", key.toString());

		}
		catch (Exception e)
		{
			Log.e(TAG, "Error File write failed");
		}
	}

	public boolean deleteFile(String file)
	{
		Context context=this.getContext();
		return context.deleteFile(file);

	}


	//add getReplicate which returns what 2 ports to replicate to
	public String [] getReplicate(String assgHashPort,ArrayList orderHashed)
	{
		HashMap<String,String> repMap=new HashMap();
		//HashMap<String,String> repMap2=new HashMap();
		for(int i=0;i<orderHashed.size()-1;i++)
		{
			String a=(String)orderHashed.get(i);
			String b=(String)orderHashed.get(i+1);
			repMap.put(a,b);

		}

		String a=(String)orderHashed.get(order.size()-1);
		String b=(String)orderHashed.get(0);
		repMap.put(a,b);
		//System.out.println("Rep map is"+repMap);
		String arrayRep[]=new String[2];
		String rep1=repMap.get(assgHashPort);
		arrayRep[0]=rep1;
		String rep2=repMap.get(rep1);
		arrayRep[1]=rep2;
		return arrayRep;
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
				System.out.println("Error File not found");
			}
		}
		return ret;
	}




	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		// TODO Auto-generated method stub
		String value = (String) values.get("value");
		String key = (String) values.get("key");
		//System.out.println(value);
		System.out.println("Key is " + key);
		FileOutputStream outputStream = null;
		Context context = this.getContext();
		int time=1000;
		int flag=0;

		System.out.println("Port" + myPort);

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

			if (a.compareTo(x) < 0)
			{
				flag=1;

				assignedPort = (String)hashOrgcopy.get(orderHashed.get(ii));
				String assgHashedPort=(String)orderHashed.get(ii);
				String repArray[]=getReplicate(assgHashedPort,orderHashed);
				rep1=repArray[0];
				rep2=repArray[1];

				rep1=(String)hashOrgcopy.get(rep1);
				rep2=(String)hashOrgcopy.get(rep2);
				String arrayNew[]=new String[3];
				arrayNew[0]=assignedPort;
				arrayNew[1]=rep1;
				arrayNew[2]=rep2;


				repMsg="";
				repMsg = "rep" + key + "," +value+","+assignedPort+","+rep1+","+rep2;
				String recoveryMsg=key + "," +value;

				System.out.println("Assigned was " + assignedPort + "and rep is"+rep1+rep2+"for msg"+key);
				try {
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, assignedPort, repMsg);
					System.out.println("Client Task called for" + assignedPort + "for message" + repMsg);

					try {
						Thread.sleep(1200);
						//was 1200 when working
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				catch (Exception e)
				{
					System.out.println("Error while client Task rep call ClientTask UnknownHostException");
					System.out.println("Failure here adding to recoverymap");
					for(int h=0;h<arrayNew.length;h++) {
						if (recoveryMap.containsKey(arrayNew[h])) {
							ArrayList<String> xx = recoveryMap.get(arrayNew[h]);
							xx.add(recoveryMsg);
							recoveryMap.put(arrayNew[h], xx);
						} else {
							ArrayList<String> temp = new ArrayList<String>();
							temp.add(recoveryMsg);
							recoveryMap.put(arrayNew[h], temp);
							System.out.println("Can come here for Phase 1,2,3");
						}
					}
				}


			}
			if(flag==1)
			{
				return uri;
			}

		}


		repMsg = "";
		assignedPort = (String) hashOrgcopy.get(orderHashed.get(0));
		String assgHashedPort = (String) orderHashed.get(0);
		String repArray[] = getReplicate(assgHashedPort, orderHashed);
		rep1 = repArray[0];
		rep2 = repArray[1];

		rep1 = (String) hashOrgcopy.get(rep1);
		rep2 = (String) hashOrgcopy.get(rep2);
		repMsg = "rep" + key + "," +value+","+assignedPort+","+rep1+","+rep2;

		System.out.println("Assigned was " + assignedPort + "and rep is" + rep1 + rep2 + "for msg" + key);
		String arrayNew[]=new String[3];
		arrayNew[0]=assignedPort;
		arrayNew[1]=rep1;
		arrayNew[2]=rep2;
		String recoveryMsg=key + "," +value;

		System.out.println("Client Task called for" + assignedPort + "for message" + repMsg);
		try {
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, assignedPort, repMsg);

			try {
				Thread.sleep(1200);
				//was 1200 when working
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		catch (Exception e)
		{
			System.out.println("Error while client Task rep call ClientTask UnknownHostException");
		}

		return uri;

	}

	@Override
	public boolean onCreate()
	{
		// TODO Auto-generated method stub
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
			Log.e(TAG, "Error Can't create a ServerSocket");
			return false;
		}

		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort, "firsttime");

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		//System.out.println("Selection is "+selection);

		String filename = selection;
		String ret = "";
		Context context = this.getContext();
		String[] arr = {"key", "value"};
		//hp.remove("0");
		MatrixCursor c = new MatrixCursor(arr);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Query called for" + selection + "in" + myPort);

		//to find where key belongs
		int flag = 0;
		HashMap<String, String> hashOrgcopy = new HashMap<String, String>();
		ArrayList orderHashed = new ArrayList();
		for (int ji = 0; ji < order.size(); ji++) {
			try {
				String x = (String) order.get(ji);
				String temp = genHash(mapper.get(x));
				orderHashed.add(temp);
				hashOrgcopy.put(temp, x);
			} catch (NoSuchAlgorithmException e) {
				System.out.println("Error in genHash");
			}
		}
		Collections.sort(orderHashed);

		for (int ii = 0; ii < orderHashed.size(); ii++) {
			String a = "";
			String x = "";
			String y = "";
			try {
				a = genHash(selection);
				x = (String) orderHashed.get(ii);
				//y = genHash(mapper.get(orderHashed.get(ii+1)));

			} catch (NoSuchAlgorithmException e) {
				System.out.println("Error in genHash");
			}

			if (a.compareTo(x) < 0) {
				flag = 1;
				assignedPort = (String) hashOrgcopy.get(orderHashed.get(ii));
				String assgHashedPort = (String) orderHashed.get(ii);
				String repArray[] = getReplicate(assgHashedPort, orderHashed);
				rep1 = repArray[0];
				rep2 = repArray[1];

				rep1 = (String) hashOrgcopy.get(rep1);
				rep2 = (String) hashOrgcopy.get(rep2);

			}
		}
		if (flag == 0) {
			assignedPort = (String) hashOrgcopy.get(orderHashed.get(0));
			String assgHashedPort = (String) orderHashed.get(0);
			String repArray[] = getReplicate(assgHashedPort, orderHashed);
			rep1 = repArray[0];
			rep2 = repArray[1];

			rep1 = (String) hashOrgcopy.get(rep1);
			rep2 = (String) hashOrgcopy.get(rep2);
		}

		String[] findPort = new String[3];
		findPort[0] = assignedPort;
		findPort[1] = rep1;
		findPort[2] = rep2;


		//if not * or @ ir filename wala
		if (!selection.equals("*") && !selection.equals("@")) {
			//message present locally
			if (hp.contains(selection)) {
				ret = readFile(selection);
				String[] m = new String[2];
				m[0] = selection;
				m[1] = ret;
				c.addRow(m);
				System.out.println("Cursor returned");
				return c;
			}

			//message present in some other avd
			else {
				//call ClientTask here for all ports so as to get hp
				String port = (String) assignedPort;
				System.out.println("Finding" + selection + "in port" + port);
				String toSend = "query" + "," + selection + "," + assignedPort + "," + rep1 + "," + rep2;
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, port, toSend);
				try {
					Thread.sleep(1000);
					//was 1000
				} catch (InterruptedException e) {
					System.out.println("Error in InterruptedException thread");
					e.printStackTrace();
				}

				// write if condition here
				System.out.println("After client task completion return is" + cmap.get(selection) + "ie not found in" + port + "for" + selection);
				if (cmap.get(selection).length() > 1) {
					String[] m = new String[2];
					m[0] = selection;
					m[1] = cmap.get(selection);
					System.out.println("Query for key" + selection + "is" + cmap.get(selection));
					c.addRow(m);
					System.out.println("Cursor returned");
					Log.v("query", selection);
					return c;
				}


			}
		}


		//local
		if (selection.equals("@")) {
			// TODO Auto-generated method stub
			for (int i = 0; i < hp.size(); i++) {
				String key = hp.get(i);
				//https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
				System.out.println("Opening file key " + key);
				ret = readFile(key);
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
		if (selection.equals("*"))
		{
			System.out.println("Global dump");
			//String port=(String)order.get(i);
			String toSend = "StarQuery---------------------";
			new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, myPort, toSend);
			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
				System.out.println("Error in InterruptedException thread");
				e.printStackTrace();
			}
			// write if condition here
			System.out.println("After client task completion return is" + starQuery);
			String[] h = starQuery.split(",");
			for (int j = 0; j < h.length; j++) {
				if (h[j].length() > 5 && h[j + 1].length() > 5) {
					String[] m = new String[2];
					m[0] = h[j];
					m[1] = h[j + 1];
					c.addRow(m);
					j++;
				}

			}

			System.out.println("Cursor returned");
			Log.v("query", selection);
			return c;
		}

		return null;


	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
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
			int flag=0;
			ServerSocket serverSocket = sockets[0];
			mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
			System.out.println("Server Task  created");
			String toSend="";

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


					if(msg.length()>0) {
						if (msg.substring(0, 1).equals("-")) {
							String restOfmsg = msg.substring(1);
							System.out.println("Server Message from new Client task is" + restOfmsg);
							String[] h = restOfmsg.split(",");
							System.out.println("Server message sent from client after partitioning is" + h[0] + h[1]);
							String key = h[0];
							String value = h[1];
							addFile(key, value);
							toSend = "stored";

						}
					}


					if(msg.length()>1) {
						if (msg.substring(0, 3).equals("rep")) {
							flag = 1;
							System.out.println("Server Message from new Client task is" + msg);
							String restOfmsg = msg.substring(3);
							String[] h = restOfmsg.split(",");
							System.out.println("Server message sent from client after partitioning is" + h[0] + h[1]);
							String key = h[0];
							String value = h[1];
							addFile(key, value);
							toSend = "hi";

							PrintWriter p = new PrintWriter(s.getOutputStream(), true);
							System.out.println("Reply from server for key" + key + ":" + toSend);
							p.println(toSend);
							p.flush();
							s.close();

						}
					}

					if(msg.length()>6)
					{
						if(msg.substring(0,5).equals("query"))
						{
							flag=1;
							if(hp.contains(msg.substring(6)))
							{
								System.out.println("I have"+hp);
								toSend=readFile(msg.substring(6));
							}
							else
							{
								toSend="-";
							}
							PrintWriter p = new PrintWriter(s.getOutputStream(), true);
							System.out.println("Query reply server :" + toSend);
							p.println(toSend);
							p.flush();
						}
					}

					if(msg.length()>6)
					{
						if(msg.substring(0,6).equals("delete"))
						{
							flag=1;
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
							PrintWriter p = new PrintWriter(s.getOutputStream(), true);
							System.out.println("Delete :" + toSend);
							p.println(toSend);
							p.flush();
						}
					}



					if(msg.length()>6)
					{
						if(msg.substring(0,4).equals("Star"))
						{
							flag=1;
							toSend="";
							for(int u=0;u<hp.size();u++)
							{
								String file=hp.get(u);
								String k=readFile(file);
								toSend=toSend+file+","+k+",";
								System.out.println("tosend is"+toSend);

							}
							PrintWriter p = new PrintWriter(s.getOutputStream(), true);
							System.out.println("Query star :" + toSend);
							p.println(toSend);
							p.flush();

						}
					}

					if(msg.length()>6)
					{
						if(msg.substring(0,8).equals("recovery"))
						{
							flag=1;
							toSend="";
							String restOfmsg = msg.substring(8);
							String[] h = restOfmsg.split(",");
							System.out.println("Actually adding messages after recovery" + h[0] + h[1]);
							for(int n=0;n<h.length;n++)
							{
								String key = h[n];
								String value = h[n+1];
								System.out.println("Key is"+key+"for recovery and key is"+value);
								n++;
								addFile(key, value);

							}
							toSend="hi";

							PrintWriter p = new PrintWriter(s.getOutputStream(), true);
							p.println(toSend);
							p.flush();
							s.close();

						}
					}

					if(msg.length()>6)
					{
						if (msg.substring(0, 9).equals("firsttime"))
						{
							flag=1;
							/*toSend="";

							//String[] h = msg.split(",");
							//System.out.println("Server message sent from client after partitioning is" + h[0] + h[1]);
							//String firsttime = h[0];
							String extra="";
							//String port = h[1];
							//repMsg = "rep" + key + "," +value+","+assignedPort+","+rep1+","+rep2;
							*//*for (Map.Entry<String, ArrayList<String>> entry : recoveryMap.entrySet())
							{
								String key = entry.getKey();
								ArrayList<String> value = entry.getValue();
								for (int id = 0; id < value.size(); id++)
								{

								}
							}*//*


							for (int f=0;f<order.size();f++)
							{
								System.out.println("Recov map is"+recoveryMap);
								System.out.println("Selection is"+recoveryMap.get(order.get(f)));

								toSend="recovery";
								System.out.println("Sending is"+toSend);
								Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(order.get(f)));
								PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
								p0.println(toSend);
								//System.out.println("For query Messages sent is " + msgs[1]);
								p0.flush();

								InputStreamReader isr1 = new InputStreamReader(socket0.getInputStream());
								BufferedReader br1 = new BufferedReader(isr1);
								String returned = br1.readLine();
								recoveryMap.remove(order.get(f));
							}

							//recoveryMap.clear();
							*/

							PrintWriter p = new PrintWriter(s.getOutputStream(), true);
							System.out.println("Recovery message from server to client :" + toSend);
							p.println(toSend);
							p.flush();


						}
					}


					if(flag==0)
					{
						toSend="Hi";
						PrintWriter p = new PrintWriter(s.getOutputStream(), true);
						System.out.println("All for star :" + toSend);
						p.println(toSend);
						p.flush();
					}

					//System.out.println("Order is" + order);
					i++;

				}
			}

			catch (IOException e)
			{
				System.out.println("Error in IOException ");
				e.printStackTrace();
			}


			return null;
		}


	}



	//code taken from PA1
	private class ClientTask extends AsyncTask<String, Void, Void>
	{
		int flag=0;
		String returnedServer="";
		@Override
		protected Void doInBackground(String... msgs)
		{

				String msgToSend = msgs[1];

				if(msgs[1].length()>6)
				{
					if(msgs[1].substring(0,5).equals("query"))
					{
						//write query code here
						//last mai add return
						flag=1;

						String[] h = msgs[1].split(",");
						System.out.println("Server message sent from client after partitioning is" + h[0]+h[1]+h[2]+h[3]);
						String query = h[0];
						String key = h[1];
						String assign=h[2];
						String rep1=h[3];
						String rep2=h[4];

						String toSend="query,"+key;

						String[] repArray=new String[3];
						repArray[0]=assign;
						repArray[1]=rep1;
						repArray[2]=rep2;

						for(int u=0;u<repArray.length;u++) {

							try {
								System.out.println("Created query for" + key + repArray[u]);
								Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(repArray[u]));
								PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
								p0.println(toSend);
								System.out.println("For query Messages sent is " + msgs[1]);
								p0.flush();

								InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
								BufferedReader br = new BufferedReader(isr);
								queryVal = br.readLine();
								if (queryVal.length() > 2) {
									String stringArray[] = msgs[1].split(",");
									cmap.put(stringArray[1], queryVal);
									System.out.println("After query value returned is" + queryVal);
								}
							}
							catch (Exception e)
							{
								System.out.println("Client socket exception caught");
							}

						}
						//socket0.close();


					}

				}


				if(msgs[1].length()>6) {
					if (msgs[1].substring(0, 3).equals("rep"))
					{

						flag=1;
						String restOfmsg = msgs[1].substring(3);
						String[] h = restOfmsg.split(",");
						System.out.println("Server message sent from client after partitioning is" + h[0]+h[1]+h[2]+h[3]+"and I am in"+myPort);
						String key = h[0];
						String value = h[1];
						String assign=h[2];
						String rep1=h[3];
						String rep2=h[4];
						String toSend="rep"+key+","+value;

						String[] repArray=new String[3];
						repArray[0]=assign;
						repArray[1]=rep1;
						repArray[2]=rep2;
						int set=0;
						String recoveryMsg=key + "," +value;

						if(assign.equals(myPort))
						{
							addFile(key,value);
							set=1;
						}


						for(int u=set;u<repArray.length;u++)
						{
							returnedServer="";
							try {
								System.out.println("Rep for insert in" + repArray[u] + "for" + toSend);
								Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(repArray[u]));
								PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
								p0.println(toSend);
								System.out.println("For rep Messages sent is " + msgs[1] + "and to be stored in" + repArray[u]);
								p0.flush();

								InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
								BufferedReader br = new BufferedReader(isr);
								returnedServer = br.readLine();
								System.out.println("After rep for" + key + "returned is" + returnedServer);

								socket0.close();
							}
							catch(Exception e)
							{
								System.out.println("Added to recoverymap2");

								for(int hh=0;hh<repArray.length;hh++)
								{
									System.out.println("Recov Map is"+recoveryMap);
									if (recoveryMap.containsKey(repArray[hh])) {
										ArrayList<String> xx = recoveryMap.get(repArray[hh]);
										xx.add(recoveryMsg);
										recoveryMap.put(repArray[hh], xx);
									} else {
										ArrayList<String> temp = new ArrayList<String>();
										temp.add(recoveryMsg);
										recoveryMap.put(repArray[hh], temp);
										//System.out.println("Can come here for Phase 1,2,3");
									}
								}
							}
						}


					}
				}


				if(msgs[1].length()>6)
				{
					if(msgs[1].substring(0,6).equals("delete"))
					{
						//write query code here
						//last mai add return
						flag=1;
						deleteQuery="";

						try{
						Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(msgs[0]));
						PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
						p0.println(msgs[1]);
						System.out.println("For delete Messages sent is " + msgs[1]);
						p0.flush();

						InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
						BufferedReader br = new BufferedReader(isr);
						deleteQuery = br.readLine();
						System.out.println("After delete query value returned is" + deleteQuery);
						}
						catch (Exception e)
						{
							System.out.println("Client socket exception caught");
						}
						//socket0.close();

					}

				}


				if(msgs[1].length()>6)
				{
					if(msgs[1].substring(0,4).equals("Star"))
					{
						//write query code here
						//last mai add return
						flag=1;
						starQuery="";
						for(int u=0;u<order.size();u++)
						{
							try{
							Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(order.get(u)));
							PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
							p0.println(msgs[1]);
							System.out.println("For * query Messages sent is " + msgs[1]);
							p0.flush();

							InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
							BufferedReader br = new BufferedReader(isr);
							starQuery += br.readLine();
							System.out.println("After star query value returned is" + starQuery);
							}
							catch (Exception e)
							{
								System.out.println("Client socket exception caught");
							}
						}
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

						//socket0.close();
					}
					catch (UnknownHostException e)
					{
						Log.e(TAG, "Error while creating for repartition part ClientTask UnknownHostException");
						//continue ;
					}
					catch (IOException e)
					{
						Log.e(TAG, "Error ClientTask socket IOException");
						//continue ;
					}
				}


				else
				{
					if(flag==0)
					{

						String toSended="";
						if(msgs[1].equals("firsttime")) {
							toSended="firsttime";
						}

						//toSended+=","+msgs[0];
						//System.out.println("Recover map is"+recoveryMap+"for"+myPort);
						HashMap<String,ArrayList<String>> simpleReturnedMap = new HashMap<String,ArrayList<String>>();

						for(int u=0;u<order.size();u++)
						{

							if(!myPort.equals(order.get(u)))
							{
								try {
									Socket socket0 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(order.get(u)));
									PrintWriter p0 = new PrintWriter(socket0.getOutputStream(), true);
									p0.println(toSended);
									System.out.println("For recovery query Messages sent is " + toSended + "to" + order.get(u));
									p0.flush();

									InputStreamReader isr = new InputStreamReader(socket0.getInputStream());
									BufferedReader br = new BufferedReader(isr);
									RecoveryQuery = br.readLine();
									System.out.println("Returned is" + RecoveryQuery);
									//System.out.println("Length of recovery is" + RecoveryQuery.length());
								}
									/*if (RecoveryQuery.length() > 10)
									{
										String[] h = RecoveryQuery.split(";");
										for (int b = 0; b < h.length; b++)
										{
											String[] here = h[0].split(",");
											System.out.println("here is"+here);
											String port = here[0];
											String key = here[1];
											String value = here[2];
											String req = key + "," + value;
											//System.out.println("No issue till here");

											if (simpleReturnedMap.containsKey(port)) {
												ArrayList<String> xx = simpleReturnedMap.get(port);
												xx.add(req);
												simpleReturnedMap.put(port, xx);
											} else {
												ArrayList<String> temp = new ArrayList<String>();
												temp.add(req);
												simpleReturnedMap.put(req, temp);
											}

										}
										//System.out.println("No issue till here");

										String joined = "recovery";
										for (int t = 0; t < order.size(); t++)
										{
											try {

												Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(order.get(t)));
												PrintWriter p1 = new PrintWriter(socket1.getOutputStream(), true);
												ArrayList<String> d = simpleReturnedMap.get(order.get(t));
												System.out.println("d is"+Arrays.toString(d.toArray()));
												System.out.println("Fine till here");
												joined += TextUtils.join(",", d);

												p1.println(joined);
												System.out.println("For recovery actually adding in client Messages sent is " + joined);
												p1.flush();

												InputStreamReader isr1 = new InputStreamReader(socket1.getInputStream());
												BufferedReader br1 = new BufferedReader(isr1);
												String recMsg = br1.readLine();
											}
											catch(Exception e) {
												System.out.println("Exception caught inside here for"+order.get(t));
											}

										}

									}*/


								catch (UnknownHostException e) {
									Log.e(TAG, "Error while creating for repartition part ClientTask UnknownHostException");
									//continue ;
								} catch (IOException e) {
									Log.e(TAG, "Error ClientTask socket IOException");
									//continue ;
								} catch (Exception e) {
									System.out.println("Replicate failed");
								}
								//socket0.close();
							}
							}

					}
				}

			return null;
		}
	}



}
