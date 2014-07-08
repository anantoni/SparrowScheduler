/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

/**
 *
 * @author anantoni
 */
public class Task {
    private final int jobID;
    private final int taskID;
    private final String taskCommand;
    private String taskResult;
    
    public Task(int jobID, int taskID, String taskCommand) {
        this.jobID = jobID;
        this.taskID = taskID;
        this.taskCommand = taskCommand;
    }
    
    public void setResult(String taskResult) {
        this.taskResult = taskResult;
    }
    
    public String getResult() {
        return this.taskResult;
    }
    
    public int getJobID() {
        return jobID;
    }
    
    public int getTaskID() {
        return taskID;
    }
    
    public String getCommand() {
        return taskCommand;
    }
    
}
