package com.ai.elasticsearch.job;

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EsscanTask {
    private static Logger logger = LoggerFactory.getLogger(EsscanTask.class);
    private String dateformat = "yyyy.MM.dd";
//    private String elasticUser,elasticPassword;
//    private String queryIndicesUrl;
//    private String elasticUrl;
    private  int elasticDataLivetime = 30;
    private String elasticSearch;
    private ClientInterface esclient;
//    private Map<String,String> head = new HashMap<>();
    public void init(){
        //Authorization
//        if(!SimpleStringUtil.isEmpty(elasticUser))
//            head.put("Authorization",getHeader(  elasticUser,  elasticPassword) );
//        if(elasticUrl != null){
//            queryIndicesUrl = elasticUrl+"/_cat/indices?v";
//        }
		if(elasticSearch == null)
			elasticSearch = ElasticSearchHelper.DEFAULT_SEARCH;
		this.esclient = ElasticSearchHelper.getRestClientUtil(elasticSearch);

    }
//    private String getHeader(String user,String password) {
//        String auth = user+":"+password;
//        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
//        return "Basic " + new String(encodedAuth);
//    }

    /**
     * 定时执行的方法
     */
    public void scanIndex(){
//       if(elasticUrl == null){
//           logger.warn("Elastic Url is null.please check task config in org\\frameworkset\\task\\quarts-task.xml");
//           return;
//       }

        try {
           //获取所有的索引信息
			logger.info("Elasticsearch Index ScanTask for "+elasticSearch + " begin....." );
//            String data = HttpRequestUtil.httpGetforString(queryIndicesUrl,head);
        	String data = esclient.executeHttp("_cat/indices?v",esclient.HTTP_GET);
            logger.debug(data);

            if(SimpleStringUtil.isNotEmpty(data)){
                Date deadLine = TimeUtil.addDates(new Date(),-elasticDataLivetime);
                List<ESIndice> indices = extractIndice(data,deadLine);
                for(int i = 0; indices != null && i < indices.size(); i ++){
                    ESIndice deadESIndice = indices.get(i);
//                    String deleteIndiceUrl = elasticUrl + "/" + java.net.URLEncoder.encode(deadESIndice.getIndex(),"UTF-8") + "?pretty";
                    try {
                    	//删除过期的索引数据
//                       String res = HttpRequestUtil.httpDelete(deleteIndiceUrl, head);
                    	String res = esclient.executeHttp(java.net.URLEncoder.encode(deadESIndice.getIndex(),"UTF-8") + "?pretty", esclient.HTTP_DELETE);
                        logger.info("deleteIndiceUrl for "+elasticSearch +":"+deadESIndice.getIndex()+","+res);
                    }
                    catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
			logger.info("Elasticsearch Index ScanTask for "+elasticSearch + " end....." );
            
        } catch (Exception e) {
            logger.error("Scan Elastic Index failed:",e);
        }
    }
    public List<ESIndice> extractIndice(String data,Date deadLine) throws IOException {
        Reader reader = null;
        BufferedReader br = null;
        try{
        	reader = new StringReader(data);
            br = new BufferedReader(reader);
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
	            ESIndice esIndice = buildESIndice(  line,  format);
	            //如果索引已经过时，则清除过时索引数据
	            if(esIndice.getGenDate() != null && esIndice.getGenDate().before(deadLine)){
	                indices.add(esIndice);
	            }
	
	        }
	        return indices;
        }
        finally
        {
        	 if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					 
				}
        	 if(br != null)
        		 try {
 					br.close();
 				} catch (IOException e) {
 					 
 				}
        }


    }
    
    private ESIndice buildESIndice(String line,SimpleDateFormat format)
    {
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
                        putGendate(  esIndice,  format);
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
        return esIndice;
    }
    private void putGendate(ESIndice esIndice,SimpleDateFormat format){
    	int dsplit = esIndice.getIndex().lastIndexOf('-');

        try {
            String date = esIndice.getIndex().substring(dsplit+1);
            esIndice.setGenDate(format.parse(date));

        } catch (Exception e) {

        }
    }
    public String getDateformat() {
        return dateformat;
    }

    public void setDateformat(String dateformat) {
        this.dateformat = dateformat;
    }

	public String getElasticSearch() {
		return elasticSearch;
	}

	public void setElasticSearch(String elasticSearch) {
		this.elasticSearch = elasticSearch;
	}
}
