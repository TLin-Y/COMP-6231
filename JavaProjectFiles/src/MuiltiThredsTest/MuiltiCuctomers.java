/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */

package MuiltiThredsTest;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import client.User;
import functions.City;
import functions.Constants;
import functions.EventType;
import functions.FuntionMembers;
import logTool.allLogger;
import remoteObject.EventSystemImplementation;
import remoteObject.EventSystemInterface;
/**
 * This class implements <code>Runnable</code> so that each customer login can be
 * handled on a separate thread. 
 */

public class MuiltiCuctomers implements Runnable{
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	User user;
	Scanner input;
	int no;
	String eID;
	int typeN;
	EventSystemInterface stub;

	/**
	 * customers constructor to initialize its object
	 * @param user
	 */
	public MuiltiCuctomers(User user,int no,String eID,int typeN) {
		this.user = user;
		input = new Scanner(System.in);
		this.no = no;
		this.eID = eID;
		this.typeN = typeN;
	}

	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		// get the registry
		Registry registry;

		try {
			setupLogging();
			LOGGER.info("Client LOGIN(" + user + ")");
			registry = LocateRegistry.getRegistry(null);//Get local host
			stub = (EventSystemInterface) registry.lookup(user.getcity().toString());
			//System.out.println("====================");
			
			//------------------------------Book some event for target user-------------------------------
			performOperations(this.no,this.eID,this.typeN);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void performOperations(int no,String eID,int typenum) throws RemoteException {

		int userSelection = no; 
		String evenId,type = null;
		int typen;
		EventType eventtype;
		SimpleEntry<Boolean, String> result;

			switch (userSelection) {
			case 1:
				//System.out.print("Enter the Event ID (eg. MTLA100519,TORE092319,...) : ");
				evenId = eID.toUpperCase();
				result = FuntionMembers.validateEvent(evenId.trim(),null,null);
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				//System.out.print("Enter EventType(1.Conferences|2.Seminars|3.TradeShows) : ");
				typen = typenum;
				if (typen==1) {
					type = "Conferences";
				}
				if (typen==2) {
					type = "Seminars";
				}
				if (typen==3) {
					type = "TradeShows";
				}
				result = FuntionMembers.validateType(type.trim());
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}else {
					eventtype = EventType.valueOf(type.toUpperCase());
				}
				//System.out.println((user.toString()+ evenId+eventtype.toString()));
				result = stub.bookevent(user.toString(), evenId, eventtype.toString());
				
				LOGGER.info(String.format(Constants.LOG_MSG, "BookEvent", Arrays.asList(user, evenId, eventtype),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS - " + result.getValue() + " || Customer ID: " + this.user);
				else
					System.out.println("FAILURE - " + result.getValue() + " || Customer ID: " + this.user);

				break;

			case 2:
				HashMap<String, ArrayList<String>> eventList = stub.getbookingSchedule(user.toString());

				LOGGER.info(String.format(Constants.LOG_MSG, "getBookingSchedule", Arrays.asList(user),
						eventList != null, eventList));
				if (eventList != null)
					{System.out.print("Customer: "+this.user+"  Has booked: ");
					System.out.println(eventList);}
				else
					System.out.println("There was some problem in getting the event schedule. Please try again later.");

				break;
			case 3:
				//System.out.print("Enter the Event ID to drop : ");
				evenId = eID.toUpperCase();
				result = FuntionMembers.validateEvent(evenId.trim(),null,null);
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				result = stub.dropevent(user.toString(), evenId);

				LOGGER.info(String.format(Constants.LOG_MSG, "dropEvent", Arrays.asList(user, evenId),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS -" + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 4:
				break;
			default:
				System.out.println("Please select a valid operation.");
				break;
			}

		}


	/**
	 * Configures the logger
	 * @throws IOException
	 */
	private void setupLogging() throws IOException {
		File files = new File(Constants.CUSTOMER_LOG_DIRECTORY);
		if (!files.exists())
			files.mkdirs();
		files = new File(Constants.CUSTOMER_LOG_DIRECTORY + user + ".log");
		if (!files.exists())
			files.createNewFile();
		allLogger.setup(files.getAbsolutePath());
	}

	
}




