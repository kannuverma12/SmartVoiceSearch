package com.smart.search.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class VoiceSearch {
	
	public static void main(String args[])	{
		try {
		      
		      String key = "AIzaSyCddsSLG7-oWo-r1y-5F4MX-Lof-_E0b7g";
		      String searchQuery = "temperature in delhi";
		      
		      URL url = new URL("https://kgsearch.googleapis.com/v1/entities:search?query="+ URLEncoder.encode(searchQuery, "UTF-8") 
		      						+"&types=Place&limit=10&indent=true&key="+key);
	
		      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		      
		      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		      
		      String output = null;
		      StringBuilder response = new StringBuilder();
		      
		      while((output=reader.readLine())!=null){
		    	  response.append(output);
		      }
		      
		      System.out.println("Response:"+response);
		      
		    } catch (Exception ex) {
		      ex.printStackTrace();
		    }
		  }
		
	}


