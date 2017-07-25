#定期清理es索引数据，修改resources/elastic.properties文件
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
