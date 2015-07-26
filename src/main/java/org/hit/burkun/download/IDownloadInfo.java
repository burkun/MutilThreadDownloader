package org.hit.burkun.download;


public interface IDownloadInfo {
	/**
	 * 初始化download这个阶段可以做连接数据库的操作
	 */
	void initDownload();
	/**
	 * 判断是否需要下载，从数据库获取断点数据
	 * @param serverInitChp checkpoint
	 * @return
	 */
	boolean isNeedDownload(FileCheckPoints serverInitChp);
	/**
	 * 写入断点，可以进行数据库写入操作
	 * @param chkp checkpoint
	 * @return
	 */
	boolean writeInfo(FileCheckPoints chkp);
	/**
	 * 获取当前的checkPoint
	 * @return
	 */
	FileCheckPoints getCurCheckPoints();
	/**
	 * 读取断点信息，可以进行数据库操作
	 * @return
	 */
	FileCheckPoints readInfo();
	/**
	 * 最后下载完成之后的操作
	 * @param chkp
	 */
	void downloadDone(FileCheckPoints chkp);
	/**
	 * 获取当前链接和本地保存的路径信息
	 * @return
	 */
	RemoteLocalPair getPair();
	/**
	 * 文件分块，默认为1
	 * @return
	 */
	int getSplitNum();
}
