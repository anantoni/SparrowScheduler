/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anantoni
 */
public class JobMap {
    Map<Integer, BlockingQueue<Task>> jobMap;
    
    public JobMap() {
        jobMap = new LinkedHashMap<>();
    }
    
    public synchronized int putJob(int taskQuantity, int taskDuration) {
        int jobID = AtomicCounter.increment();
        BlockingQueue tasksQueue = new LinkedBlockingQueue<>();
        for (int i =0; i<taskQuantity; i++) {
            try {
                tasksQueue.put(new Task(taskDuration));
            } catch (InterruptedException ex) {
                Logger.getLogger(JobMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        jobMap.put(jobID, tasksQueue);
        
        return jobID;
    }
    
    public synchronized int putTask(int jobID, Task task) {
        jobMap.get(jobID).add(task);
        return 0;
    }
    
    public synchronized void removeJob(int jobID) {
        jobMap.remove(jobID);
    }

    public synchronized Task getTask(int jobID) {
        if (jobMap.get(jobID).isEmpty())
            return null;
        return jobMap.get(jobID).remove();
    }
    
}
