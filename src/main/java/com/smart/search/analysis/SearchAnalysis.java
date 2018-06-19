package com.smart.search.analysis;

import java.io.BufferedReader;
import javax.inject.Provider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.Token;
import com.smart.search.google.GoogleSearchNew;
import com.smart.search.google.KnowledgeSearch;
import com.smart.search.google.MapApi;
import com.smart.search.ms.NewsApi;
import com.smart.search.nlp.InitialFilter;
import com.smart.search.weather.WeatherApi;

public class SearchAnalysis {
	
	static int valid = 0;
	static int invalid = 0;
	static String one;
	static String two;
	public static String configPath;
	public static String currentCity;
	
	public static FileWriter fw = null;
	public static BufferedWriter bw = null;
	
//	static List<Entity> entityList = new ArrayList<Entity>();
//	static List<Token> tokenList = new ArrayList<Token>();
	
	
	public static Map<String, String> mapKeywords = new HashMap<String, String>();
	public static Map<String, String> distanceKeywords = new HashMap<String, String>();
	public static Map<String, String> knowledgeKeywords  = new HashMap<String, String>();
	public static Map<String, String> newsKeywords  = new HashMap<String, String>();
	public static Map<String, String> weatherKeywords  = new HashMap<String, String>();
	
	

	public static void main(String[] args) {
		try{
			initMaps();
			String FILENAME = "/Users/karan.verma/Documents/nlp/output.txt";
			fw = new FileWriter(FILENAME);
			bw = new BufferedWriter(fw);
			
			loadConfigFileAndSearch();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
	
	public static void nlp(String searchQuery){
		try{
			LanguageServiceClient language = LanguageServiceClient.create();
			Document doc = Document.newBuilder().setContent(searchQuery).setType(Type.PLAIN_TEXT).build();
			
//			entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
//			tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void loadConfigFileAndSearch(){
		try{
			BufferedReader br = null;
			String line = "";
			br = new BufferedReader(new FileReader(new File("config.properties")));
			while ((line = br.readLine()) != null) {
				//System.out.println(">>" + line);
				if (line.split("=")[0].equals("one"))
					one = line.split("one=")[1].trim();
				else if (line.split("=")[0].equals("two"))
					two = line.split("two=")[1].trim();
				else if (line.contains("path"))
					configPath = line.split("=")[1].trim();
				else if (line.contains("currentCity"))
					currentCity = line.split("=")[1].trim();
			}
			//System.out.println("<<<<< one: " + one + " two: " + two + " configpath: " + configPath);
			//alLevelOne = new ArrayList<LevelOne>();
			br = new BufferedReader(new FileReader(new File(configPath)));
			line = "";
			while ((line = br.readLine()) != null) {
				System.out.println("Query String : " + line);
				String searchString = line.substring(0, line.length() - 2).trim();
				
				processSearchString(searchString,
						line.substring(line.length() - 1, line.length()));
			} // end of while
			//System.out.println(" valid count: " + valid + " invalid count: " + invalid);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void processSearchString(String searchQuery, String validInvalid){
		boolean searchingDone = false;
		String textWebSearch = null;
		String text=searchQuery;
		
		try{
			
			
			LanguageServiceClient language = LanguageServiceClient.create();
			searchQuery=InitialFilter.removeArticles(searchQuery);
			Document doc = Document.newBuilder().setContent(searchQuery).setType(Type.PLAIN_TEXT).build();
			searchQuery=text;
			List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
			List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
		
			boolean validateSearchString = validateSearchString(searchQuery,tokenList);
			
			
			if(validateSearchString)
			{
				
				System.out.println("Initial Filter Passed. Query String is Valid.");
				searchQuery = covertSearchQueryForMapApi(searchQuery, entityList, tokenList);
				String[] query = searchQuery.split(" ");
				boolean mapSearch = false;
    		    boolean knowledgeSearch = false;
    		    
    		    boolean weatherSearch = false;
    		    boolean forecastSearch = false;
    		    
    		    boolean newsSearch = false;
    		    
    			boolean distanceSearch = false;
    		    
				for(int i=0; query!=null && i<query.length; i++){
					String qString = query[i].toLowerCase();
    		    	if(mapKeywords.containsKey(qString)){
    		    		mapSearch = true;
    		        
    		    	}else if(distanceKeywords.containsKey(qString)) {
    		    		//type = knowledgeKeywords.get(query[i]);
    		    		distanceSearch = true;
    		        }else if(knowledgeKeywords.containsKey(qString)) {
    		    		//type = knowledgeKeywords.get(query[i]);
    		    		knowledgeSearch = true;
    		        }
    		    	else if(weatherKeywords.containsKey(qString)) {
    		            weatherSearch = true;
    		        }
    		    	//else if (forecastKeywords.containsKey(query[i])) {
//    		            forecastSearch = true;
//    		        }
//    		    	
    		        else if(newsKeywords.containsKey(qString))
    		    		newsSearch = true;
    		    }
				
				if(mapSearch){
					System.out.println("Calling Map Api...");
					
					textWebSearch = mapSearch(searchQuery, entityList, tokenList);
					if(textWebSearch != null && !textWebSearch.equals("")){
						bw.write("Consumed in Map Api. Query : "+searchQuery+". Map Filter Val : 2. Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Map Api | Query : "+searchQuery+" | Calculated Validity : 2 | Original Validity : "+validInvalid);
					}
				}else if(distanceSearch){
					System.out.println("Calling Distance Api...");
					
					textWebSearch = distanceSearch(searchQuery, entityList, tokenList);
					if(textWebSearch != null && !textWebSearch.equals("")){
						bw.write("Consumed in Distance Api | Query : "+searchQuery+" | Calculated Validity : 2. Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Distance Api | Query : "+searchQuery+" | Calculated Validity : 2. Original Validity : "+validInvalid);
					}
				}else if(knowledgeSearch){
					System.out.println("Calling Knowledge Api...");
					textWebSearch = knowledgeSearch(searchQuery, entityList, tokenList);
					if(textWebSearch != null && !textWebSearch.equals("")){
						bw.write("Consumed in Knowledge Api | Query : "+searchQuery+" | Calculated Validity : 5 | Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Knowledge Api | Query : "+searchQuery+" | Calculated Validity : 5 | Original Validity : "+validInvalid);
					}
				}else if(newsSearch){
					System.out.println("Calling News Api...");
					textWebSearch = newsSearch(searchQuery, entityList, tokenList);
					if(textWebSearch != null && !textWebSearch.equals("")){
						bw.write("Consumed in News Api | Query : "+searchQuery+" | Calculated Validity : 4 | Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in News Api | Query : "+searchQuery+" | Calculated Validity : 4 | Original Validity : "+validInvalid);
					}
				}else if(weatherSearch){
					System.out.println("Calling Weather Api...");
					textWebSearch = weatherSearch(searchQuery, entityList, tokenList);
					if(textWebSearch != null && !textWebSearch.equals("")){
						bw.write("Consumed in Weather Api | Query : "+searchQuery+" | Calculated Validity : 3 | Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Weather Api | Query : "+searchQuery+" | Calculated Validity : 3 | Original Validity : "+validInvalid);
					}
				}
				
				
				if(textWebSearch == null || (textWebSearch != null && textWebSearch.equals(""))){
					textWebSearch = GoogleSearchNew.googleSearch(searchQuery, entityList, tokenList, validInvalid);
					if(textWebSearch != null && !textWebSearch.equals("")){
//						bw.write("Consumed in Custom Google Api | Query : "+searchQuery+" | Calculated Validity : 6 | Original Validity : "+validInvalid+"\n");
//						System.out.println("Consumed in Custom Google Api | Query : "+searchQuery+" | Calculated Validity : 6 | Original Validity : "+validInvalid);
					}else{
						bw.write("Not an eligible query for custom GoogleSearch | Query : "+searchQuery+" | Calculated Validity : 7 | Original Validity : "+validInvalid+"\n");
						System.out.println("Not an eligible query for custom GoogleSearch | Query : "+searchQuery+" | Calculated Validity : 7 | Original Validity : "+validInvalid);
					}
				}
			}
			else
			{
				bw.write("Initial Filter Failed | Query String is Invalid | Query : "+searchQuery+" | Calculated Validity : 7 | Original Validity : "+validInvalid+"\n");
				System.out.println("Initial Filter Failed | Query String is Invalid | Query : "+searchQuery+" | Calculated Validity : 7 | Original Validity : "+validInvalid);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		
	}
	
	public static boolean validateSearchString(String queryString, List<Token> tokenList){
		return InitialFilter.validateSearchString(queryString,tokenList);
	}
	
	public static String mapSearch(String searchQuery, List<Entity> entityList, List<Token >tokenList){
		return MapApi.mapSearch(searchQuery, entityList, tokenList);
	}
	
	public static String distanceSearch(String searchQuery, List<Entity> entityList, List<Token> tokenList){
		return MapApi.distanceSearch(searchQuery, entityList, tokenList);
	}
	
	public static String knowledgeSearch(String searchQuery, List<Entity> entityList, List<Token> tokenList){
		return KnowledgeSearch.keywordsSearch(searchQuery, entityList, tokenList);
	}
	
	public static String newsSearch(String searchQuery, List<Entity> entityList, List<Token >tokenList){
		return NewsApi.callApiNews(searchQuery, entityList, tokenList);
	}
	
	public static String weatherSearch(String searchQuery, List<Entity> entityList, List<Token >tokenList){
		return WeatherApi.weatherSearch(searchQuery, tokenList, entityList);
	}
	
	public static String covertSearchQueryForMapApi(String searchQuery, List<Entity> entityList, List<Token >tokenList){
		if(searchQuery.toLowerCase().contains("take me to")){
			String entityName = "";
			String tagVal = "";
			for(Entity e : entityList){
				if(e.getType().toString().equalsIgnoreCase("Location")){
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
		return searchQuery;
	}
	
	public static void initMaps(){
		newsKeywords.put("news", "news");
		newsKeywords.put("updates", "news");
		newsKeywords.put("headlines", "news");
		newsKeywords.put("trending", "news");
		newsKeywords.put("bulletin", "news");
		newsKeywords.put("rumors", "news");
		newsKeywords.put("scandal", "news");
		newsKeywords.put("press release", "news");
		newsKeywords.put("current affairs", "news");
		newsKeywords.put("related to", "news");
		newsKeywords.put("involving", "news");
		newsKeywords.put("about", "news");
		newsKeywords.put("breaking news", "news");
		
		
		weatherKeywords.put("weather", "weather");
	    weatherKeywords.put("temperature", "weather");
	    weatherKeywords.put("current temperature", "weather");
	    weatherKeywords.put("climate", "weather");
	    weatherKeywords.put("rain", "weather");
	    weatherKeywords.put("rainy", "weather");
	    weatherKeywords.put("wind", "weather");
	    weatherKeywords.put("humid", "weather");
	    weatherKeywords.put("humidity", "weather");
	    weatherKeywords.put("forecast", "weather");
	    weatherKeywords.put("hot", "weather");
	    weatherKeywords.put("sunny", "weather");
	    weatherKeywords.put("cold", "weather");
	    weatherKeywords.put("monsoon", "weather");
	    weatherKeywords.put("raining", "weather");
	    weatherKeywords.put("windy", "weather");
	    weatherKeywords.put("storm", "weather");
	    weatherKeywords.put("thunderstorm", "weather");
	    weatherKeywords.put("thunder", "weather");
	    weatherKeywords.put("cloud", "weather");
	    weatherKeywords.put("cloudy", "weather");
	    weatherKeywords.put("clouds", "weather");
	    weatherKeywords.put("snow", "weather");
	    weatherKeywords.put("snowy", "weather");
	    weatherKeywords.put("snowing", "weather");
	    weatherKeywords.put("fog", "weather");
	    weatherKeywords.put("foggy", "weather");
	    
		
		//{"involving","current affairs","about","headlines","scandal","rumours","press release","bulletin","breaking news","related to"};
		
		mapKeywords.put("nearby", "map");
	    mapKeywords.put("nearest", "map");
	    mapKeywords.put("near", "map");
	    
	    mapKeywords.put("locate", "map");
	    mapKeywords.put("find", "map");
	    mapKeywords.put("location", "map");
	    
	    mapKeywords.put("purchase", "map"); //consumergoods
	    mapKeywords.put("buy", "map");  // consumergoods
	    mapKeywords.put("get", "map"); // consumergoods and lcoation
	    mapKeywords.put("take", "map"); // location
	    
	    //mapKeywords.put("where", "map"); // location
	    mapKeywords.put("place", "map");
	    
	    distanceKeywords.put("distance between", "map");
	    distanceKeywords.put("distance", "map");
	    distanceKeywords.put("from", "map");
	    distanceKeywords.put("directions", "map");
	    distanceKeywords.put("direction", "map");
	    distanceKeywords.put("travel", "map");
	    distanceKeywords.put("route", "map");
	    distanceKeywords.put("where", "map");
	    
	    knowledgeKeywords.put("book", "Book");
		knowledgeKeywords.put("books", "Book");
		knowledgeKeywords.put("novels", "Book");
    	knowledgeKeywords.put("book series", "BookSeries");
    	knowledgeKeywords.put("government organisations", "GovernmentOrganization");
    	knowledgeKeywords.put("event", "Event");
    	knowledgeKeywords.put("movies", "Movie");
    	knowledgeKeywords.put("films", "Movie");
    	knowledgeKeywords.put("movie series", "MovieSeries");
    	knowledgeKeywords.put("educational organisations", "EducationalOrganization");
    	knowledgeKeywords.put("schools", "EducationalOrganization");
    	knowledgeKeywords.put("colleges", "EducationalOrganization");
    	knowledgeKeywords.put("universities", "EducationalOrganization");
    	knowledgeKeywords.put("restaurants", "LocalBusiness");
    	knowledgeKeywords.put("bank", "LocalBusiness");
    	knowledgeKeywords.put("banks", "LocalBusiness");	
    	knowledgeKeywords.put("music album", "MusicAlbum");
    	knowledgeKeywords.put("music group", "MusicGroup");
    	knowledgeKeywords.put("music recording", "MusicRecording");
    	knowledgeKeywords.put("organization", "Organization");
    	knowledgeKeywords.put("periodical", "Periodical");
    	knowledgeKeywords.put("tv episode", "TVEpisode");
    	knowledgeKeywords.put("organization", "Organization");
    	knowledgeKeywords.put("band", "MusicGroup");
    	knowledgeKeywords.put("sports team", "SportsTeam");
    	knowledgeKeywords.put("baseball", "SportsTeam");
    	knowledgeKeywords.put("football", "SportsTeam");
    	knowledgeKeywords.put("rugby", "SportsTeam");
    	knowledgeKeywords.put("NGO", "Organization");
    	knowledgeKeywords.put("magazine", "Periodical");
    	knowledgeKeywords.put("journal", "Periodical");
    	knowledgeKeywords.put("newspapers", "Periodical");
    	knowledgeKeywords.put("concert", "Event");
    	knowledgeKeywords.put("lecture", "Event");
    	knowledgeKeywords.put("festival", "Event");
    	knowledgeKeywords.put("seminar", "Event");
    	knowledgeKeywords.put("conference", "Event");
    	knowledgeKeywords.put("government agency", "GovernmentOrganization");
    	knowledgeKeywords.put("orchestra", "MusicGroup");
    	knowledgeKeywords.put("choir", "MusicGroup");
    	knowledgeKeywords.put("track", "MusicRecording");
    	knowledgeKeywords.put("song", "MusicRecording");
    	knowledgeKeywords.put("songs", "MusicRecording");
    	knowledgeKeywords.put("episode", "TVEpisode");
    	knowledgeKeywords.put("tv series", "TVSeries");
    	knowledgeKeywords.put("video game", "VideoGame");
    	knowledgeKeywords.put("video game series", "VideoGameSeries");
    	knowledgeKeywords.put("president", "Person");
    	knowledgeKeywords.put("prime minister", "Person");
    	knowledgeKeywords.put("chief minister", "Person");
    	knowledgeKeywords.put("home minister", "Person");
    	knowledgeKeywords.put("chief justice", "Person");
    	knowledgeKeywords.put("secretary of state", "Person");
    	knowledgeKeywords.put("ambassador", "Person");
    	knowledgeKeywords.put("senator", "Person");
    	knowledgeKeywords.put("minister", "Person");
    	knowledgeKeywords.put("what", "Thing");
    	knowledgeKeywords.put("inventor", "Person");
    	knowledgeKeywords.put("who", "Person");
	    
	}

}
