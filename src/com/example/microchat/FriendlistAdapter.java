package com.example.microchat;

import java.util.ArrayList;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class FriendlistAdapter extends BaseAdapter {

	  private ArrayList<UserPreference> listData;

	  private LayoutInflater layoutInflater;
	  
	  public FriendlistAdapter(Context context, ArrayList<UserPreference> listData){
			    this.listData = listData;
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
	  
	  public void add(UserPreference item){
		  listData.add(item);
	  }
	  
	  public void clean(){
		  if(!listData.isEmpty()){

			  listData.clear();
		  }
	  }

	  public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;
	    
		convertView = layoutInflater.inflate(R.layout.frienditem, null);
		holder = new ViewHolder();
		holder.ip = (TextView) convertView.findViewById(R.id.ipView);
		holder.nickname=(TextView) convertView.findViewById(R.id.nicknameView);
		holder.photo=(ImageView)convertView.findViewById(R.id.photoView);
		holder.lastmsg=(TextView) convertView.findViewById(R.id.latestMsg);
		convertView.setTag(holder);

		holder.ip.setText(listData.get(position).getIP());
		Log.i("position", String.valueOf(position));
		Log.i("IP",listData.get(position).getIP());
	    holder.nickname.setText(listData.get(position).getNickName());
	    holder.photo.setImageURI(listData.get(position).getPhoto());
	    holder.lastmsg.setText(Utils.chatHistory.getLatestMsg(listData.get(position).getIP()));
	    
	    if(!Utils.chatHistory.isRead(listData.get(position).getIP())){
	    	//If this message hasn't been read, set it red

		    holder.lastmsg.setTextColor(Color.RED);
	    }
	    
	    return convertView;
	}

	  static class ViewHolder {
	    TextView ip;
	    TextView lastmsg;
	    TextView nickname;
	    ImageView photo;	    
	    
	  }
	  
}
