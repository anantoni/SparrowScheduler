/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpscheduler;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import utils.Task;

/**
 *
 * @author anantoni
 */
public class LateBindingTaskSubmitThread implements Runnable{
    HttpResponse response;
    BlockingQueue taskQueue;
    
    public LateBindingTaskSubmitThread(HttpResponse response, BlockingQueue<Task> taskQueue) {
        this.response = response;
        this.taskQueue = taskQueue;
    }
    
    @Override
    public void run() {
        StringEntity stringEntity = null;
        response.setStatusCode(HttpStatus.SC_OK);
        System.out.println("Received probe response.");

        // send NOOP if task queue empty for specified job
        if (taskQueue.isEmpty()) {
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
            Task task = (Task)taskQueue.remove();
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
