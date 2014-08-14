/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import policies.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

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
        
        int fixedExecutorSize = 4;
        
        ArrayList<String> workerList = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader( "./config/workers.conf" ))) {
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
        //jobMap.put(1, new String[1000]);
        
        //Creating fixed size executor
        ThreadPoolExecutor taskCommExecutor = new ThreadPoolExecutor(fixedExecutorSize, fixedExecutorSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        int port = 8080;

        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();

        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        reqistry.register("*", new RequestHandler(taskCommExecutor));

        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);

        SSLServerSocketFactory sf = null;
        // SSL code removed as it is not needed

        // create a thread to listen for possible scheduler available connections
        Thread t = new RequestListenerThread(port, httpService, sf);
        System.out.println("Request Listener Thread created");
        t.setDaemon(false);
        t.start();
        
        // main thread should wait for the listener to exit before shutdown the
        // task executor pool
        t.join();
        
        // shutdown task executor pool and wait for any taskCommExecutor thread
        // still running
        taskCommExecutor.shutdown();
        while (!taskCommExecutor.isTerminated()) {}
        
        System.out.println("Finished all task communication executor threads");

//        ExecutorService executor = Executors.newFixedThreadPool(20);
//        for (int taskID = 0; taskID < 1000; taskID++) {
//            Task task = new Task(1, taskID, "sleep 240s");
//            TaskCommThread worker = new TaskCommThread(task, jobMap);
//            executor.execute(worker);
//        }
//        executor.shutdown();
//        while (!executor.isTerminated()) {}
        
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
