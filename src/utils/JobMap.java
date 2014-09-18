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

/**
 *
 * @author anantoni
 */
public class JobMap {
    Map<Integer, BlockingQueue<Task>> jobMap;
    
    public JobMap() {
        jobMap = new LinkedHashMap<>();
    }
    
    public synchronized int putJob(ArrayList<Task> tasksList) {
        int jobID = AtomicCounter.increment();
        BlockingQueue tasksQueue = new LinkedBlockingQueue<>();
        tasksQueue.addAll(tasksList);
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

    public synchronized BlockingQueue<Task> getTaskQueue(int jobID) {
        return jobMap.get(jobID);
    }
    
}
