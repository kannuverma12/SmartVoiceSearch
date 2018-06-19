package com.smart.search.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class KnowledgeApi {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String query = "Who killed my cat";
		String type = null;
		searchViaKnowledgeGraphApi(query, type);
	}
	
	public static String searchViaKnowledgeGraphApi(String searchQuery, String type) {
    	try {
		      
		      String key = "AIzaSyCddsSLG7-oWo-r1y-5F4MX-Lof-_E0b7g";
		      //String searchQuery = "Universities in delhi";
		      URL url = null;
		      if(type != null)
		    	  url = new URL("https://kgsearch.googleapis.com/v1/entities:search?query="+ URLEncoder.encode(searchQuery, "UTF-8") 
		      						+"&types="+ type +"&types=Place&limit=10&indent=true&key="+key);
		      else
		    	  url = new URL("https://kgsearch.googleapis.com/v1/entities:search?query="+ URLEncoder.encode(searchQuery, "UTF-8") 
					+"&limit=10&indent=true&key="+key);
	
		      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		      
		      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		      
		      String output = null;
		      StringBuilder response = new StringBuilder();
		      
		      while((output=reader.readLine())!=null){
		    	  response.append(output);
		      }
		      
		      System.out.println("Response:"+response);
		      return response.toString();
		      
		   } catch (Exception ex) {
		      ex.printStackTrace();
		      return null;
		  }
		  
	}

}
