/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
        
/**
 *
 * @author anantoni
 */
public class HttpComm {
    static String hostname = "127.0.0.1";
    static Integer port = 51000;
    static final CloseableHttpClient httpClient;
    static {
        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }
    
//    public static HttpClient getHttpClient() {
//        return httpClient;
//    }
    
    public static String probe( String workerURL ) throws Exception { 
        // TODO: handle probe result
        
        Map<String, String> postArguments = new LinkedHashMap();
        postArguments.put( "probe", "yes");
        String s = schedulerPost( workerURL, postArguments );
        return s;
    }
    
    public static Map<String, String> multiProbe( List<String> workersList ) throws Exception {
        Map<String, String> results = new LinkedHashMap<>();
        for ( String workerURL : workersList ) 
                results.put(workerURL, probe(workerURL));
        return results;
    }
    
    public static List<String> lateBindingMultiProbe(List<String> workersList, int jobID) throws Exception {
        StringBuilder thisSchedulerURL = new StringBuilder("http://");
        thisSchedulerURL.append(hostname).append(":").append(port).toString();
        
        List<String> results = new LinkedList<>();
        Map<String, String> postArguments = new LinkedHashMap();
        
        postArguments.put( "probe", "yes" );
        postArguments.put( "scheduler-url", thisSchedulerURL.toString() );
        postArguments.put( "job-id", Integer.toString(jobID) );
        
        for ( String workerURL : workersList ) 
                results.add(workerURL + schedulerPost( workerURL, postArguments ));
        
        return results;
    }
    
    public static String sendTask( String workerURL, String taskDuration) throws Exception {
        // TODO: handle worker response for task completion
        Map<String, String> postArguments = new LinkedHashMap();
        postArguments.put( "task-duration", taskDuration );
        String s = schedulerPost(workerURL, postArguments);
        return s;
    }
    
    public static String heartbeat( String workerURL ) throws Exception {
        Map<String, String> postArguments = new LinkedHashMap();
        postArguments.put( "heartbeat", "yes");
        String s = schedulerPost(workerURL, postArguments);
        return s;
    }
    
    public static String schedulerPost( String workerURL, Map<String, String> postArguments) throws Exception {
        //try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(workerURL);
            httpPost.setProtocolVersion(HttpVersion.HTTP_1_1);
            List <NameValuePair> nvps = new ArrayList <>();
            for (String key :postArguments.keySet())
                nvps.add( new BasicNameValuePair( key, postArguments.get(key) ) );

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                //System.out.println(response2.getStatusLine());
                HttpEntity entity = response.getEntity();
                String s = EntityUtils.toString(entity);
                EntityUtils.consume(entity);
                return s;
            }
            finally {
                httpPost.releaseConnection();
            }
        }
    //}
}
