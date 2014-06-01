package com.example.microchat;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ChatRoomAdapter extends BaseAdapter {

	  private ArrayList<OneSnts> listData;
	  private UserPreference currentPeer;

	  private LayoutInflater layoutInflater;
	  
	  public ChatRoomAdapter(Context context, UserPreference CurrentPeer,ArrayList<OneSnts> listData){
			    this.listData = listData;
			    this.currentPeer=CurrentPeer;
			    layoutInflater = LayoutInflater.from(context);
	  }
	  
	  @Override
	  public int getCount() {
	    return listData.size();
	  }

	  @Override
	  public Object getItem(int position) {
	    return listData.get(position);
	  }

	  @Override
	  public long getItemId(int position) {
	    return position;
	  }
	  
	  public void add(OneSnts item){
		  listData.add(item);
	  }
	  
	  public void clean(){
		  if(!listData.isEmpty()){

			  listData.clear();
		  }
	  }

	  public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;
	    
	    if(listData.get(position).WhoSaid.equals("You")){
	    	//If the message is sent by peer
	    	convertView = layoutInflater.inflate(R.layout.message1, null);
			holder = new ViewHolder();
			holder.content = (TextView) convertView.findViewById(R.id.ChatContent1);
			//holder.nickname=(TextView) convertView.findViewById(R.id.nicknameViewInChat1);
			holder.photo=(ImageView)convertView.findViewById(R.id.photoViewInChat1);
			holder.timestamp=(TextView) convertView.findViewById(R.id.TimeStamp1);
			convertView.setTag(holder);
			//holder.nickname.setText(currentPeer.getNickName());
		    holder.photo.setImageURI(currentPeer.getPhoto());
	    	
	    }
	    else{
	    	//If the message sent by myself
	    	convertView = layoutInflater.inflate(R.layout.message2, null);
			holder = new ViewHolder();
			holder.content = (TextView) convertView.findViewById(R.id.ChatContent2);
			//holder.nickname=(TextView) convertView.findViewById(R.id.nicknameViewInChat2);
			holder.photo=(ImageView)convertView.findViewById(R.id.photoViewInChat2);

			holder.timestamp=(TextView) convertView.findViewById(R.id.TimeStamp2);
			convertView.setTag(holder);
			//holder.nickname.setText(Utils.chatHistory.Myself.getNickName());
		    holder.photo.setImageURI(Utils.chatHistory.Myself.getPhoto());
	    }
	    
		
	    
	    holder.timestamp.setText(timeStampOut(listData.get(position).timestamp));
		holder.content.setText(listData.get(position).SaidWhat);
		
		Log.i("position", String.valueOf(position));
		//Log.i("IP",listData.get(position).getIP());
	    
	    
	    return convertView;
	}
	  
	  private String timeStampOut(String timestamp){
		 String timePart=timestamp.substring(timestamp.indexOf("_")+1);
		 //Log.i("timePart", timePart);
		 return timePart.substring(0, 2)+":"+timePart.substring(2, 4)+":"+timePart.substring(4);
	  }

	  static class ViewHolder {
	    TextView content;
	    //TextView nickname;
	    TextView timestamp;
	    ImageView photo;
	  }
	  
	 
	  
}
