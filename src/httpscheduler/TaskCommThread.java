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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import policies.*;
import utils.StatsLog;

/**
 *
 * @author anantoni
 */
public class TaskCommThread extends Thread {

    private final Task task;
    private final String workerURL;
    //private final SchedulingPolicy policy, backupPolicy;

    TaskCommThread(Task task, String workerURL) {
        super();
        this.task = task;
        this.workerURL = workerURL;
        //this.policy = policy;
        //this.backupPolicy = new PerTaskSamplingSchedulingPolicy();
    }
    
    
    @Override
    public void run() {
        //boolean workerDown = false;
        //String workerURL = policy.selectWorker();
        //System.out.println(workerURL);
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        StatsLog.writeToLog(ft.format(dNow) + " Thread #" + Thread.currentThread().getId() + " started");
        try {
            task.setResult(HttpComm.sendTask(workerURL, String.valueOf(task.getJobID()), String.valueOf(task.getTaskID()), task.getCommand()));
            dNow = new Date( );
            ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
            StatsLog.writeToLog(ft.format(dNow) + " Thread #" + Thread.currentThread().getId() + " Sending job #" + task.getJobID() + " task #" + task.getTaskID() + " to worker: " + workerURL);
        } catch ( HttpHostConnectException | NoHttpResponseException ex) {
            WorkerManager.getWriteLock().lock();
            WorkerManager.getWorkerMap().put(workerURL, "DOWN");
            WorkerManager.getWriteLock().unlock();
            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
            //workerDown = true;
        } 
        catch ( SocketException ex) {
            WorkerManager.getWriteLock().lock();
            WorkerManager.getWorkerMap().put(workerURL, "DOWN");
            WorkerManager.getWriteLock().unlock();
            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
            //workerDown = true;
        } catch (Exception ex) {
            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // This part is executed only if the worker selected by the primary policy goes down
        // It is guaranteed that as long as worker is up the task will be completed eventually
//        while (workerDown == true) {
//            try {
//                    workerURL = backupPolicy.selectWorker();
//                    task.setResult(HttpComm.sendTask(workerURL, String.valueOf(task.getJobID()), String.valueOf( task.getTaskID() ), task.getCommand()));
//                    workerDown = false;
//            } catch (Exception ex) {
//                    Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }
};

