package com.example.microchat;



import java.util.ArrayList;
import java.util.List;


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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Chat extends Activity 
{

	final static private String TAG = "Chat";
	final static private boolean D	= true;
	final static int PACKET_CAME = 1;
	final static int TOAST  = 2;
	final static int INFO = 3;
	
	boolean mIsBound;
	
	
	
	String			peerAddr;
	EditText		msg;
	Button 			send;
	ListView		msgList;
	ChatRoomAdapter chatroomAdapter;
	Button			backToLogin;
	TextView		ChatWith;

	final Messenger ChatMessenger = new Messenger(new ChatHandler());
	Messenger NetworkMessager = null;
	
	class ChatHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

        	if(D) Log.d(TAG, "In the Handler");
        	switch (msg.what) 
        	{
	            case NetworkService.MSG_RECIEVED:
	            	//New message coming
	            	String incomingPeerAddr=msg.getData().getString("peerAddress");
	            	String incomingMessage=msg.getData().getString("content");
	            	Log.i("incoming", incomingPeerAddr+".");
	            	Log.i("peerAddr",peerAddr+".");
	            	if(incomingPeerAddr.equals(peerAddr)){
	            		//If the incoming message is sent by the peer which chating with, show it out
	            		chatroomAdapter.add(new OneSnts("You",incomingMessage));
	            		chatroomAdapter.notifyDataSetChanged();
	            		Utils.chatHistory.addSnts(peerAddr,"You",incomingMessage);
	            		Log.i("IncomingMessage", incomingMessage);
	            	}
	            	else{
	            		//If the incoming message is sent by another guy, notify
	            		//Shake 
	            		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);	            		            		
	            		v.vibrate(300);
	            		
	            		//Notify by Toast
	            		String toastToMake = incomingPeerAddr+" send you an message";
	            		Toast.makeText(getApplicationContext(), toastToMake, Toast.LENGTH_SHORT).show();
	            		Utils.chatHistory.addSnts(incomingPeerAddr,"You",incomingMessage);

	                    Utils.chatHistory.setUnRead(incomingPeerAddr);
	            		
	            		// add unread marks, waiting to be done.
	            		
	            		Log.i("IncomingMessage", incomingMessage);
	            	}
	            	break;
	            case TOAST:
	            	String toastToMake= (String) msg.obj;
	            	Toast.makeText(getApplicationContext(), toastToMake, Toast.LENGTH_SHORT).show();
	                break;  
	            case INFO:
	            	String infoMessage= msg.getData().getString("myAddress");
	            	//myAddr.setText(infoMessage);
	            	break;
            }
        }
    };
    
    //Service Connection
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            NetworkMessager = new Messenger(service);
            //textStatus.setText("Attached.");
            try {
                Message msg = Message.obtain(null, NetworkService.MSG_REGISTER_CLIENT);
                msg.replyTo = ChatMessenger;
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
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
     
        send = (Button)findViewById(R.id.send);
        send.setOnClickListener(send_listener);
        msgList = (ListView)findViewById(R.id.msgList);
        
        ChatWith=(TextView)findViewById(R.id.chatWith);
        
     
        msg = (EditText)findViewById(R.id.msg);
        msg.setOnKeyListener(new OnKeyListener() {

        public boolean onKey(View v, int keyCode, KeyEvent event) 
        {
        		if ((event.getAction() == KeyEvent.ACTION_UP) &&
        				(keyCode == KeyEvent.KEYCODE_ENTER)) {
        			postMessage();
        			return true;
        		}
        		return false;
        	}
        });
        

    }
    
    private void postMessage()
    {//Sent the message to peer
    	
    	String theNewMessage = msg.getText().toString();
    	
    	if(!theNewMessage.isEmpty()){
    		//Sent to NetworkService	
        	try{
        		Bundle b = new Bundle();
                b.putString("peerAddress", peerAddr);
                b.putString("content", theNewMessage); 
                Message msgToSend = Message.obtain(null, NetworkService.MSG_TO_SEND);
                msgToSend.setData(b);
                NetworkMessager.send(msgToSend);

        	}catch(Exception e){
        		Log.e(TAG,"Cannot send message"+e.getMessage());
        	}
        	
        	//receivedMessages.add("Me: "+theNewMessage);
        	chatroomAdapter.add(new OneSnts("Me",theNewMessage));
        	chatroomAdapter.notifyDataSetChanged();
        	msg.setText("");
        	    	
        	Utils.chatHistory.addSnts(peerAddr, "Me", theNewMessage);
    		
    	}
    	
    }
    
    private OnClickListener send_listener = new OnClickListener() 
    {
        public void onClick(View v) {
        	postMessage();
        }
    };
    
    public void backToFriendList(View clickedButton) {
		Intent intent = new Intent(Chat.this, FriendListActivity.class);
        startActivity(intent);
        doUnBindService();
        //stopService(new Intent(Chat.this, NetworkService.class));
        //Chat.this.finish();
    }
    
    public void ModifyFriendPreference(View clickedButton)
    {//Enter the modify friend preference page
		Intent intent = new Intent(Chat.this, ModifyActivity.class);
		intent.putExtra("chatWith", peerAddr);
        startActivity(intent);
        doUnBindService();
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	
    	//Take out things from login page
        Intent intent = getIntent();
        peerAddr=intent.getStringExtra("peerAddr");
        
        
        //If didn't input a peer address, use a default one
        if(peerAddr.equals("")){
        	peerAddr = "10.0.2.3";
        }
        Utils.chatHistory.addFriendIP(peerAddr);
        Utils.chatHistory.addNewUser(peerAddr);
        Utils.chatHistory.setRead(peerAddr);
        
        ChatWith.setText(Utils.chatHistory.getNickName(peerAddr));
        
        Log.i(TAG,"peerAddr"+peerAddr);
        
        ArrayList<OneSnts> emptylist=new ArrayList<OneSnts>();
        
        chatroomAdapter = new ChatRoomAdapter(this,Utils.chatHistory.returnFriendPreference(peerAddr),emptylist);
        chatroomAdapter.clean();
        
        //receivedMessages = new ArrayAdapter<String>(this, R.layout.message1);
       // msgList.setAdapter(receivedMessages);
        msgList.setAdapter(chatroomAdapter);
        
        //If chatted with this guy before, load the chat history into the chatting room
        if(Utils.chatHistory.isPeerExist(peerAddr)){     	
        	
        	List<OneSnts> dialoglist=Utils.chatHistory.returnDialog(peerAddr);
        	        	
        	for(int i=0;i<dialoglist.size();i++){        		
        		String dialog=dialoglist.get(i).WhoSaid+" "+dialoglist.get(i).SaidWhat;
        		chatroomAdapter.add(dialoglist.get(i));
        		//receivedMessages.add(dialog);
        	}
        	chatroomAdapter.notifyDataSetChanged();
        	       	
        }
    	
    	doBindService();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	doUnBindService();
    }
    

    
    @Override
    public void onDestroy()
    {
    	Log.d(TAG, "Destroyed");
    	super.onDestroy();
    	//myThread.closeSocket();
    }
    
    void doBindService() {
    	//Bind with NetworkService
        bindService(new Intent(this, NetworkService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;   
        Log.i("Bind", "Suceessfully");
    }
    void doUnBindService() {
    	//Unbind with NetworkService
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (NetworkMessager!= null) {
                try {
                    Message msg = Message.obtain(null, NetworkService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = ChatMessenger;
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