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
public class Job {
    private final int taskDuration;
    private final int taskQuantity;
    
    public Job(int taskDuration, int taskQuantity) {
        this.taskQuantity = taskQuantity;
        this.taskDuration = taskDuration;
    }
    
    public int getTaskDuration() {
        return taskDuration;
    }
    
    public int getTaskQuantiy() {
        return taskQuantity;
    }
    
}
