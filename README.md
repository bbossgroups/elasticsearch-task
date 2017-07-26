#定期清理es索引数据
基于quartz 2.3.0，定期清理es索引数据

#作业配置
修改resources/elastic.properties文件

## elasticsearch服务器地址
elastic.url=http://10.1.236.88:9200
## elasticsearch访问账号，没有则配置为空
elastic.user=elastic
## elasticsearch访问口令，没有则配置为空
elastic.password=changeme
##数据有效期,以天为单位
elastic.data.livetime=30
##con time，定时扫描时间点
elastic.crontime=0/10 * * * * ?
##索引表对应的日期格式
elastic.dateformat=yyyy.MM.dd

# 构建部署
前提：安装和配置好最新的gradle版本，下载源码，在源码根目录命令行下执行：
## 利用gradle构建发布版本
gradle releaseVersion

## 运行作业
执行成功后，在build/distributions目录下会生成可以允许的zip包，解压后启动和运行quartz作业


linux：

chmod +x startup.sh

./startup.sh

windows: startup.bat


