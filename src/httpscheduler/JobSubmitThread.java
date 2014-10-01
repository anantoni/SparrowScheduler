package httpscheduler;

import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import policies.BatchSamplingSchedulingPolicy;
import policies.SchedulingPolicy;
import utils.HttpComm;
import utils.Job;
import utils.ProbePair;
import utils.WorkerManager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author anantoni
 */
public class JobSubmitThread implements Runnable{
    private final Job job;
    private final BatchSamplingSchedulingPolicy policy;

    public JobSubmitThread(Job job, SchedulingPolicy policy) {
        this.job = job;
        this.policy = (BatchSamplingSchedulingPolicy) policy;
    }
    
    @Override
    public void run() {
        List<ProbePair> results = policy.selectBatchWorker(job.getTaskQuantiy());
        String workerURL ="";
        try {
            //HttpComm.sendJob(workerURL, String.valueOf(job.getTaskDuration()), String.valueOf(job.getTaskQuantiy()));
            for (ProbePair pair : results) {
                workerURL = pair.getWorkerURL();
                HttpComm.sendTask(workerURL, String.valueOf(job.getTaskDuration()));
            }
            Date dNow = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
            //StatsLog.writeToLog(ft.format(dNow) + " Thread #" + Thread.currentThread().getId() + " Sending job #" + task.getJobID() + " task #" + task.getTaskID() + " to worker: " + workerURL);
        } catch ( HttpHostConnectException | NoHttpResponseException ex) {
            WorkerManager.getWriteLock().lock();
            WorkerManager.getWorkerMap().put(workerURL, "DOWN");
            WorkerManager.getWriteLock().unlock();
            Logger.getLogger(TaskSubmitThread.class.getName()).log(Level.SEVERE, null, ex);
            //workerDown = true;
        } 
        catch ( SocketException ex) {
            WorkerManager.getWriteLock().lock();
            WorkerManager.getWorkerMap().put(workerURL, "DOWN");
            WorkerManager.getWriteLock().unlock();
            Logger.getLogger(TaskSubmitThread.class.getName()).log(Level.SEVERE, null, ex);
            //workerDown = true;
        } catch (Exception ex) {
            Logger.getLogger(TaskSubmitThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
