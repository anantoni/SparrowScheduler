/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package policies;

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
    
    public void setSelectedWorker(String workerURL) {
            selectedWorker = workerURL;
    }
    
}
