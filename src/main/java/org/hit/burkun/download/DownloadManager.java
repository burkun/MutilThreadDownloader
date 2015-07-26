package org.hit.burkun.download;

import java.util.ArrayList;
import java.util.Collection;

import org.hit.burkun.file.FileHelper;
import org.hit.burkun.file.IDataLineProcesser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class DownloadManager {
	
	private static Logger logger = LoggerFactory.getLogger(DownloadManager.class);
	
	enum ProtocolType{HTTP, FTP, OTHER};
	public static void feedLinks(Collection<RemoteLocalPair> list, int maxHttpRetry){
		for(RemoteLocalPair pair : list){
			if(getLinkProtocol(pair.remoteUrl) == ProtocolType.HTTP){
				HttpDownload(pair, maxHttpRetry);
			}else if(getLinkProtocol(pair.remoteUrl) == ProtocolType.FTP){
				FtpDownload(pair);
			}else{
				logger.error(pair.remoteUrl+" Url is not http or ftp!");
			}
			
		}
	}
	
	public static void FtpDownload(RemoteLocalPair pair){
		GeneralDownloadInfo info = new GeneralDownloadInfo(pair);
		String[] res = GeneralDownloadInfo.getHostNameAndFilePath(pair.remoteUrl);
		FtpDownloader down = new FtpDownloader(res[0], info);
		//可以用thread.start()不过不建议
		down.run();
	}
	
	public static void HttpDownload(RemoteLocalPair pair, int maxHttpRetry){
		GeneralDownloadInfo info = new GeneralDownloadInfo(pair);
		HttpDownloader downloader = new HttpDownloader(info, maxHttpRetry);
		//可以用thread.start()不过不建议
		downloader.run();
	}
	
	public static Collection<RemoteLocalPair> readListFromFile(String name){
		final ArrayList<RemoteLocalPair> list = new ArrayList<RemoteLocalPair>();
		FileHelper.readFile(name, new IDataLineProcesser() {
			
			@Override
			public String[] splitLine(String line) {
				String[] items = commaCsv(line);
				return items;
			}
			
			@Override
			public void doPostOutside(String[] items) {
				if(items.length == 4){
					RemoteLocalPair pair = new RemoteLocalPair(items[0], items[3], items[2]);
					if(items[1].length() != 0){
						pair.splitNum = Integer.parseInt(items[1]);
					}
					list.add(pair);
				}
			}
			
			@Override
			public void cleanUp() {
				
			}
			
			@Override
			public String chooseLine(String line) {
				line = line.trim();
				if(!line.startsWith("#") && line.length() > 0){
					return line;
				}
				return null;
			}

			@Override
			public String[] processItems(String[] items) {
				return items;
			}
		});
		return list;
	}
	
	protected static ProtocolType getLinkProtocol(String link){
		int idx = link.lastIndexOf("://");
		if(idx == -1){
			return ProtocolType.OTHER;
		}
		String name = link.substring(0, idx);
		if(name.equals("http")){
			return ProtocolType.HTTP;
		}
		if(name.equals("ftp")){
			return ProtocolType.FTP;
		}
		return ProtocolType.OTHER;
	}
}
