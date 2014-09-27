/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package policies;

import utils.HttpComm;
import utils.WorkerManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anantoni
 */
public class PerTaskSamplingSchedulingPolicy implements SchedulingPolicy {

    @Override
    public void setWorkerManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String selectWorker() {
        
        WorkerManager.getReadLock().lock();
        Map<String,String> workerMap = WorkerManager.getWorkerMap();        
        Random random    = new Random();
        List<String> keys  = new ArrayList<>(workerMap.keySet());
        
        if (Collections.frequency(workerMap.values(), "OK") == 1) {
            for (String workerURL : workerMap.keySet()) 
                    if (workerMap.get(workerURL).equals("OK")) 
                        return workerURL;
        }
        else if (Collections.frequency(workerMap.values(), "OK") == 0) {
            System.err.println("CRITICAL: All workers down - Exiting ");
            System.exit(-1);
        }
        // Select a random active worker
        String workerURL = "";
        do {
                workerURL = keys.get( random.nextInt(keys.size()) );           
        } while (workerMap.get(workerURL).equals("DOWN"));
        
        // Select a second random active worker, different from the first one
        String workerURL1 = "";
        do {
                workerURL1 = keys.get( random.nextInt(keys.size()) );           
        } while (workerMap.get(workerURL1).equals("DOWN") || workerURL.equals(workerURL1));
        
        WorkerManager.getReadLock().unlock();
        
        int result = -1;
        try {
            result = Integer.parseInt(HttpComm.probe(workerURL));
            //System.out.println("Worker " + workerURL + ": " + result);
        } catch (Exception ex) {
            Logger.getLogger(PerTaskSamplingSchedulingPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (result == 0)
            return workerURL;
        
        int result1 = -1;
        try {
            result1 = Integer.parseInt(HttpComm.probe(workerURL1));
            //System.out.println("Worker " + workerURL1 + ": " + result1);
        } catch (Exception ex) {
            Logger.getLogger(PerTaskSamplingSchedulingPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (result1 == 0)
            return workerURL1;
        
        return result > result1 ? workerURL1 : workerURL;
        
    }   

    @Override
    public String selectBatchWorker(int size) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
