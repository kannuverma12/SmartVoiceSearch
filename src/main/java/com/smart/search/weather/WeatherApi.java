package com.smart.search.weather;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.PartOfSpeech;
import com.google.cloud.language.v1.Sentence;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Token;
import com.google.gson.JsonObject;

public class WeatherApi {
	static int valid=0;
	static int invalid=0;
	static String one;
	static String two;
	static String configPath;
	static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy");
	static int[] days={31,28,31,30,31,30,31,31,30,31,30,31};
	static int[] daymultiples={7,14,21,28};
  public static void main(String... args) throws Exception {
    // Instantiates a client
    //levOne=new LevelOne();
	  
	  BufferedReader br = null;
	  String line="";
	  
	  br = new BufferedReader(new FileReader(new File("config.properties")));
	  while((line=br.readLine())!=null)
	  {
		  System.out.println(">>"+line);
		  if(line.split("=")[0].equals("one"))
			  one = line.split("one=")[1].trim();
		  else if(line.split("=")[0].equals("two"))
			  two = line.split("two=")[1].trim();
		  else if(line.contains("path"))
			  configPath=line.split("=")[1].trim();
	  }
	 System.out.println("<<<<< one: "+one+" two: "+two+" configpath: "+configPath);
    /*alLevelOne=new ArrayList<LevelOne>();
    br=new BufferedReader(new FileReader(new File(configPath)));
    line="";
    while((line=br.readLine())!=null)
    {
    	System.out.println("line being sent for processing: "+line);
    	processString(line.substring(0,line.length()-2).trim(),line.substring(line.length()-1, line.length()));
    }//end of while
    System.out.println(" valid count: "+valid+" invalid count: "+invalid);*/
    processString(configPath, "VALID");
  }
  private static void processString(String text,String validInvalid){
	  try 
	  {
		  //validInValid will be 1|2
		  LanguageServiceClient language = LanguageServiceClient.create();
		  Document doc = Document.newBuilder()
		            .setContent(text).setType(Type.PLAIN_TEXT).build();

		    // Detects the sentiment of the text
		    //Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
		    List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
		//    
//		    List<Sentence> sentenceList = language.analyzeSyntax(doc, EncodingType.UTF8).getSentencesList();
		    
		    long dur = System.currentTimeMillis();
		    List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
		    dur=System.currentTimeMillis()-dur;
		    dur= (dur/1000);
		    		    
		    //System.out.println("no of tokens in query: "+tokenList.size()+" tokenList \n<<"+tokenList.toString()+">>");
			    /*for(Token t : tokenList){
			    	
			    	PartOfSpeech pos=t.getPartOfSpeech();	    	
			    	//String token=t.getText().getContent();
			    	String tag = pos.getTag().toString();//NOUN/ADP/DET/PRON/VERB
			    	String person = pos.getPerson().toString();
			    	String proper = pos.getProper().toString();
			    	String lemma = t.getLemma();
					String label = t.getDependencyEdge().getLabel().toString();
			    	//System.out.println("text ["+text+"] label: ["+label+"]");
			    	alLevelOne.add(new LevelOne(tag, person, proper, lemma, label));
			    }
			    boolean rejectOne = filterOne(text,alLevelOne);*/
			    String rejectFromWeather = weatherSearch(text, tokenList,entityList);
			    boolean rej = (rejectFromWeather==null || rejectFromWeather.equals("")) ? false : true;
			    
			    System.out.println("REJECT BY WEATHER API: "+rej);
			    //alLevelOne.clear();
			    
			   /* if(validInvalid.equalsIgnoreCase("1"))
			    	System.out.println(">> text : "+text+" result: "+rejectOne+" duration: "+dur+" valid: "+NLPTest.one);
			    else if(validInvalid.equalsIgnoreCase("2"))
			    	System.out.println(">> text : "+text+" result: "+rejectOne+" duration: "+dur+" valid: "+NLPTest.two);
			    
			    if(rejectOne)
			    	invalid++;
			    else
			    	valid++;*/
	  }
	  catch (Exception e) 
	  {
		e.printStackTrace();
	  }
  }
	
