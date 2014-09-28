/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anantoni
 */
public class HttpParser {
    public static ArrayList<Task> parseGenericHttpClientRequest(String httpRequest) {
        ArrayList<Task> tasksList = new ArrayList<>();
        String result = "";
        try {
                result = java.net.URLDecoder.decode(httpRequest, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(HttpParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] requestArguments = result.split("&");
        
        if (requestArguments.length != 2) {
                System.err.println("Invalid HTTP request: " + result);
                return null; 
        }
        else if (requestArguments.length == 2) {
            int taskDuration = -1;
            int taskQuantity = -1;

            String[] keyValuePair = requestArguments[0].split("=");
            if ( keyValuePair[0].equals("task-duration") ) {
                assert keyValuePair[1].matches("[0-9]+");
                taskDuration = Integer.parseInt(keyValuePair[1]);
            }
            else 
                    System.err.println("Invalid argument - task duration");

            keyValuePair = requestArguments[1].split("=");
            if ( keyValuePair[0].equals("task-quantity") ) {
                assert keyValuePair[1].matches("[0-9]+");
                taskQuantity = Integer.parseInt(keyValuePair[1]);
            }
            else 
                System.err.println("Invalid argument - task quantity");

            for (int i = 0; i < taskQuantity; i++) 
                tasksList.add(new Task(taskDuration));

            //StatsLog.writeToLog("Accepted job #" + AtomicCounter.increment() + " - number of tasks: " + tasksList.size());
        }
        return tasksList;
    }
    
     public static Job parseBatchSamplingHttpClientRequest(String httpRequest) {        
         Job job = null;
        String result = "";
        try {
                result = java.net.URLDecoder.decode(httpRequest, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(HttpParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] requestArguments = result.split("&");
        
        if (requestArguments.length != 2) {
                System.err.println("Invalid HTTP request: " + result);
                return null; 
        }
        else if (requestArguments.length == 2) {
            int taskDuration = -1;
            int taskQuantity = -1;

            String[] keyValuePair = requestArguments[0].split("=");
            if ( keyValuePair[0].equals("task-duration") ) {
                assert keyValuePair[1].matches("[0-9]+");
                taskDuration = Integer.parseInt(keyValuePair[1]);
            }
            else 
                    System.err.println("Invalid argument - task duration");

            keyValuePair = requestArguments[1].split("=");
            if ( keyValuePair[0].equals("task-quantity") ) {
                assert keyValuePair[1].matches("[0-9]+");
                taskQuantity = Integer.parseInt(keyValuePair[1]);
            }
            else 
                System.err.println("Invalid argument - task quantity");

            job = new Job(taskDuration, taskQuantity);

            //StatsLog.writeToLog("Accepted job #" + AtomicCounter.increment() + " - number of tasks: " + tasksList.size());
        }
        return job;
    }
}
