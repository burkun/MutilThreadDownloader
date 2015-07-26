package org.hit.burkun.download;

import java.io.IOException;
import java.io.RandomAccessFile;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * <b>function:</b> 写入文件、保存文件
 */
public class SaveFileItem {
    //存储文件
    private RandomAccessFile itemFile;
    private String name;
    private Logger logger = LoggerFactory.getLogger(SaveFileItem.class);
    
    /**
     * @param name 文件路径、名称
     * @param pos 写入点位置position
     * @throws IOException
     */
    public SaveFileItem(String name, long pos) throws IOException {
    	this.name = name;
        itemFile = new RandomAccessFile(name, "rwd");
        //在指定的pos位置写入数据
        itemFile.seek(pos);
    }
    
    /**
     * <b>function:</b> 同步方法写入文件
     * @author hoojo
     * @createDate 2011-9-26 下午12:21:22
     * @param buff 缓冲数组
     * @param start 起始位置
     * @param length 长度
     * @return
     */
    public synchronized int write(byte[] buff, int start, int length) {
        int i = -1;
        try {
            itemFile.write(buff, start, length);
            i = length;
        } catch (IOException e) {
        	logger.debug(name, e);
        }
        return i;
    }
    
    public void close() throws IOException {
        if (itemFile != null) {
            itemFile.close();
        }
    }
    
    public String getFileName(){
    	return this.name;
    }
    /**
     * 设置文件大小，在old 文件大于新文件时特别有用
     * @param newLength 新的文件长度
     */
    public void setLength(long newLength){
    	try {
    		if(newLength != itemFile.length()){
    			itemFile.setLength(newLength);
    		}
		} catch (IOException e) {
			logger.debug(name, e);
		}
    }
}