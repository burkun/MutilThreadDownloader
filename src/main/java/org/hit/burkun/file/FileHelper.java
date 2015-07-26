package org.hit.burkun.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;




public class FileHelper {

	public static void main(String[] args){
	
	}
	
	public static String SYS_LINE_SEPARATOR = System.getProperty("line.separator", "\n");
	
	/***
	 * read file from local path, return a linkedlist
	 * @param filename
	 * @return
	 */
	public static LinkedList<String> readFile(String fileName){
		LinkedList<String> res = new LinkedList<String>();
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = reader.readLine()) != null){
				res.add(line);
			}
			reader.close();
		}catch(IOException e){
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * read gzfile
	 * @param filleFullPath
	 * @return lines as list
	 */
	public static LinkedList<String> readGzFile(String filleFullPath){
		LinkedList<String> res = new LinkedList<String>();
		BufferedReader reader = null;
		GZIPInputStream gzi = null;
		try{
			gzi = new GZIPInputStream(new FileInputStream(filleFullPath)); 
			reader = new BufferedReader(new InputStreamReader(gzi));
			String line;
			while((line = reader.readLine()) != null){
				res.add(line);
			}
			reader.close();
			gzi.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(gzi != null){
				try {
					gzi.close();
				}catch(IOException e2) {
					e2.printStackTrace();
				}
			}
		}
		return res;
	}
	public static void readGzFile(String fileName, IDataLineProcesser processer){
		Charset charSet = Charset.defaultCharset();
		readGzFile(fileName, charSet, processer);
	}
	public static void readZipFile(String fileName, IDataLineProcesser processer){
		Charset charSet = Charset.defaultCharset();
		readZipFile(fileName, charSet, processer);
	}
	/**
	 * read gz file with IDataLineProcesser
	 */
	public static void readZipFile(String filleFullPath, Charset charSet, IDataLineProcesser processer){
		BufferedReader reader = null;
		ZipInputStream gzi = null;
		try{
			gzi = new ZipInputStream(new FileInputStream(filleFullPath)); 
			reader = new BufferedReader(new InputStreamReader(gzi));
			String line;
			//注意这里
			if(gzi.getNextEntry()!=null){
				while((line = reader.readLine()) != null){
					processer.processEachLine(line);
				}
			}
			gzi.close();
			reader.close();
			processer.cleanUp();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(gzi != null){
				try {
					gzi.close();
				}catch(IOException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	/**
	 * read gz file with IDataLineProcesser
	 */
	public static void readGzFile(String filleFullPath, Charset charSet, IDataLineProcesser processer){
		BufferedReader reader = null;
		GZIPInputStream gzi = null;
		try{
			gzi = new GZIPInputStream(new FileInputStream(filleFullPath)); 
			reader = new BufferedReader(new InputStreamReader(gzi, charSet));
			String line;
			while((line = reader.readLine()) != null){
				processer.processEachLine(line);
			}
			gzi.close();
			reader.close();
			processer.cleanUp();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(gzi != null){
				try {
					gzi.close();
				}catch(IOException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	/**
	 * read file with IDataLineProcesser
	 * @param fileName
	 * @param processer
	 */
	public static void readFile(String fileName, IDataLineProcesser processer){
		Charset charSet = Charset.defaultCharset();
		readFile(fileName, charSet, processer);
	}
	/**
	 * read file with IDataLineProcesser and charset
	 * @param fileName
	 * @param charSet
	 * @param processer
	 */
	public static void readFile(String fileName, Charset charSet, IDataLineProcesser processer){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charSet));
			String line;
			processer.init();
			while((line = reader.readLine()) != null){
				processer.processEachLine(line);
			}
			processer.cleanUp();
			reader.close();
		}catch(IOException e){
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}
	/**
	 * write file by a linked list, each line is the item in the list
	 * @param <T>
	 * @param filename
	 * @param list
	 */
	public static <T> void writeFile(String filename, Collection<T> list, boolean isAppend){
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new FileWriter(filename, isAppend));
			for(T line : list){
				writer.write(line.toString()+ FileHelper.SYS_LINE_SEPARATOR);
			}
			writer.close();
		}catch(IOException e){
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}

	
	public static <T> void writeFile(String filename, Collection<T> list){
		writeFile(filename,list, false);
	}
	
	public static File[] getDirFile(String filePath, final String filter){
		File file = new File(filePath);
		return file.listFiles(new FilenameFilter() {			
			@Override
			public boolean accept(File arg0, String file) {
				if(file.lastIndexOf(filter) != -1){
					return true;
				}
				return false;
			}
		});
	}
	

}
