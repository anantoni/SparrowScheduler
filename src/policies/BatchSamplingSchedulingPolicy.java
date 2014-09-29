/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package policies;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.HttpComm;
import utils.WorkerManager;

/**
 *
 * @author anantoni
 */
/**
 *
 * @author anantoni
 */
public class BatchSamplingSchedulingPolicy implements SchedulingPolicy {
        String selectedWorker;

        @Override
        public void setWorkerManager() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public String selectWorker() {
                return selectedWorker;
        }

        @Override
        public String selectBatchWorker(int taskListSize) {
                WorkerManager.getReadLock().lock();
                Map<String, String> results = null;
                List workerURLs = new LinkedList<>();
                List toBeProbed = new LinkedList<>();
                workerURLs.addAll(WorkerManager.getWorkerMap().keySet());

                // if #tasks >= #workers 
                if (taskListSize * 2 >= WorkerManager.getWorkerNumber()) {
                        // add all workers for multiprobe
                        toBeProbed.addAll(workerURLs);
                }
                // else
                else {
                        //multiprobe d * #tasks where d = 2;
                        for (int i=0; i< 2 * taskListSize - 1; i++) {
                                Collections.shuffle(workerURLs);
                                toBeProbed.add(workerURLs.get(0));
                        }
                }

                // Execute multiprobe
                try {
                        results =  HttpComm.multiProbe(toBeProbed);
                        for (String url : results.keySet())
                                System.out.println("Worker url: " + url + " - probe result: " + results.get(url));
                } 
                catch (Exception ex) {
                        Logger.getLogger(BatchSamplingSchedulingPolicy.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Find the key of the minimum probe result
                Map.Entry<String, String> min = null;
                for (Map.Entry<String, String> entry : results.entrySet()) {
                        if (min == null || Integer.parseInt(min.getValue()) > Integer.parseInt(entry.getValue())) {
                            min = entry;
                        }
                }

//                System.out.println( "Least loaded worker: " + min.getKey());
                WorkerManager.getReadLock().unlock();
                return min.getKey();
        }

        public void setSelectedWorker(String workerURL) {
                selectedWorker = workerURL;
        }
    
}
