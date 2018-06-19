package com.smart.search.wiki;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;

import info.bliki.api.Page;
import info.bliki.api.User;
import info.bliki.wiki.model.WikiModel;

public class WikiSearch {

	private String mediaAPIURL;
	private String lgname;
	private String lgpassword;
	private List<String[]> patternToRemove;

	public WikiSearch() {
		this.mediaAPIURL = "https://en.wikipedia.org/w/api.php";
		this.lgname="";
		this.lgpassword="";
		this.patternToRemove = new ArrayList<String[]>();
		String[] p1 = {"{{","}}"};
		String[] p2 = {"[","]"};
		this.patternToRemove.add(p1);
		this.patternToRemove.add(p2);
	}

	public WikiSearch(String mediaAPIURL, String lgname, String lgpassword) {
		this.mediaAPIURL = mediaAPIURL;
		this.lgname = lgname;
		this.lgpassword = lgpassword;
	}

	public void setPatternToRemove(List<String[]> patternToRemove) {
		this.patternToRemove=patternToRemove;
	}

	public String search(String searchURL, String snippet) throws IOException {
		String sResult = wikiParagraph(searchURL).trim();
		
		if(sResult.startsWith("REDIRECT")) {
			String searchString = sResult.substring("REDIRECT".length()).trim();
			sResult = new WikiSearch().search(searchString, snippet);
		}
		return sResult;
	}

	private String trimResult(String finalResult) {
		if(finalResult.contains("PageID:") && finalResult.contains("Content:")) finalResult = finalResult.substring(finalResult.indexOf("Content:")+"Content:".length());

		String sTrimResult = finalResult;
		//		System.out.println("finalResult: "+finalResult);
		for(String p[]:patternToRemove) 
			sTrimResult = trimResult1(sTrimResult,p[0],p[1]).trim();
		//		System.out.println("sTrimResult: "+sTrimResult);
		return sTrimResult;
	}

	private String trimResult1(String finalResult,String f1,String f2) {

		if(finalResult.contains(f1) && finalResult.contains(f2) && finalResult.indexOf(f1)<finalResult.indexOf(f2)) {
			String sResult = finalResult.substring(0,finalResult.indexOf(f1)).concat(finalResult.substring(finalResult.indexOf(f2)+f2.length()));
			return trimResult1(sResult,f1,f2);
		}
		else return finalResult;
	}

	private String wikiParagraph(String sWikiURL) {

		String sSearchTexts[] = sWikiURL.split("/");
		String sSearchText = sSearchTexts[sSearchTexts.length-1];
		//		System.out.println("SearchText: "+sSearchText);
		String sWikiData = fetchDataFromWiki(sSearchText.split("/"));
		String sFinalData[] = sWikiData.split("</p>");
		String sResult[] = {"",""};

		//		System.out.println("WikiData: "+sWikiData);

		if(sFinalData.length>0) {
			sResult[0] = trimResult(Jsoup.parse(sFinalData[0]).text()).trim();
			//			System.out.println("sFinalData[0]: "+sFinalData[0]);
			//			System.out.println("sResult[0]: "+sResult[0]);
		}
		if(sFinalData.length>1)
			sResult[1] = trimResult(Jsoup.parse(sFinalData[1]).text()).trim();
		//		System.out.println("sResult: "+(sResult[0].length()>1?sResult[0]:sResult[1]));
		return sResult[0].length()>1?sResult[0]:sResult[1];
	}

	private String fetchDataFromWiki(String[] sSearch) {
		User user = new User(this.lgname, this.lgpassword, this.mediaAPIURL);
		user.login();
		//		System.out.println("logged in");
		List<Page> listOfPages = user.queryContent(sSearch);
		//		System.out.println("listed "+listOfPages.size());
		if(listOfPages!=null) {
			for (Page page : listOfPages) {
				//			System.out.println("page: "+page);
				WikiModel wikiModel = new WikiModel("${image}", "${title}");
				String html = wikiModel.render(page.toString());
				if(html!=null && html.length()>10) return html;
			}
		}
		return "";
	}

}
