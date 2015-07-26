package org.hit.burkun.test;

import java.util.Collection;

import org.hit.burkun.download.DownloadManager;
import org.hit.burkun.download.RemoteLocalPair;

public class Demo {

	public static void main(String[] args) {
		batchDownload();
	}
	
	public static void batchDownload(){
		//读取文件，并批量下载
		Collection<RemoteLocalPair> list = DownloadManager.readListFromFile("configure/downloadlist.ini");
		//最大重试次数为3次
		DownloadManager.feedLinks(list, 3);
	}
	
}
