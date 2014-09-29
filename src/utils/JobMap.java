/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anantoni
 */
public class JobMap {
    ConcurrentMap<Integer, BlockingQueue<Task>> jobMap;
    
    public JobMap() {
        jobMap = new ConcurrentHashMap<>();
    }
    
    public int putJob(int taskQuantity, int taskDuration) {
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
    
    public Task getTask(int jobID) {
        if (jobMap.get(jobID).isEmpty())
            return null;
        return jobMap.get(jobID).remove();
    }
    
}
