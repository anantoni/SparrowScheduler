/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpscheduler;

import utils.Task;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
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
import utils.AtomicCounter;
import utils.HttpComm;
import utils.JobMap;
import utils.WorkerManager;

/**
 *
 * @author anantoni
 */
public class LateBindingRequestHandler implements HttpRequestHandler {
        private final ThreadPoolExecutor taskCommExecutor;         
        private final JobMap jobMap;
        private final AtomicCounter jobCounter;

        // Pass reference to the requestsQueue to the RequestHandler
        public LateBindingRequestHandler(ThreadPoolExecutor taskCommExecutor, JobMap jobMap, AtomicCounter jobCounter) {
                super();
                this.taskCommExecutor = taskCommExecutor;
                this.jobMap = jobMap;
                this.jobCounter = jobCounter;
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
                        System.out.println("Incoming entity content (string): " + entity);

                        // Parse HTTP request
                        String parseResult = parseHttpClientRequest(entity);
                        
                        if (parseResult.contains("new-job")) {
                                WorkerManager.getReadLock().lock();
                                List<String> results;
                                List workerURLs = new LinkedList<>();
                                List toBeProbed = new LinkedList<>();
                                workerURLs.addAll(WorkerManager.getWorkerMap().keySet());
                                WorkerManager.getReadLock().unlock();

                                String[] pieces = parseResult.split(":");
                                //multiprobe d * #tasks where d = 2;
                                int numberOfProbes = 2 * Integer.parseInt(pieces[2]);
                                for (int i=0; i<numberOfProbes; i++) {
                                        Collections.shuffle(workerURLs);
                                        toBeProbed.add(workerURLs.get(0));
                                }

                                // Execute late binding multiprobe - we expect instant OK responses
                                try {
                                        System.out.println("Needed probes: " + numberOfProbes);
                                        System.out.println("Sending :" + toBeProbed.size() + " probes");
                                        System.out.println("Job id: " + pieces[1]);
                                        results =  HttpComm.lateBindingMultiProbe(toBeProbed, Integer.parseInt(pieces[1]));
                                        for (String result : results)
                                                System.out.println("Late binding probe result: " + result);
                                } 
                                catch (Exception ex) {
                                        Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                        else if (parseResult.contains("probe-response")) {
                                System.out.println("Received probe response.");
                                String[] pieces = parseResult.split(":");
                                BlockingQueue<Task> taskQueue = jobMap.getTaskQueue(Integer.parseInt(pieces[1]));
                                
                                if (taskQueue.isEmpty()) {
                                        response.setStatusCode(HttpStatus.SC_OK);
                                        stringEntity = new StringEntity("NOOP");
                                        System.out.println("Responding with NOOP");
                                }
                                else {
                                        Task task = taskQueue.remove();
                                        stringEntity = new StringEntity(String.valueOf( task.getTaskID() ) + "&" +  task.getCommand());
                                        System.out.println("Responding with task");
                                }
                                response.setEntity(stringEntity); 
                        }
                }
        }

String parseHttpClientRequest(String httpRequest) {
        ArrayList<Task> tasksList = new ArrayList<>();
        String[] taskCommandsList = null;
        String[] taskIDsList = null;
        
        String result = "";
        try {
                result = java.net.URLDecoder.decode(httpRequest, "UTF-8");
                System.out.println("Decoded request: " + result);
        } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] requestArguments = result.split("&");
        
        if (requestArguments.length == 2) {
                int jobID = 0;
                String[] keyValuePair = requestArguments[0].split("=");
                
                assert keyValuePair[0].equals("probe-response");
                keyValuePair = requestArguments[1].split("=");
                assert keyValuePair[0].equals("job-id");
                assert keyValuePair[1].matches("[0-9]+");
                jobID = Integer.parseInt(keyValuePair[1]);
                
                return "probe-response:" + jobID;
        }
        else if (requestArguments.length == 3) {
                int jobID = 0;
                String[] keyValuePair = requestArguments[0].split("=");
                
                if ( keyValuePair[0].equals("job-id") ) {
                        assert keyValuePair[1].matches("[0-9]+");
                        jobID = Integer.parseInt(keyValuePair[1]);
                }
                else {
                        System.err.println("Invalid argument - expecting job-id");
                }
            
                keyValuePair = requestArguments[1].split("=");
                if ( keyValuePair[0].equals("task-commands") ) {
                        taskCommandsList = keyValuePair[1].split(",");
                }
                else {
                        System.err.println("Invalid argument - expecting task commands");
                }
            
                keyValuePair = requestArguments[2].split("=");
                if ( keyValuePair[0].equals("task-ids") ) {
                        taskIDsList = keyValuePair[1].split(",");
                }
                else {
                        System.err.println("Invalid argument - expecting task-ids");
                }
                assert(taskCommandsList != null && taskIDsList != null);
                for (int i = 0; i < taskCommandsList.length; i++) {
                        tasksList.add(new Task(jobID, Integer.parseInt(taskIDsList[i]), taskCommandsList[i]));
                }
                int sJobID = jobMap.putJob(jobCounter, tasksList);
                
                return "new-job:"+ Integer.toString(sJobID) + ":" + tasksList.size();
        }
        else {
                System.err.println("Invalid HTTP request: " + result);
                return "Invalid"; 
        }
    }
}
