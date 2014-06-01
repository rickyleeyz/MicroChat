package com.example.microchat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	
	public static ChatHistory chatHistory = new ChatHistory();// used to store chat history
	
	
	public static boolean isValidIP(String ip) {
		//Check if the IP is a valid one
	    if (ip == null || ip.isEmpty()) return false;
	    ip = ip.trim();
	    if ((ip.length() < 6) & (ip.length() > 15)) return false;

	    try {
	        Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
	        Matcher matcher = pattern.matcher(ip);
	        return matcher.matches();
	    } catch (PatternSyntaxException ex) {
	        return false;
	    }
	}
	
	public static Uri SaveImage(Bitmap finalBitmap,String whose) {
		//Store the image into the sdcard and return the uri
	    String root = Environment.getExternalStorageDirectory().toString();
	    File myDir = new File(root + "/saved_images");    
	    myDir.mkdirs();
	    //String fname =new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())+".jpg";
	    String fname=whose+".jpg";
	    File file = new File (myDir, fname);
	    if (file.exists ()) file.delete (); 
	    try {
	           FileOutputStream out = new FileOutputStream(file);
	           finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
	           out.flush();
	           out.close();

	    } catch (Exception e) {
	           e.printStackTrace();

				return Uri.parse("android.resource://com.android.chat/"+R.drawable.ic_launcher);
	    }	   	    
	    
	   return Uri.parse("file://" + Environment.getExternalStorageDirectory()+"/saved_images/"+fname);
	}
	
	
	
}
