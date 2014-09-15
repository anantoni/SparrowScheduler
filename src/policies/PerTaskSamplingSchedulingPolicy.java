/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package policies;

import httpscheduler.HttpComm;
import httpscheduler.WorkerManager;
import java.util.ArrayList;
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
        String workerURL = "";
        String workerURL1 = "";
        String result = "";
        String result1 = "";
        
        Random random    = new Random();
        List<String> keys  = new ArrayList<>(workerMap.keySet());
        
        // Select a random active worker
        do {
                workerURL = keys.get( random.nextInt(keys.size()) );           
        } while (workerMap.get(workerURL).equals("DOWN"));
        
        // Select a second random active worker, different from the first one
        do {
                workerURL1 = keys.get( random.nextInt(keys.size()) );           
        } while (workerMap.get(workerURL1).equals("DOWN") || workerURL.equals(workerURL1));
        
        WorkerManager.getReadLock().unlock();
        try {
                result = HttpComm.probe(workerURL);
                System.out.println("First worker: " + result);
        } catch (Exception ex) {
            Logger.getLogger(PerTaskSamplingSchedulingPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            result1 = HttpComm.probe(workerURL1);
            System.out.println("Second worker: " + result1);
        } catch (Exception ex) {
            Logger.getLogger(PerTaskSamplingSchedulingPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
        assert result.matches("[0-9]+");
        assert result1.matches("[0-9]+");
        //Integer.parseInt(result);
        
        return Integer.parseInt(result) > Integer.parseInt(result1) ? workerURL : workerURL1;
    }
    
}
