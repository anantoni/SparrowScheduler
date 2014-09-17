/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package httpscheduler;

import utils.WorkerManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anantoni
 */
public class UpdateWorkerStatusThread extends Thread {
        // Periodically update worker status
        @Override
        public void run() {
                while (true) {
                        try {
                                Thread.sleep(30000);
                        } catch (InterruptedException ex) {
                                Logger.getLogger(UpdateWorkerStatusThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        WorkerManager.getWriteLock().lock();
                        WorkerManager.updateWorkerStatus();
                        WorkerManager.getWriteLock().unlock();
                }
        }
}
