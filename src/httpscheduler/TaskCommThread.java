/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import utils.WorkerManager;
import utils.Task;
import utils.HttpComm;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import policies.*;

/**
 *
 * @author anantoni
 */
public class TaskCommThread extends Thread {

    private final Task task;
    private final SchedulingPolicy policy;

    TaskCommThread(Task task, SchedulingPolicy policy) {
        super();
        this.task = task;
        this.policy = policy;
    }
    
    @Override
    public void run() {
        String workerURL = policy.selectWorker();
        
        try {
            HttpComm.sendTask(workerURL, String.valueOf(task.getDuration()));
        } catch ( HttpHostConnectException | NoHttpResponseException ex) {
            WorkerManager.getWriteLock().lock();
            WorkerManager.getWorkerMap().put(workerURL, "DOWN");
            WorkerManager.getWriteLock().unlock();
            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch ( SocketException ex) {
            WorkerManager.getWriteLock().lock();
            WorkerManager.getWorkerMap().put(workerURL, "DOWN");
            WorkerManager.getWriteLock().unlock();
            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
};

