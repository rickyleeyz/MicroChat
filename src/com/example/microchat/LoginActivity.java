package com.example.microchat;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	Button StartChat;
	EditText		myName;
	ImageView myPhoto;
	Uri				myPhotoUri;
	boolean			isMyPhotoClicked;
	String			newfilename;
	
	
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //peerAddr = (EditText)findViewById(R.id.peerIpToAdd);
        //peerName = (EditText)findViewById(R.id.peerNameToAdd);
        //peerPhoto =(ImageButton)findViewById(R.id.peerPhotoToAdd);
        //peerPhotoUri = Uri.parse("android.resource://com.android.chat/"+R.drawable.ic_launcher);
       //peerPhoto.setImageURI(peerPhotoUri);
        
        
        myPhoto = (ImageView)findViewById(R.id.myPhoto);
        myName =(EditText)findViewById(R.id.myName);
        myPhotoUri = Uri.parse("android.resource://com.example.microchat/"+R.drawable.ic_launcher);

        myPhoto.setImageURI(myPhotoUri);
        

        
    }
	public void onStart(){
		super.onStart();
		if(Utils.chatHistory.isMyselfExist()){
			myName.setText(Utils.chatHistory.Myself.getNickName());

			myPhoto.setImageURI(Utils.chatHistory.Myself.getPhoto());
		}
	}
	
	public void startChatting(View clickedButton) {
		String myname=myName.getText().toString();

			
		if(!myname.isEmpty()){
			//If every input is valid, set the preference and start to chat with this peer
			

			Utils.chatHistory.setMyNickName(myname);

			
			
			Intent intent=null;
			if(Utils.chatHistory.isEmpty()){
				intent = new Intent(LoginActivity.this, AddNewActivity.class);
			}
			else
			{
				intent = new Intent(LoginActivity.this, FriendListActivity.class);
			}
				

			startService(new Intent(LoginActivity.this, NetworkService.class));
	        startActivity(intent);
			
			
		}
		else{
			Toast.makeText(getApplicationContext(), "Wrong input. Please try again", Toast.LENGTH_SHORT).show();
		}
		
    }
	
	public void setMyPhoto(View clickedView){
		//Set the photo of myself
		isMyPhotoClicked = true;
		newfilename=new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		showDialog();
		
	}
	public void setPeerPhoto(View clickedView){
		//Set the photo of peer
		isMyPhotoClicked = false;
		newfilename=new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
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
									newfilename+".jpg")));
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
					+ newfilename
					+".jpg");			
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
		//Trim the chosen photo
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
			myPhotoUri = Utils.SaveImage(photo,"myselfPhoto");

			myPhoto.setImageDrawable(drawable);
				
				
				Utils.chatHistory.setMyPhoto(myPhotoUri);;
				
			

					
						
		}
	}
	

}
