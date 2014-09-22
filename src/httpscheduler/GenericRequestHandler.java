/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import utils.Task;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import policies.BatchSamplingSchedulingPolicy;
import policies.PerTaskSamplingSchedulingPolicy;
import policies.RandomSchedulingPolicy;
import policies.SchedulingPolicy;
import utils.AtomicCounter;
import utils.StatsLog;

/**
 *
 * @author anantoni
 */
class GenericRequestHandler implements HttpRequestHandler  {
        private final ThreadPoolExecutor taskCommExecutor;
        private final String mode;

        // Pass reference to the requestsQueue to the GenericRequestHandler
        public GenericRequestHandler(ThreadPoolExecutor taskCommExecutor, String mode) {
                super();
                this.taskCommExecutor = taskCommExecutor;
                this.mode = mode;
        }

        @Override
         public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

                String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
                if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                        throw new MethodNotSupportedException(method + " method not supported");
                }
        
                StringEntity stringEntity;
                if (request instanceof HttpEntityEnclosingRequest) {
                        HttpEntity httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
                        String entity = EntityUtils.toString(httpEntity);
                        //System.out.println("Incoming entity content (string): " + entity);

                        // Parse HTTP request
                        ArrayList <Task> tasksList = parseHttpClientRequest(entity);

                        Future threadMonitor = null;
                        
                        
                        // Set scheduling policy
                        SchedulingPolicy policy = null;
                        
                        switch (mode) {
                                case "random":
                                        policy = new RandomSchedulingPolicy();
                                        break;
                                case "per-task":
                                        policy = new PerTaskSamplingSchedulingPolicy();
                                        break;
                                case "batch":
                                        policy = new BatchSamplingSchedulingPolicy();
                                        break;
                                
                                default:
                                        throw new IllegalArgumentException("Invalid mode: " + mode);
                        }
                        
                        // Different handling for Batch Processing
                        if (policy instanceof BatchSamplingSchedulingPolicy) {
                                policy.selectBatchWorker(tasksList.size());
                        }
                        
                        // Create communication thread
                        SendTaskThread[] threads = new SendTaskThread[tasksList.size()];
                        System.out.println("number of send task threads: " + threads.length);
                        int i = 0;
                        for (Task taskToProcess : tasksList) {
                            String workerURL = policy.selectWorker();
                            Date dNow = new Date( );
                            SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
                            StatsLog.writeToLog( ft.format(dNow) + "Task scheduled");
                            Thread taskCommExecutorThread = new TaskCommThread(taskToProcess, workerURL);
//                            threadMonitor = taskCommExecutor.submit(taskCommExecutorThread);
                            taskCommExecutorThread.start();

//                            threads[i] = new SendTaskThread(taskToProcess, workerURL);
//                            threads[i].start();
                            i++;
                        }
                        
//                    for (SendTaskThread thread : threads) {
//                        try {
//                            thread.join();
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(GenericRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                        
                        
// ---------> For Tom: Why do we need this?
//                        try {
//                                 the main thread should wait until the submitted thread
//                                 finishes its computation
//                                threadMonitor.get();
//                        } catch (InterruptedException | ExecutionException ex) {
//                            Logger.getLogger(GenericRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
//                        }

                        response.setStatusCode(HttpStatus.SC_OK);
                        stringEntity = new StringEntity("result:success");
                } 
                else{
                        response.setStatusCode(HttpStatus.SC_OK);
                        stringEntity = new StringEntity("result:fail");
                }
               
            response.setEntity(stringEntity); 
    }

    ArrayList<Task> parseHttpClientRequest(String httpRequest) {
        ArrayList<Task> tasksList = new ArrayList<>();
        String[] taskCommandsList = null;
        String[] taskIDsList = null;
        
        String result = "";
        try {
                result = java.net.URLDecoder.decode(httpRequest, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(GenericRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] requestArguments = result.split("&");
        
        if (requestArguments.length != 3 ) {
                System.err.println("Invalid HTTP request: " + result);
                return null; 
        }
        else if (requestArguments.length == 3) {
                int jobID = 0;
                String[] keyValuePair = requestArguments[0].split("=");
                
                if ( keyValuePair[0].equals("job-id") ) {
                        assert keyValuePair[1].matches("[0-9]+");
                        jobID = Integer.parseInt(keyValuePair[1]);
                }
                else 
                        System.err.println("Invalid argument - expecting job-id");
            
                keyValuePair = requestArguments[1].split("=");
                if ( keyValuePair[0].equals("task-commands") ) {
                        taskCommandsList = keyValuePair[1].split(",");
                }
                else 
                        System.err.println("Invalid argument - expecting task commands");
            
                keyValuePair = requestArguments[2].split("=");
                if ( keyValuePair[0].equals("task-ids") ) {
                        taskIDsList = keyValuePair[1].split(",");
                }
                else 
                        System.err.println("Invalid argument - expecting task-ids");
             
                assert(taskCommandsList != null && taskIDsList != null);
                for (int i = 0; i < taskCommandsList.length; i++) 
                        tasksList.add(new Task(jobID, Integer.parseInt(taskIDsList[i]), taskCommandsList[i]));
                
                //StatsLog.writeToLog("Accepted job #" + AtomicCounter.increment() + " - number of tasks: " + tasksList.size());
        }
        return tasksList;
    }
}

