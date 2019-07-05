/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */
package server;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.logging.Logger;

import functions.City;
import functions.Constants;
import logTool.allLogger;
import remoteObject.EventSystemImplementation;
import remoteObject.EventSystemInterface;
public class TOR_Server {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * @param args
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {

		EventSystemInterface stub = new EventSystemImplementation("TOR");
		try {
			setupLogging();
			// bind the remote object in the registry
			try {
				LocateRegistry.createRegistry(1097);
				           } catch (ExportException e) {
				        	   System.out.println("Create registry for Toronto failed!");
				           }
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(City.TOR.toString(), stub);

		} catch (Exception e) {
			// TODO - catch only the specific exception
			e.printStackTrace();
		}

		// start the city's UDP server for inter-city communication
		// the UDP server is started on a new thread 
		new Thread(() -> {
			((EventSystemImplementation) stub).UDPServer();
		}).start();
		System.out.println("Toronto server started!");
	}
	
	/**
	 * Logging setup for TOR server
	 * @throws IOException
	 */
	private static void setupLogging() throws IOException {
		File files = new File(Constants.SERVER_LOG_DIRECTORY);
        if (!files.exists()) 
            files.mkdirs(); 
        files = new File(Constants.SERVER_LOG_DIRECTORY+"TOR_Server.log");
        if(!files.exists())
        	files.createNewFile();
        allLogger.setup(files.getAbsolutePath());
	}

}
