package org.hit.burkun.download;


/**
 * 使用这个info可以实现无断线续传功能的下载
 * @author burkun
 *
 */
public class NoCheckPointInfo implements IDownloadInfo{

	
	private boolean isDownloding;
	
	public NoCheckPointInfo(RemoteLocalPair pair) {
		this.pair = pair;
	}
	
	@Override
	public void initDownload() {
		isDownloding = true;
	}

	@Override
	public boolean isNeedDownload(FileCheckPoints serverInitChp) {
		chp = serverInitChp;
		return true;
	}

	@Override
	public boolean writeInfo(FileCheckPoints chkp) {
		chp = chkp;
		return true;
	}

	@Override
	public FileCheckPoints getCurCheckPoints() {
		return chp;
	}

	@Override
	public FileCheckPoints readInfo() {
		return null;
	}

	@Override
	public void downloadDone(FileCheckPoints chkp) {
		isDownloding = false;
	}

	@Override
	public RemoteLocalPair getPair() {
		return pair;
	}

	@Override
	public int getSplitNum() {
		return 1;
	}
	
	public boolean IsDownloading(){
		return isDownloding;
	}
	
	private RemoteLocalPair pair;
	private FileCheckPoints chp = null;

}
