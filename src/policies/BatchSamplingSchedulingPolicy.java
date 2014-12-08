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
import utils.ProbePair;
import utils.WorkerManager;

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
        public List<ProbePair> selectBatchWorker(int taskListSize) {
                WorkerManager.getReadLock().lock();
                List results = null;
                List workerURLs = new LinkedList<>();
                List toBeProbed = new LinkedList<>();
                workerURLs.addAll(WorkerManager.getWorkerMap().keySet());

//                // if #tasks >= #workers 
//                if (taskListSize * 2 >= WorkerManager.getWorkerNumber()) {
//                        // add all workers for multiprobe
//                        toBeProbed.addAll(workerURLs);
//                }
//                // else
//                else {
//                        //multiprobe d * #tasks where d = 2;
//                        for (int i=0; i< 2 * taskListSize - 1; i++) {
//                                Collections.shuffle(workerURLs);
//                                toBeProbed.add(workerURLs.get(0));
//                        }
//                }
                // select d*m workers to be probed
                for (int i = 0; i < taskListSize; i++) {
                    Collections.shuffle(workerURLs);
                    toBeProbed.add(workerURLs.get(0));
                    toBeProbed.add(workerURLs.get(1));
                }

                // Execute multiprobe
                try {
                        results =  HttpComm.multiProbe(toBeProbed);
                } 
                catch (Exception ex) {
                        Logger.getLogger(BatchSamplingSchedulingPolicy.class.getName()).log(Level.SEVERE, null, ex);
                }

                WorkerManager.getReadLock().unlock();
                return results.subList(0, taskListSize);
        }

        public void setSelectedWorker(String workerURL) {
                selectedWorker = workerURL;
        }
    
}
