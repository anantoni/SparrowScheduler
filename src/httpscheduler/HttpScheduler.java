/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

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
import utils.JobMap;

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
        
         if (args.length != 2) {
            System.err.println("Invalid command line parameters for worker");
            System.exit(-1);
        }
        
        int fixedExecutorSize = 100;
        
        //Creating fixed size executor
        ThreadPoolExecutor taskCommExecutor = new ThreadPoolExecutor(fixedExecutorSize, fixedExecutorSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        // Used for late binding
        JobMap jobMap = new JobMap();

        // Set port number
        int port = Integer.parseInt(args[0]);
        
         // Set worker mode
        String mode = args[1].substring(2);
        
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Test/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();

        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        // Different handlers for late binding and generic cases
        if (mode.equals("late"))
                reqistry.register("*", new LateBindingRequestHandler(taskCommExecutor, jobMap));
        else
                reqistry.register("*", new GenericRequestHandler(taskCommExecutor, mode));
        
        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);

        SSLServerSocketFactory sf = null;

        // create a thread to listen for possible client available connections
        Thread t;
        if (mode.equals("late"))
            t = new LateBindingRequestListenerThread(port, httpService, sf);
        else 
            t = new GenericRequestListenerThread(port, httpService, sf);
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
        System.out.println("Finished all tasks");
        
    }
}
