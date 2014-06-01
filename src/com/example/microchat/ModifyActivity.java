package com.example.microchat;

import java.io.File;


import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ModifyActivity extends Activity{
	
	final Messenger MFMessenger = new Messenger(new MFHandler());
	Messenger NetworkMessager = null;
	boolean mIsBound;
	private String ChatWith;
	ImageView	photoButton;
	TextView nickname;
	TextView ipView;
	
	class MFHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

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
	            	String toastToMake = incomingPeerAddr+" send you an message";
	            	Toast.makeText(getApplicationContext(), toastToMake, Toast.LENGTH_SHORT).show();
	            	Utils.chatHistory.addSnts(incomingPeerAddr,"You",incomingMessage);

	            	Utils.chatHistory.setUnRead(incomingPeerAddr);
	            	
	            	// add unread marks, waiting to be done.	            		
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
                msg.replyTo = MFMessenger;
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
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.modifyfriend);
        photoButton = (ImageView) findViewById(R.id.photoToChange);
        
        nickname=(TextView) findViewById(R.id.editNickName);
        ipView = (TextView) findViewById(R.id.IP_display);
       
    }
	
	public void onStart(){
			super.onStart();
	        Intent intent=getIntent();
	        ChatWith=intent.getStringExtra("chatWith");
	        
	        photoButton.setImageURI(Utils.chatHistory.getPhoto(ChatWith));
	        nickname.setText(Utils.chatHistory.getNickName(ChatWith));
	        ipView.setText(ChatWith);
	        
			BindNetworkService();			
	}
	
	public void backToChat(View clickedButton){
		//Finished changing, back to chat
		Intent intent = new Intent(ModifyActivity.this, Chat.class);	
		intent.putExtra("peerAddr", ChatWith);
		
		String nickName=nickname.getText().toString();
		Log.i("changed nickname", nickName);
		Log.i("chatwith", ChatWith);
		Utils.chatHistory.setNickName(ChatWith, nickName);
		Log.i("changed nickname",Utils.chatHistory.getNickName(ChatWith));
		
		
        startActivity(intent);
        UnBindNetworkService();
	
	}
	
	
	
	
	
	void BindNetworkService() {
        bindService(new Intent(this, NetworkService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;   
        Log.i("Bind", "Suceessfully");
    }
	void UnBindNetworkService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (NetworkMessager!= null) {
                try {
                    Message msg = Message.obtain(null, NetworkService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = MFMessenger;
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
	
	public void changePhoto(View clicked){
		//Change the photo
		showDialog();
		
	}
	
	private void showDialog() {
    		new AlertDialog.Builder(this)
				.setTitle("Change Photo...")
				.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(Intent.ACTION_PICK, null);
						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						startActivityForResult(intent, 1);

					}
				})
				.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						//Specify storage
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
								.fromFile(new File(Environment
										.getExternalStorageDirectory(),
										ChatWith+".jpg")));
						startActivityForResult(intent, 2);
					}
				}).show();
	
    	
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// If comes from gallery
		case 1:
			startPhotoZoom(data.getData());
			//faceimagebutton.setImageURI(data.getData());
			break;
		// If comes from Camera
		case 2:
			File temp = new File(Environment.getExternalStorageDirectory()
					+ ChatWith+".jpg");			
			startPhotoZoom(Uri.fromFile(temp));
			break;		
		// Get the zoomed photo
		case 3:
			if(data != null){
				//ImageButton crntClicked = (ImageButton)showDialog(crntClicked);
				setPicToView(data);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void startPhotoZoom(Uri uri) {
		//Trim the photo
		Log.i("inital uri", String.valueOf(uri));
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}
	
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(photo);
			photoButton.setImageDrawable(drawable);

			Utils.chatHistory.setPhoto(ChatWith, Utils.SaveImage(photo,ChatWith));
						
		}
	}
	


}