	public static String weatherSearch(String searchQuery,List<Token> tokenList,List<Entity> entityList){
		String ret = null;
		//if got result and  
		try	{
	    	
			String key = "a0e0686a1eca20c3b7d5399fef2e15cd";//String key = "4998b1151d0a28e6493a95f103999d15";
	    	
	    	String city = "Delhi";//String city = "Mombasa";
	    	String countryCode = "IND";//String countryCode = "ken";
	    	
	    	//System.out.println("tokenlist: <<"+tokenList.toString()+">>\n entity list <<"+entityList.toString()+">>\n\n");
	    	
	    	
	    	
	    	String [] keywords = {"climate","temperature","rain","rainy","wind","windy","humid","humidity","forecast","weather","hot day","sunny","cold","monsoon","hot","storm","thunderstorm","thunder","cloud","cloudy","clouds","snow","snowy","snowing","fog","foggy","precipitation"};
	    	String [] timeOrLocKeyWords = {"today","outside","right now","evening","morning","night","afternoon","noon","days","tomorrow","day after tomorrow","this week","upto this week","until this week's end","next week"};
	    	String [] words = searchQuery.split(" +");
	    	
	    	boolean validKeyword=false;
	    	boolean verb=false;
	    	boolean validTense=false;
	    	boolean validTimeOrLoc=false;
	    	
	    	if(searchQuery!=null && searchQuery.toLowerCase().contains("why") && searchQuery.toLowerCase().contains("where"))
	    	{
	    		System.out.println("[Weather api]contains 'why' or 'where' token advance to next api");
	    		return ret;
	    	}
	    	
	    	int y=0;
	    	for(; y< keywords.length ; y++)
	    	{
	    		if(searchQuery.toLowerCase().contains(keywords[y]+" ") || searchQuery.toLowerCase().contains(" "+keywords[y]) || searchQuery.toLowerCase().contains(keywords[y]))
	    			{
	    				System.out.println("[Weather api]valid keyword: "+keywords[y]+" FOUND");
	    				validKeyword=true;
	    				break;
	    			}
	    	}//end of for
	    	System.out.println("validKeyword: ["+validKeyword+"]");
	    	if(!validKeyword)
	    		{
	    			System.out.println("[Weather api]No Valid keywords found advance to next api");
	    			return ret;
	    		}
	    	int j=0;
	    	for(; j<timeOrLocKeyWords.length ; j++)
	    	{
	    		if(searchQuery!=null && (searchQuery.toLowerCase().contains(timeOrLocKeyWords[j]+" ")|| searchQuery.toLowerCase().contains(" "+timeOrLocKeyWords[j])||searchQuery.toLowerCase().contains(timeOrLocKeyWords[j])))
	    			{
	    				validTimeOrLoc=true;
	    				break;
	    			}
	    	}//end of for
	    	if(!validTimeOrLoc)
	    	{
	    		//TODO check for location should be either city/Country 
	    		for(Entity entity: entityList)
	    		{
	    			com.google.cloud.language.v1.Entity.Type type=entity.getType();
	    			if(type!=null && type.toString().equalsIgnoreCase("location"))
	    			{
	    				String typee=type.toString();
	    				validTimeOrLoc=true;
	    				break;
	    			}
	    		}
	    	}
	    	System.out.println("validTimeOrLoc: ["+validTimeOrLoc+"]");
	    	if(!validTimeOrLoc)
	    	{
	    		System.out.println("[Weather api]No Valid timeORLoc KEYWORDS found advance to next api");
	    		//TODO now see if there is any determiner in searchquery
	    		
	    		/*boolean detpresent=false;
	    		for(Token token: tokenList)
		    	{
		    		PartOfSpeech pos= token.getPartOfSpeech();
		    		
		    		String tag=pos.getTag().toString();
		    		System.out.print(" tag: "+tag);
		    		if(tag!=null && !tag.equals("") && tag.equalsIgnoreCase("det"))
		    		{
		    			detpresent =true;
		    			break;
		    		}
		    		
		    	}
	    		System.out.println("detpresent: "+detpresent);
	    		if(!detpresent)
	    		{
	    			System.out.println("[Weather api]No determiner found and noTimeOrLoc based Keywords also there advance to next api");
	    			return false;
	    		}*/
	    		int numberOfTokens=searchQuery.split(" +").length;
	    		System.out.println("number of tokens in query: "+numberOfTokens);
	    		if(searchQuery!=null && (searchQuery.toLowerCase().contains("is it")||searchQuery.toLowerCase().contains("it is")||searchQuery.toLowerCase().contains("is ")||searchQuery.toLowerCase().contains(" is")||searchQuery.toLowerCase().contains("will it")||searchQuery.toLowerCase().contains("it will")||numberOfTokens==2||numberOfTokens==3))
	    		{
	    			System.out.println("[Weather api]contains [is it/it is/is/will it/it will/length(string)==2/length(string)==3]:");
	    			validTimeOrLoc=true;
	    		}
	    		else
	    		{
	    			System.out.println("[Weather api]does NOT contains (is it/it is/is) advance to next api");
	    			return ret;
	    		}
	    		System.out.println("validTimeOrLoc afterwards (is it/it is/is) check: ["+validTimeOrLoc+"]");
	    	}
	    	
	    	
	    	for(Token token: tokenList)
	    	{
	    		PartOfSpeech pos= token.getPartOfSpeech();
	    		
	    		String tense=pos.getTense().toString();
	    		if(pos.getTag()!=null && pos.getTag().toString().equalsIgnoreCase("verb"))
	    		{
	    			verb=true;
	    			System.out.println("[Weather api]verb found tense: "+tense);
	    			if(tense!=null && tense.length()>0 && !tense.equalsIgnoreCase("PAST") )//if(tense!=null && tense.length()>0 && (tense.equalsIgnoreCase("FUTURE") || tense.equalsIgnoreCase("PRESENT")) )
		    		{
		    			validTense = true;
		    			break;
		    		}
	    		}
	    		
	    	}
	    	
	    	if(verb && !validTense)
	    	{
    			System.out.println("[Weather api]No Valid tense found advance to next api verb found: "+verb);
    			return ret;
    		}
	    	
	    	  URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q="+city+","+countryCode+"&units=metric&APPID="+key+"&cnt="+16);//URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q="+ city +","+ countryCode +"&units=metric&APPID="+key);
	    	  //a0e0686a1eca20c3b7d5399fef2e15cd
	  	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	  	      
	  	      BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	  	      
	  	      String output = null;
	  	      StringBuilder response = new StringBuilder();
	  	      
	  	      while((output=reader.readLine())!=null)
	  	      {
	  	    	  response.append(output);
	  	      }
	  	      
	  	      System.out.println("Response:\n"+response.toString());
	  	      
	  	      JSONObject weatherSearchRoot = new JSONObject(response.toString());
	  	      JSONArray list = (JSONArray) weatherSearchRoot.get("list");
	  	      JSONObject tempObj = null;
	  	      if(searchQuery!=null)
	  	      {
	  	    	  //TODO handle single day data 
		  	    	  Date date = new Date();
		  	    	  System.out.println("todays date: "+sdf.format(date));
		  	    	
	  	    	  	if(searchQuery.toLowerCase().contains("today") || searchQuery.toLowerCase().contains("outside") || searchQuery.toLowerCase().contains("tomorrow")|| searchQuery.toLowerCase().contains("day after tomorrow") ||searchQuery.toLowerCase().contains("right now")||searchQuery.toLowerCase().contains("evening")||searchQuery.toLowerCase().contains("morning")||searchQuery.toLowerCase().contains("night")||searchQuery.toLowerCase().contains("afternoon")||searchQuery.toLowerCase().contains("noon"))
	  	    	  	{
	  	    	  		if(searchQuery.toLowerCase().contains("today") || searchQuery.toLowerCase().contains("outside") || searchQuery.toLowerCase().contains("right now") || searchQuery.toLowerCase().contains("evening") || searchQuery.toLowerCase().contains("morning") || searchQuery.toLowerCase().contains("night") || searchQuery.toLowerCase().contains("afternoon") || searchQuery.toLowerCase().contains("noon"))
  	    	  			{
  	    	  				tempObj = (JSONObject) list.get(0);	  	    	  					  	
  	    	  			}
	  	    	  		else if(searchQuery.toLowerCase().contains("tomorrow"))
	  	    	  		{
	  	    	  			tempObj = (JSONObject) list.get(1);
	  	    	  			date = addDays(date, 1); 
	  	    	  		}
	  	    	  		else if(searchQuery.toLowerCase().contains("day after tomorrow"))
	  	    	  		{
	  	    	  			tempObj = (JSONObject) list.get(2);
  	    	  				date = addDays(date, 2); 
	  	    	  		}
	  	    	  		
	  	    	  		//process query
	  	    	  		
	  	    	  		if(searchQuery.toLowerCase().contains("temperature ")||searchQuery.toLowerCase().contains(" temperature")||searchQuery.toLowerCase().contains("temperature"))
  	    	  			{
	  	    	  			ret = "the temperature on: "+sdf.format(date)+" is: "+tempObj.getJSONObject("temp").getInt("day")+" degree celcius";
  	    	  				System.out.println("the temperature on: "+sdf.format(date)+" is: "+tempObj.getJSONObject("temp").getInt("day")+" degree celcius");
  	    	  				//return true;
  	    	  			}
	  	    	  		else if(searchQuery.toLowerCase().contains("wind ")||searchQuery.toLowerCase().contains(" wind")||searchQuery.toLowerCase().contains("windy ")||searchQuery.toLowerCase().contains(" windy")||searchQuery.toLowerCase().contains("wind")||searchQuery.toLowerCase().contains("windy"))
	  	    	  		{
	  	    	  			Double winSpeed =(Double) tempObj.get("speed");
	  	    	  			System.out.println("winSpeed: "+winSpeed);
	  	    	  			if(winSpeed>=2.777 && winSpeed<=13.889){
	  	    	  				ret = "its very windy outside on: "+sdf.format(date);
	  	    	  				System.out.println("its very windy outside on: "+sdf.format(date));
	  	    	  			}else if(winSpeed<2.777){
	  	    	  				ret = "its not windy on: "+sdf.format(date);
	  	    	  				System.out.println("its not windy on: "+sdf.format(date));
	  	    	  			}
	  	    	  			//return true;
	  	    	  		}
	  	    	  		else if(searchQuery.toLowerCase().contains("humidity ")||searchQuery.toLowerCase().contains(" humidity")||searchQuery.toLowerCase().contains("humid ")||searchQuery.toLowerCase().contains(" humid")||searchQuery.toLowerCase().contains("humid")||searchQuery.toLowerCase().contains("humidity"))
	  	    	  		{
	  	    	  			Double humidityPerc = Double.parseDouble((String) tempObj.get("humidity").toString());
	  	    	  			System.out.println("humity: "+humidityPerc);
	  	    	  			if(humidityPerc>50){
	  	    	  				ret = "its very humid outside on: "+sdf.format(date);
	  	    	  				System.out.println("its very humid outside on: "+sdf.format(date));
	  	    	  			}else if(humidityPerc>=20 && humidityPerc<=50){
	  	    	  				ret = "its average humidity outisde on: "+sdf.format(date);
	  	    	  				System.out.println("its average humidity outisde on: "+sdf.format(date));
	  	    	  			}else{
	  	    	  				ret = "its very less or no humid outside on: "+sdf.format(date);
	  	    	  				System.out.println("its very less or no humid outside on: "+sdf.format(date));
	  	    	  			}
	  	    	  			//return true;
	  	    	  		}
	  	    	  		else if(searchQuery.toLowerCase().contains("rain ") || searchQuery.toLowerCase().contains(" rain") || searchQuery.toLowerCase().contains(" rainy") || searchQuery.toLowerCase().contains("rainy ")||searchQuery.toLowerCase().contains("raining ")||searchQuery.toLowerCase().contains(" raining") || searchQuery.toLowerCase().contains("weather ") || searchQuery.toLowerCase().contains(" weather") || searchQuery.toLowerCase().contains(" sunny") || searchQuery.toLowerCase().contains("sunny ") || searchQuery.toLowerCase().contains("cold ") || searchQuery.toLowerCase().contains(" cold") || searchQuery.toLowerCase().contains(" monsoon") || searchQuery.toLowerCase().contains("monsoon ") || searchQuery.toLowerCase().contains("climate ") || searchQuery.toLowerCase().contains(" climate")||searchQuery.toLowerCase().contains("forecast ")||searchQuery.toLowerCase().contains(" forecast")||searchQuery.toLowerCase().contains("hot day ")||searchQuery.toLowerCase().contains(" hot day")||searchQuery.toLowerCase().contains("hot ")||searchQuery.toLowerCase().contains(" hot")||searchQuery.toLowerCase().contains("fog ")||searchQuery.toLowerCase().contains(" fog")||searchQuery.toLowerCase().contains("foggy ")||searchQuery.toLowerCase().contains(" foggy")||searchQuery.toLowerCase().contains("storm ")||searchQuery.toLowerCase().contains(" storm")||searchQuery.toLowerCase().contains("thunderstorm ")||searchQuery.toLowerCase().contains(" thunderstorm")||searchQuery.toLowerCase().contains("thunder ")||searchQuery.toLowerCase().contains(" thunder")||searchQuery.toLowerCase().contains("cloud ")||searchQuery.toLowerCase().contains(" cloud")||searchQuery.toLowerCase().contains("clouds ")||searchQuery.toLowerCase().contains(" clouds")||searchQuery.toLowerCase().contains("cloudy ")||searchQuery.toLowerCase().contains(" cloudy")||searchQuery.toLowerCase().contains("snow ")||searchQuery.toLowerCase().contains(" snow")||searchQuery.toLowerCase().contains("snowy ")||searchQuery.toLowerCase().contains(" snowy")||searchQuery.toLowerCase().contains("snowing ")||searchQuery.toLowerCase().contains(" snowing")||searchQuery.toLowerCase().contains("precipitation ")||searchQuery.toLowerCase().contains(" precipitation"))
	  	    	  		{
	  	    	  			ret = tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(date);
	  	    	  			System.out.println(tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(date));
	  	    	  			//return true;
	  	    	  		}
	  	    	  	}
	  	    	  	else if(searchQuery.toLowerCase().contains("this week") || searchQuery.toLowerCase().contains("upto this week") ||searchQuery.toLowerCase().contains("until this week's end"))
	  	    	  	{
	  	    	  		int numberOfdaysInThisWeek=thisWeek(date);
	  	    	  		System.out.println("numberOfDaysInThisWeek: ["+numberOfdaysInThisWeek+"] list size: "+list.length());
	  	    	  		for(int z=0; z<numberOfdaysInThisWeek ; z++)
	  	    	  		{
	  	    	  			System.out.println("inside this week loop for index: "+z);
	  	    	  			tempObj = (JSONObject) list.get(z);
	  	    	  				if(searchQuery.toLowerCase().contains("temperature ")||searchQuery.toLowerCase().contains(" temperature")||searchQuery.toLowerCase().contains("temperature"))
		  	    	  			{
	  	    	  					ret = "the temperature on"+sdf.format(addDays(date, z))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius";
		  	    	  				System.out.println("the temperature on"+sdf.format(addDays(date, z))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius");
		  	    	  				//return true;
		  	    	  			}
	  	    	  				else if(searchQuery.toLowerCase().contains("wind ")||searchQuery.toLowerCase().contains(" wind")||searchQuery.toLowerCase().contains("windy ")||searchQuery.toLowerCase().contains(" windy")||searchQuery.toLowerCase().contains("wind")||searchQuery.toLowerCase().contains("windy"))
			  	    	  		{
	  	    	  					Double winSpeed = Double.parseDouble((String) tempObj.get("speed").toString());
			  	    	  			System.out.println("winSpeed: "+winSpeed);
			  	    	  			if(winSpeed>=2.777 && winSpeed<=13.889){
			  	    	  				ret = "its very windy outside on: "+sdf.format(addDays(date, z));
			  	    	  				System.out.println("its very windy outside on: "+sdf.format(addDays(date, z)));
			  	    	  			}else if(winSpeed<2.777){
			  	    	  				ret = "its not windy on: "+sdf.format(addDays(date, z));
			  	    	  				System.out.println("its not windy on: "+sdf.format(addDays(date, z)));
			  	    	  			}
			  	    	  			//return true;
			  	    	  		}
	  	    	  				else if(searchQuery.toLowerCase().contains("humidity ")||searchQuery.toLowerCase().contains(" humidity")||searchQuery.toLowerCase().contains("humid ")||searchQuery.toLowerCase().contains(" humid")||searchQuery.toLowerCase().contains("humid")||searchQuery.toLowerCase().contains("humidity"))
			  	    	  		{
	  	    	  					Double humidityPerc = Double.parseDouble((String) tempObj.get("humidity").toString());
			  	    	  			System.out.println("humity: "+humidityPerc);
			  	    	  			if(humidityPerc>50){
			  	    	  				ret = "its very humid outside on: "+sdf.format(addDays(date, z));
			  	    	  				System.out.println("its very humid outside on: "+sdf.format(addDays(date, z)));
			  	    	  			}else if(humidityPerc>=20 && humidityPerc<=50){
			  	    	  				ret = "its average humidity outisde on: "+sdf.format(addDays(date, z));
			  	    	  				System.out.println("its average humidity outisde on: "+sdf.format(addDays(date, z)));
			  	    	  			}else{
			  	    	  				ret = "its very less or no humid outside on: "+sdf.format(addDays(date, z));
			  	    	  				System.out.println("its very less or no humid outside on: "+sdf.format(addDays(date, z)));
			  	    	  			}
			  	    	  			//return true;
			  	    	  		}
	  	    	  				else if(searchQuery.toLowerCase().contains("rain ") || searchQuery.toLowerCase().contains(" rain") || searchQuery.toLowerCase().contains(" rainy") || searchQuery.toLowerCase().contains("rainy ")||searchQuery.toLowerCase().contains("raining ")||searchQuery.toLowerCase().contains(" raining") || searchQuery.toLowerCase().contains("weather ") || searchQuery.toLowerCase().contains(" weather") || searchQuery.toLowerCase().contains(" sunny") || searchQuery.toLowerCase().contains("sunny ") || searchQuery.toLowerCase().contains("cold ") || searchQuery.toLowerCase().contains(" cold") || searchQuery.toLowerCase().contains(" monsoon") || searchQuery.toLowerCase().contains("monsoon ") || searchQuery.toLowerCase().contains("climate ") || searchQuery.toLowerCase().contains(" climate")||searchQuery.toLowerCase().contains("forecast ")||searchQuery.toLowerCase().contains(" forecast")||searchQuery.toLowerCase().contains("hot day ")||searchQuery.toLowerCase().contains(" hot day")||searchQuery.toLowerCase().contains("hot ")||searchQuery.toLowerCase().contains(" hot")||searchQuery.toLowerCase().contains("fog ")||searchQuery.toLowerCase().contains(" fog")||searchQuery.toLowerCase().contains("foggy ")||searchQuery.toLowerCase().contains(" foggy")||searchQuery.toLowerCase().contains("storm ")||searchQuery.toLowerCase().contains(" storm")||searchQuery.toLowerCase().contains("thunderstorm ")||searchQuery.toLowerCase().contains(" thunderstorm")||searchQuery.toLowerCase().contains("thunder ")||searchQuery.toLowerCase().contains(" thunder")||searchQuery.toLowerCase().contains("cloud ")||searchQuery.toLowerCase().contains(" cloud")||searchQuery.toLowerCase().contains("clouds ")||searchQuery.toLowerCase().contains(" clouds")||searchQuery.toLowerCase().contains("cloudy ")||searchQuery.toLowerCase().contains(" cloudy")||searchQuery.toLowerCase().contains("snow ")||searchQuery.toLowerCase().contains(" snow")||searchQuery.toLowerCase().contains("snowy ")||searchQuery.toLowerCase().contains(" snowy")||searchQuery.toLowerCase().contains("snowing ")||searchQuery.toLowerCase().contains(" snowing")||searchQuery.toLowerCase().contains("precipitation ")||searchQuery.toLowerCase().contains(" precipitation"))
			  	    	  		{
			  	    	  			ret = tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(date, z));
			  	    	  			System.out.println(tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(date, z)));
			  	    	  			//return true;
			  	    	  		}
			  	    	  		if(z==numberOfdaysInThisWeek-1)
			  	    	  			return ret;
	  	    	  		}//end of for
	  	    	  	}//end of 'this week'
	  	    	  	else if(searchQuery.toLowerCase().contains("next week"))
	  	    	  	{
	  	    	  				//get days using thisWeek() and run the announcement from index 
	  	    	  				//of today(i.e 0) to < number of days returned by func	  	    	  			
	  	    	  			int numberOfResultDays=thisWeek(date);// the offset to start from in response of 16 days weather API
	  	    	  			int monthDays = getDaysInMonth(date);
	  	    	  			String firstDayNextWeek = nextWeek(date);
	  	    	  			//Date LastDayOfNextWeek = addDays(date, days);
	  	    	  			int valFirstDayNextWeek=Integer.parseInt(firstDayNextWeek.split("/")[0]);
	  	    	  			int upperLimit =  -1;
	  	    	  			if(valFirstDayNextWeek<28)
	  	    	  				upperLimit=numberOfResultDays+7;
	  	    	  			else
	  	    	  				upperLimit=numberOfResultDays+monthDays-28;
	  	    	  			System.out.println("daysInThisWeek: ["+numberOfResultDays+"] monthDays: ["+monthDays+"] firstDayNextWeek: ["+firstDayNextWeek+"] valFirstDayNextWeek: ["+valFirstDayNextWeek+"] upperLimit: ["+upperLimit+"]");
	  	    	  		for(int z=numberOfResultDays ; z<upperLimit ; z++)
	  	    	  		{
	  	    	  			System.out.println("inside next week loop for index: "+z);
	  	    	  			tempObj = (JSONObject) list.get(z);
	  	    	  				if(searchQuery.toLowerCase().contains("temperature ")||searchQuery.toLowerCase().contains(" temperature")||searchQuery.toLowerCase().contains("temperature"))
		  	    	  			{
	  	    	  					ret = "the temperature on"+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius";
		  	    	  				System.out.println("the temperature on"+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius");
		  	    	  				//return true;
		  	    	  			}
	  	    	  				else if(searchQuery.toLowerCase().contains("wind ")||searchQuery.toLowerCase().contains(" wind")||searchQuery.toLowerCase().contains("windy ")||searchQuery.toLowerCase().contains(" windy")||searchQuery.toLowerCase().contains("wind")||searchQuery.toLowerCase().contains("windy"))
			  	    	  		{
				  	    	  		Double winSpeed =(Double) tempObj.get("speed");
			  	    	  			System.out.println("winSpeed: "+winSpeed);
			  	    	  			if(winSpeed>=2.777 && winSpeed<=13.889){
			  	    	  				ret = "its very windy outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays));
			  	    	  				System.out.println("its very windy outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays)));
			  	    	  			}else if(winSpeed<2.777){
			  	    	  				ret = "its not windy on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays));
			  	    	  				System.out.println("its not windy on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays)));
			  	    	  			}
			  	    	  			//return true;
			  	    	  		}
	  	    	  				else if(searchQuery.toLowerCase().contains("humidity ")||searchQuery.toLowerCase().contains(" humidity")||searchQuery.toLowerCase().contains("humid ")||searchQuery.toLowerCase().contains(" humid")||searchQuery.toLowerCase().contains("humid")||searchQuery.toLowerCase().contains("humidity"))
			  	    	  		{
	  	    	  					Double humidityPerc = Double.parseDouble((String) tempObj.get("humidity").toString());
			  	    	  			System.out.println("humity: "+humidityPerc);
			  	    	  			if(humidityPerc>50){
			  	    	  				ret = "its very humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays));
			  	    	  				System.out.println("its very humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays)));
			  	    	  			}else if(humidityPerc>=20 && humidityPerc<=50){
			  	    	  				ret = "its average humidity outisde on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays));
			  	    	  				System.out.println("its average humidity outisde on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays)));
			  	    	  			}else{
			  	    	  				ret = "its very less or no humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays));
			  	    	  				System.out.println("its very less or no humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays)));
			  	    	  			}
			  	    	  			//return true;
			  	    	  		}
	  	    	  				else if(searchQuery.toLowerCase().contains("rain ") || searchQuery.toLowerCase().contains(" rain") || searchQuery.toLowerCase().contains(" rainy") || searchQuery.toLowerCase().contains("rainy ")||searchQuery.toLowerCase().contains("raining ")||searchQuery.toLowerCase().contains(" raining") || searchQuery.toLowerCase().contains("weather ") || searchQuery.toLowerCase().contains(" weather") || searchQuery.toLowerCase().contains(" sunny") || searchQuery.toLowerCase().contains("sunny ") || searchQuery.toLowerCase().contains("cold ") || searchQuery.toLowerCase().contains(" cold") || searchQuery.toLowerCase().contains(" monsoon") || searchQuery.toLowerCase().contains("monsoon ") || searchQuery.toLowerCase().contains("climate ") || searchQuery.toLowerCase().contains(" climate")||searchQuery.toLowerCase().contains("forecast ")||searchQuery.toLowerCase().contains(" forecast")||searchQuery.toLowerCase().contains("hot day ")||searchQuery.toLowerCase().contains(" hot day")||searchQuery.toLowerCase().contains("hot ")||searchQuery.toLowerCase().contains(" hot")||searchQuery.toLowerCase().contains("fog ")||searchQuery.toLowerCase().contains(" fog")||searchQuery.toLowerCase().contains("foggy ")||searchQuery.toLowerCase().contains(" foggy")||searchQuery.toLowerCase().contains("storm ")||searchQuery.toLowerCase().contains(" storm")||searchQuery.toLowerCase().contains("thunderstorm ")||searchQuery.toLowerCase().contains(" thunderstorm")||searchQuery.toLowerCase().contains("thunder ")||searchQuery.toLowerCase().contains(" thunder")||searchQuery.toLowerCase().contains("cloud ")||searchQuery.toLowerCase().contains(" cloud")||searchQuery.toLowerCase().contains("clouds ")||searchQuery.toLowerCase().contains(" clouds")||searchQuery.toLowerCase().contains("cloudy ")||searchQuery.toLowerCase().contains(" cloudy")||searchQuery.toLowerCase().contains("snow ")||searchQuery.toLowerCase().contains(" snow")||searchQuery.toLowerCase().contains("snowy ")||searchQuery.toLowerCase().contains(" snowy")||searchQuery.toLowerCase().contains("snowing ")||searchQuery.toLowerCase().contains(" snowing")||searchQuery.toLowerCase().contains("precipitation ")||searchQuery.toLowerCase().contains(" precipitation"))
			  	    	  		{
			  	    	  			//TODO for all these cases provide weather info
	  	    	  					ret = tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays));
			  	    	  			System.out.println(tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfResultDays)));
			  	    	  			//return true;
			  	    	  		}
			  	    	  		if(z==(upperLimit-1))
			  	    	  			return ret;
	  	    	  		}//end of for
	  	    	  			
	  	    	  		
	  	    	  	}//end of 'next week'
	  	    	  	else if(searchQuery.toLowerCase().contains("next") && searchQuery.toLowerCase().contains("days"))
	  	    	  	{
	  	    	  		String afterNext=searchQuery.substring(searchQuery.indexOf("next")+4,searchQuery.length()).trim();
	  	    	  		int numberOfDays=Integer.parseInt(afterNext.split(" +")[0].trim());//will give us the value of X in 'X days'
	  	    	  		int numberOfdaysInThisWeek=thisWeek(date);
	  	    	  		System.out.println("numberOfDays: "+numberOfDays+" numberOfdaysInThisWeek: "+numberOfdaysInThisWeek);
			  	    	  	for(int z=0; z<numberOfdaysInThisWeek ; z++)
		  	    	  		{
			  	    	  		System.out.println("inside this 'next X days' loop for index: "+z);
		  	    	  			tempObj = (JSONObject) list.get(z);
		  	    	  				if(searchQuery.toLowerCase().contains("temperature ")||searchQuery.toLowerCase().contains(" temperature")||searchQuery.toLowerCase().contains("temperature"))
			  	    	  			{
		  	    	  					ret = "the temperature on"+sdf.format(addDays(date, z))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius";
			  	    	  				System.out.println("the temperature on"+sdf.format(addDays(date, z))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius");
			  	    	  				//return true;
			  	    	  			}
		  	    	  				else if(searchQuery.toLowerCase().contains("wind ")||searchQuery.toLowerCase().contains(" wind")||searchQuery.toLowerCase().contains("windy ")||searchQuery.toLowerCase().contains(" windy")||searchQuery.toLowerCase().contains("wind")||searchQuery.toLowerCase().contains("windy"))
				  	    	  		{
				  	    	  			Double winSpeed =(Double) tempObj.get("speed");
				  	    	  			System.out.println("winSpeed: "+winSpeed);
				  	    	  			if(winSpeed>=2.777 && winSpeed<=13.889){
				  	    	  				ret = "its very windy outside on: "+sdf.format(addDays(date, z));
				  	    	  				System.out.println("its very windy outside on: "+sdf.format(addDays(date, z)));
				  	    	  			}else if(winSpeed<2.777){
				  	    	  				ret = "its not windy on: "+sdf.format(addDays(date, z));
				  	    	  				System.out.println("its not windy on: "+sdf.format(addDays(date, z)));
				  	    	  			}
				  	    	  			//return true;
				  	    	  		}
		  	    	  				else if(searchQuery.toLowerCase().contains("humidity ")||searchQuery.toLowerCase().contains(" humidity")||searchQuery.toLowerCase().contains("humid ")||searchQuery.toLowerCase().contains(" humid")||searchQuery.toLowerCase().contains("humid")||searchQuery.toLowerCase().contains("humidity"))
				  	    	  		{
		  	    	  					Double humidityPerc = Double.parseDouble((String) tempObj.get("humidity").toString());
				  	    	  			System.out.println("humity: "+humidityPerc);
				  	    	  			if(humidityPerc>50){
				  	    	  				ret = "its very humid outside on: "+sdf.format(addDays(date, z));
				  	    	  				System.out.println("its very humid outside on: "+sdf.format(addDays(date, z)));
				  	    	  			}else if(humidityPerc>=20 && humidityPerc<=50){
				  	    	  				ret = "its average humidity outisde on: "+sdf.format(addDays(date, z));
				  	    	  				System.out.println("its average humidity outisde on: "+sdf.format(addDays(date, z)));
				  	    	  			}else{
				  	    	  				ret = "its very less or no humid outside on: "+sdf.format(addDays(date, z));
				  	    	  				System.out.println("its very less or no humid outside on: "+sdf.format(addDays(date, z)));
				  	    	  			}
				  	    	  			//return true;
				  	    	  		}
		  	    	  				else if(searchQuery.toLowerCase().contains("rain ") || searchQuery.toLowerCase().contains(" rain") || searchQuery.toLowerCase().contains(" rainy") || searchQuery.toLowerCase().contains("rainy ")||searchQuery.toLowerCase().contains("raining ")||searchQuery.toLowerCase().contains(" raining") || searchQuery.toLowerCase().contains("weather ") || searchQuery.toLowerCase().contains(" weather") || searchQuery.toLowerCase().contains(" sunny") || searchQuery.toLowerCase().contains("sunny ") || searchQuery.toLowerCase().contains("cold ") || searchQuery.toLowerCase().contains(" cold") || searchQuery.toLowerCase().contains(" monsoon") || searchQuery.toLowerCase().contains("monsoon ") || searchQuery.toLowerCase().contains("climate ") || searchQuery.toLowerCase().contains(" climate")||searchQuery.toLowerCase().contains("forecast ")||searchQuery.toLowerCase().contains(" forecast")||searchQuery.toLowerCase().contains("hot day ")||searchQuery.toLowerCase().contains(" hot day")||searchQuery.toLowerCase().contains("hot ")||searchQuery.toLowerCase().contains(" hot")||searchQuery.toLowerCase().contains("fog ")||searchQuery.toLowerCase().contains(" fog")||searchQuery.toLowerCase().contains("foggy ")||searchQuery.toLowerCase().contains(" foggy")||searchQuery.toLowerCase().contains("storm ")||searchQuery.toLowerCase().contains(" storm")||searchQuery.toLowerCase().contains("thunderstorm ")||searchQuery.toLowerCase().contains(" thunderstorm")||searchQuery.toLowerCase().contains("thunder ")||searchQuery.toLowerCase().contains(" thunder")||searchQuery.toLowerCase().contains("cloud ")||searchQuery.toLowerCase().contains(" cloud")||searchQuery.toLowerCase().contains("clouds ")||searchQuery.toLowerCase().contains(" clouds")||searchQuery.toLowerCase().contains("cloudy ")||searchQuery.toLowerCase().contains(" cloudy")||searchQuery.toLowerCase().contains("snow ")||searchQuery.toLowerCase().contains(" snow")||searchQuery.toLowerCase().contains("snowy ")||searchQuery.toLowerCase().contains(" snowy")||searchQuery.toLowerCase().contains("snowing ")||searchQuery.toLowerCase().contains(" snowing")||searchQuery.toLowerCase().contains("precipitation ")||searchQuery.toLowerCase().contains(" precipitation"))
				  	    	  		{
				  	    	  			ret = tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(date, z));
				  	    	  			System.out.println(tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(date, z)));
				  	    	  			//return true;
				  	    	  		}
				  	    	  		if((z==numberOfdaysInThisWeek-1) && (numberOfDays<=numberOfdaysInThisWeek) )
				  	    	  			return ret;
		  	    	  		}//end of for
	  	    	  		if( numberOfDays >numberOfdaysInThisWeek )
	  	    	  		{
		  	    	  		//means number of days queried by the user is > the this week's remaining days including today
			  	    	  		int monthDays = getDaysInMonth(date);
		  	    	  			String firstDayNextWeek = nextWeek(date);
		  	    	  			//Date LastDayOfNextWeek = addDays(date, days);
		  	    	  			int valFirstDayNextWeek=Integer.parseInt(firstDayNextWeek.split("/")[0]);
		  	    	  			int upperLimit =  -1;
		  	    	  			int min= -1;
		  	    	  			
		  	    	  			if(numberOfDays>16)
		  	    	  				numberOfDays=16;
		  	    	  			if(valFirstDayNextWeek<28)
		  	    	  				min= Math.min(7, numberOfDays-numberOfdaysInThisWeek);
		  	    	  			else
		  	    	  				min =Math.min(monthDays-28,numberOfDays-numberOfdaysInThisWeek);
		  	    	  				
		  	    	  			upperLimit=numberOfdaysInThisWeek+min;
		  	    	  			System.out.println("daysInThisWeek: ["+numberOfdaysInThisWeek+"] monthDays: ["+monthDays+"] firstDayNextWeek: ["+firstDayNextWeek+"] valFirstDayNextWeek: ["+valFirstDayNextWeek+"] upperLimit: ["+upperLimit+"] min: ["+min+"]");
			  	    	  		for(int z=numberOfdaysInThisWeek ; z<upperLimit ; z++)
			  	    	  		{
			  	    	  			System.out.println("inside 'next X days' loop for index: "+z);
			  	    	  			tempObj = (JSONObject) list.get(z);
			  	    	  				if(searchQuery.toLowerCase().contains("temperature ")||searchQuery.toLowerCase().contains(" temperature")||searchQuery.toLowerCase().contains("temperature"))
				  	    	  			{
			  	    	  					ret = "the temperature on"+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius";
				  	    	  				System.out.println("the temperature on"+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek))+" is: "+tempObj.getJSONObject("temp").getString("day")+" degree celcius");
				  	    	  				//return true;
				  	    	  			}
			  	    	  				else if(searchQuery.toLowerCase().contains("wind ")||searchQuery.toLowerCase().contains(" wind")||searchQuery.toLowerCase().contains("windy ")||searchQuery.toLowerCase().contains(" windy")||searchQuery.toLowerCase().contains("wind")||searchQuery.toLowerCase().contains("windy"))
					  	    	  		{
						  	    	  		Double winSpeed =(Double) tempObj.get("speed");
					  	    	  			System.out.println("winSpeed: "+winSpeed);
					  	    	  			if(winSpeed>=2.777 && winSpeed<=13.889){
					  	    	  				ret = "its very windy outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek));
					  	    	  				System.out.println("its very windy outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek)));
					  	    	  			}else if(winSpeed<2.777){
					  	    	  				ret = "its not windy on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek));
					  	    	  				System.out.println("its not windy on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek)));
					  	    	  			}
					  	    	  			//return true;
					  	    	  		}
			  	    	  				else if(searchQuery.toLowerCase().contains("humidity ")||searchQuery.toLowerCase().contains(" humidity")||searchQuery.toLowerCase().contains("humid ")||searchQuery.toLowerCase().contains(" humid")||searchQuery.toLowerCase().contains("humid")||searchQuery.toLowerCase().contains("humidity"))
					  	    	  		{
			  	    	  					Double humidityPerc = Double.parseDouble((String) tempObj.get("humidity").toString());
					  	    	  			System.out.println("humity: "+humidityPerc);
					  	    	  			if(humidityPerc>50){
					  	    	  				ret = "its very humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek));
					  	    	  				System.out.println("its very humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek)));
					  	    	  			}else if(humidityPerc>=20 && humidityPerc<=50){
					  	    	  				ret = "its average humidity outisde on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek));
					  	    	  				System.out.println("its average humidity outisde on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek)));
					  	    	  			}else{
					  	    	  				ret = "its very less or no humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek));
					  	    	  				System.out.println("its very less or no humid outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek)));
					  	    	  			}
					  	    	  			//return true;
					  	    	  		}
			  	    	  				else if(searchQuery.toLowerCase().contains("rain ") || searchQuery.toLowerCase().contains(" rain") || searchQuery.toLowerCase().contains(" rainy") || searchQuery.toLowerCase().contains("rainy ")||searchQuery.toLowerCase().contains("raining ")||searchQuery.toLowerCase().contains(" raining") || searchQuery.toLowerCase().contains("weather ") || searchQuery.toLowerCase().contains(" weather") || searchQuery.toLowerCase().contains(" sunny") || searchQuery.toLowerCase().contains("sunny ") || searchQuery.toLowerCase().contains("cold ") || searchQuery.toLowerCase().contains(" cold") || searchQuery.toLowerCase().contains(" monsoon") || searchQuery.toLowerCase().contains("monsoon ") || searchQuery.toLowerCase().contains("climate ") || searchQuery.toLowerCase().contains(" climate")||searchQuery.toLowerCase().contains("forecast ")||searchQuery.toLowerCase().contains(" forecast")||searchQuery.toLowerCase().contains("hot day ")||searchQuery.toLowerCase().contains(" hot day")||searchQuery.toLowerCase().contains("hot ")||searchQuery.toLowerCase().contains(" hot")||searchQuery.toLowerCase().contains("fog ")||searchQuery.toLowerCase().contains(" fog")||searchQuery.toLowerCase().contains("foggy ")||searchQuery.toLowerCase().contains(" foggy")||searchQuery.toLowerCase().contains("storm ")||searchQuery.toLowerCase().contains(" storm")||searchQuery.toLowerCase().contains("thunderstorm ")||searchQuery.toLowerCase().contains(" thunderstorm")||searchQuery.toLowerCase().contains("thunder ")||searchQuery.toLowerCase().contains(" thunder")||searchQuery.toLowerCase().contains("cloud ")||searchQuery.toLowerCase().contains(" cloud")||searchQuery.toLowerCase().contains("clouds ")||searchQuery.toLowerCase().contains(" clouds")||searchQuery.toLowerCase().contains("cloudy ")||searchQuery.toLowerCase().contains(" cloudy")||searchQuery.toLowerCase().contains("snow ")||searchQuery.toLowerCase().contains(" snow")||searchQuery.toLowerCase().contains("snowy ")||searchQuery.toLowerCase().contains(" snowy")||searchQuery.toLowerCase().contains("snowing ")||searchQuery.toLowerCase().contains(" snowing")||searchQuery.toLowerCase().contains("precipitation ")||searchQuery.toLowerCase().contains(" precipitation"))
					  	    	  		{
					  	    	  			ret = tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek));
					  	    	  			System.out.println(tempObj.getJSONArray("weather").getJSONObject(0).getString("description")+" outside on: "+sdf.format(addDays(sdf.parse(firstDayNextWeek), z-numberOfdaysInThisWeek)));
					  	    	  			//return true;
					  	    	  		}
					  	    	  		if(z==(upperLimit-1))
					  	    	  			return ret;
			  	    	  		}//end of for
	  	    	  		}//end of if(numberOfDays<=numberOfdaysInThisWeek)
	  	    	  	}//end of else if(next X days)
	  	      }
	  	      /*for(int x=0; list!=null && x< list.length() ;x++)
	  	      {
	  	    	  //we will get temp and weather of each day from today 
	  	    	  tempObj = (JSONObject) list.get(x);
	  	    	  String pressure = (String) tempObj.get("pressure");
	  	    	  String humidity = (String) tempObj.get("humidity");
	  	    	  String winSpeed = (String) tempObj.get("speed");
	  	    	  String temp= (String) tempObj.getJSONObject("temp").get("day");
	  	    	  if()
	  	      }*/
	    	
		      //return true;
		      
		    } catch (Exception ex) {
		    	ex.printStackTrace();
		    	//return false;
		    }
	    return ret;
	}
	
