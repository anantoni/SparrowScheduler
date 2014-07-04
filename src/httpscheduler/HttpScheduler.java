/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author anantoni
 */
public class HttpScheduler {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    
    public static void main(String[] args) throws Exception{
        // TODO code application logic here
        HttpScheduler s = new HttpScheduler();
        
        // For test purposes
        for ( int i = 0 ; i < 100 ; i++ )
            s.sendTask( "http://localhost:8080/", "1", "sleep 240s");
    }
    
    public void probe( String workerURL ) throws Exception { 
        // TODO: handle probe result
        
        Map<String, String> postArguments = new HashMap();
        postArguments.put( "probe", "yes");
        schedulerPost( workerURL, postArguments );
    }
    
    public void multiProbe( List<String> workersList ) throws Exception {
        // TODO: handle multiprobe result
        for ( String workerURL : workersList ) 
            probe( workerURL );
    }
    
    public void sendTask( String workerURL, String jobID, String taskCommand ) throws Exception {
        // TODO: handle worker response for task completion
        Map<String, String> postArguments = new HashMap();
        postArguments.put( "job-id", jobID );
        postArguments.put( "task-command", taskCommand );
        schedulerPost( workerURL, postArguments );
    }
    
    public void schedulerPost( String workerURL, Map<String, String> postArguments) throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            
            HttpPost httpPost = new HttpPost( workerURL );
            List <NameValuePair> nvps = new ArrayList <>();
            postArguments.keySet().stream().forEach((key) -> { 
                nvps.add( new BasicNameValuePair( key, postArguments.get(key) ) );
            });
            //for ( String key : postArguments.keySet() ) 
            //nvps.add( new BasicNameValuePair( key, postArguments.get(key) ) );
            
            //nvps.add(new BasicNameValuePair("username", "vip"));
            //nvps.add(new BasicNameValuePair("password", "secret"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            
            try (CloseableHttpResponse response2 = httpclient.execute(httpPost)) {
                System.out.println(response2.getStatusLine());
                HttpEntity entity2 = response2.getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity2);
                String a = new String(entityContent);
                System.out.println(a);
                // do something useful with the response body
                // and ensure it is fully consumed
                EntityUtils.consume(entity2);
            }
        }
    }
    
}
