package com.example.microchat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OneSnts {
	//One message
	
	public String WhoSaid;
	public String SaidWhat;
	public String timestamp;
	
	public OneSnts(String whoSaid, String saidWhat)
	{
		this.WhoSaid = whoSaid;
		this.SaidWhat = saidWhat;
		this.timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
}
