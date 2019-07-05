/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */
package remoteObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import functions.City;
import functions.Constants;
import functions.FuntionMembers;

public class EventSystemImplementation extends UnicastRemoteObject implements EventSystemInterface {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final long serialVersionUID = 1L;

	private City city;

	//In memory database
	private HashMap<String, HashMap<String, HashMap<String, Object>>> cityDatabase;

	protected EventSystemImplementation() throws RemoteException {
		super();
	}

/**
 * Ctor
 * @param city
 * @throws RemoteException
 */
	public EventSystemImplementation(String city ) throws RemoteException {
		this.city = City.valueOf(city);
		cityDatabase = new HashMap<>();
	}


	/**
	 * Adds event to the event list
	 */
	public synchronized boolean addEvent(String managerId, String eventId, String eventtype, int capacity) throws RemoteException {
		boolean status = false;
		String msg = Constants.EMPTYSTRING;
		if (cityDatabase.containsKey(eventtype)) {
			HashMap<String, HashMap<String, Object>> event = cityDatabase.get(eventtype);

			if (event.containsKey(eventId)) {
				status = false;
				msg = "Event already exists for "+eventtype+", update the capacity to "+capacity+".";
				

						event.remove(eventId);
						HashMap<String, Object> eventDetails = new HashMap<>();
						eventDetails.put(Constants.CAPACITY, capacity);
						eventDetails.put(Constants.CUSTOMER_ENROLLED, 0);
						eventDetails.put(Constants.CUSTOMER_IDS, new HashSet<String>());
						event.put(eventId, eventDetails);

					
			} else {

					HashMap<String, Object> eventDetails = new HashMap<>();
					eventDetails.put(Constants.CAPACITY, capacity);
					eventDetails.put(Constants.CUSTOMER_ENROLLED, 0);
					eventDetails.put(Constants.CUSTOMER_IDS, new HashSet<String>());
					event.put(eventId, eventDetails);

				status = true;
				msg = eventId + " Added.";
			}

		} else {
			//event doesn't exists
			HashMap<String, Object> eventDetails = new HashMap<>();
			eventDetails.put(Constants.CAPACITY, capacity);
			eventDetails.put(Constants.CUSTOMER_ENROLLED, 0);
			eventDetails.put(Constants.CUSTOMER_IDS, new HashSet<String>());
			HashMap<String, HashMap<String, Object>> event = new HashMap<>();
			event.put(eventId, eventDetails);
			
			//synchronizing the write operation to the in-memory database

				this.cityDatabase.put(eventtype, event);

			status = true;
			msg = eventId + " Added.";
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_ADD_EVENT,
				Arrays.asList(managerId, eventId, eventtype, capacity), status, msg));

