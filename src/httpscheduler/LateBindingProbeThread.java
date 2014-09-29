/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpscheduler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.HttpComm;
import utils.WorkerManager;

/**
 *
 * @author anantoni
 */
public class LateBindingProbeThread implements Runnable {
    int jobID = -1;
    int taskQuantity = -1;
    
    public LateBindingProbeThread(int jobID, int taskQuantity) {
        this.jobID = jobID;
        this.taskQuantity = taskQuantity;
    }
    
    @Override
    public void run() {
        WorkerManager.getReadLock().lock();
        List<String> results;
        List workerURLs = new LinkedList<>();
        List toBeProbed = new LinkedList<>();
        workerURLs.addAll(WorkerManager.getWorkerMap().keySet());
        WorkerManager.getReadLock().unlock();

        
        //multiprobe d * #tasks where d = 2;
//        int numberOfProbes = 2 * taskQuantity;
        if (taskQuantity <= workerURLs.size()) {
            Collections.shuffle(workerURLs);
            for (int i=0; i<taskQuantity; i++) 
                toBeProbed.add(workerURLs.get(i));
        }
        else {
            Collections.shuffle(workerURLs);
            int j = 0;
            for (int i=0; i<taskQuantity; i++) {
                toBeProbed.add(workerURLs.get(j));
                j++;
                if (j==workerURLs.size()) {
                    j = 0;
                    Collections.shuffle(workerURLs);
                }
            } 
        }
        
        //toBeProbed.addAll(workerURLs);
        // Execute late binding multiprobe - we expect instant OK responses
        try {
            //System.out.println("Needed probes: " + numberOfProbes);
//            System.out.println("Sending :" + toBeProbed.size() + " probes");
            //System.out.println("Job id: " + jobID);
            results =  HttpComm.lateBindingMultiProbe(toBeProbed, jobID);
//            for (String result : results)
//                    System.out.println("Late binding probe result: " + result);
        } 
        catch (Exception ex) {
            Logger.getLogger(GenericRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
