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
public class HttpComm {
    public static String probe( String workerURL ) throws Exception { 
        // TODO: handle probe result
        
        Map<String, String> postArguments = new HashMap();
        postArguments.put( "probe", "yes");
        String s = schedulerPost( workerURL, postArguments );
        return s;
    }
    
    public static void multiProbe( List<String> workersList ) throws Exception {
        // TODO: handle multiprobe result
        for ( String workerURL : workersList ) 
                probe( workerURL );
    }
    
    public static String sendTask( String workerURL, String jobID, String taskCommand ) throws Exception {
        // TODO: handle worker response for task completion
        Map<String, String> postArguments = new HashMap();
        postArguments.put( "job-id", jobID );
        postArguments.put( "task-command", taskCommand );
        String s = schedulerPost( workerURL, postArguments );
        return s;
    }
    
    public static String heartbeat( String workerURL ) throws Exception {
        Map<String, String> postArguments = new HashMap();
        postArguments.put( "heartbeat", "yes");
        String s = schedulerPost( workerURL, postArguments );
        return s;
    }
    
    public static String schedulerPost( String workerURL, Map<String, String> postArguments) throws Exception {
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
                        String s = EntityUtils.toString(entity2);
        //                byte[] entityContent = EntityUtils.toByteArray(entity2);
        //                String a = new String(entityContent);
        //                System.out.println(a);
                        // do something useful with the response body
                        // and ensure it is fully consumed
                        EntityUtils.consume(entity2);
                        return s;
                }
        }
    }
}
