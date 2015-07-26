package org.hit.burkun.file;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FileProcessHelper {
	private Logger logger = LoggerFactory.getLogger(FileProcessHelper.class);
	public enum FILE_TYPE{
		GZIP,
		PLAIN,
		ZIP
	}
	public FileProcessHelper(String fileName){
		this.fileName = fileName;
		this.charset = Charset.defaultCharset();
	}
	
	public FileProcessHelper(String fileName, Charset charset){
		this.fileName = fileName;
		this.charset = charset;
	}
	
	public void process(IDataLineProcesser processer, FILE_TYPE type){
		if(type == FILE_TYPE.GZIP){
			FileHelper.readGzFile(fileName, charset, processer);
		}else if(type == FILE_TYPE.PLAIN){
			FileHelper.readFile(fileName, charset, processer);
		}else if(type == FILE_TYPE.ZIP){
			FileHelper.readZipFile(fileName, charset, processer);
		}else{
			logger.error("can not find this type parser!");
			System.exit(-1);
		}
	}
	
	private String fileName;
	private Charset charset;
	
}
