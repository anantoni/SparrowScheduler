/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import utils.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;
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
import static utils.HttpParser.parseBatchSamplingHttpClientRequest;
import static utils.HttpParser.parseGenericHttpClientRequest;
import utils.Job;

/**
 *
 * @author anantoni
 */
class GenericRequestHandler implements HttpRequestHandler  {
        private final ThreadPoolExecutor commExecutor;
        private final String mode;

        // Pass reference to the requestsQueue to the GenericRequestHandler
        public GenericRequestHandler(ThreadPoolExecutor commExecutor, String mode) {
                super();
                this.commExecutor = commExecutor;
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

                    ArrayList <Task> tasksList; 
                    Job job;
                    switch (mode) {
                        case "random":
                        case "per-task":
                            tasksList = parseGenericHttpClientRequest(entity);
                            for (Task taskToProcess : tasksList) {
                                Runnable taskSubmitThread = new TaskSubmitThread(taskToProcess, policy);
                                commExecutor.execute(taskSubmitThread);
                            }   break;
                        case "batch":
                            job = parseBatchSamplingHttpClientRequest(entity);
                            Runnable jobSubmitThread = new JobSubmitThread(job, policy);
                            commExecutor.execute(jobSubmitThread);
                            break;
                    }
                    
                     response.setStatusCode(HttpStatus.SC_OK);
                    stringEntity = new StringEntity("result:success");
                    // Create communication thread
                    //SendTaskThread[] threads = new SendTaskThread[tasksList.size()];
                    //System.out.println("number of send task threads: " + threads.length);                   
                } 
                else{
                    response.setStatusCode(HttpStatus.SC_OK);
                    stringEntity = new StringEntity("result:fail");
                }

                response.setEntity(stringEntity); 
    }
}

