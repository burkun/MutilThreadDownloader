package org.hit.burkun.download;


import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;



import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FtpDownloader implements Runnable {
	private String contrlEncoding, host, uname, password;
	private FTPClient ftpClient;
	private IDownloadInfo info;
	private int port;
	
    private Logger logger = LoggerFactory.getLogger(FtpDownloader.class);

	/**
	 * 构造函数
	 * 
	 * @param host
	 *            ftp host name, ip address or host name
	 * @param port
	 *            the defualt port is 21
	 */
	public FtpDownloader(String host, int port, IDownloadInfo info) {
		this(host, "anonymous", "visiter@qq.com", 21, info);
	}

	/**
	 * 构造函数
	 * 
	 * @param host
	 *            主机名，默认端口号为21
	 */
	public FtpDownloader(String host, IDownloadInfo info) {
		this(host, 21, info);
	}

	/**
	 * 构造函数
	 * 
	 * @param host
	 *            服务器域名
	 * @param uname
	 *            帐号
	 * @param password
	 *            密码
	 * @param port
	 *            端口号
	 */
	public FtpDownloader(String host, String uname, String password, int port,
			IDownloadInfo info) {
		this.host = host;
		this.uname = uname;
		this.password = password;
		this.port = port;
		this.info = info;
		init();
	}

	protected void init() {
		ftpClient = new FTPClient();
		//调用初始化函数
		info.initDownload();
	}

	public boolean connect() {
		try {
			ftpClient.connect(this.host, this.port);
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				this.contrlEncoding = ftpClient.getControlEncoding();
				if (ftpClient.login(this.uname, this.password)) {
					logger.info("Connect " + host + " success!");
					return true;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		disconnect();
		logger.error("Connect " + host + " failed!");
		return false;
	}

	public void disconnect() {
		if (ftpClient.isConnected()) {
			try {
				ftpClient.logout();
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		if(connect()){
			// 设置被动模式
			ftpClient.enterLocalPassiveMode();
			ftpClient.setControlEncoding(contrlEncoding);
			try {
				// 设置以二进制方式传输
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				//必须是相对路径
				String[] res = getHostNameAndFilePath(this.info.getPair().remoteUrl);
				String remote = new String(res[1].getBytes(),contrlEncoding);
		        FTPFile[] files = ftpClient.listFiles(remote);   
				if(files.length != 1){
					logger.error(info.getPair().localName + " remote file does not exist!");
		        	return;
		        }
				FTPFile targetFile = files[0];
				// 获取时间戳
				long lastModified = targetFile.getTimestamp().getTimeInMillis();
				// 从服务器获取文件的总大小
				long totalSize = targetFile.getSize();
				FileCheckPoints chp = initCheckPoint(info.getSplitNum(), totalSize, lastModified);
				
				if (info.isNeedDownload(chp)) {
					RetriveSingleStream[] rss = new RetriveSingleStream[info
							.getSplitNum()];
					for (int i = 0; i < chp.getSplit(); i++) {
						rss[i] = new RetriveSingleStream(ftpClient, info,
								info.getCurCheckPoints(), i);
						Thread th = new Thread(rss[i]);
						th.start();
					}
					boolean isDone = false;
					while (!isDone) {
						Thread.sleep(1000);
						isDone = true;
						for (int i = 0; i < info.getSplitNum(); i++) {
							isDone &= rss[i].isDone();
						}

					}
					//在这里会假死
					ftpClient.completePendingCommand();
					logger.info(info.getPair().localName+ " Download is done!");
				} else {
					logger.info(info.getPair().localName+ " Need not to download!");
				}
			} catch (IOException e) {
				logger.error(info.getPair().localName, e);
			} catch (InterruptedException e) {
				logger.error(info.getPair().localName, e);
			} finally {
				disconnect();
			}
			logger.info("download done!");
		}
	}
	public static String[] getHostNameAndFilePath(String fullUrl){
		fullUrl = fullUrl.trim();
		String[] res = new String[2];
		int beginIndex = "ftp://".length();
		int endIndex = fullUrl.indexOf('/', beginIndex);
		if(endIndex != -1){
			res[0] =  fullUrl.substring(beginIndex, endIndex);
			res[1] = fullUrl.substring(endIndex+1);
		}else{
			res[0] =fullUrl.substring(beginIndex);
			res[1] = "";
		}
		return res;
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

	protected class RetriveSingleStream implements Runnable {
		private boolean _isDone = false;

		private FileCheckPoints chp;
		private int curIndex;
		private SaveFileItem file;
		private long startPos;
		private long endPos;
		private FTPClient client;
		private byte[] __buffer = new byte[1024 * 12];
		private IDownloadInfo __info;

		private Logger logger = LoggerFactory.getLogger(FtpDownloader.class);

		
		public boolean isDone() {
			return _isDone;
		}

		public RetriveSingleStream(FTPClient client, IDownloadInfo info,
				FileCheckPoints chp, int curIndex) {
			this.__info = info;
			this.chp = chp;
			this.curIndex = curIndex;
			this.startPos = chp.getStartPos()[curIndex];
			this.endPos = chp.getEndPos()[curIndex];
			this.client = client;
		}

		@Override
		public void run() {
			InputStream in = null;
			client.setRestartOffset(startPos);
			try {
				file = new SaveFileItem(__info.getPair().getLocalFullPath(),
						startPos);
				//--bug fixed
				file.setLength(__info.getCurCheckPoints().totalSize);
				//--bug fixed
				String[] res = getHostNameAndFilePath(__info.getPair().remoteUrl);
				String remote = new String(res[1].getBytes(),contrlEncoding);
				in = client.retrieveFileStream(remote);
				logger.info(__info.getPair().localName+ " #Block"
						+ (curIndex + 1) + "# begin downloading...");
				int len;
				long counter = 0;
				while (startPos < endPos && (len = in.read(__buffer)) != -1) {
					startPos += file.write(__buffer, 0, len);
					counter += 1;
					chp.getStartPos()[curIndex] = Math.min(startPos, endPos);
					if (counter % 20 == 0) {
						__info.writeInfo(chp);
						logger.info(__info.getPair().localName + " #Block"
								+ (curIndex + 1)+ "# download "
								+ getPercentage() + "%...");
						Thread.yield();
					}
				}
				__info.writeInfo(chp);
				_isDone = true;
			} catch (IOException e) {
				e.printStackTrace();
				logger.debug(__info.getPair().localName, e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (file != null) {
						file.close();
					}
				} catch (IOException e) {
					logger.error(__info.getPair().localName, e);
				}
				//防止程序假死
				_isDone = true;
			}
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
