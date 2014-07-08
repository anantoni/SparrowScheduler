/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author anantoni
 */
public class RandomSchedulingPolicy implements SchedulingPolicy {
    private WorkerManager workerManager = null;
    
    RandomSchedulingPolicy(WorkerManager workerManager) {
        this.workerManager = workerManager;
    }
            
    @Override
    public void setWorkerManager(WorkerManager workerManager) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String selectWorker() {
        Map<String,String> workerMap = workerManager.getWorkerMap();
        String workerURL = "";
        
        do {
            Random random    = new Random();
            List<String> keys  = new ArrayList<>(workerMap.keySet());
            workerURL = keys.get( random.nextInt(keys.size()) );           
        } while (workerMap.get(workerURL).equals("DOWN"));
        
        return workerURL;
    }
    
}
