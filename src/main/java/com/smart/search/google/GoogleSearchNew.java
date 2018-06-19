package com.smart.search.google;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.Token;
import com.smart.search.analysis.SearchAnalysis;
import com.smart.search.wiki.WikiSearch;
import com.smart.search.wiki.WikihowSearch;
import com.google.cloud.language.v1.Document.Type;

public class GoogleSearchNew {

	public static void main(String args[]) {

		try {
			String text = "how can you tell the difference between zeeshan and iman";

			LanguageServiceClient language = LanguageServiceClient.create();
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();

			List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();

			List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
			String validInvalid = "9";
			googleSearch(text, entityList, tokenList, validInvalid);

		} catch (Exception e) {
			e.printStackTrace();
		} // End Of TryCatch

	}// End Of Main

	public static String googleSearch(String searchQuery, List<Entity> entityList, List<Token> tokenList, String validInvalid) {

		String query = searchQuery.trim();
		
		String ret = null;

		boolean wikiHow = false;
		boolean noun = false;
		//boolean verb = false;
		boolean beVerb = false;
		//boolean doVerb = false;
		//boolean adverb = false;
		boolean pronoun = false;
		//boolean adjective = false;
		//boolean properNoun = false;
		boolean googleSearch = false;
		boolean questionWords = false;
		boolean iPresent = false;
		boolean youPresent = false;
		boolean mePresent = false;
		boolean willPresent = false;


		String wikipediaUrl = null;

		for (Token t : tokenList) {
			String lemma = t.getLemma().toString();
			String tag = t.getPartOfSpeech().getTag().toString();
			//String prop = t.getPartOfSpeech().getProper().toString();
			String content = t.getText().getContent().toString();

			if (tag.equals("NOUN"))
				noun = true;
			else if (tag.equals("PRON")) {
				pronoun = true;

				if (content.equalsIgnoreCase("what") || content.equalsIgnoreCase("which")
						|| content.equalsIgnoreCase("when") || content.equalsIgnoreCase("where"))
					questionWords = true;

			}/* else if (tag.equals("ADV"))
				adverb = true;
			else if (tag.equals("ADJ"))
				adjective = true;
			else if (tag.equals("VERB"))
				verb = true;*/

			/*if (prop.equals("PROPER"))
				properNoun = true;*/

			if (lemma.equals("be"))
				beVerb = true;
			/*else if (lemma.equals("do"))
				doVerb = true;*/
			
			if(content.equals("i"))
				iPresent = true;
			else if(content.equals("you"))
				youPresent = true;
			else if(content.equals("me"))
				mePresent = true;
			else if(content.equals("will"))
				willPresent = true;
		}

		for (Entity e : entityList) {
			if (e.getMetadataMap().containsKey("wikipedia_url")) {
				wikipediaUrl = e.getMetadataMap().get("wikipedia_url");
			}
		}

		if (searchQuery.startsWith("how to") || searchQuery.startsWith("how can i") || searchQuery.startsWith("what should i do to") || searchQuery.startsWith("what should you do to")) {
			// map to WikiHow
			wikiHow = true;
			googleSearch = true;
		} else if (wikipediaUrl != null) {
			// go to wikipediaUrl
			googleSearch = true;
		} else if (searchQuery.startsWith("how come"))
			return ret;
		else if (searchQuery.startsWith("why do i") || searchQuery.startsWith("why do you") || searchQuery.startsWith("why did i") || searchQuery.startsWith("why did you")) {
			return ret;
		} else if (beVerb && (pronoun && !questionWords)) {
			return ret;
		} else if (!noun && pronoun) {
			return ret;
		} else if (searchQuery.startsWith("who")) {
			if (iPresent || youPresent)
				return ret;
			googleSearch = true;
		} else if (searchQuery.startsWith("when")) {
			if (iPresent || youPresent || mePresent)
				return ret;
			googleSearch = true;
		} else if (searchQuery.startsWith("which")) {
			if (willPresent)
				return ret;
			googleSearch = true;
		} else if (searchQuery.startsWith("will")) {
			if (iPresent || youPresent) {
				return ret;
			}
			googleSearch = true;
		} else if (!noun) {
			return ret;
		} else {
			// search on google
			googleSearch = true;
		}

		/*if (wikiHow) {
			return true;
		} else if (wikipediaUrl != null) {
			return true;
		} else*/ 
		if (googleSearch) {
			System.out.println("Second Filter Passed. Query String is Valid.");
			System.out.println("Calling Google Custom Api...");

			try {
				//String key = "AIzaSyBrvELoc03re5k7-6ZeXDQfdM4kG6Y051o"; //for Karan Account
				String key = "AIzaSyDwcngdH1nr9H0RQUZh6L5qoUePEHPqpxk";

				URL url = new URL("https://www.googleapis.com/customsearch/v1?key=" + key
						+ "&cx=013036536707430787589:_pqjad5hr1a&q=" + URLEncoder.encode(query, "UTF-8") + "&alt=json");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "application/json");
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				String output = null;
				StringBuilder response = new StringBuilder();

				while ((output = br.readLine()) != null) {
					response.append(output);
				}

				System.out.println("Response:" + response);
				
				JSONObject obj = new JSONObject(response.toString());
			    JSONArray webPages = obj.getJSONArray("items");

			    JSONObject temp = null;
			    String displayLink = null;
			    
			    String wikiHowUrl = null;
			    String wikiUrl = null;
			    
			    String snippetWikiHow = null;
			    String snippetWiki = null;
			    
			    String link = null;
			    String snippet = null;
			    
				for(int i=0; webPages!=null && i<5; i++)	{
					temp = (JSONObject)webPages.get(i);
					displayLink = (String)temp.get("displayLink");
					
					if(displayLink.contains("wikihow"))	{
						wikiHowUrl = (String)temp.get("link");
						snippetWikiHow = (String)temp.get("snippet");
					}else if(displayLink.contains("wikipedia"))	{
						wikiUrl = (String)temp.get("link");
						snippetWiki = (String)temp.getString("snippet");
					}	
				}//End Of WikiHow and wikipedia check
				
				if(wikiHowUrl == null && wikiUrl == null)	{
					temp = (JSONObject)webPages.get(0);
					link = (String)temp.get("link");
					snippet = (String)temp.get("snippet");
				}
				
				
				if (wikiHowUrl != null) {
					// send to wiki how method with parameters(wikiHowUrl and
					// snippetWikiHow)
					System.out.println("Calling WikiHow API...");
					String wikihowResults = new WikihowSearch().search(wikiHowUrl, snippetWikiHow);
					if(wikihowResults != null && wikihowResults.length() > 0){
						SearchAnalysis.bw.write("Consumed in WikiHow Api | Query : "+searchQuery+" | Calculated Validity : 8 | Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Wikihow Api | Query : "+searchQuery+" | Calculated Validity : 8 | Original Validity : "+validInvalid);
						ret = wikihowResults;
						return ret;
					}
				}else if (wikiUrl != null) {
					System.out.println("Calling Wikipedia API...");
					String wikiResults = new WikiSearch().search(wikiUrl, snippetWiki);
					if(wikiResults != null && wikiResults.length() > 0){
						SearchAnalysis.bw.write("Consumed in Wikipedia Api | Query : "+searchQuery+" | Calculated Validity : 9 | Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Wikipedia Api | Query : "+searchQuery+" | Calculated Validity : 9 | Original Validity : "+validInvalid);
						ret = wikiResults;
						return ret;
					}
					// send to wiki method with parameters(wikiUrl and
					// snippetWiki)
				}else {
					// send first link and snippet
					String crawlResults = Crawler.crawl(link, snippet);
					if(crawlResults != null && crawlResults.length() > 0){
						SearchAnalysis.bw.write("Consumed in Crawler Api | Query : "+searchQuery+" | Calculated Validity : 6 | Original Validity : "+validInvalid+"\n");
						System.out.println("Consumed in Crawler Api | Query : "+searchQuery+" | Calculated Validity : 6 | Original Validity : "+validInvalid);
						ret = crawlResults;
						return ret;
					}
				}
				
					
				conn.disconnect();
//				SearchAnalysis.bw.write("Consumed in Custom Google Api | Query : "+searchQuery+" | Calculated Validity : 6 | Original Validity : "+validInvalid+"\n");
//				System.out.println("Consumed in Custom Google Api | Query : "+searchQuery+" | Calculated Validity : 6 | Original Validity : "+validInvalid);
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
			} // End Of TryCatch

		} 
		return ret;

	}// End Of GoogleSearch

}
