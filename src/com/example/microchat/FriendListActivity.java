package com.example.microchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendListActivity extends Activity  {
	//List<String>  friends;
	List<String>  friendsOnShow = new ArrayList<String>();
	//ArrayList<UserPreference> friends;
	ArrayList<UserPreference> emptylist = new ArrayList<UserPreference>();
	
	FriendlistAdapter	friendItems;
	ListView friendlist;
	ImageButton logout;
	boolean mIsBound;

	final static boolean D = true;
	final static private String TAG = "Friendlist";
	
	final Messenger FLMessenger = new Messenger(new FLHandler());
	Messenger NetworkMessager = null;
	
	class FLHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        	if(D) Log.d(TAG, "In the Handler");
        	switch (msg.what) 
        	{
	            case NetworkService.MSG_RECIEVED:
	            	//New message coming
	            	String incomingPeerAddr=msg.getData().getString("peerAddress");
	            	String incomingMessage=msg.getData().getString("content");
	            	
	            	//Shake
	            	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	            	v.vibrate(300);
	            		
	            	//Notify by Toast
	            	if(!friendsOnShow.contains(incomingPeerAddr)){
	            		friendItems.add(new UserPreference(incomingPeerAddr));
	            		friendsOnShow.add(incomingPeerAddr);
	            	}

	            	String toastToMake = incomingPeerAddr+" send you an message";
	            	Toast.makeText(getApplicationContext(), toastToMake, Toast.LENGTH_SHORT).show();

	            	Utils.chatHistory.addSnts(incomingPeerAddr,"You",incomingMessage);
	            	Utils.chatHistory.setUnRead(incomingPeerAddr);

            		friendItems.notifyDataSetChanged();
	            	
	            	// add unread marks, waiting to be read.	            		
	            	Log.i("IncomingMessage", incomingMessage);	            	
	            	break;
	            default:
	                super.handleMessage(msg);
            }
        }
    };
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            NetworkMessager = new Messenger(service);
            try {
                Message msg = Message.obtain(null, NetworkService.MSG_REGISTER_CLIENT);
                msg.replyTo = FLMessenger;
                NetworkMessager.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            NetworkMessager = null;
            //textStatus.setText("Disconnected.");
        }
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendlist);
        
        friendlist=(ListView)findViewById(R.id.friendlist);
        logout=(ImageButton)findViewById(R.id.logout);
      
    }
	
	@Override
	public void onStart(){
		super.onStart();
		
		friendItems=new FriendlistAdapter(this, emptylist);
        friendlist.setAdapter(friendItems);
        friendItems.clean();

        Collection<UserPreference> friends = Utils.chatHistory.returnFriendList();

        friendsOnShow=Utils.chatHistory.returnFriendIPs();
        
		Iterator it= friends.iterator();
        
        while(it.hasNext()){
        	Log.i("add frienditem", "enter the loop");
//        	String currentFriend=(String) it.next();
        	UserPreference currentFriend = (UserPreference) it.next();
        	friendItems.add(currentFriend);
        	
//        	friendsOnShow.add(currentFriend);
        } 
        friendItems.notifyDataSetChanged();
        
        	
		BindNetworkService();
		
		friendlist.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		        // When clicked, show a toast with the TextView text or do whatever you need.
		    	Intent intent = new Intent(FriendListActivity.this, Chat.class);
				//PutExtra. The peerIPAddress. your name. your photo, etc. 		
		    	TextView ipView = (TextView) view.findViewById(R.id.ipView);
				intent.putExtra("peerAddr", ipView.getText().toString());
				startActivity(intent);
		        //Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
		    }
		});
	}
	
	@Override
    public void onPause(){
    	super.onPause();
    	UnBindNetworkService();
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	
	
	public void backToLogout(View clickedButton){
		//SentIntent to login page
		Intent intent = new Intent(FriendListActivity.this, LoginActivity.class);
        startActivity(intent);
        stopService(new Intent(FriendListActivity.this, NetworkService.class));
        UnBindNetworkService();
	}
	
	public void addNewFriend(View clickedButton){
		//Enter the page to add a new friend
		Intent intent = new Intent(FriendListActivity.this, AddNewActivity.class);
        startActivity(intent);
        UnBindNetworkService();
	}
	
	
	 void BindNetworkService() {
		 //Bind with NetworkService
	        bindService(new Intent(this, NetworkService.class), mConnection, Context.BIND_AUTO_CREATE);
	        mIsBound = true;   
	        Log.i("Bind", "Suceessfully");
	    }
	 void UnBindNetworkService() {
		 //Unbind with netwokrService
	        if (mIsBound) {
	            // If we have received the service, and hence registered with it, then now is the time to unregister.
	            if (NetworkMessager!= null) {
	                try {
	                    Message msg = Message.obtain(null, NetworkService.MSG_UNREGISTER_CLIENT);
	                    msg.replyTo = FLMessenger;
	                    NetworkMessager.send(msg);
	                } catch (RemoteException e) {
	                    // There is nothing special we need to do if the service has crashed.
	                }
	            }
	            // Detach our existing connection.
	            unbindService(mConnection);
	            mIsBound = false;
	        }
	    }

}
