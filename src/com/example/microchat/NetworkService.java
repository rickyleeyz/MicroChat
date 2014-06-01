package com.example.microchat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class NetworkService extends Service {
    private NotificationManager nm;
    Thread NetworkThread;

    private static boolean isRunning = false;

	final static boolean D = true;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_TO_SEND = 3;
    static final int MSG_RECIEVED = 4;
    
		
    DatagramSocket 	serverSocket;
	final static int PORT = 6666;
	final static int PORT_send=6666; 
	boolean 		socketOK=true;

	InetAddress 	myIPAddress; 			
	InetAddress 	myPeerAddress;

	final static private String TAG = "NetworkService";
   
    final Messenger mMessenger = new Messenger(new ServiceHandler()); // Target we publish for clients to send messages to IncomingHandler.


    
    class ServiceHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
            	//Binding message
                mClients.add(msg.replyTo);
                Log.i("ChatBind", "received");
                break;
            case MSG_UNREGISTER_CLIENT:
            	//Unbing message
                mClients.remove(msg.replyTo);
                Log.i("ChatBind", "unBinded");
                break;
            case MSG_TO_SEND:
            	//retrieve data from msg and sent to the remote
            	String PeerAddrToSend=msg.getData().getString("peerAddress");
            	String MessageToSend=msg.getData().getString("content");
            	try{
            		sendMessageToNetwork(PeerAddrToSend,MessageToSend);
            	}catch(Exception e){
            		Log.e(TAG,"Cannot send message"+e.getMessage());
            	}         	
            	break;            	
            default:
                super.handleMessage(msg);
            }
        }
    }
    
    

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyService", "Service Started.");
        
        isRunning = true;
        
        //Create Socket
        try
		{
			serverSocket = new DatagramSocket(PORT);
			Log.i(TAG,"trying to create the datagram...");
			serverSocket.setBroadcast(true);
			//showNotification("login");

	        //Notification You have logging in. 
		} 
		catch(Exception e)
		{
			Log.e(TAG,"Cannot open socket"+e.getMessage());
			socketOK = false;
			return;
		}
        
        //Get myIPAddress
        try
		{
			getMyWiFiIPAddress();
			Log.i(TAG,"My IP address:"+myIPAddress);
		}
		catch(Exception e)
		{
			Log.e(TAG,"Cannot get my own Broadcast IP address");
		}
        
        
        
        //Create Thread
        NetworkThread = new Thread(new Runnable(){
        	public void run(){
        		byte[] receiveData = new byte[1024]; 
        		while(socketOK) 
        		{ 
        			Log.i("socket","is working");
        			DatagramPacket receivePacket = 
        			    new DatagramPacket(receiveData, receiveData.length); 
        			try
        			{
        				Log.i("socket","is receiving");
        				serverSocket.receive(receivePacket);
        		        Log.i(TAG,"Received a packet");
        		        InetAddress sourceIPAddress = receivePacket.getAddress();
        		        Log.d(TAG,"Received a packet | Source IP Address: "+sourceIPAddress);
        		        //if(!sourceIPAddress.equals(myIPAddress))
        		        //{//If the message not sent by my computer
        		        	String sentence = new String(receivePacket.getData(),0,receivePacket.getLength()); 
        		        	
        		        	sendMessageToUI(sourceIPAddress.getHostAddress(),sentence);
        		        	Log.i(TAG,"Received sentence: "+sentence);
        		        //}
        		        
        			} 
        			catch (Exception e)
        			{
        				Log.e(TAG,"Problems receiving packet: "+e.getMessage());
        				socketOK = false;
        			} 
        		}
        	}
        	
        });
        
        NetworkThread.start();
        if(NetworkThread.isAlive()){
        	Log.i("NetworkThread", "is alive");
        }
        		
    }
    private void showNotification(String type,String sourceIP,String message) {
    	//If the app is out of running, show a notification to the desktop.
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(type=="login"){
        	CharSequence text = getText(R.string.user_login);
        	Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Chat.class), 0);
        	notification.setLatestEventInfo(this, getText(R.string.user_login), text, contentIntent);
        	nm.notify(R.string.user_login, notification);
        }
        else if(type=="NewMessage"){
        	//CharSequence text = getText(R.string.user_login);
        	CharSequence text = message;
        	Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        	Intent intent = new Intent(this, Chat.class); 
            intent.putExtra("peerAddr", sourceIP); 
            
            //When user click the notification, bring him to the chating room directly.
        	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        	notification.setLatestEventInfo(this, getText(R.string.new_message), text, contentIntent);
        	nm.notify(R.string.new_message, notification);        	
        	
        }
        
       
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //close thread for networking.
        serverSocket.close();
        NetworkThread.stop();
        Log.i("MyService", "Service Stopped.");
        isRunning = false;
    }
    
    private void sendMessageToUI(String sourceIPAddress,String content) {
    	Log.i("mClient", String.valueOf(mClients.size()));
    	if (mClients.size()==0){
    		//notification
    		//chatHistory.add.
    		Utils.chatHistory.addSnts(sourceIPAddress, sourceIPAddress, content);
    		//The application has been minized, show new message through notification 
    		showNotification("NewMessage",sourceIPAddress,content);
    		
    	}
    	else{
    		 for (int i=mClients.size()-1; i>=0; i--) {
    	            try {    	                
    	                //Send data as a String
    	                Bundle b = new Bundle();
    	                b.putString("peerAddress", sourceIPAddress);
    	                b.putString("content", content);
    	                Log.i("IncomingMessage", content);
    	                Message msg = Message.obtain(null, MSG_RECIEVED);
    	                msg.setData(b);
    	                mClients.get(i).send(msg);

    	            } catch (RemoteException e) {
    	                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
    	                mClients.remove(i);
    	            }
    	        }
    		
    	}       
    }
    
    
    
    private void sendMessageToNetwork(String PeerAddrToSend,String msgToSend) throws IOException {
    	byte[] sendData  = new byte[1024]; 
		sendData = msgToSend.getBytes(); 

		myPeerAddress=InetAddress.getByName(PeerAddrToSend);
		//Log.i(TAG,"myPeerAddress"+myPeerAddress);
		DatagramPacket sendPacket =
			new DatagramPacket(sendData, sendData.length, myPeerAddress, PORT_send);
		
		serverSocket.send(sendPacket);
        Log.i(TAG,"Sent packet: "+msgToSend);
        if(NetworkThread.isAlive()){
        	Log.i("NetworkThread", "is alive");
        }
        if(socketIsOK()){
        	Log.i("socket", "is alive");
        }
    	//network operation
    }
    
    boolean socketIsOK()
	{
	  return socketOK;
	}
    
    //getWiFi?
    private void getMyWiFiIPAddress() throws UnknownHostException
    {
        WifiManager mWifi = (WifiManager) (getSystemService(Context.WIFI_SERVICE));
        WifiInfo info = mWifi.getConnectionInfo();
        if(info==null)
        {
            if(D) Log.e(TAG,"Cannot Get WiFi Info");
            return;
        }
        else
        {
        	if(D) Log.d(TAG,"\n\nWiFi Status: " + info.toString());
        }
		  
        DhcpInfo dhcp = mWifi.getDhcpInfo(); 
        if (dhcp == null) 
        { 
          Log.d(TAG, "Could not get dhcp info"); 
          return; 
        } 

        int myIntegerIPAddress = dhcp.ipAddress;

        byte[] quads = new byte[4]; 
        for (int k = 0; k < 4; k++) 
           quads[k] = (byte) ((myIntegerIPAddress>> k * 8) & 0xFF);

        myIPAddress = InetAddress.getByAddress(quads);
        Utils.chatHistory.setMyIPAddr(myIPAddress.getHostAddress());
        //Is it a good way here..?
        
        //mHandler.obtainMessage(Chat.INFO,"MyIP:"+myIPAddress).sendToTarget(); 
        //Send self address to activities.
//        if (mClients.size()!=0){
//    		 for (int i=mClients.size()-1; i>=0; i--) {
//    	            try {
//    	                
//    	                //Send data as a String
//    	                Bundle b = new Bundle();
//    	                b.putString("myAddress", "MyIP:"+myIPAddress.getHostAddress());
//    	                Log.i("my IPAddress", myIPAddress.getHostAddress());
//    	                //send ... 
//    	                Message msg = Message.obtain(null, Chat.INFO);
//    	                msg.setData(b);
//    	                mClients.get(i).send(msg);
//
//    	            } catch (RemoteException e) {
//    	                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
//    	                mClients.remove(i);
//    	            }
//    	        }    		
//    	} 
    }
}