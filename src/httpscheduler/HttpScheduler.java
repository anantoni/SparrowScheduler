/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
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
        
        ArrayList<String> workerList = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader( "config\\workers.conf" ))) {
            for(String line; (line = br.readLine()) != null; ) {
                workerList.add(line);
            }
        }
        
        // Initialize worker manager
        WorkerManager workerManager = new WorkerManager(workerList);
        
        // Set random scheduling policy
        SchedulingPolicy policy = new RandomSchedulingPolicy(workerManager);
        String workerURL = policy.selectWorker();
        
        Map<Integer, String[]> jobMap = new HashMap<>();
        jobMap.put(1, new String[1000]);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int taskID = 0; taskID < 1000; taskID++) {
            Task task = new Task(1, taskID, "sleep 240s");
            TaskCommThread worker = new TaskCommThread(task, jobMap);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {}
        
        System.out.println("Finished all tasks");
        
        for (String result : jobMap.get(1) )
            System.out.println(result);
        
//        // For test purposes
//        for ( int i = 0 ; i < 1000 ; i++ ) {
//            // sleep for about 2 seconds
//            Thread.sleep(5000L);
//            s.sendTask( "http://localhost:8080/", "1", "sleep 240s");
//        }
    }
    
    
    
}
