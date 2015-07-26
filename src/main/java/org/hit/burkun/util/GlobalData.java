package org.hit.burkun.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GlobalData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	
	private Properties properties;
	private String filePath;
	private GlobalData(String filePath){
		this.filePath = filePath;
		properties = loadProperties(filePath);
	}
	
	//===================================
	private Properties loadProperties(String filePath){
		InputStream is;
		try {
			is = new BufferedInputStream(new FileInputStream(new File(filePath)));
			Properties properties = new Properties();
			properties.load(is);
			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Properties getProperties(){
		return properties;
	}
	
	public void setPropertity(String key, String value){
		FileOutputStream oFile;
		try {
			properties.setProperty(key, value);
			oFile = new FileOutputStream(filePath);
			properties.store(oFile, "add counter!");
			oFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getTempDir(){
		String path = properties.getProperty("TEMP_PATH");
		File dir = new File(path);
		if(!dir.exists()){
			try {
				dir.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	public String getParserDir(){
		String path = properties.getProperty("PARSER_DIR");
		File dir = new File(path);
		if(!dir.exists()){
			try {
				dir.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	
	public String getHomoGoaUrl(){
		return properties.getProperty("GO_HOMO_GOA_GZ_LINK");
	}
	
	public String getGoBasicOboUrl(){
		return properties.getProperty("GO_OBO_BASIC_LINK");
	}
	
	public String getGoOboUrl(){
		return properties.getProperty("GO_OBO_LINK");
	}
	
	public String getGoOwlUrl(){
		return properties.getProperty("GO_OWL_LINK");
	}
	
	public String getGoOwlPlusUrl(){
		return properties.getProperty("GO_OWL_PLUS_LINK");
	}
	
	public String getDoOboUrl(){
		return properties.getProperty("DO_OBO_LINK");
	}
	
	public String getProOboUrl(){
		return properties.getProperty("PRO_OBO_LINK");
	}
	
	public String getChebiOboUrl(){
		return properties.getProperty("CHEBI_OBO_LINK");
	}
	
	public String getLocalGoBasicPath(){
		String goUrl = getGoBasicOboUrl();
		return getTempDir() +File.separator+GlobalData.getFileName(goUrl);
	}
	
	//====================================
	private static GlobalData single;
	private static Object lock = new Object();

	//====================================
	public static String getFileName(String url){
	    return url.substring(url.lastIndexOf("/") + 1, url.length());
	}
	public static GlobalData getInstance(){
		synchronized (lock) {
			if(single == null){
				single = new GlobalData("configure/settings.properties");
			}
			return single;
		}
	}
	

}
