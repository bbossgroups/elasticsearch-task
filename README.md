#定期清理es索引数据
基于quartz 2.3.0，定期清理es索引数据，支持多个elasticsearch集群数据清理


#作业配置
修改resources/application.properties文件

## elasticsearch服务器地址，多个集群配置，参考application.properties配置文件内容，主要的配置内容如下

### rest协议配置，集群需要配置多个，单机只需配置一个即可
elasticsearch.rest.hostNames=10.1.236.85:9200,10.1.236.88:9200,10.1.236.86:9200
## elasticsearch访问账号，没有则配置为空，开启x-pack认证机制情况下有用
elasticUser=elastic
## elasticsearch访问口令，没有则配置为空，开启x-pack认证机制情况下有用
elasticPassword=changeme
##数据有效期,以天为单位
elastic.data.livetime=30
##con time，定时扫描时间点
elastic.crontime=0/10 * * * * ?
##索引表对应的日期格式
elasticsearch.dateFormat=yyyy.MM.dd

# 构建部署
前提：安装和配置好最新的gradle版本，下载源码
## 利用gradle构建发布版本
gradle releaseVersion

## 运行作业
gradle构建成功后，在build/distributions目录下会生成可以运行的zip包，解压后启动和运行quartz作业即可：


linux：

chmod +x startup.sh

./startup.sh

停止：
./stop.sh

重启

./restart.sh

windows: startup.bat stop.bat,restart.bat


