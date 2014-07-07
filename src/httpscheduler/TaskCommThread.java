/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.pool.BasicPoolEntry;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author anantoni
 */
class TaskCommThread extends Thread {

            private final Task task;
            private final HttpScheduler s;
            private final Map<Integer, String[]> jobMap;
            
            TaskCommThread(Task task, HttpScheduler s, Map<Integer, String[]> jobMap) {
                super();
                this.s = s;
                this.task = task;
                this.jobMap = jobMap;
            }

            @Override
            public void run() {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    // TODO: enforce scheduling policy
                    task.setResult(s.sendTask("http://localhost:8080/", String.valueOf( task.getTaskID() ), task.getCommand()));
                } catch (Exception ex) {
                    Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                String[] resultArray = jobMap.get(task.getJobID());
                resultArray[task.getTaskID()] = task.getResult();
            }
        };


//                ConnectionReuseStrategy connStrategy = DefaultConnectionReuseStrategy.INSTANCE;
//                try {
//                    Future<BasicPoolEntry> future = pool.lease(this.target, null);
//
//                    boolean reusable = false;
//                    BasicPoolEntry entry = future.get();
//                    try {
//                        HttpClientConnection conn = entry.getConnection();
//                        HttpCoreContext coreContext = HttpCoreContext.create();
//                        coreContext.setTargetHost(this.target);
//
//                        BasicHttpRequest request = new BasicHttpRequest("GET", "/");
//                        System.out.println(">> Request URI: " + request.getRequestLine().getUri());
//
//                        httpexecutor.preProcess(request, httpproc, coreContext);
//                        HttpResponse response = httpexecutor.execute(request, conn, coreContext);
//                        httpexecutor.postProcess(response, httpproc, coreContext);
//
//                        System.out.println("<< Response: " + response.getStatusLine());
//                        System.out.println(EntityUtils.toString(response.getEntity()));
//
//                        reusable = connStrategy.keepAlive(response, coreContext);
//                    } catch (IOException ex) {
//                        throw ex;
//                    } catch (HttpException ex) {
//                        throw ex;
//                    } finally {
//                        if (reusable) {
//                            System.out.println("Connection kept alive...");
//                        }
//                        pool.release(entry, reusable);
//                    }
//                } catch (Exception ex) {
//                    System.out.println("Request to " + this.target + " failed: " + ex.getMessage());
//                }
