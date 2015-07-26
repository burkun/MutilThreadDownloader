package org.hit.burkun.download;

public class FileCheckPoints {
	public long timestamp = -99;
	public long totalSize = -99;
	private int split = -1; 
	private long[] startPos;
	private long[] endPos;
	public long[] getStartPos() {
		return startPos;
	}
	public void setStartPos(long[] startPos) {
		split = startPos.length;
		this.startPos = startPos;
	}
	public long[] getEndPos() {
		return endPos;
	}
	public void setEndPos(long[] endPos) {
		split = endPos.length;
		this.endPos = endPos;
	}
	public int getSplit() {
		return split;
	}
	
	public void copy(FileCheckPoints _chp){
		this.setEndPos(_chp.endPos);
		this.setStartPos(_chp.startPos);
		this.totalSize = _chp.totalSize;
		this.timestamp = _chp.timestamp;
	}

}
