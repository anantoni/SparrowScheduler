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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
        Map<Integer, String[]> jobMap = new HashMap<>();
        jobMap.put(1, new String[100]);
        //Creating shared object to store requested tasks
        BlockingQueue taskQueue = new LinkedBlockingQueue();
        for (int taskID = 0; taskID < 100; taskID++) {
            taskQueue.put(new Task(1,taskID,"sleep 240s"));
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 100; i++) {
            TaskCommThread worker = new TaskCommThread(taskQueue, s, jobMap);
            executor.execute(worker);
          }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
//        // For test purposes
//        for ( int i = 0 ; i < 1000 ; i++ ) {
//            // sleep for about 2 seconds
//            Thread.sleep(5000L);
//            s.sendTask( "http://localhost:8080/", "1", "sleep 240s");
//        }
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
    
    public String sendTask( String workerURL, String jobID, String taskCommand ) throws Exception {
        // TODO: handle worker response for task completion
        Map<String, String> postArguments = new HashMap();
        postArguments.put( "job-id", jobID );
        postArguments.put( "task-command", taskCommand );
        String s = schedulerPost( workerURL, postArguments );
        return s;
    }
    
    public String schedulerPost( String workerURL, Map<String, String> postArguments) throws Exception {
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
