package com.smart.search.nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.PartOfSpeech;
import com.google.cloud.language.v1.Sentence;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Token;

public class InitialFilter {
	// static LevelOne levOne;
	// static ArrayList<LevelOne> alLevelOne;
	static int valid = 0;
	static int invalid = 0;
	static String one;
	static String two;
	static String configPath;

	public static void main(String... args) throws Exception {
		// Instantiates a client
		// levOne=new LevelOne();
		BufferedReader br = null;
		String line = "";
		br = new BufferedReader(new FileReader(new File("config.properties")));
		while ((line = br.readLine()) != null) {
			System.out.println(">>" + line);
			if (line.split("=")[0].equals("one"))
				one = line.split("one=")[1].trim();
			else if (line.split("=")[0].equals("two"))
				two = line.split("two=")[1].trim();
			else if (line.contains("path"))
				configPath = line.split("=")[1].trim();
		}
		System.out.println("<<<<< one: " + one + " two: " + two + " configpath: " + configPath);
		// alLevelOne = new ArrayList<LevelOne>();
		br = new BufferedReader(new FileReader(new File(configPath)));
		line = "";
		while ((line = br.readLine()) != null) {
			System.out.println("line being sent for processing: " + line);
			processString(line.substring(0, line.length() - 2).trim(),
					line.substring(line.length() - 1, line.length()));
		} // end of while
		System.out.println(" valid count: " + valid + " invalid count: " + invalid);

	}

