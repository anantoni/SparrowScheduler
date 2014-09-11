/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author anantoni
 */
public class WorkerManager {
    private static Map<String, String> workerMap;
    
    public WorkerManager(List<String> workerList) throws Exception {
        workerMap = new HashMap<>();
        for (String workerURL :workerList) {
            String result = HttpComm.heartbeat(workerURL);
            System.out.println(result);
            if (result.equals("result:success")) 
                workerMap.put(workerURL, "OK");
            else 
                workerMap.put(workerURL, "DOWN");
        }
    }
    
    public static void useWorkerList(List<String> workerList) throws Exception {
        workerMap = new HashMap<>();
        for (String workerURL :workerList) {
            String result = HttpComm.heartbeat(workerURL);
            System.out.println("heartbeat: " + result);
            if (result.equals("result:success")) 
                workerMap.put(workerURL, "OK");
            else 
                workerMap.put(workerURL, "DOWN");
        }
    }
    
    public static void printWorkerMap() {
        Set<String> keys = workerMap.keySet();
        for (String key : keys) {
            System.out.println(key);
        }
        
        Collection<String> values = workerMap.values();
        for (String value : values) {
            System.out.println(value);
        }
    }
    
    public static String getWorkerStatus(String workerURL) {
        return workerMap.get(workerURL);
    }
    
    public static Map<String,String> getWorkerMap() {
        return workerMap;
    }
    
}
