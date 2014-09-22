 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package httpscheduler;

import utils.WorkerManager;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import javax.net.ssl.SSLServerSocketFactory;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.protocol.HttpService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thomas
 */

    class GenericRequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connFactory;
        private final ServerSocket serversocket;
        private final HttpService httpService;
        private final ExecutorService connectionHandlerExecutor;
        
        public GenericRequestListenerThread(
                final int port,
                final HttpService httpService,
                final SSLServerSocketFactory sf) throws IOException {
            
                this.connFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
                this.serversocket = sf != null ? sf.createServerSocket(port) : new ServerSocket(port);
                this.httpService = httpService;
                // only 4 connections can run concurrently
                connectionHandlerExecutor = Executors.newFixedThreadPool(4);
                System.out.println("Request Listener Thread created");
        }

        @Override
        public void run() {
                System.out.println("Listening on port " + this.serversocket.getLocalPort());
                ArrayList<String> workerList = new ArrayList<>();
            
                // Read list of workers from configuration file
                try(BufferedReader br = new BufferedReader(new FileReader( "./config/workers.conf" ))) {
                        for(String line; (line = br.readLine()) != null; ) {
                                workerList.add(line);
                        }
                } catch (FileNotFoundException ex) {
                        Logger.getLogger(GenericRequestListenerThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                        Logger.getLogger(GenericRequestListenerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
        
                // Initialize worker manager
                try {
                        WorkerManager.useWorkerList(workerList);
                } catch (Exception ex) {
                        Logger.getLogger(GenericRequestListenerThread.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(-1);
                }
                WorkerManager.printWorkerMap();

                Thread workerStatusThread = new UpdateWorkerStatusThread();
                workerStatusThread.start();
                System.out.println("ready for connections");
                while (!Thread.interrupted()) {
                        try {
                                // Set up HTTP connection
                                Socket socket = this.serversocket.accept();
                                System.out.println("Incoming connection from " + socket.getInetAddress());
                                HttpServerConnection conn = this.connFactory.createConnection(socket);

                                // Initialize the pool
                                Thread connectionHandler = new ConnectionHandlerThread(this.httpService, conn);             
                                connectionHandler.setDaemon(true);
                                //connectionHandler.setDaemon(true);
                                connectionHandler.start();
                                //connectionHandlerExecutor.execute(connectionHandler);
                                //System.out.println("\tConnection Handler Thread created");
                        } catch (InterruptedIOException ex) {
                                break;
                        } catch (IOException e) {
                                System.err.println("I/O error initialising connection thread: "
                                    + e.getMessage());
                            break;
                        }
                }
                // when the listener is interupted shutdown the pool
                // and wait for any Connection Handler threads still running
                connectionHandlerExecutor.shutdown();
                while (!connectionHandlerExecutor.isTerminated()) {}

                System.out.println("Finished all connection handler threads");            
        }
    }
