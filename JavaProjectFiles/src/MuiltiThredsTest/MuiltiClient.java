/*
 * COMP6231 A1
 * Tianlin Yang 40010303
 * Gaoshuo Cui 40085020
 */

package MuiltiThredsTest;

import client.User;
import functions.City;
import functions.FuntionMembers;
import functions.Role;

public class MuiltiClient {


	public static void main(String[] args) throws InterruptedException {

		System.out.println("MUILTI THREADED EVENT BOOKING SYSTEM");

		//------------------------------------Customer 4 threads------------------------
				String idc1 = "MTLC1001";
				String idc2 = "MTLC2001";
				String idc3 = "TORC8009";
				String idc4 = "OTWC3345";
				
				User userC1 = new User();User userC2 = new User();User userC3 = new User();User userC4 = new User();
				String valueC1 = validateUser(idc1, userC1);String valueC2 = validateUser(idc2, userC2);String valueC3 = validateUser(idc3, userC3);String valueC4 = validateUser(idc4, userC4);


				switch (valueC1) {

				case "success":
					System.out.println("\nC1 Login Successful : " + userC1);
					System.out.println("C2 Login Successful : " + userC2);
					System.out.println("C3 Login Successful : " + userC3);
					System.out.println("C4 Login Successful : " + userC4);
					Thread t1 = null;Thread t2 = null;Thread t3 = null;Thread t4 = null;
					
//Case1: 4 customers(from different server) book same event(3 slots) at same time.
					System.out.println("----------------------------------------------------------------------------------------------");
					System.out.println("Case1: 4 customers(from different server) book same event(3 slots) at same time.\n");
						//Book event
						t1 = new Thread(new MuiltiCuctomers(userC1,1,"OTWA100519",1));
						t2 = new Thread(new MuiltiCuctomers(userC2,1,"OTWA100519",1));
						t3 = new Thread(new MuiltiCuctomers(userC3,1,"OTWA100519",1));
						t4 = new Thread(new MuiltiCuctomers(userC4,1,"OTWA100519",1));
						System.out.println("\n* 4 customers book event OTWA100519 start: \n");
						
					t1.start();
					t2.start();
					t3.start();
					t4.start();
					t1.sleep(500);t2.sleep(500);t3.sleep(500);t4.sleep(500);
					
					//List event, 1 user randomly be chosen book nothing here
						t1 = new Thread(new MuiltiCuctomers(userC1,2,"OTWA100519",1));
						t2 = new Thread(new MuiltiCuctomers(userC2,2,"OTWA100519",1));
						t3 = new Thread(new MuiltiCuctomers(userC3,2,"OTWA100519",1));
						t4 = new Thread(new MuiltiCuctomers(userC4,2,"OTWA100519",1));
						System.out.println("\n* List customers booked events: \n");
						t1.start();
						t2.start();
						t3.start();
						t4.start();
						
					t1.sleep(500);t2.sleep(500);t3.sleep(500);t4.sleep(500);
					System.out.println("----------------------------------------------------------------------------------------------");
//case2 , 1 user remove , other 3 try to book to event(slots only 3).
					System.out.println("Case2 1 customer remove , other 3 customers try to book to same event(slots only 3).\n");
					//Add "OTWA100520" to user1
					System.out.println("\n* Add \"OTWA100520\" to user1 for remove, now capacity is only 2: \n");
					t1 = new Thread(new MuiltiCuctomers(userC1,1,"OTWA100520",1));
					t1.start();
					t1.sleep(100);
					
					//user1 remove "OTWA100520", other 3 try add "OTWA100520".
					t1 = new Thread(new MuiltiCuctomers(userC1,3,"OTWA100520",1));
					t2 = new Thread(new MuiltiCuctomers(userC2,1,"OTWA100520",1));
					t3 = new Thread(new MuiltiCuctomers(userC3,1,"OTWA100520",1));
					t4 = new Thread(new MuiltiCuctomers(userC4,1,"OTWA100520",1));
					System.out.println("\n* 3 customers book event OTWA100520, 1 customer remove start: \n");
					t1.start();
					t2.start();
					t3.start();
					t4.start();
					
				t1.sleep(500);t2.sleep(500);t3.sleep(500);t4.sleep(500);
				System.out.println("----------------------------------------------------------------------------------------------");

//case3 Montreal manager try add event on Tronto.
				System.out.println("Case3 Montreal manager try add event on Tronto.\n");
				//Add "TORA100520" to Tronto Server
				String idM1 = "MTLM0001";User userM1 = new User();String valueM1 = validateUser(idM1, userM1);
				
				t1 = new Thread(new MuiltiManagers(userM1,1,"TORA110519",3,1));
				t1.start();
				t1.sleep(100);
				
				System.out.println("\n* List the events schedule: \n");
				
				t1 = new Thread(new MuiltiManagers(userM1,3,"TORA110519",3,1));
				t1.start();
				t1.sleep(500);

			System.out.println("----------------------------------------------------------------------------------------------");
//case4 Montreal customer try book 4 events on Tronto in same month.
			System.out.println("Case4 Montreal customer \"MTLC1001\" try book 4 events on Tronto in same month(May 05).\n");
			//Add "TORA100520" to Tronto Server

			t1 = new Thread(new MuiltiCuctomers(userC1,1,"TORA100522",1));
			t2 = new Thread(new MuiltiCuctomers(userC1,1,"TORA100523",1));
			t3 = new Thread(new MuiltiCuctomers(userC1,1,"TORA100524",1));
			t4 = new Thread(new MuiltiCuctomers(userC1,1,"TORA100525",1));
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			
		t1.sleep(100);t2.sleep(100);t3.sleep(100);t4.sleep(100);
		t1.sleep(100);t2.sleep(100);t3.sleep(100);t4.sleep(100);
		t1 = new Thread(new MuiltiCuctomers(userC1,2,"OTWA100519",1));
		System.out.println("\n* \"MTLC1001\" List the \"MTLC1001\" booked events: \n");
		t1.start();
		t1.sleep(100);
		
		System.out.println("\n* \"MTLC1001\" than book June(06) 4 events in Toronto & Ottawa: \n");
		t1 = new Thread(new MuiltiCuctomers(userC1,1,"TORA100625",1));
		t2 = new Thread(new MuiltiCuctomers(userC1,1,"TORA100626",1));
		t3 = new Thread(new MuiltiCuctomers(userC1,1,"OTWA100625",1));
		t4 = new Thread(new MuiltiCuctomers(userC1,1,"OTWA100626",1));
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t1.sleep(100);t2.sleep(100);t3.sleep(100);t4.sleep(100);
		t1 = new Thread(new MuiltiCuctomers(userC1,2,"OTWA100519",1));
		System.out.println("\n* \"MTLC1001\" List the \"MTLC1001\" booked events: \n");
		
		t1.start();
		t1.sleep(100);
System.out.println("----------------------------------------------------------------------------------------------");
				
			break;
	
				default:
					System.out.println(valueC1);
					break;
				}
	}


	private static String validateUser(final String id, final User user) {
		String returnValue = null, city, role, value;
		int userId;
		// string length !=8
		if (id.length() != 8)
			return "Seems to be an invalid id(length not equal to8).";

		city = id.substring(0, 3);
		role = id.substring(3, 4);
		value = id.substring(4);

		// validate city
		if (!FuntionMembers.cityMatch(city))
			return "Your city('" + city + "') isn't recognized.";
		// validate role
		else if (!FuntionMembers.roleMatch(role))
			return "Your role('" + role + "') isn't recognized.";

		try {
			// validate user id (integer value)
			userId = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			return "Your id('" + value + "') isn't recognized.";
		}
		returnValue = "success";
		user.setcity(City.valueOf(city.toUpperCase()));
		user.setRole(Role.fromString(role.toUpperCase()));
		user.setId(userId);
		return returnValue;
	}

}
