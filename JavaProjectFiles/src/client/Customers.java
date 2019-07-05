/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */
package client;
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

import functions.City;
import functions.Constants;
import functions.EventType;
import functions.FuntionMembers;
import logTool.allLogger;
import remoteObject.EventSystemImplementation;
import remoteObject.EventSystemInterface;

/**
 * UI operation for Customers, runnable.
 * @author TLIN
 *
 */
public class Customers implements Runnable{
	
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	User user;
	Scanner input;
	EventSystemInterface stub;

	/**
	 * customers constructor to initialize its object
	 * @param user
	 */
	public Customers(User user) {
		this.user = user;
		input = new Scanner(System.in);
	}

	public void run() {

		// get the registry
		Registry registry;

		try {
			setupLogging();//Initialize the log file.
			LOGGER.info("Client LOGIN(" + user + ")");//Record current customer
			registry = LocateRegistry.getRegistry(null);//Get local host
			stub = (EventSystemInterface) registry.lookup(user.getcity().toString());//Generate customer's city stub
			System.out.println("------------------------------------------------");
			Options();//Get UI run
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

	private void Options() throws RemoteException {

		int userSelection = displayMenu(); //Call console println menu
		String evenId,type = null;
		int typen;
		EventType eventtype;
		SimpleEntry<Boolean, String> result;//Save result as key/value
		//When user not quit call 4
		while (userSelection != 4) {

			switch (userSelection) {
			case 1:
				System.out.print("Enter the Event ID (eg. MTLA100519,TORE092319,...) : ");
				evenId = input.next().toUpperCase();
				result = FuntionMembers.validateEvent(evenId.trim(),null,null);//Check event format
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}
				System.out.print("Enter EventType(1.Conferences|2.Seminars|3.TradeShows) : ");
				typen = input.nextInt();
				if (typen==1) {
					type = "Conferences";
				}
				if (typen==2) {
					type = "Seminars";
				}
				if (typen==3) {
					type = "TradeShows";
				}
				result = FuntionMembers.validateType(type.trim());//Check type validate
				if (!result.getKey()) {
					System.out.println(result.getValue());
					break;
				}else {
					eventtype = EventType.valueOf(type.toUpperCase());
				}
				System.out.println((user.toString()+ evenId+eventtype.toString()));
				result = stub.bookevent(user.toString(), evenId, eventtype.toString());//Call bookevent from remoteObject
				
				LOGGER.info(String.format(Constants.LOG_MSG, "BookEvent", Arrays.asList(user, evenId, eventtype),
						result.getKey(), result.getValue()));
				if (result.getKey())
					System.out.println("SUCCESS - " + result.getValue());
				else
					System.out.println("FAILURE - " + result.getValue());

				break;

			case 2:
				HashMap<String, ArrayList<String>> eventList = stub.getbookingSchedule(user.toString());

				LOGGER.info(String.format(Constants.LOG_MSG, "getBookingSchedule", Arrays.asList(user),
						eventList != null, eventList));
				if (eventList != null)
					System.out.println(eventList);
				else
					System.out.println("There was some problem in getting the event schedule. Please try again later.");

				break;
				
			case 3:
				System.out.print("Enter the Event ID to drop : ");
				evenId = input.next().toUpperCase();
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

			System.out.println("\n\n");
			userSelection = displayMenu();
		}
		System.out.println("HAVE A NICE DAY!");
	}

	/**
	 * Display menu
	 * @return
	 */
	private int displayMenu() {
		System.out.println("*     Select a operation     *");
		System.out.println("(1) Enroll in Event.");
		System.out.println("(2) Get Event Schedule.");
		System.out.println("(3) Drop a Event.");
		System.out.println("(4) Quit.");
		System.out.print("Please input the number: ");
		return input.nextInt();
	}

	/**
	 * Setup logger
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




