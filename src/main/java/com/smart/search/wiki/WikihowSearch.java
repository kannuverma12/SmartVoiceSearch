package com.smart.search.wiki;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import info.bliki.api.Page;
import info.bliki.api.User;
import info.bliki.wiki.model.WikiModel;

public class WikihowSearch {

	private String mediaAPIURL;
	private String lgname;
	private String lgpassword;
	private List<String[]> patternToRemove;

	public WikihowSearch() {
		this.mediaAPIURL = "https://www.wikihow.com/api.php";
		this.lgname="";
		this.lgpassword="";
		this.patternToRemove = new ArrayList<String[]>();
		String[] p1 = {"{{","}}"};
		String[] p2 = {"[","]"};
		this.patternToRemove.add(p1);
		this.patternToRemove.add(p2);
	}

	public WikihowSearch(String mediaAPIURL, String lgname, String lgpassword) {
		this.mediaAPIURL = mediaAPIURL;
		this.lgname = lgname;
		this.lgpassword = lgpassword;
	}

	public void setPatternToRemove(List<String[]> patternToRemove) {
		this.patternToRemove=patternToRemove;
	}

	public String search(String searchURL, String snippet) throws IOException {

		return wikiParagraph(searchURL).trim();
	}

	private String trimResult(String finalResult) {
		if(finalResult.contains("PageID:") && finalResult.contains("Content:")) finalResult = finalResult.substring(finalResult.indexOf("Content:")+"Content:".length());

		String sTrimResult = finalResult;
		for(String p[]:patternToRemove) 
			sTrimResult = trimResult1(finalResult,p[0],p[1]).trim();

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

		Document jDoc = Jsoup.parse(sWikiData);
		Elements elms = jDoc.select("h2");

		String sFinalText = "";

		for(Element e:elms) {
			if(e.text()!=null) {
				if(e.text().toLowerCase().contains("steps")) {
					sFinalText = sFinalText.concat(e.text()).concat(".");
					Element nextSibling = e;
					do {
						nextSibling = nextSibling.nextElementSibling();
						if(nextSibling.nodeName().equalsIgnoreCase("h3") || nextSibling.nodeName().equalsIgnoreCase("ol") || nextSibling.nodeName().equalsIgnoreCase("ul") || nextSibling.nodeName().equalsIgnoreCase("li")) {
							
							if(nextSibling.nodeName().equalsIgnoreCase("h3")) {
								sFinalText = sFinalText.concat(nextSibling.text()).concat(".");
							}
							if(nextSibling.nodeName().equalsIgnoreCase("ol")) {
								Elements liNodes = nextSibling.children();
								for(Element li:liNodes) {
									sFinalText = sFinalText.concat(li.text().substring(0, li.text().indexOf(".")+1));
								}
							}
						}
//						System.out.println("DoWhile: "+nextSibling.nodeName());
					} while(!nextSibling.nodeName().equalsIgnoreCase("h2"));
					break;
				} else if(e.text().toLowerCase().contains("summary")) {
					sFinalText = sFinalText.concat(e.text()).concat(".");
					Element nextSibling = e.nextElementSibling();
					sFinalText = sFinalText.concat(nextSibling.text());
					break;
				}
			}
		}
		if(sFinalText.equals("")) {
			String sFinalData[] = sWikiData.split("</p>");
			String sResult[] = {"",""};

			if(sFinalData.length>0) {
				sResult[0] = trimResult(Jsoup.parse(sFinalData[0]).text()).trim();
			}
			if(sFinalData.length>1)
				sResult[1] = trimResult(Jsoup.parse(sFinalData[1]).text()).trim();

			sFinalText = sResult[0].length()>1?sResult[0]:sResult[1];
		}

		//		System.out.println("sFinalText: "+sFinalText);
		return sFinalText;
	}

	private String fetchDataFromWiki(String[] sSearch) {
		User user = new User(this.lgname, this.lgpassword, this.mediaAPIURL);
		user.login();
		//		System.out.println("logged in");
		List<Page> listOfPages = user.queryContent(sSearch);
		//		System.out.println("listed "+listOfPages.size());
		if(listOfPages!=null) {
			for (Page page : listOfPages) {
				WikiModel wikiModel = new WikiModel("${image}", "${title}");
				String html = wikiModel.render(page.toString());
				if(html!=null && html.length()>10) return html;
			}
		}
		return "";
	}

}