	public static Date addDays(Date date, int days)
    {
		try 
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, days); //minus number would decrement the days
			return cal.getTime();			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return date;
		}
    }//end of addDays()
	/*public static void abc(String[] args) {
		try {
			
			//System.out.println(thisWeek(sdf.parse("29/03/2017")));
			int offset=thisWeek(new Date());
			String firstDayNextWeek=nextWeek(new Date());
			String lastDayOfNextWeek=sdf.format(addDays(sdf.parse("29/04/2017"), 7));
			System.out.println("["+firstDayNextWeek+"] ["+lastDayOfNextWeek+"]");
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/
	public static int thisWeek(Date date){
		//returns how many days from today we need to make weather announcements
		try 
		{
			int day=Integer.parseInt(sdf.format(date).split("/")[0]);
			int z=0;
			for(;z<daymultiples.length;z++)
			{
				if(day<=daymultiples[z])
					break;
			}
			if(z!=daymultiples.length)//means day's value is upto max 28
			{
				return ((daymultiples[z]-day)+1);
			}
			else{
				//System.out.println("days in month["+getDaysInMonth(date)+"]");
				int monthdays=getDaysInMonth(date);
				return ((monthdays-day)+1);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return 0;
	}
	public static String nextWeek(Date date){
		//returns the starting date of next week
		try 
		{
			int startOffset=thisWeek(date);
			date= addDays(date, startOffset);
			return sdf.format(date);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	public static int getDaysInMonth(Date date){
		try {
			if(Integer.parseInt(sdf.format(date).split("/")[1])!=2)
				return days[Integer.parseInt(sdf.format(date).split("/")[1])-1];
			else
			{
				if(leapYearCheck(Integer.parseInt(sdf.format(date).split("/")[2])))
					return 29;
				else
					return 28;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	public static boolean leapYearCheck(int year){
		try {
			if(year%400==0)
				return true;
			if(year%100==0)
				return false;
			if(year%4==0)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return false;
	}
}
