package org.hit.burkun.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class HttpDownloader extends Thread {
	
    private Logger logger = LoggerFactory.getLogger(HttpDownloader.class);
	private IDownloadInfo info;
	private int maxRetry = 5;

	public HttpDownloader(IDownloadInfo info,int maxRetry) {
		this.info = info;
		this.maxRetry = maxRetry;
	}

	public HttpDownloader(IDownloadInfo info) {
		this.info = info;
	}

	@Override
	public void run() {
		
		FileCheckPoints chp = getInitedCheckPoint();
		if(chp.totalSize < 0){
			logger.info(info.getPair().localName + " The size of file is "+ chp.totalSize);
			return;
		}
		if(info.isNeedDownload(chp)){
			RetriveSingleStream[] rss = new RetriveSingleStream[info.getSplitNum()];
			for (int i = 0; i < info.getSplitNum(); i++) {
				rss[i] = new RetriveSingleStream(info,
						info.getCurCheckPoints(), i, maxRetry);
				Thread th = new Thread(rss[i]);
				th.start();
			}
			boolean isDone = false;
			try {
				while (!isDone) {
					Thread.sleep(1000);
					isDone = true;
					for (int i = 0; i < info.getSplitNum(); i++) {
						isDone &= rss[i].isDone();
					}
				}
				logger.info(info.getPair().localName + " Download is done!");
			}
			catch (InterruptedException e) {
				logger.debug(info.getPair().localName, e);
			}
			
		}else{
			logger.info(info.getPair().localName + " Need not to download!");
		}
	}
	
	
	
	public static FileCheckPoints initCheckPoint(int splitNum, long totalSize, long timeStamp) {
		long[] startPos = new long[splitNum];
		long[] endPos = new long[splitNum];
		for (int i = 0, len = startPos.length; i < len; i++) {
			long size = i * (totalSize / len);
			startPos[i] = size;
			// 设置最后一个结束点的位置
			if (i == len - 1) {
				endPos[i] = totalSize;
			} else {
				size = (i + 1) * (totalSize / len);
				endPos[i] = size;
			}
		}
		FileCheckPoints chp = new FileCheckPoints();
		chp.setEndPos(endPos);
		chp.setStartPos(startPos);
		chp.totalSize = totalSize;
		chp.timestamp = timeStamp;
		return chp;
	}

	private FileCheckPoints getInitedCheckPoint() {
		long fileLength = -1;
		long timeStamp = -1;
		HttpURLConnection conn = null;
		try {
			URL url = new URL(this.info.getPair().remoteUrl);
			conn = (HttpURLConnection) url.openConnection();
			HttpDownloader.RetriveSingleStream.setHeader(conn);
			int stateCode = conn.getResponseCode();
			// 判断http status是否为HTTP/1.1 206 Partial Content或者200 OK
			if (stateCode != HttpURLConnection.HTTP_OK
					&& stateCode != HttpURLConnection.HTTP_PARTIAL) {
				logger.warn(info.getPair().localName+ " #Error Code:# "
						+ stateCode);
				fileLength = -2;
			} else if (stateCode >= 400) {
				logger.warn(info.getPair().localName + " #Error Code:# "
						+ stateCode);
				fileLength = -2;
			} else {
				// 获取长度
				fileLength = conn.getContentLengthLong();
				timeStamp = conn.getLastModified();
				logger.info(info.getPair().localName+ " #FileLength:# "
						+ fileLength);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}
		FileCheckPoints chp;
		if(fileLength > 0){
			chp = initCheckPoint(info.getSplitNum(), fileLength, timeStamp);
			chp.timestamp = timeStamp;
		}else{
			chp = new FileCheckPoints();
		}
		return chp;
	}

	/**
	 * bug fixed change the RandomAccessFile size
	 * @author burkun
	 *
	 */
	
	
	protected static class RetriveSingleStream implements Runnable {
		private boolean isDone = false;
		private FileCheckPoints chp;
		private int curIndex;
		private SaveFileItem file;
		private long startPos;
		private long endPos;
		byte[] buffer = new byte[1024*12];
		private IDownloadInfo __info;
		private int maxRetry;
		private Logger logger = LoggerFactory.getLogger(RetriveSingleStream.class);
		
		public boolean isDone() {
			return isDone;
		}

		public RetriveSingleStream(IDownloadInfo info, FileCheckPoints chp,
				int curIndex, int maxRetry) {
			this.__info = info;
			this.chp = chp;
			this.curIndex = curIndex;
			this.startPos = chp.getStartPos()[curIndex];
			this.endPos = chp.getEndPos()[curIndex];
			this.maxRetry = maxRetry;
		}

		@Override
		public void run() {
			InputStream in = null;
			HttpURLConnection conn = null;
			int curRetry = 0;
			
			while(curRetry < maxRetry && !isDone){
				try {
					URL url = new URL(__info.getPair().remoteUrl);
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(10000);
					conn.setReadTimeout(30000);
					setHeader(conn);
					String property = "bytes=" + startPos + "-";
		            conn.setRequestProperty("RANGE", property);
					logger.info(__info.getPair().localName + " #Block"
							+ (curIndex + 1) + "# begin downloading...");
					int length;
					long counter = 0;
					InputStream is = conn.getInputStream();
					file = new SaveFileItem(__info.getPair().getLocalFullPath(), startPos);
					//--bug fixed
					file.setLength(__info.getCurCheckPoints().totalSize);
					//--bug fixed
					 while (!isDone && startPos < endPos && (length = is.read(buffer)) > 0) {
		                	startPos += file.write(buffer, 0, length);
		                	counter += 1;
		                	chp.getStartPos()[curIndex] = Math.min(startPos, endPos);
		                	if (counter % 20 == 0) {
								__info.writeInfo(chp);
								logger.info(__info.getPair().localName + " #Block"
										+ (curIndex + 1) + "# download "
										+ getPercentage() + "%...");
								Thread.yield();
							}
		              }
					__info.writeInfo(chp);
					isDone = true;
				} catch (IOException e) {
					isDone = false;
					logger.debug(__info.getPair().localName, e);
				} finally {
					if(!isDone){
						curRetry++;
						logger.debug(__info.getPair().localName + " download failed, retry again!");
						if(curRetry >= maxRetry){
							//保证循环跳出
							isDone = true;
						}
					}else{
						curRetry = maxRetry;
					}
					try {
						if (in != null) {
							in.close();
						}
						if (file != null) {
							file.close();
						}
						if(conn != null){
							conn.disconnect();
						}
					} catch (IOException e) {
						logger.debug(__info.getPair().localName, e);
					}
				}
			}
		}


		
		public static void setHeader(URLConnection conn) {
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 BIDUBrowser/7.0 Safari/537.36");
			conn.setRequestProperty("Accept-Language",
					"en-us,en;q=0.7,zh-cn;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "utf-8");
			conn.setRequestProperty("Accept-Charset",
					"ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			conn.setRequestProperty("Keep-Alive", "300");
			conn.setRequestProperty("connnection", "keep-alive");
			// conn.setRequestProperty("If-Modified-Since",
			// "Fri, 02 Jan 2009 17:00:05 GMT");
			// conn.setRequestProperty("If-None-Match",
			// "\"1261d8-4290-df64d224\"");
			conn.setRequestProperty("Cache-conntrol", "max-age=0");
			conn.setRequestProperty("Referer", "http://www.baidu.com");
		}

		private int getPercentage() {
			long total = 0;
			for (int i = 0; i < chp.getSplit(); i++) {
				total += chp.getEndPos()[i] - chp.getStartPos()[i];
			}
			return (int) ((chp.totalSize - total) * 100 / chp.totalSize);
		}

	}
}
