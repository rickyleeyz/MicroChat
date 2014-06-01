package com.example.microchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.net.Uri;
import android.util.Log;

public class ChatHistory {

	final static private String TAG = "ChatHistory";
	private HashMap<String,List<OneSnts>> chatHistory = new HashMap<String,List<OneSnts>>();
	private HashMap<String,UserPreference> userPreferences = new HashMap<String,UserPreference>();
	private HashMap<String,String> beReadOrNot = new HashMap<String,String>();
	public UserPreference Myself = new UserPreference();
	private List<String> FriendIPs= new ArrayList<String>();
	private String MyAddress;
	public ChatHistory(){
		//UserPreference Myself= new UserPreference();
		
		
	}
	
	public boolean isPeerExist(String chatWith){
		return chatHistory.containsKey(chatWith);
		
	}
	public boolean isEmpty(){
		return userPreferences.isEmpty();
	}
	
	public void addSnts(String chatWith,String WhoSaid,String SaidWhat){
		if(this.isPeerExist(chatWith)){
			chatHistory.get(chatWith).add(new OneSnts(WhoSaid,SaidWhat));
			
		}
		else
		{
			addFriendIP(chatWith);
			List<OneSnts> Dialog = new ArrayList<OneSnts>();
			Dialog.add(new OneSnts(WhoSaid,SaidWhat));
			chatHistory.put(chatWith, Dialog);	
			addNewUser(chatWith);
		}
		
	}
	
	public boolean isMyselfExist(){
		return (!Myself.nickname.equals("unkown"));
	}

	
	
	public List<OneSnts> returnDialog(String chatWith){
		return chatHistory.get(chatWith);
		
	}
	
	public void setNickName(String Who,String nickName){
		userPreferences.get(Who).setNickName(nickName);
		
	}
	public String getNickName(String Who){
		return userPreferences.get(Who).getNickName();
	}
	public void setPhoto(String Who,Uri Photo){
		userPreferences.get(Who).setPhoto(Photo);
	}
	public Uri getPhoto(String Who){
		return userPreferences.get(Who).getPhoto();
	}
	public void setRead(String chatWith){
			
		beReadOrNot.put(chatWith, "Yes");
	}
	public void setUnRead(String chatWith){
		
			beReadOrNot.put(chatWith, "No");
		
	}
	public boolean isRead(String chatWith){
		if(beReadOrNot.get(chatWith).equals("Yes")){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	
	public void addNewUser(String Who,String nickName,Uri Photo){
		if(!userPreferences.containsKey(Who)){
		userPreferences.put(Who,new UserPreference(Who,nickName,Photo));
		}
		else{
			userPreferences.get(Who).setNickName(nickName);
			userPreferences.get(Who).setPhoto(Photo);
		}
	}
	
	
	public void addNewUser(String Who){
		if(!userPreferences.containsKey(Who)){
			userPreferences.put(Who, new UserPreference(Who));			
		}
	}
	public void setMyPhoto(Uri Photo){
		Myself.setPhoto(Photo);
	}
	public void setMyNickName(String nickName){
		Myself.setNickName(nickName);
		
	}
	public void setMyIPAddr(String myIP){
		Myself.ipAddress=myIP;
		//this.MyAddress=myIP;
		
	}
	
	public Set<String> getFriends(){
		return chatHistory.keySet();
		
	}
	
	public String getLatestMsg(String chatWith){
		String msg;
		if(chatHistory.get(chatWith)==null){

			msg="(No Message)";
			
		}
		else{
			ArrayList<OneSnts> dialog=(ArrayList<OneSnts>) chatHistory.get(chatWith);
			msg=dialog.get(dialog.size()-1).SaidWhat;			
		}

		return msg;
	}
	
	public void addFriendIP(String Who){
		if(!FriendIPs.contains(Who)){
			FriendIPs.add(Who);
		}		
	}
	
	public  Collection<UserPreference> returnFriendList(){
		return userPreferences.values();		
	}
	
	public List<String> returnFriendIPs(){
		return FriendIPs;
	}
	
	public UserPreference returnFriendPreference(String peerIP){
		return userPreferences.get(peerIP);
	}
	

}
