/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author anantoni
 */
public class WorkerManager {
    private final Map<String, String> workerMap;
    
    public WorkerManager(List<String> workerList) throws Exception {
        workerMap = new HashMap<>();
        for (String workerURL :workerList) {
            String result = HttpComm.heartbeat(workerURL);
            if (result.equals("OK")) 
                workerMap.put(workerURL, "OK");
            else 
                workerMap.put(workerURL, "DOWN");
        }
    }
    
    public String getWorkerStatus(String workerURL) {
        return workerMap.get(workerURL);
    }
    
    public Map<String,String> getWorkerMap() {
        return workerMap;
    }
    
}
