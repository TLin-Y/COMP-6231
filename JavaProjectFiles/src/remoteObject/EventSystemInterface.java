/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */
package remoteObject;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface contract for the event Registration System
 */
public interface EventSystemInterface extends Remote {

	/* manager Operations */

	boolean addEvent(String managerId, String eventId, String eventtype, int capacity) throws RemoteException;

	boolean removeEvent(String managerId, String eventId, String eventtype) throws RemoteException;

	HashMap<String, Integer> listEventAvailability(String managerId, String eventtype) throws RemoteException;

	/* Customer Operations */

	SimpleEntry<Boolean, String> bookevent(String customerId, String eventId, String eventtype) throws RemoteException;

	HashMap<String, ArrayList<String>> getbookingSchedule(String customerId) throws RemoteException;

	SimpleEntry<Boolean, String> dropevent(String customerId, String eventId) throws RemoteException;

}
