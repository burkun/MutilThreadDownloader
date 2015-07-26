# MutilThreadDownloader
JAVA实现的多线程http下载和单线程ftp下载，并支持断点下载

针对http下载可以自定义下载线程，针对ftp下载下载线程为1，可以自定义重新下载重试次数

断点续传原理：
1. 首先从服务器请求文件的大小
  1.1 如果本地保存的断点文件记录的大小和服务器的不一致，那么清除断点信息，重新下载
  1.2 如果本地保存的断点文件记录的文件大小和服务器的一致，那么检查服务器的文件时间戳，如果不一致，重新下载
  1.3 如果文件和大小都一致，那么读取断点信息，初始化线程个数，从断点处下载
2. NoCheckpointInfo针对的是不需要保存断点信息的场景
