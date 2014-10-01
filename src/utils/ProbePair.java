/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

/**
 *
 * @author anantoni
 */
public class ProbePair {
    String workerURL;
    int probeResult;
    
    public ProbePair(String workerURL, int probeResult) {
        this.workerURL = workerURL;
        this.probeResult = probeResult;
    }
    
    public int getProbeResult() {
        return probeResult;
    }

    public String getWorkerURL() {
        return workerURL;
    }
}
