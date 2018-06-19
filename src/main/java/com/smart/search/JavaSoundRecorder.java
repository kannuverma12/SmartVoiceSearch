package com.smart.search;

import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.bcel.Const;
import org.json.JSONArray;
import org.json.JSONObject;

import com.smart.search.constants.Constants;
import com.smart.search.texttospeech.TTSSample;



public class JavaSoundRecorder {
    // record duration, in milliseconds
    static final long RECORD_TIME = Constants.RECORD_TIME;  // 1 minute
    
    static BufferedReader br = null;
	static HttpsURLConnection con = null;
	static InputStream is = null;
	static List<NameValuePair> headers;
	static JSONObject root = null;
	
//	static JSONObject webPages = null; //for bing
	static JSONArray webPages = null; //for google
	
	static JSONArray  value = null;
	static JSONObject tempObj = null;
	static String tempVal =null;
	
	static Map<String, String> mapKeywords;
    static Map<String, String> knowledgeKeywords;
	
    static Map<String, String> weatherKeywords;
    static Map<String, String> forecastKeywords;
    
    static Map<String, String> newsKeywords;
	
    // path of the wav file
    File wavFile = new File(Constants.WAV_FILE_LOCATION);
 
    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
 
    // the line from which audio data is captured
    TargetDataLine line;
 
    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }
 
    /**
     * Captures the sound and record into a WAV file
     */
    void start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
 
            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing
 
            System.out.println("Start capturing...");
 
            AudioInputStream ais = new AudioInputStream(line);
 
            System.out.println("Start recording...");
 
            // start recording
            AudioSystem.write(ais, fileType, wavFile);
 
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
 
    /**
     * Closes the target data line to finish capturing and recording
     */
	    void finish() {
	    	try 
	    	{
	            line.stop();
	            line.close();
	            System.out.println("Finished");
	            
	            
	            String token = null;
	            String speechToText = null;
	            String textWebSearch = null;
	            
	    		
	    		headers.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
	    		//headers.add(new BasicNameValuePair("Ocp-Apim-Subscription-Key", "5187c84ce7164289bd6cbc9b9e13ca52"));	
	    		headers.add(new BasicNameValuePair("Ocp-Apim-Subscription-Key", "0bb5bf28e29e474cb765b28b4a370adf"));	
	    		
	    		token = makePost("https://api.cognitive.microsoft.com/sts/v1.0/issueToken", headers, null);
	    		
	    		headers.clear();
	    		
	    		headers.add(new BasicNameValuePair("Content-type", "audio/wav"));
	    		headers.add(new BasicNameValuePair("codec", "audio/pcm"));
	    		headers.add(new BasicNameValuePair("samplerate", "44100"));
	    		headers.add(new BasicNameValuePair("Authorization","Bearer "+token));
	    		String searchQuery = "";
	    		
	    		//commenting the below for google SpeechtoText
	    		/*
	    		speechToText = makePost("https://speech.platform.bing.com/recognize?scenarios=smd&appid=D4D52672-91D7-4C74-8AD8-42B1D98141A5&locale=en-US&device.os=mac&version=3.0&format=json&instanceid=b2c95ede-97eb-4c88-81e4-80f32d6aee55&requestid=b2c95ede-97eb-4c88-81e4-80f32d6aee56",headers, wavFile);
	    		
	    		
	    		
	    		System.out.println("speechToText = "+speechToText);
	    		root = new JSONObject(speechToText);
	    		System.out.println("JSON root: "+root.toString(1));
	    		String speechStatus = (String) ((JSONObject) root.get("header")).get("status");
	    		
	    		if(speechStatus.equalsIgnoreCase("success"))
	    			searchQuery = (String) ((JSONObject) root.get("header")).get("lexical");
	    		*/
	    		
	    		//calling google speechtotext
	    		long c = System.currentTimeMillis();
	    		System.out.println("Google STT start : "+c);
	    		//searchQuery = GoogleSTT.speechToText(wavFile.getAbsolutePath());
	    		System.out.println("Google STT finish : "+System.currentTimeMillis()+ ". Total time "+(System.currentTimeMillis()-c));
	    		searchQuery = "who is Bill clinton";
	    		if(searchQuery!=null && !searchQuery.equals("")) {
	    			System.out.println("\nsearchQuery for api: "+searchQuery+"\n");
	    		      
	    			String[] query = searchQuery.split(" ");
	    		      
	    		    boolean mapSearch = false;
	    		    boolean knowledgeSearch = false;
	    		    
	    		    boolean weatherSearch = false;
	    		    boolean forecastSearch = false;
	    		    
	    		    boolean newsSearch = false;
	    		    
	    		    
	    		    String type = null;
	    		      
	    		    for(int i=0; query!=null && i<query.length; i++){
	    		    	if(mapKeywords.containsKey(query[i])){
	    		    		mapSearch = true;
	    		        
	    		    	}else if(knowledgeKeywords.containsKey(query[i])) {
	    		    		type = knowledgeKeywords.get(query[i]);
	    		    		knowledgeSearch = true;
	    		        }
	    		    	if(weatherKeywords.containsKey(query[i])) {
	    		            weatherSearch = true;
	    		        }else if (forecastKeywords.containsKey(query[i])) {
	    		            forecastSearch = true;
	    		        }
	    		    	
	    		    	if(newsKeywords.containsKey(query[i]))
	    		    		newsSearch = true;
	    		    }
	    		    
	    		    StringBuffer convertToVoice = new StringBuffer();
	    		    
	    		    
	    		    if(newsSearch){
	    		    	textWebSearch = newsSearch(searchQuery);
	    		    	
	    		    	//json structure : root -> main -> value array -> ith element -> name
	    		    	root = new JSONObject(textWebSearch);
		    			webPages = root.getJSONArray("value");
		    			
		    			for(int x=0;webPages!=null && x<webPages.length();x++) 
		        		{
		    				tempObj = (JSONObject) webPages.get(x);
		    				tempVal = (String) tempObj.get("name");
		    				convertToVoice.append(tempVal+". ");
		        		}
	    		    }
	    		    
	//    		    if(forecastSearch){
	//    		    	System.out.println("Going to call Temparature forecast API...");
	//    		    	textWebSearch = forecastSearch(searchQuery);
	//    		    }
	    		    else if(weatherSearch){
	    		    	System.out.println("Going to call Current Temparature API...");
	    		    	textWebSearch = weatherSearch(searchQuery);
	    		    	
	    		    	//json structure : root -> main -> temp, pressure, humidity
	    		    	root = new JSONObject(textWebSearch);
	    		    	JSONObject mainObj = root.getJSONObject("main");
	    		    	String temp = (String) mainObj.get("temp").toString();
	//    		    	String pressure = (String) mainObj.get("pressure").toString();
	//    		    	String humidity = (String) mainObj.get("humidity").toString();
	    		    	convertToVoice.append("Temparature today is "+temp+" degree.");	// Pressure here is  "+pressure+". And Humidity is "+humidity);
	    		    	
	    		    }else if(mapSearch){
		    			//call map/place api
	    		    	System.out.println("Going to Call Map API...");
		    			textWebSearch = searchViaGoogleMapApi(searchQuery);
		    			
		    			// json structure- > root-> results array -> ith element -> name
		    			
		    			root = new JSONObject(textWebSearch);
		    			webPages = root.getJSONArray("results");
		    			
		    			for(int x=0;webPages!=null && x<webPages.length();x++) 
		        		{
		    				tempObj = (JSONObject) webPages.get(x);
		    				tempVal = (String) tempObj.get("name");
		    				String address = (String) tempObj.get("vicinity");
		    				
		    				String lastContent = getStreetAddress(address);
		    				convertToVoice.append(tempVal+", "+lastContent+". ");
		        		}
		    			System.out.println("From Map Api. ConvertToVoice : "+convertToVoice.toString());
		    			//System.out.println("Converting to voice...");
		    			//TTSSample.tts(convertToVoice.toString());
		    			
		    		}
	    		    else if(knowledgeSearch){
		    			//call knowledge Graph Api
		    			System.out.println("Going to Call Knowldege Graph API...");
		    			textWebSearch = searchViaKnowledgeGraphApi(searchQuery, type);
		    			
		    			// json structure -> root -> itemListElement array -> ith element -> result -> name
		    			root = new JSONObject(textWebSearch);
		    			webPages = root.getJSONArray("itemListElement");
		    			
		    			for(int x=0;webPages!=null && x<webPages.length();x++) 
		        		{
		    				tempObj = (JSONObject) webPages.get(x);
		    				JSONObject resultObject = tempObj.getJSONObject("result");
		    				tempVal = (String) resultObject.get("name");
		    				convertToVoice.append(tempVal+". ");
		        		}
		    			System.out.println("From Knowledge Graph Api. ConvertToVoice : "+convertToVoice.toString());
		    			
		    			//TTSSample.tts(convertToVoice.toString());
		    			
		    		}
	    		    
	    		    if(!convertToVoice.toString().equals("")  || convertToVoice.toString().length() != 0){
	    		    	System.out.println("Converting to voice...");
	    		    	//System.out.println("Snippet = "+convertToVoice.toString()+" \nlength : "+convertToVoice.length());
	    		    	if(convertToVoice.length() > 250)
	    		    		convertToVoice.setLength(250);
	    		    	System.out.println("Snippet = "+convertToVoice.toString()+" \nlength : "+convertToVoice.length());
	    		    	TTSSample.tts(convertToVoice.toString());
	    		    }
		    		else{
		    			System.out.println("Calling Google Search APIs...");
		    			
		    			headers.clear();
		        		
		        		headers.add(new BasicNameValuePair("Ocp-Apim-Subscription-Key", "3175a2b2db31462dbbb78104df9e920d"));
		        		String webSearchUrl="https://api.cognitive.microsoft.com/bing/v5.0/search?q="+URLEncoder.encode(searchQuery,"UTF-8")+"&count=10&offset=0&mkt=en-us&safesearch=Moderate";
		        		
		        		//textWebSearch = makeGet(webSearchUrl, headers);	//for bing
		        		textWebSearch = searchViaGoogleApi(searchQuery);	//for google
		        		
		        		headers.clear();
		        		
		        		root = new JSONObject(textWebSearch);
		        		
		        		//webPages = root.getJSONObject("webPages");   	//for bing
		        		webPages = root.getJSONArray("items");   		//for google
		        		
		        		//value = webPages.getJSONArray("value"); 		//for bing
		        		
		        		String snippet = null;
		        		String snippet1 = "";
		        		boolean found = false;
		        		
	   	        		//for(int x=0;value!=null && x<value.length();x++) 			//for bing
		        		for(int x=0;webPages!=null && x<webPages.length();x++) 		//for google
		        		{	
		        			//tempObj = (JSONObject) value.get(x);  		//for bing
		        			tempObj = (JSONObject) webPages.get(x);  		//for google
		        			//System.out.println("tempObj : "+tempObj.toString(1));
		        			
		        			//tempVal = (String) tempObj.get("displayUrl"); 		//for bing
		        			tempVal = (String) tempObj.get("displayLink"); 			//for google
		        			
		        			//System.out.println("tempVal : ["+tempVal+"]");
		        			
		        			if(tempVal!=null && tempVal.toLowerCase().indexOf("wiki")!=-1)
		    				{
		    					snippet = (String) tempObj.get("snippet");
		    					System.out.println("Snippet = "+snippet);
		    					TTSSample.tts(snippet);
		    					found = true;
		    					break;
		    				}
		        			else{
		        				snippet1 = (String) tempObj.get("snippet");
		        			}
		        				
		        		}
		        		if(!found) {
		        			System.out.println("Snippet1 = "+snippet1);
		        			TTSSample.tts(snippet1);
		        		}
		    		}
	    		      
	    		}else{
	    			System.out.println("Audio is not recognizable. Please try again...");
	    		}
			}
	    	catch (Exception e) 
	    	{
				e.printStackTrace();
			}
		
    }
 
    public String getStreetAddress(String address) {
    	String retString = "";
    	String[] addList = address.split(",");
//    	if(addList.length > 1){
//    		String[] ret = new String[2];
//	    	for(int i = addList.length-1, j=1; i > 0 && j>=0; i--,j-- ){
//	    		String add = addList[i];
//	    		if(add != null && !add.equals("null") && !add.equals(""))
//	    			ret[j] = add;
//	    	}
//	    	for(int x=0; x<ret.length;x++){
//	    		if(ret[x]!=null && !ret[x].equals("null") && !ret[x].equals("")){
//	    			retString += ret[x];
//	    			if(x == 0)
//	    				retString += ", ";
//	    		}
//	    	}
//    	}
    	System.out.println(" addList.length = "+addList.length);
    	//for san fransisco
    	if (addList.length == 1 || addList.length == 2) {
			retString += addList[0];
		}else if (addList.length == 3) {
			retString += addList[2];
		}else if (addList.length == 4) {
			retString += addList[1] + ", " + addList[2];
		}else{
			retString += addList[2] + ", " + addList[3];
		}
    	//System.out.println("retString = "+retString);
		return retString;
	}

	public static String searchViaKnowledgeGraphApi(String searchQuery, String type) {
    	try {
		      
		      String key = "AIzaSyCddsSLG7-oWo-r1y-5F4MX-Lof-_E0b7g";
		      //String searchQuery = "Universities in delhi";
		      URL url = null;
		      if(type != null)
		    	  url = new URL("https://kgsearch.googleapis.com/v1/entities:search?query="+ URLEncoder.encode(searchQuery, "UTF-8") 
		      						+"&types="+ type +"&limit=10&indent=true&key="+key);
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

	private String searchViaGoogleApi(String searchQuery) {
    	try {
	    	String key="AIzaSyDu17eqwHNlp6NViyTJ7WMnYneic2nw81A";
		    URL url = new URL(
				        "https://www.googleapis.com/customsearch/v1?key="+key+ "&cx=013036536707430787589:_pqjad5hr1a&q="+ URLEncoder.encode(searchQuery,"UTF-8") + "&alt=json");
			
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("GET");
		    conn.setRequestProperty("Accept", "application/json");
		    BufferedReader br = new BufferedReader(new InputStreamReader(
		            (conn.getInputStream())));
	
		    String output;
		    StringBuffer returnOutput = new StringBuffer();
		    while ((output = br.readLine()) != null) {
		    	returnOutput.append(output);
		    }
		    
		    conn.disconnect(); 
		    System.out.println("Json Result \n"+returnOutput.toString());
		    return returnOutput.toString();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	/**
     * Entry to run the program
     */
    public static void main(String[] args) {
    	
    		newsKeywords = new HashMap<String, String>();
    	
    		mapKeywords = new HashMap<String, String>();
        knowledgeKeywords = new HashMap<String, String>();
        
        weatherKeywords = new HashMap<String, String>();
        forecastKeywords = new HashMap<String, String>();
        
        JavaSoundRecorder.populateMap();
    	
    		headers=new ArrayList<NameValuePair>();
        final JavaSoundRecorder recorder = new JavaSoundRecorder();
 
        // creates a new thread that waits for a specified
        // of time before stopping
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(RECORD_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                recorder.finish();
            }
        });
 
        stopper.start();
 
        // start recording
        recorder.start();
    }
    
	public static String makePost(String url,List<NameValuePair> headList,File data){
		try 
		{
			/*final HttpParams httpParams = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(httpParams, 40000);*/
		    
		    
     		   HttpClient httpclient = new DefaultHttpClient();
     		   
			   URIBuilder builder = new URIBuilder(url);
	           URI uri = builder.build();	           
	           HttpPost request = new HttpPost(uri);
	           
	           for(int x=0;headList!=null && x<headList.size();x++)
	           {
	        	   request.setHeader(headList.get(x).getName(), headList.get(x).getValue());
	           }
	           /*request.setHeader("Content-Type", "application/x-www-form-urlencoded");
	           request.setHeader("Ocp-Apim-Subscription-Key", "5187c84ce7164289bd6cbc9b9e13ca52");*/	           
	           	//System.out.println("after setting of header in func");
	           	

	           // Request body
	           	if(data!=null && data.length()>0)
	           		{
		           		//StringEntity reqEntity = new StringEntity(data);
		           		FileEntity reqEntity=new FileEntity(data);
		           		request.setEntity(reqEntity);
	           		}	
	           	
	           HttpResponse response = httpclient.execute(request);
	           HttpEntity entity = response.getEntity();
	           //System.out.println("after getting of response");

	           if (entity != null) 
	           {
	        	   String sResp = EntityUtils.toString(entity);
	               //System.out.println("resp\n"+sResp);
	               return sResp;
	           }

		} 
		catch (Exception e) 
		{
			System.out.println("caught exception inside MAKE POST");
			e.printStackTrace();
		}
		return null;
	}// end of makePOST
	
	
	public static String makeGet(String url,List<NameValuePair> headList){
		try 
		{
			System.out.println("inside get for URL: "+url);
			

			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");
			for(int x=0; headList!=null && x<headList.size() ;x++)
			{
				con.setRequestProperty(headList.get(x).getName(), headList.get(x).getValue());				
			}

			//add request header
			//con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			
			System.out.println("[Get]Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			System.out.println("[Get]response from textual web search:\n"+response.toString());
			return response.toString();
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void populateMap() {
		
		newsKeywords.put("news", "news");
		newsKeywords.put("updates", "news");
		newsKeywords.put("headlines", "news");
		newsKeywords.put("trending", "news");
	     
		 mapKeywords.put("nearby", "map");
	     mapKeywords.put("nearest", "map");
	     mapKeywords.put("near", "map");
	     
	     weatherKeywords.put("weather", "weather");
	     weatherKeywords.put("temperature", "weather");
	     weatherKeywords.put("current temperature", "weather");
	     weatherKeywords.put("climate", "weather");
	     
	     forecastKeywords.put("forecast", "forecast");
	          
	     knowledgeKeywords.put("books", "Book");
	     knowledgeKeywords.put("book series", "BookSeries");
	     knowledgeKeywords.put("government organisations", "GovernmentOrganization");
	     knowledgeKeywords.put("event", "Event");
	     knowledgeKeywords.put("movies", "Movie");
	     knowledgeKeywords.put("films", "Movie");
	     knowledgeKeywords.put("movie series", "MovieSeries");
	     knowledgeKeywords.put("educational organisations", "EducationalOrganization");
	     knowledgeKeywords.put("schools", "EducationalOrganization");
	     knowledgeKeywords.put("school", "EducationalOrganization");
	     knowledgeKeywords.put("colleges", "EducationalOrganization");
	     knowledgeKeywords.put("universities", "EducationalOrganization");
	     knowledgeKeywords.put("restaurants", "LocalBusiness");
	     knowledgeKeywords.put("banks", "LocalBusiness");
	     knowledgeKeywords.put("medical shop", "LocalBusiness");
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
	     knowledgeKeywords.put("episode", "TVEpisode");
	     knowledgeKeywords.put("tv series", "TVSeries");
	     knowledgeKeywords.put("video game", "VideoGame");
	     knowledgeKeywords.put("video game series", "VideoGameSeries");

	     
	     
	}//End Of Method
	
	
	private String searchViaGoogleMapApi(String searchQuery) {
		try {
	    
		    String key = "AIzaSyA_DsW_ZuDAhqG_P4R006GHFhua0-ijvhs";
		    String location = "28.498414,77.084925"; //gurgaon
		    //String location = "-4.036878,39.669571"; //mumbasa
		    //String location = "37.7884,-122.4076"; //Union Square, San Fransisco
		
		    URL url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ location +"&radius=200&name="+URLEncoder.encode(searchQuery, "UTF-8")+"&key="+key);
		 
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
	}//End of map search
	
	
	private String weatherSearch(String searchQuery) {
	     try {
	     
	     String key = "0ece6aa6502526cd89014d4d21da07a1";
	     
	     String city = "San+fransisco";
	     String countryCode = "us";
	     
	     URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+ city +","+ countryCode +"&units=imperial&APPID=" +key);
	     
	       HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	       
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
	     
	    }//End Of WeatherSearch
	    
	    private String forecastSearch(String searchQuery) {
	     try {
	      
	       String key = "4998b1151d0a28e6493a95f103999d15"; 
	       String city = "Mombasa";
	         String countryCode = "ken"; 
	       
	       URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q="+ city +","+ countryCode +"&units=metric&APPID="+key);
	         
	         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	         
	         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         
	         String output = null;
	         StringBuilder response = new StringBuilder();
	         
	         while((output=reader.readLine())!=null){
	          response.append(output);
	         }
	         
	         System.out.println("Response:"+response);
	         return response.toString();
	         
	     }catch(Exception e) {
	      e.printStackTrace();
	      return null;
	     }
	    }
	
	
	
	    
	    private String newsSearch(String searchQuery) {
	        try {
	      
	      //String key = "3175a2b2db31462dbbb78104df9e920d";
	      String key = Constants.NEWS_KEY;
	      String country = Constants.NEWS_COUNTRY;
	      String newsUrl = Constants.NEWS_URL;
	      //URL url = new URL("https://api.cognitive.microsoft.com/bing/v5.0/news/search?q="+ country +"+news");
	      URL url = new URL(newsUrl+"/search?q="+ country +"+news");
	      
	      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	      conn.setRequestProperty("Ocp-Apim-Subscription-Key", key);
	      
	      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	           
	      String output = null;
	      StringBuilder response = new StringBuilder();
	          
	      while((output=reader.readLine())!=null){
	       response.append(output);
	      }
	          
	      System.out.println("Response:"+response);
	      
	      return response.toString();
	      
	      //parsing the result
	      
	      /*JSONObject root = new JSONObject(response.toString());
	      JSONArray values = root.getJSONArray("value");
	      
	      String name = "";
	      
	      for(int i=0; values!=null && i<values.length(); i++) {
	       name = ((JSONObject)values.get(i)).getString("name");
	       System.out.println("headline:"+name);
	      }*/
	      
	     }catch(Exception e) {
	      e.printStackTrace();
	      return null;
	     }
	       }
	
	
	
	
	
	
	
	
}