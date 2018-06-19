package com.smart.search.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleSearch {

	public static void main(String[] args) throws Exception {

	    String key="AIzaSyDu17eqwHNlp6NViyTJ7WMnYneic2nw81A";
	    String qry="best+hotels+in+delhi";
	    URL url = new URL(
	            "https://www.googleapis.com/customsearch/v1?key="+key+ "&cx=013036536707430787589:_pqjad5hr1a&q="+ qry + "&alt=json");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("Accept", "application/json");
	    BufferedReader br = new BufferedReader(new InputStreamReader(
	            (conn.getInputStream())));

	    String output;
	    System.out.println("Output from Server .... \n");
	    while ((output = br.readLine()) != null) {
	    	System.out.println(""+output);
	    	
//	        if(output.contains("\"link\": \"")){                
//	            String link=output.substring(output.indexOf("\"link\": \"")+("\"link\": \"").length(), output.indexOf("\","));
//	            System.out.println(link);       //Will print the google search links
//	        }     
	    }
	    conn.disconnect();                              
	}

}


