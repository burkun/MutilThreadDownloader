package org.hit.burkun.download;

import java.io.File;

import org.hit.burkun.util.GlobalData;


/**
 * 需要的下载和保存的文件对
 * @author burkun
 *
 */
public class RemoteLocalPair{
	public int splitNum = 1;
	public String remoteUrl;
	public String localPath;
	public String localName;
	public String getLocalFullPath(){
		return localPath + File.separator + localName;
	}
	/**
	 * 
	 * @param remoteUrl 远程url
	 * @param localPath 本地路径
	 * @param localName 本地文件名
	 */
	public RemoteLocalPair(String remoteUrl, String localPath, String localName){
		this.remoteUrl = remoteUrl;
		if(localName.length() == 0){
			this.localName = getFileName(remoteUrl);;
		}else{
			this.localName = localName;
		}
		if(localPath.length() == 0){
			this.localPath = GlobalData.getInstance().getTempDir();
		}else{
			this.localPath = localPath;
		}
	}
	/**
	 * 
	 * @param remoteUrl 远程url
	 * @param localPath 本地路径 默认文件名从url中提取
	 */
	public RemoteLocalPair(String remoteUrl, String localPath){
		this.remoteUrl = remoteUrl;
		this.localName = getFileName(remoteUrl);
		this.localPath = localPath;
	}
	
	private String getFileName(String url) {
		return url.substring(url.lastIndexOf("/") + 1, url.length());
	}
}
