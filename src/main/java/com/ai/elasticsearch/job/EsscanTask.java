package com.ai.elasticsearch.job;

import com.frameworkset.util.SimpleStringUtil;
import org.apache.commons.codec.binary.Base64;
import org.frameworkset.spi.remote.http.HttpRequestUtil;
import org.frameworkset.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class EsscanTask {
    private static Logger logger = LoggerFactory.getLogger(EsscanTask.class);
    private String dateformat = "yyyy.MM.dd";
    private String elasticUser,elasticPassword;
    private String queryIndicesUrl;
    private String elasticUrl;
    private  int elasticDataLivetime = 30;
    private Map<String,String> head = new HashMap<>();
    public void init(){
        //Authorization
        if(!SimpleStringUtil.isEmpty(elasticUser))
            head.put("Authorization",getHeader(  elasticUser,  elasticPassword) );
        if(elasticUrl != null){
            queryIndicesUrl = elasticUrl+"/_cat/indices?v";
        }

    }
    private String getHeader(String user,String password) {
        String auth = user+":"+password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        return authHeader;
    }
    public void scanIndex(){
       if(elasticUrl == null){
           logger.warn("Elastic Url is null.please check task config in org\\frameworkset\\task\\quarts-task.xml");
           return;
       }

        try {
           //获取所有的索引信息

            String data = HttpRequestUtil.httpGetforString(queryIndicesUrl,head);
            System.out.println(data);

            if(SimpleStringUtil.isNotEmpty(data)){
                Date deadLine = TimeUtil.addDates(new Date(),-elasticDataLivetime);
                List<ESIndice> indices = extractIndice(data,deadLine);
                for(int i = 0; indices != null && i < indices.size(); i ++){
                    ESIndice deadESIndice = indices.get(i);
                    String deleteIndiceUrl = elasticUrl + "/" + deadESIndice.getIndex() + "?pretty";
                    try {
                       String res = HttpRequestUtil.httpDelete(deleteIndiceUrl, head);
                        logger.warn("deleteIndiceUrl:"+deleteIndiceUrl+","+res);
                    }
                    catch (Exception e) {
                        //logger.warn("elasticUrl:"+deleteIndiceUrl,e);
                        logger.warn(e.getMessage());
                    }
                }
            }

            //删除过期的索引数据
//            List<ESIndice> indices = SimpleStringUtil.json2ObjectWithType(data,new JsonTypeReference<List<ESIndice>>(){});
//            HttpRequestUtil.httpDelete(elasticUrl+"/");
        } catch (Exception e) {
            logger.warn("elasticUrl:"+queryIndicesUrl,e);
        }
    }
    public List<ESIndice> extractIndice(String data,Date deadLine) throws IOException {
        StringReader reader = new StringReader(data);
        BufferedReader br = new BufferedReader(reader);
        List<ESIndice> indices = new ArrayList<ESIndice>();
        int i = 0;
        SimpleDateFormat format = new SimpleDateFormat(dateformat);
        while(true){
            String line = br.readLine();
            if(line == null)
                break;
            if(i == 0){
                i ++;
                continue;
            }
            String[] indice = line.split(" ");
            StringBuilder token = new StringBuilder();
            ESIndice esIndice = new ESIndice();

            int k = 0;
            for(int j = 0; j < line.length(); j ++){
                char c = line.charAt(j);
                if(c != ' '){
                    token.append(c);
                }
                else {
                    if(token.length() == 0)
                        continue;
                    switch (k ){
                        case 0:
                            esIndice.setHealth(token.toString());
                            token.setLength(0);
                            k ++;
                            break;
                        case 1:
                            esIndice.setStatus(token.toString());
                            token.setLength(0);
                            k ++;
                            break;
                        case 2:
                            esIndice.setIndex(token.toString());
                            int dsplit = esIndice.getIndex().lastIndexOf('-');

                            try {
                                String date = esIndice.getIndex().substring(dsplit+1);
                                esIndice.setGenDate(format.parse(date));

                            } catch (Exception e) {

                            }
                            token.setLength(0);
                            k ++;
                            break;
                        case 3:
                            esIndice.setUuid(token.toString());
                            token.setLength(0);
                            k ++;
                            break;
                        case 4:
                            esIndice.setPri(Integer.parseInt(token.toString()));
                            token.setLength(0);
                            k ++;
                            break;
                        case 5:
                            esIndice.setRep(Integer.parseInt(token.toString()));
                            token.setLength(0);
                            k ++;
                            break;
                        case 6:
                            esIndice.setDocsCcount(Long.parseLong(token.toString()));
                            token.setLength(0);
                            k ++;
                            break;
                        case 7:
                            esIndice.setDocsDeleted(Long.parseLong(token.toString()));
                            token.setLength(0);
                            k ++;
                            break;
                        case 8:
                            esIndice.setStoreSize(token.toString());
                            token.setLength(0);
                            k ++;
                            break;
                        case 9:
                            esIndice.setPriStoreSize(token.toString());
                            token.setLength(0);
                            k ++;
                            break;
                        default:
                            break;

                    }
                }
            }
            esIndice.setPriStoreSize(token.toString());
            //如果索引已经过时，则清除过时索引数据
            if(esIndice.getGenDate() != null && esIndice.getGenDate().before(deadLine)){
                indices.add(esIndice);
            }
            token = null;

        }
        return indices;


    }
    public String getDateformat() {
        return dateformat;
    }

    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }
}
