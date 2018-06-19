package com.smart.search.google;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.Token;
import com.smart.search.analysis.SearchAnalysis;
import com.smart.search.nlp.InitialFilter;
import com.google.cloud.language.v1.Document.Type;

public class MapApi {
	public static Map<String, String> mapKeywords = new HashMap<String, String>();
	public static Map<String, String> distanceKeywords = new HashMap<String, String>();
	
	static boolean mapSearch;
	static boolean distanceSearch;
	
	static String result;
	
	static String[] mapSearchKeywords = {"nearest", "nearby", "near", "find", "get", "buy", "purchase",
											"take me to", "buy a", "get a", "locate", "location","Nearest","best place to", "places to",
											"place to"};
	static String[] distanceSearchKeywords = {"distance between", "distance from", "how far", "directions to", "where is",
												"shortest route", "how do i reach","reach","direction to","travel time"};

	
	public static void main(String[] args) {
		initMaps();
		
		loadProperties();
		
		String searchQuery = "mountain near the river";
		try{
			searchQuery = InitialFilter.removeArticles(searchQuery);
			
			LanguageServiceClient language = LanguageServiceClient.create();
			Document doc = Document.newBuilder().setContent(searchQuery).setType(Type.PLAIN_TEXT).build();
			
			List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
			List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
			
			if(searchQuery.toLowerCase().contains("take me to")){
				String entityName = "";
				String tagVal = "";
				for(Entity e : entityList){
					if(e.getType().toString().equalsIgnoreCase("location")){
						entityName = e.getName();
						break;
					}
				}
				for(Token t : tokenList){
					if(t.getText().getContent().toLowerCase().equals(entityName.toLowerCase())){
						tagVal = t.getPartOfSpeech().getTag().toString().toLowerCase();
					}
				}
				if(tagVal.equals("x"))
					searchQuery = searchQuery.replace("take me to", "direction to");
				else
					searchQuery = searchQuery.replace("take me to", "nearby");
			}	
			System.out.println("searchQuery : "+searchQuery);
		    String[] query = searchQuery.split(" ");
		    
		    for(int i=0; query!=null && i<query.length; i++){
		    	if(mapKeywords.containsKey(query[i].toLowerCase())){
		    		mapSearch = true;
		        
		    	}else if(distanceKeywords.containsKey(query[i].toLowerCase())) {
		    		distanceSearch = true;
		        }
		    }
		    
		    if(mapSearch){
		    	System.out.println("Calling Map API...");
		    	result = mapSearch(searchQuery, entityList, tokenList);
		    	System.out.println("Valid search for Map API ? : "+result);
		    }else if(distanceSearch){
		    	System.out.println("Calling Distance API...");
		    	result = distanceSearch(searchQuery, entityList, tokenList);
		    	System.out.println("Valid search for Distance API ? : "+result);
		    }else{
		    	System.out.println("No Category");
		    }

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String mapSearch(String searchQuery, List<Entity> entityList, List<Token> tokenList) {
		String ret = null;
		boolean locationSearch = false;
		//System.out.println("entityList "+entityList );
		for(Entity e : entityList){
			System.out.println("Entity : "+e.getName()+" , type : "+e.getType().toString());
			if(e.getType().toString().equalsIgnoreCase("Location")){
				locationSearch = true;
				break;
			}
			if(searchQuery.contains("buy") || searchQuery.contains("purchase") || searchQuery.contains("get")){
				if(e.getType().toString().equalsIgnoreCase("consumer_good")){
					locationSearch = true;
					break;
				}
			}
		}
		
		boolean validSearch = false;
		//System.out.println("searchQuery : "+searchQuery);
		for(String s :mapSearchKeywords){
			//System.out.println("s : "+s);
			if(searchQuery.toLowerCase().contains(s)){
				validSearch = true;
				break;
			}
		}
	  
		if(locationSearch && validSearch){
			System.out.println("Found Location entity in search string.\nLooking into Map API...");
			try {
			    String key = "AIzaSyA_DsW_ZuDAhqG_P4R006GHFhua0-ijvhs";
			    String location = "28.498414,77.084925"; //gurgaon
			    //String location = "-4.036878,39.669571"; //mumbasa
			    //String location = "37.7884,-122.4076"; //Union Square, San Fransisco
			    
			    if(searchQuery.toLowerCase().contains("take me to"))
			    	searchQuery = searchQuery.replace("take me to", "nearby");
			    
			    System.out.println("SearchQuery for Map : "+searchQuery);
			
			    URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ location 
			    						+"&radius=5000&name="+URLEncoder.encode(searchQuery, "UTF-8")+"&key="+key);
			 
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				
				String output = null;
				StringBuilder response = new StringBuilder();
				
				while((output=reader.readLine())!=null){
					response.append(output);
				}
			
				System.out.println("Response:"+response);
			    //return response.toString();
				ret = response.toString();
				} catch (Exception ex) {
					ex.printStackTrace();
			}
		}else{
			System.out.println("Not a valid Location Entity.");
		}
		
		return ret;
	}
	
	public static String distanceSearch(String searchQuery, List<Entity> entityList, List<Token> tokenList) {
		String ret = null;
		try {
			String[] locationArr = new String[2];
			int i = 0;
			for(Entity e : entityList){
				if(e.getType().toString().equalsIgnoreCase("Location")){
					locationArr[i] = e.getName();
					i++;
				}
			}
			
			boolean validSearch = false;
			for(String s : distanceSearchKeywords){
				if(searchQuery.toLowerCase().contains(s)){
					validSearch = true;
				}
			}
			
			
			
			String origin = locationArr[0];
			String destination = locationArr[1];
			System.out.println("origin = "+origin+" destination = "+destination);
			if(null == destination || (destination != null && destination.equals(""))){
				origin = SearchAnalysis.currentCity;
				destination = locationArr[0];
			}
			System.out.println("origin = "+origin+" destination = "+destination);
			if(origin != null && destination != null && !origin.equals("") && !destination.equals("") && validSearch){
				String key = "AIzaSyDt6hMMJGsv0nZZ9zMyXGSlk3BWELB1VLI";
				URL url = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins="
									+URLEncoder.encode(origin, "UTF-8")
									+"&destinations="+URLEncoder.encode(destination, "UTF-8")
									+"&key="+key);	
			 
			
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			  
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			  
				String output = null;
				StringBuilder response = new StringBuilder();
			  
				while((output=reader.readLine())!=null){
					response.append(output);
				}
			  
				System.out.println("Response:"+response.toString());
				
				JSONObject root = new JSONObject(response.toString());
				JSONArray webPages = (JSONArray)root.get("rows");
				JSONObject tempObj = (JSONObject) webPages.get(0);
				JSONArray elems = tempObj.getJSONArray("elements");
				JSONObject firstElem = (JSONObject) elems.get(0);
				String status = (String)firstElem.get("status");
				if(!status.equalsIgnoreCase("ZERO_RESULTS")){
					JSONObject distanceObj = (JSONObject)firstElem.get("distance");
	    			String dis = (String)distanceObj.get("text");
	    			ret = "Distance between "+origin+" and "+destination+" is "+dis+".";
	    			System.out.println(ret);
	    			
				}else{
					System.out.println("Can not find distance outside current country...");
					
				}
				
				
				//return response.toString();
			}else{
				System.out.println("Not valid Location Entities.");
			}
		  
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	  
	}
	
	public static void loadProperties(){
		try {
			File file = new File("config.properties");
			FileInputStream fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();

			Enumeration enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				//System.out.println(key + ": " + value);
				if(key.equalsIgnoreCase("currentcity")){
					SearchAnalysis.currentCity = value;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initMaps(){
		mapKeywords.put("nearby", "map");
	    mapKeywords.put("nearest", "map");
	    mapKeywords.put("near", "map");
	    mapKeywords.put("Nearest", "map");
	    
	    mapKeywords.put("locate", "map");
	    mapKeywords.put("find", "map");
	    mapKeywords.put("location", "map");
	    
	    mapKeywords.put("take", "map");
	    mapKeywords.put("get", "map");
	    mapKeywords.put("buy", "map");
	    mapKeywords.put("purchase", "map");
	    
	    distanceKeywords.put("distance between", "map");
	    distanceKeywords.put("distance", "map");
	    distanceKeywords.put("from", "map");
	    distanceKeywords.put("where", "map");
	    distanceKeywords.put("far", "map");
	    distanceKeywords.put("reach", "map");
	    distanceKeywords.put("direction", "map");
	    distanceKeywords.put("directions", "map");
	    
	}

}
