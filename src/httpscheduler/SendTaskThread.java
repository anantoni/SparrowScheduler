///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package httpscheduler;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.http.HttpEntity;
//import org.apache.http.NameValuePair;
//import org.apache.http.ParseException;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.BasicHttpContext;
//import org.apache.http.protocol.HttpContext;
//import org.apache.http.util.EntityUtils;
//import utils.HttpComm;
//import utils.Task;
//
///**
// *
// * @author anantoni
// */
//public class SendTaskThread extends Thread {
//        //private final CloseableHttpClient httpClient;
//        private final HttpContext context;
//        private final String workerURL;
//        private final Task task;
//
//        public SendTaskThread (Task task, String workerURL) {
//            //this.httpClient = (CloseableHttpClient) HttpComm.getHttpClient();
//            this.context = new BasicHttpContext();
//            this.task  = task;
//            this.workerURL = workerURL;
//        }
//
//        /**
//         * Executes the GetMethod and prints some status information.
//         */
//        @Override
//        public void run() {
//            Map<String, String> postArguments = new LinkedHashMap();
//            postArguments.put( "job-id", String.valueOf(task.getJobID()));
//             postArguments.put( "task-id", String.valueOf(task.getTaskID()));
//            postArguments.put( "task-command", task.getCommand());
//            
//            try {
//                HttpPost httpPost = new HttpPost( workerURL );
//                List <NameValuePair> nvps = new ArrayList <>();
//                postArguments.keySet().stream().forEach((key) -> { 
//                    nvps.add( new BasicNameValuePair( key, postArguments.get(key) ) );
//                });
//            
//                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//                CloseableHttpResponse response = httpClient.execute(httpPost, context);
//                try {
//                    HttpEntity entity2 = response.getEntity();
//                    String s = EntityUtils.toString(entity2);
//                    EntityUtils.consume(entity2);
//                } finally {
//                    response.close();
//                }
//            } catch (IOException | ParseException e) {
//                System.out.println("Error " + e);
//            }
//        }
//}
//
