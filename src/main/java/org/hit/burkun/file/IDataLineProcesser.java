package org.hit.burkun.file;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class IDataLineProcesser {
	public abstract String chooseLine(String line);
	public abstract String[] splitLine(String line);
	public abstract void cleanUp();
	public abstract void doPostOutside(String[] items);
	public abstract String[] processItems(String[] items);
	
	public void init(){
		
	}
	
	public void processEachLine(String line){
		String new_line = chooseLine(line);
		if(new_line != null){
			String[] items = splitLine(new_line);
			if(items != null){
				items = processItems(items);
				doPostOutside(items);
			}
		}
	}
	
	public static String[] commaCsv(String line){
		Pattern pattern = Pattern.compile("\\G(?:^|,)(?:\"([^\"]*+(?:\"\"[^\"]*+)*+)\"|([^\",]*+))");
		//即 \G(?:^|,)(?:"([^"]*+(?:""[^"]*+)*+)"|([^",]*+))  《精通正则表达式》 第401页
		 Matcher matcherMain = pattern.matcher("");
	     Matcher matcherQuoto = Pattern.compile("\"\"").matcher("");
	     matcherMain.reset(line);
	     List<String> strList = new ArrayList<String>();
	     while (matcherMain.find()) {
	            String field;
	            if (matcherMain.start(2) >= 0) {
	                field = matcherMain.group(2);
	            } else {
	                field = matcherQuoto.reset(matcherMain.group(1)).replaceAll("\"");
	            }
	            strList.add(field);
	     }
	     String[] res = strList.toArray(new String[0]);
	     return res;
	}
	public static String[] commaCsv1(String line){
		return anyCsv(line, ',');
	}
//	public static String[] tabCsv(String line){
//		Pattern pattern = Pattern.compile("\\G(?:^|\t)(?:\"([^\"]*+(?:\"\"[^\"]*+)*+)\"|([^\"\t]*+))");
//		 Matcher matcherMain = pattern.matcher("");
//	     Matcher matcherQuoto = Pattern.compile("\"\"").matcher("");
//	     matcherMain.reset(line);
//	     List<String> strList = new ArrayList<String>();
//	     while (matcherMain.find()) {
//	            String field;
//	            if (matcherMain.start(2) >= 0) {
//	                field = matcherMain.group(2);
//	            } else {
//	                field = matcherQuoto.reset(matcherMain.group(1)).replaceAll("\"");
//	            }
//	            strList.add(field);
//	     }
//	     String[] res = strList.toArray(new String[0]);
//	     return res;
//	}
	
	
	public static String[] tabCsv(String line){
		return anyCsv(line, '\t');
	}
	
	public static void main(String[] args){
		String[] items = tabCsv("123\tadas\tdsadas\t");
		System.out.println(items);
	}
	public static String[] anyCsv(String line, char token){
		LinkedList<String> list = new LinkedList<String>();
		char comma = '\"';
		char tab = token;
		int comma1idx = -1, comma2idx = -1, wbegin = 0, wend = -1;
		wend = line.indexOf(tab);
		comma1idx = line.indexOf(comma);
		while(wend != -1){
			if(comma1idx == -1 || comma1idx > wend){
				String word = line.substring(wbegin, wend);
				list.add(word.trim());
				wbegin = wend + 1;
				wend = line.indexOf(tab, wend+1);
			}else{
				if(comma1idx < wend){
					comma2idx = line.indexOf(comma, comma1idx + 1);
					if(comma2idx < wend){
						//包含在里面，且没有tab，那么
						String word = line.substring(wbegin, wend);
						list.add(word.trim());
						wbegin = wend;
						wend = line.indexOf(tab, wend+1);
					}else{
						//没包含在里面， 把tab包含了，那么忽略这个tab
						wend = line.indexOf(tab, comma2idx+1);
					}
					//重新找
					comma1idx = line.indexOf(comma, wend+1);
					comma2idx = comma1idx;
				}
			}
		}
		list.add(line.substring(wbegin));
		return list.toArray(new String[0]);
	}
}
