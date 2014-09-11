/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

/**
 *
 * @author thomas
 */
class RequestHandler implements HttpRequestHandler  {
    private final ThreadPoolExecutor taskCommExecutor;         

    // Pass reference to the requestsQueue to the RequestHandler
    public RequestHandler(ThreadPoolExecutor taskCommExecutor) {
        super();
        this.taskCommExecutor = taskCommExecutor;
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
                        ArrayList <Task> tasksList = parseHttpClientRequest(entity);

                        Future threadMonitor = null;
                        for (Task taskToProcess : tasksList) {
                            Thread taskCommExecutorThread = new TaskCommThread(taskToProcess);
                            threadMonitor = taskCommExecutor.submit(taskCommExecutorThread);
                        }
               
// ---------> For Tom: Why do we need this?
                        try {
                                // the main thread should wait until the submitted thread
                                // finishes its computation
                                threadMonitor.get();
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }

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
                System.out.println("Decoded request: " + result);
        } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
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
                        System.out.println("i'm in!");
                        taskIDsList = keyValuePair[1].split(",");
                }
                else {
                        System.err.println("Invalid argument - expecting task-ids");
                }
                assert(taskCommandsList != null && taskIDsList != null);
                for (int i = 0; i < taskCommandsList.length; i++) {
                        tasksList.add(new Task(jobID, Integer.parseInt(taskIDsList[i]), taskCommandsList[i]));
                }
            
        }
        return tasksList;
    }
}

