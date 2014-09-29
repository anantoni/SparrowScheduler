/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpscheduler;

import utils.Task;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
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
import utils.JobMap;
import static utils.HttpParser.parseLateBindingRequest;
import utils.StatsLog;

/**
 *
 * @author anantoni
 */
public class LateBindingRequestHandler implements HttpRequestHandler {
    private final ThreadPoolExecutor taskCommExecutor;         
    private final JobMap jobMap;

    // Pass reference to the requestsQueue to the GenericRequestHandler
    public LateBindingRequestHandler(ThreadPoolExecutor taskCommExecutor, JobMap jobMap) {
            super();
            this.taskCommExecutor = taskCommExecutor;
            this.jobMap = jobMap;
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

        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
            String entity = EntityUtils.toString(httpEntity);
            //System.out.println("Incoming entity content (string): " + entity);

            // Parse HTTP request
            String parseResult = parseLateBindingRequest(entity, jobMap);

            StringEntity stringEntity = new StringEntity("");
            // if new job received from a client, start executing late binding policy
            if (parseResult.contains("new-job")) {
                response.setStatusCode(HttpStatus.SC_OK);
                String pieces[] = parseResult.split(":");
                taskCommExecutor.execute(new LateBindingProbeThread(Integer.parseInt(pieces[1]), 
                                                                                                                        Integer.parseInt(pieces[2])));
                response.setEntity(new StringEntity("result:success"));
            }
            // else if probe response from worker, handle it accordingly
            else if (parseResult.contains("probe-response")) {
                StatsLog.writeToLog("received probe-response");
                response.setStatusCode(HttpStatus.SC_OK);
                String[] pieces = parseResult.split(":");
                int jobID = Integer.parseInt(pieces[1]);
              
                Task task = jobMap.getTask(jobID);
                // send NOOP if task queue empty for specified job
                if (task == null) {
                    response.setStatusCode(HttpStatus.SC_OK);
                    try {
                        stringEntity = new StringEntity("NOOP");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(LateBindingTaskSubmitThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Responding with NOOP");
                }
                // else send job id, task id and task commmand to worker
                else {
                    try {
                        stringEntity = new StringEntity(String.valueOf(task.getDuration())) ;
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(LateBindingTaskSubmitThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                     System.out.println("Responding with task duration: " + task.getDuration());
                }
                 response.setEntity(stringEntity);    
            }
        }
    }
}
