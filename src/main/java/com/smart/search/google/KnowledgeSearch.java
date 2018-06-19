package com.smart.search.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.Token;
import com.smart.search.analysis.SearchAnalysis;


public class KnowledgeSearch {
	
	public static Map<String, String> knowledgeKeywords = new HashMap<String, String>();
	
	public static void main(String args[])	{
		
		try	{
			String text = "what is microsoft";
			
			//knowldegeknowledgeKeywords  = new HashMap<String, String>();
			
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

			
			LanguageServiceClient language = LanguageServiceClient.create();
			Document doc = Document.newBuilder()
		            .setContent(text).setType(Type.PLAIN_TEXT).build();

			List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
		    
			List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
			
			keywordsSearch(text, entityList, tokenList);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}//End Of Main
	
	public static String keywordsSearch(String searchQuery, List<Entity> entityList, List<Token> tokenList)	{
		String ret = null;
		//System.out.println("searchQuery : "+searchQuery);
		String query = searchQuery.trim();
		//String aptApi = searchQuery.trim().substring(searchQuery.length()-2);
		
		//System.out.println("query:"+query);
		//System.out.println("aptApi:"+aptApi);
		
		String[] tokens = query.toLowerCase().split(" ");
		
		boolean verb = false;
		boolean noun = false;
		boolean proper = false;
		boolean inventor = false;
		
		String type = null;
		
		String finalQuery = "";
		
		List<String> searchWords = new ArrayList<String>();
		Map<String, String> kk = new HashMap<String, String>(SearchAnalysis.knowledgeKeywords);
		for(int i=0; tokens!=null && i<tokens.length; i++)	{
			if(kk.containsKey(tokens[i]))	{
				type = kk.get(tokens[i]);
				if(!tokens[i].equals("who") && !tokens[i].equals("what"))
					searchWords.add(tokens[i]);
				if(tokens[i].equals("inventor"))
					inventor = true;
			}	
		}
		
		for(Token t : tokenList)	{
			if(t.getPartOfSpeech().getTag().toString().equals("NOUN"))	{
				noun = true;
				if(inventor)
					searchWords.add(t.getText().getContent());
			}else if(t.getPartOfSpeech().getTag().toString().equals("VERB") && !t.getLemma().toString().equals("be") && !t.getText().getContent().toString().equals("invented"))
				verb = true;
				
			if(t.getPartOfSpeech().getProper().toString().equals("PROPER"))	{
				proper = true;
				searchWords.add(t.getText().getContent());
			}	
		}
		
		
		if((type!=null && proper && !verb) || (inventor && noun && !proper))	{
			
			//System.out.println("type:"+type);
			for(int i=0; searchWords!=null && i<searchWords.size(); i++)
				 finalQuery += searchWords.get(i) +" ";
			
			//System.out.println("final query:"+finalQuery);
			
			try {
			
				String key = "AIzaSyCddsSLG7-oWo-r1y-5F4MX-Lof-_E0b7g";

				URL url = null;

				url = new URL(
						"https://kgsearch.googleapis.com/v1/entities:search?query="+ URLEncoder.encode(finalQuery, "UTF-8")+ "&types="+ type + "&limit=10&indent=true&key="+ key);

				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String output = null;
				StringBuilder response = new StringBuilder();

				while ((output = reader.readLine()) != null) {
					response.append(output);
				}

				System.out.println("Response:" + response);
				
				JSONObject obj = new JSONObject(response.toString());
			    JSONArray itemList = obj.getJSONArray("itemListElement");
			      
			    //System.out.println(itemList.length());
			      
			    if(itemList.length()>0){
			    	//System.out.println("text passed: "+query + "valid : true by E4 aptApi by input:"+aptApi);
			    	ret = response.toString();
			    	//return true;
			    }

			}catch (Exception ex) {
				ex.printStackTrace();
			}//End Of Try-Catch
		}
		return ret;
		
	}//End of knowledgeGraph

}
