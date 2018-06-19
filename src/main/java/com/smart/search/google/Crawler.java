package com.smart.search.google;

import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Result{
	boolean status;
	String paraText;

	public Result(){
		status =false;
	}
}

public class Crawler {
	
	public static void main(String args[]) {
		String sResult = crawl("https://www.realsimple.com/health/mind-mood/dreams/facts-about-dreams", "is dream real");
		System.out.println(sResult);
	}
	
	//static ArrayList<String> al=new ArrayList<String>();
	   public static String crawl(String url,String snippetInJson)	//type is the selector 
	   {
		   String type="null";
		   
	       try 
	       {
//	    	    System.out.println("inside crawl type: "+type+"\nsnippet passed: "+snippetInJson);
	    	    Connection con=Jsoup.connect(url);
	    	    con.userAgent("*");
		   		Response resp=con.execute();
		   		Document doc=null;
		   		if(resp.statusCode()==200)
		   		{
		   			doc=resp.parse();
		   			//return doc.html();
		   		}
		   		else
		   		{
//		   			System.out.println("------ returning null from crawl -------");
		   			return null;
		   		}
	    	   if(type!=null && type.equals("html"))
	    		   {	
	    		   		return doc.html();	    		   		
	    		   }
	    	   else if(type!=null)
	    	   {
	    		   
	    		   String rules[]=new String[12];
	    		   
	    		   rules[0] = "blockquote";
	    		   rules[1] = "q";
	    		   rules[2] = "p+strong";
	    		   rules[3] = "p > em";
	    		   rules[4]	= "p";
	    		   rules[5] = "p+ol";
	    		   rules[6] = "p+ul";
	    		   rules[7] = "p:has(b)";
	    		   rules[8] = "ol";
	    		   rules[9] = "ul";
	    		   rules[10] = "td ul";
	    		   rules[11] = "td ol";
	    				   
	    		   Result ans = new Result();	    		   
	    		   for(int i=0;i<rules.length;i++){
//	    			   System.out.println("rule: "+rules[i]);
	    			   
	    			   ans=applyRule(doc, snippetInJson, rules[i]);
	    			   if(ans.status){
	    				   break;
	    			   }
	    		   }
	    		   
	    		   if(ans.status){
//	    			   System.out.println("\n<<<<< returning matched para >>>>>\n");
	    			   return ans.paraText;
	    		   }
	    		   else{
//	    			   System.out.println("------ returning null from crawl 1-------");
	    			   return null;
	    		   }
	    	   }
	       }
	       catch(Exception e)
	       {   
	    	   e.printStackTrace();
//	    	   System.out.println("------ returning null from crawl 2-------");
	    	   return null;
	       }
//	       System.out.println("------ returning null from crawl 3-------");
	       return null;
	   }
	   
	   public static int matchedWords(String paraText,String snippet){		//function to count matched words of snippet in paraText 
		   int count=0;
		   
		   if(snippet==""||snippet==null){
			   return 0;
		   }
		   
		   paraText=new String(paraText.toLowerCase());
		   snippet=new String(snippet.toLowerCase());
		   
		   String[] splitArray = snippet.split("\\s+");
		   
		   for(int i=0;i<splitArray.length;i++){
			   if(paraText.contains(splitArray[i])){
				   count++;
			   }
		   }
		   
		   return count;
	   }
	   
	   public static Result applyRule(Document doc, String snippet, String rule){		//function to apply rules
		   Result res=new Result();
		   
		   Elements elems=doc.select(rule);
		   
		   if(rule=="p"){
			   int maxCount=0,matchCount;
//			   System.out.println("\nparagraph found! with rule: "+rule);

			   for(Element ele:elems)
			   {
					String paraText=ele.text();
					matchCount=matchedWords(paraText, snippet);
					
					if(matchCount>maxCount){
						res.status=true;
						res.paraText=paraText;
						maxCount=matchCount;
					}					
			   }
			   
/*			   rule="div[itemprop=description]";
			   elems=doc.select(rule);

			   for(Element ele:elems)
			   {
					String paraText=ele.text();
					matchCount=matchedWords(paraText, snippet);
					
//					System.out.println("description match count: "+matchCount);
					
					if(matchCount>maxCount){
						res.status=true;
						res.paraText=paraText;
						maxCount=matchCount;
					}					
			   }*/
			   
			   return res;
		   }
		   else{
			for(Element ele:elems)
			{
				String paraText=ele.text();
				System.out.println("\nparagraph found! with rule: "+rule);
				
				int matchCount=matchedWords(paraText, snippet);
											
				if(paraText.length()>4){
					res.status=true;
					res.paraText=paraText;
					return res;
				}
			}//end of for loop
		   }
		   
		   return res;
	   }
}