		return status;
	}


	/**
	 * Removes a event from event list
	 */
	public boolean removeEvent(String managerId, String eventId, String eventtype) throws RemoteException {

		boolean status = false;
		String msg = Constants.EMPTYSTRING;
		if (cityDatabase.containsKey(eventtype)) {
			HashMap<String, HashMap<String, Object>> event = cityDatabase.get(eventtype);

			if (event.containsKey(eventId)) {
				//Synchronized for remove operation
				synchronized(this) {
					event.remove(eventId);
				}
				status = true;
				msg = eventId + " removed";
			} else {
				status = false;
				msg = eventtype + "doesn't offer this event yet.";
			}
		} else {
			status = false;
			msg = eventtype + "doesn't have any event yet.";
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_REMOVE_EVENT,
				Arrays.asList(managerId, eventId, eventtype), status, msg));

		return status;
	}



	/**
	 * Lists the events available along with the no. of vacant seats for a particular event
	 */
	public HashMap<String, Integer> listEventAvailability(String managerId, String eventtype) throws RemoteException {

		HashMap<String, Integer> result = new HashMap<>();
		result.putAll(listEventAvailabilityForThisServer(eventtype));

		// inquire different city
		for (City ci : City.values()) {
			if (ci != this.city) {
				result.putAll((HashMap<String, Integer>) FuntionMembers
						.byteArrayToObject(udpCommunication(ci, eventtype, Constants.OP_LIST_EVENT_AVAILABILITY)));
			}
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_LIST_EVENT_AVAILABILITY,
				Arrays.asList(managerId, eventtype), result != null, result));

		return result;
	}

	/**
	 * Lists the events available along with the no. of vacant seats for a particular type on this server
	 * @param semester
	 * @return
	 */
	private HashMap<String, Integer> listEventAvailabilityForThisServer(String eventtype) {
		HashMap<String, Integer> result = new HashMap<>();
		// get event from the current city
		if (cityDatabase.containsKey(eventtype)) {
			cityDatabase.get(eventtype).forEach(
					(event, eventDetails) -> result.put(event, (Integer) eventDetails.get(Constants.CAPACITY)
							- (Integer) eventDetails.get(Constants.CUSTOMER_ENROLLED)));
		}

		return result;
	}


	/**
	 * Enrolls a customer in a particular event
	 */

	public synchronized SimpleEntry<Boolean, String> bookevent(String customerId, String eventId, String eventtype)
			throws RemoteException {
		boolean status = true;
		String msg = null;
		SimpleEntry<Boolean, String> result = null;

		// get customer schedule
		HashMap<String, ArrayList<String>> customerSchedule = getbookingSchedule(customerId);
		List<String> cityEvents = new ArrayList<>();
		List<String> outOfcityEvents = new ArrayList<>();
		LinkedList<Integer> difCitySameMonth = new LinkedList<Integer>();
		int f,max=0;
		List<String> maxCityInSameMonth = new ArrayList<>();
		
		customerSchedule.forEach((city, events) -> {
			events.forEach((event) -> {
				City ci= City.valueOf(event.substring(0, 3).toUpperCase());
				if (ci == this.city)
					cityEvents.add(event);
				else
					//Add the month info to out of city events list. Max are:05 05 05, if another 05 add here, fail.
					outOfcityEvents.add(event.substring(6, 8));
			});
		});
		City eventCity = City.valueOf(eventId.substring(0, 3).toUpperCase());
		// enroll in this city only
		if (city == eventCity) {

			// customer already taking this event
			if (cityEvents.contains(customerId)) {
				status = false;
				msg = eventId + " is already enrolled in "+eventId+".";
			}
			if (status) {
				result = enrollmentForThiscity(customerId, eventId, eventtype);
			}

		} else {

			//Create HashSet for count the frequency of each month.
			Set<String> uniqueSet = new HashSet<String>(outOfcityEvents);
			for (String temp1 : uniqueSet) {
				 f = Collections.frequency(outOfcityEvents, temp1);
				 if (max<=f) {
					max = f;
				}
			}
			//Save the String "month"("10") to a list maxCityInSameMonth
			for (String temp1 : uniqueSet) {
				f = Collections.frequency(outOfcityEvents, temp1);
				if (max<=f) {
					maxCityInSameMonth.add(temp1);
				}
			}

			// check if customer is already enrolled in 3 out-city events in same month
			if ( max >= Constants.MAX_OUTCITY_EVENTS && maxCityInSameMonth.contains(eventId.substring(6, 8)) ) {
				status = false;
				msg = customerId + " is already enrolled in " + Constants.MAX_OUTCITY_EVENTS
						+ " out-of-city events in same month.";
			} else {
				// enquire respective city
				for (City ci : City.values()) {
					if (ci == eventCity) {
						HashMap<String, String> data = new HashMap<>();
						data.put(Constants.CUSTOMER_ID, customerId);
						data.put(Constants.EVENT_ID, eventId);
						data.put(Constants.EVENTTYPE, eventtype);

						result = (SimpleEntry<Boolean, String>) FuntionMembers
								.byteArrayToObject(udpCommunication(eventCity, data, Constants.OP_BOOK_EVENT));
					}
				}
			}

			//status = false;
			//msg = "city not found.";
		}

		if (result == null)
			result = new SimpleEntry<Boolean, String>(status, msg);

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_BOOK_EVENT,
				Arrays.asList(customerId, eventId, eventtype), result.getKey(), result.getValue()));

		return result;
	}

	
	
	private SimpleEntry<Boolean, String> enrollmentForThiscity(String customerId, String eventId,
			String eventtype) {
		boolean status;
		String msg;
		if (cityDatabase.containsKey(eventtype)) {
			HashMap<String, HashMap<String, Object>> events = cityDatabase.get(eventtype);

			if (events.containsKey(eventId)) {
				HashMap<String, Object> eventDetails = events.get(eventId);

				if (((Integer) eventDetails.get(Constants.CAPACITY)
						- (Integer) eventDetails.get(Constants.CUSTOMER_ENROLLED)) > 0) {
					
					synchronized(this) {
						status = ((HashSet<String>) eventDetails.get(Constants.CUSTOMER_IDS)).add(customerId);
						if (status) {
							eventDetails.put(Constants.CUSTOMER_ENROLLED,
									(Integer) eventDetails.get(Constants.CUSTOMER_ENROLLED) + 1);
							status = true;
							msg = "Enrollment Successful.";
						} else {
							status = false;
							msg = customerId + " is already enrolled in "+eventId+".";
						}
					}
				} else {
					status = false;
					msg = eventId + " is full.";
				}
			} else {
				status = false;
				msg = eventId + " is not offered in "+eventtype+".";
			}
		} else {
			status = false;
			msg = "No events avialable for " + eventtype + ".";
		}

		return new SimpleEntry<Boolean, String>(status, msg);
	}


	/**
	 * Returns the class schedule for a student. Enquires all the departments
	 */

	public HashMap<String, ArrayList<String>> getbookingSchedule(String customerId) throws RemoteException {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		schedule.putAll(getbookingScheduleThisServer(customerId));

		// inquire different cities
		for (City ci : City.values()) {
			if (ci!= this.city) {
				
				HashMap<String, ArrayList<String>> citySchedule = 
						(HashMap<String, ArrayList<String>>) FuntionMembers
						.byteArrayToObject(udpCommunication
						(ci, customerId, Constants.OP_GET_EVENT_SCHEDULE));
				
				for(String eventtype : citySchedule.keySet()) {
					if(schedule.containsKey(eventtype)) {
						schedule.get(eventtype).addAll(citySchedule.get(eventtype));
					}else {
						schedule.put(eventtype, citySchedule.get(eventtype));
					}
				}
			}
		}
		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_GET_EVENT_SCHEDULE, Arrays.asList(customerId),
				schedule != null, schedule));
		return schedule;
	}

	private HashMap<String, ArrayList<String>> getbookingScheduleThisServer(String customerId) {
		HashMap<String, ArrayList<String>> schedule = new HashMap<>();
		cityDatabase.forEach((eventtype, events) -> {
			events.forEach((event, eventdetails) -> {
				if (((HashSet<String>) eventdetails.get(Constants.CUSTOMER_IDS)).contains(customerId)) {
					if (schedule.containsKey(eventtype)) {
						schedule.get(eventtype).add(event);
					} else {
						ArrayList<String> temp = new ArrayList<>();
						temp.add(event);
						schedule.put(eventtype, temp);
					}
				}
			});
		});
		return schedule;
	}


	/**
	 * Drops a particular event for the given customer
	 */
	public SimpleEntry<Boolean, String> dropevent(String customerId, String eventId) throws RemoteException {

		City eventcity = City.valueOf(eventId.substring(0, 3).toUpperCase());
		SimpleEntry<Boolean, String> result;
		if (this.city == eventcity) {
			result = dropeventOnThisServer(customerId, eventId);
		} else {
			HashMap<String, String> data = new HashMap<>();
			data.put(Constants.CUSTOMER_ID, customerId);
			data.put(Constants.EVENT_ID, eventId);
			result = (SimpleEntry<Boolean, String>) FuntionMembers
					.byteArrayToObject(udpCommunication(eventcity, data, Constants.OP_DROP_EVENT));
		}

		LOGGER.info(String.format(Constants.LOG_MSG, Constants.OP_DROP_EVENT, Arrays.asList(customerId, eventId),
				result.getKey(), result.getValue()));
		return result;
	}

	private SimpleEntry<Boolean, String> dropeventOnThisServer(String customerId, String eventId) {
		final Map<Boolean, String> temp = new HashMap<>();
		if (cityDatabase.size() > 0) {
			cityDatabase.forEach((ci, events) -> {
				if (events.containsKey(eventId)) {
					events.forEach((event, eventDetails) -> {
						synchronized(this) {
							if (event.equals(eventId)) {
								boolean status = ((HashSet<String>) eventDetails.get(Constants.CUSTOMER_IDS))
										.remove(customerId);
								if (status) {
									eventDetails.put(Constants.CUSTOMER_ENROLLED,
											((Integer) eventDetails.get(Constants.CUSTOMER_ENROLLED) - 1));
									temp.put(true, "success");
								} else {
									temp.put(false, customerId + " isn't enrolled in "+eventId+".");
								}
							}
						}
					});
				} else {
					temp.put(false, eventId + " isn't offered by the city yet.");
				}
			});
		} else {
			temp.put(false, eventId + " isn't offered by the city yet.");
		} 

		if (temp.containsKey(true)) {
			return new SimpleEntry<Boolean, String>(true, "Event Dropped.");
		} else {
			return new SimpleEntry<Boolean, String>(false, temp.get(false));
		}
	}

	/**
	 * UDP Server for Inter-city communication
	 */
	public void UDPServer() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(city.getUdpPort());
			byte[] buffer = new byte[1000];// to stored the received data from the client.
			LOGGER.info(this.city + " UDP Server Started............");
			// non-terminating loop as the server is always in listening mode.
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				// Server waits for the request to come
				socket.receive(request); // request received

				byte[] response = processUDPRequest(request.getData());

				DatagramPacket reply = new DatagramPacket(response, response.length, request.getAddress(),
						request.getPort());// reply packet ready
				socket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	/**
	 * Handles the UDP request for information
	 * @param data
	 * @return
	 */
	private byte[] processUDPRequest(byte[] data) {

		byte[] response = null;
		HashMap<String, Object> request = (HashMap<String, Object>) FuntionMembers.byteArrayToObject(data);

		for (String key : request.keySet()) {

			LOGGER.info("Received UDP Socket call for method[" + key + "] with parameters[" + request.get(key) + "]");
			switch (key) {
			case Constants.OP_LIST_EVENT_AVAILABILITY:
				String semester = (String) request.get(key);
				response = FuntionMembers.objectToByteArray(listEventAvailabilityForThisServer(semester));
				break;
			case Constants.OP_BOOK_EVENT:
				HashMap<String, String> info = (HashMap<String, String>) request.get(key);
				response = FuntionMembers.objectToByteArray(enrollmentForThiscity(info.get(Constants.CUSTOMER_ID),
						info.get(Constants.EVENT_ID), info.get(Constants.EVENTTYPE)));
				break;
			case Constants.OP_GET_EVENT_SCHEDULE:
				String studentId = (String) request.get(key);
				response = FuntionMembers.objectToByteArray(getbookingScheduleThisServer(studentId));
				break;
			case Constants.OP_DROP_EVENT:
				info = (HashMap<String, String>) request.get(key);
				response = FuntionMembers
						.objectToByteArray(dropeventOnThisServer(info.get(Constants.CUSTOMER_ID), info.get(Constants.EVENT_ID)));
				break;
			}
		}

		return response;
	}

	/**
	 * Creates & sends the UDP request
	 * @param dept
	 * @param info
	 * @param method
	 * @return
	 */
	private byte[] udpCommunication(City dept, Object info, String method) {

		LOGGER.info("Making UPD Socket Call to " + dept + " Server for method : " + method);

		// UDP SOCKET CALL AS CLIENT
		HashMap<String, Object> data = new HashMap<>();
		byte[] response = null;
		data.put(method, info);
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			byte[] message = FuntionMembers.objectToByteArray(data);
			InetAddress remoteUdpHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, message.length, remoteUdpHost, dept.getUdpPort());
			socket.send(request);
			byte[] buffer = new byte[65556];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			socket.receive(reply);
			response = reply.getData();
		} catch (SocketException e) {
			LOGGER.severe("SocketException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("IOException : " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}

		return response;
	}


}
