package com.smart.search.nlp;

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
import com.smart.search.ms.NewsApi;

public class NLPTest {
  public static void main(String... args) throws Exception {
    // Instantiates a client
    LanguageServiceClient language = LanguageServiceClient.create();

    // The text to analyze
    String text = "take me to ladakh";
    Document doc = Document.newBuilder()
            .setContent(text).setType(Type.PLAIN_TEXT).build();

    // Detects the sentiment of the text
    Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
    List<Entity> entityList = language.analyzeEntities(doc, EncodingType.UTF8).getEntitiesList();
    
    List<Sentence> sentenceList = language.analyzeSyntax(doc, EncodingType.UTF8).getSentencesList();
    
    List<Token> tokenList = language.analyzeSyntax(doc, EncodingType.UTF8).getTokensList();
    

    System.out.println("Text: "+ text);
    
    //System.out.println("Sentiment: "+ sentiment.toString());
    
    
    
    for(Entity e : entityList){
    	System.out.println("Entity :>>> e: "+e.getName()+", Type: "+e.getType()+" <<<");
    }
    
//    for(Sentence s : sentenceList){
//    	System.out.println("Sentence : "+s.toString());
//    }
    
    for(Token t : tokenList){
    	PartOfSpeech pos = t.getPartOfSpeech();
    	t.getText().getContent();
    	
    	System.out.println("Token >>  : "+t.getText().getContent() +" " +t.getPartOfSpeech().getTag()+" <<");
    }
    
    
    NewsApi.callApiNews(text, entityList, tokenList);
    
  }
}
