package com.smart.search.ms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.Token;

public class NewsApi {
	
	
	
	public static String callApiNews(String searchQuery, List<Entity> entityList, List<Token> tokenList){
		String ret = null;
		try	{
			
			//System.out.println("Entering callApiNews");
			String searchQueryFirstToken = searchQuery.split(" +")[0];
			
			if(searchQueryFirstToken.equalsIgnoreCase("why"))
			{
				System.out.println("'why' found in search string!");
				return ret;
			}
			
			String key = "ba143a6eb2ac4f609a00f8ea8e09e183";
			String country = "";
			
			boolean goAheadFlag = false;
			
			String checkKeyword[] = {"involving","current affairs","headlines","scandal","rumours","press release", "hot news",
									"bulletin","breaking news", "latest news", "news updates","trending news","top news"};
			boolean keywords = false;
			boolean noun = false;
			boolean specificPrep = false;
			String keywordFound = "";
			String entityName = "";
			String entityType = "";
			String entityMentionType = "";
			
			/*for(Token t : tokenList){
		    	PartOfSpeech pos=t.getPartOfSpeech();	    	
		    	String token=t.getText().getContent();
		    	String tag = pos.getTag().toString();//NOUN/ADP/DET/PRON/VERB
		    	//System.out.println("text ["+text+"] label: ["+label+"]");
		    	//alLevelOne.add(new LevelOne(tag, person, proper, lemma, label));
		    }*/
			
			for(String check : checkKeyword)
			{
				if(searchQuery.contains(check))
				{
					keywordFound = check;
					keywords = true;
					break;
				}
			}
			
			for(Entity x : entityList)
			{
				entityType = x.getType().toString();
				
				List<EntityMention> entityMentionList= x.getMentionsList();
				for(EntityMention em : entityMentionList)
				{
					entityMentionType = em.getType().toString();
					//System.out.println("EntityMention Type : " + entityMentionType);
					if(entityMentionType.equals("COMMON") || entityMentionType.equals("PROPER"))
					{
						noun = true;
					}
				}
				//System.out.println("Entity Type : " + entityType);
				
				if(entityType.equals("LOCATION") && entityMentionType.equals("PROPER"))
				{
					country = x.getName().toString();
				}
				else
				{
					entityName += " "+x.getName().toString();
				}
			}
			
			for(Token t : tokenList)
			{
				String tag = t.getPartOfSpeech().getTag().toString();
				String prep = t.getText().getContent().toString();
				if(tag.equalsIgnoreCase("adp") && (prep.equalsIgnoreCase("about") || prep.equalsIgnoreCase("to") || prep.equalsIgnoreCase("on")))
				{
					specificPrep = true;
					break;
				}
			}
			
			System.out.println("Country : " + country);
			System.out.println("Entity Name : " + entityName);
			//if(entityMentionType.equals("COMMON"))
			System.out.println("Keywords : "+keywords+", Noun : "+noun+", Specific Prep : "+specificPrep);
			
			URL url = null;
			if(searchQuery.toLowerCase().contains("news ") && specificPrep)
			{
				goAheadFlag = true;
				url = new URL("https://api.cognitive.microsoft.com/bing/v5.0/news/search?q=" + URLEncoder.encode(searchQuery, "UTF-8"));
			}
			else if(keywords && noun)
			{
				
				goAheadFlag = true;
				if(country.equals(""))
				{
					url = new URL("https://api.cognitive.microsoft.com/bing/v5.0/news/search?q=" + URLEncoder.encode(entityName, "UTF-8") + "+news");
				}
				else
				{
					url = new URL("https://api.cognitive.microsoft.com/bing/v5.0/news/search?q=" + URLEncoder.encode(country, "UTF-8") + "+" + URLEncoder.encode(entityName, "UTF-8") + "+news");
				}
			}
			else if(tokenList.size()==1 && tokenList.get(0).getText().getContent().toString().equalsIgnoreCase("news"))
			{
				goAheadFlag = true;
				url = new URL("https://api.cognitive.microsoft.com/bing/v5.0/news/search?q=news");
			}
			
			if(goAheadFlag)
			{
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("Ocp-Apim-Subscription-Key", key);
			
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		      
				String output = null;
				StringBuilder response = new StringBuilder();
	      
				while((output=reader.readLine())!=null){
					response.append(output);
				}
	      
				System.out.println("Response:"+response);
			
				ret = response.toString();
			//parsing the result
			
				JSONObject root = new JSONObject(response.toString());
				JSONArray values = root.getJSONArray("value");
			
				String name = "";
				
				int limitLoop = 2;
				if(values.length() < 3)
					limitLoop = values.length();
				
				for(int i=0; values!=null && i<=limitLoop; i++)	{
					name = ((JSONObject)values.get(i)).getString("name");
					System.out.println("headline:"+name);
				}
			
			}
			//parsing the result
			
			/*
			JSONObject root = new JSONObject(response.toString());
			JSONArray values = root.getJSONArray("value");
			
			String name = "";
			
			for(int i=0; values!=null && i<values.length(); i++)	{
				name = ((JSONObject)values.get(i)).getString("name");
				System.out.println("headline:"+name);
			}*/
		
		//System.out.println(goAheadFlag);
		//return goAheadFlag;
		}catch(Exception e)	{
			e.printStackTrace();
			//return false;
		}
		return ret;
	}

}
