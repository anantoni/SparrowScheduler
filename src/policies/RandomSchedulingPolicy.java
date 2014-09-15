/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package policies;

import httpscheduler.WorkerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author anantoni
 */
public class RandomSchedulingPolicy implements SchedulingPolicy {
            
    @Override
    public void setWorkerManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String selectWorker() {
        WorkerManager.getReadLock().lock();
        Map<String,String> workerMap = WorkerManager.getWorkerMap();
        String workerURL = "";
        
        do {
                Random random    = new Random();
                List<String> keys  = new ArrayList<>(workerMap.keySet());
                workerURL = keys.get(random.nextInt(keys.size()));      
        } while (workerMap.get(workerURL).equals("DOWN"));
        WorkerManager.getReadLock().unlock();
        System.out.println( "Random scheduling policy: " + workerURL);
        return workerURL;
    }
}
