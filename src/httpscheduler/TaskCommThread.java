/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

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
class TaskCommThread extends Thread {

    private final Task task;
    private final SchedulingPolicy policy, backupPolicy;
    //private final Map<Integer, String[]> jobMap;

    TaskCommThread(Task task, SchedulingPolicy policy) {
        super();
        this.task = task;
        this.policy = policy;
        this.backupPolicy = new PerTaskSamplingSchedulingPolicy();
        //this.jobMap = jobMap;
    }

    @Override
    public void run() {
        boolean workerDown = false;
        String workerURL = policy.selectWorker();
        System.out.println(workerURL);

        try {
                 task.setResult(HttpComm.sendTask(workerURL, String.valueOf( task.getTaskID() ), task.getCommand()));
        } catch ( HttpHostConnectException | NoHttpResponseException ex) {
                WorkerManager.getWriteLock().lock();
                WorkerManager.getWorkerMap().put(workerURL, "DOWN");
                WorkerManager.getWriteLock().unlock();
                Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
                workerDown = true;
        } 
        catch ( SocketException ex) {
                WorkerManager.getWriteLock().lock();
                WorkerManager.getWorkerMap().put(workerURL, "DOWN");
                WorkerManager.getWriteLock().unlock();
                Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
                workerDown = true;
        } catch (Exception ex) {
                Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // This part is executed only if the worker selected by the primary policy goes down
        // It is guaranteed that as long as worker is up the task will be completed eventually
        while (workerDown == true) {
                try {
                        workerURL = backupPolicy.selectWorker();
                        task.setResult(HttpComm.sendTask(workerURL, String.valueOf( task.getTaskID() ), task.getCommand()));
                        workerDown = false;
                } catch (Exception ex) {
                        Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
//        String[] resultArray = jobMap.get(task.getJobID());
//        resultArray[task.getTaskID()] = task.getResult();
    }
};

