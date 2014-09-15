/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.util.logging.Level;
import java.util.logging.Logger;
import policies.*;

/**
 *
 * @author anantoni
 */
class TaskCommThread extends Thread {

    private final Task task;
    private final SchedulingPolicy policy;
    //private final Map<Integer, String[]> jobMap;

    TaskCommThread(Task task, SchedulingPolicy policy) {
        super();
        this.task = task;
        this.policy = policy;
        //this.jobMap = jobMap;
    }

    @Override
    public void run() {
        String workerURL = policy.selectWorker();
        System.out.println(workerURL);
//        try {
//            Thread.sleep(2000L);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
//        }
        try {
                 task.setResult(HttpComm.sendTask(workerURL, String.valueOf( task.getTaskID() ), task.getCommand()));
        } catch (Exception ex) {
                Logger.getLogger(TaskCommThread.class.getName()).log(Level.SEVERE, null, ex);
        }
//        String[] resultArray = jobMap.get(task.getJobID());
//        resultArray[task.getTaskID()] = task.getResult();
    }
};