	private static void processString(String text, String validInvalid) {
		String searchQuery=text;
		text=removeArticles(text);
		if(searchQuery.toLowerCase().contains("take me to "))
		{
	    	searchQuery = searchQuery.replace("take me to ", "nearby ");
	    	text = text.replace("take me to ", "nearby ");
	    	searchQuery.trim();
	    	text.trim();
		}
		try {
			
			
			//System.out.println(text);
			// validInValid will be 1|2
			//edit text - remove a, an , the
			LanguageServiceClient language = LanguageServiceClient.create();
			Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();

			// Detects the sentiment of the text
			// Sentiment sentiment =
			// language.analyzeSentiment(doc).getDocumentSentiment();
			//
			List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();

			// List<Sentence> sentenceList = language.analyzeSyntax(doc,
			// EncodingType.UTF8).getSentencesList();

			long dur = System.currentTimeMillis();
			List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
			dur = System.currentTimeMillis() - dur;
			dur = (dur / 1000);

			// System.out.println("no of tokens in query: "+tokenList.size()+"
			// tokenList \n<<"+tokenList.toString()+">>");
			for (Token t : tokenList) {

				PartOfSpeech pos = t.getPartOfSpeech();
				// String token=t.getText().getContent();
				String tag = pos.getTag().toString();// NOUN/ADP/DET/PRON/VERB
				String person = pos.getPerson().toString();
				String proper = pos.getProper().toString();
				String lemma = t.getLemma();
				String label = t.getDependencyEdge().getLabel().toString();
				// System.out.println("text ["+text+"] label: ["+label+"]");
				// alLevelOne.add(new LevelOne(tag, person, proper, lemma,
				// label));
			}
			// boolean rejectOne = filterOne(text, alLevelOne);
			boolean validateSearchString = validateSearchString(searchQuery, tokenList);

			// alLevelOne.clear();

			if (validateSearchString) {
				System.out.println(
						">> -map-API-- text : " + text + " result: " + validateSearchString + " duration: " + dur);
			} else {
				System.out.println(
						">> -Rejected--text : " + text + " result: " + validateSearchString + " duration: " + dur);
			}
			// if (validInvalid.equalsIgnoreCase("1"))
			// System.out.println(
			// ">> text : " + text + " result: " + rejectOne + " duration: " +
			// dur + " valid: " + NLPTest.one);
			// else if (validInvalid.equalsIgnoreCase("2"))
			// System.out.println(
			// ">> text : " + text + " result: " + rejectOne + " duration: " +
			// dur + " valid: " + NLPTest.two);
			//
			// if (rejectOne)
			// invalid++;
			// else
			// valid++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String removeArticles(String text)
	{
		text=text.trim();
		String temp=new String();
		ArrayList<String> articles=new ArrayList<String>();
		articles.add("a");
		articles.add("an");
		articles.add("the");
		String words[]=text.split(" ");
		for(String w :words)
		{
			if(!articles.contains(w))
			{
				temp=temp+w+" ";
			}
				
		}
		text=temp.trim();
		return text;
	}
	public static boolean validateSearchString(String searchQuery, List<Token> tokenlist) {
		ArrayList<String> Determiners = new ArrayList<String>();
		Determiners.add("these");
		Determiners.add("those");
		Determiners.add("that");
		Determiners.add("this");

		ArrayList<String> Pronouns = new ArrayList<String>();
		Pronouns.add("he");
		Pronouns.add("she");
		Pronouns.add("he");
		Pronouns.add("him");
		Pronouns.add("her");
		Pronouns.add("your");
		Pronouns.add("its");
		Pronouns.add("my");

		ArrayList<String> AcceptPhrases = new ArrayList<String>();
		AcceptPhrases.add("i want to");
		AcceptPhrases.add("i want");
		AcceptPhrases.add("i need to");
		AcceptPhrases.add("i need");
		AcceptPhrases.add("i would like to");
		AcceptPhrases.add("i am eager to");
		AcceptPhrases.add("i wish to");
		AcceptPhrases.add("i wish");
		AcceptPhrases.add("can I ask");
		AcceptPhrases.add("please");
		AcceptPhrases.add("tell me");

		ArrayList<String> RejectPhrases = new ArrayList<String>();
		RejectPhrases.add("don't");
		RejectPhrases.add("do not");
		RejectPhrases.add("didn't");
		RejectPhrases.add("did not");
		RejectPhrases.add("won't");

		ArrayList<String> GlobalLocationTerms = new ArrayList<String>();
		GlobalLocationTerms.add("country");
		GlobalLocationTerms.add("city");
		GlobalLocationTerms.add("district");
		GlobalLocationTerms.add("town");

		ArrayList<String> GlobalWeatherTerms = new ArrayList<String>();
		GlobalWeatherTerms.add("day");
		GlobalWeatherTerms.add("week");
		GlobalWeatherTerms.add("fortnight");
		GlobalWeatherTerms.add("month");
		GlobalWeatherTerms.add("year");

		boolean containsThis = false;
		boolean containsI = false;
		boolean containsMy = false;
		boolean containsVerb = false;
	
		// Level 1

		for (Token t : tokenlist) {

			String word = t.getText().getContent().toLowerCase();
			String tag = t.getPartOfSpeech().getTag().toString();
			searchQuery=searchQuery.toLowerCase();
			
			if (Determiners.contains(word) && tag.equals("DET")) 
			{

				if (word.contains("this")) 
				{
					containsThis = true;
				} 
				else
				{ /* as we have to reject the rest */
					return false;
				}

			} 

			if(tag.equals("VERB"))
			{
				containsVerb=true;
			}
			else if (tag.equals("PRON")) 
			{
				
				if (word.equals("my")) 
				{
					containsMy = true;
				} 
				else if(word.equals("i"))
				{
					containsI=true;
				}
				else if(Pronouns.contains(word) && searchQuery.startsWith(word))
				{
					return false; /* reject the rest */
				} 
			}

			if (containsThis == true) 
			{
				if (GlobalWeatherTerms.contains(word) || GlobalLocationTerms.contains(word))
				{
					containsThis = false;
					return true; // forward to map API
				}
			} 

			if (containsMy) 
			{
				if (GlobalLocationTerms.contains(word)) {
					containsMy = false;
					return true;
				} 
			} 
			
			if(containsI && containsVerb)
			{
				if(word.equals("not"))
					return false;
			}
		} 

		if (containsThis|| containsMy)
			return false;
		
		// Level 2
		for (String phrase : RejectPhrases) {
			if (searchQuery.contains(phrase)) {
				return false;
			} 
		} 

		for (String phrase : AcceptPhrases) {
			if (searchQuery.contains(phrase)) {
				/* we pass it */
				return true;
			} 
		} 

		return true;
	}

}
