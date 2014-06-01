package com.example.microchat;


import android.net.Uri;

public class UserPreference {
	//The properties of one user
	public String ipAddress;
	public String nickname;
	public Uri photo;
	
	public UserPreference(String IPAddress,String nickName,Uri Photo){
		this.ipAddress=IPAddress;
		
		this.nickname=nickName;
		photo=Photo;
		
	}
	public UserPreference(String IPAddress){
		this.ipAddress=IPAddress;
		this.nickname="unknown";
		this.photo=Uri.parse("android.resource://com.example.microchat/"+R.drawable.ic_launcher);
		//photo=....
	}
	public UserPreference(){
		//Just for  Myself.
		this.ipAddress="localhost";
		this.nickname="unkown";
		this.photo=Uri.parse("android.resource://com.example.microchat/"+R.drawable.ic_launcher);
	}
	
//	public UserPreference(){
//		nickname="unknown";
//		//photo=...some default picture
//	}
//	
	public void setPhoto(Uri Photo){
		this.photo=Photo;
	}
	
	public Uri getPhoto(){
		return this.photo;
	}
	public String getIP(){
		return this.ipAddress;
	}
	
	public void setNickName(String nickName){
		this.nickname=nickName;
	}
	
	public String getNickName(){
		return this.nickname;
	}
	
	

	
}
