/**
 * This class supplies a global references to all possible attributes,
 * and possible domains, and each domain may contain what attributes.
 * Whenever a registration happens, the user needs to provide an instance
 * of ContextDomain, namely, the name of context space he wants to register,
 * and what type of attributes he wants to provide or indicate in the 
 * registration. This file can be as a reference for the user or other classes
 * to prepare or do the registration, namely as a global knowledge file.
 * 
 */

/**
 * @modifiedBy Ivan
 * @date 20 Jul 2012
 * @actions
 * 1) logically deleted, reserved for future reference -- 20 Jul 2012
 * */

package proxy.service.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;


public class GlobalContextSchemas {
	// data
	
	// attribute names
	public static String [] attributesArray = {
		"name", // the name of an entity (in all context domains)
		"phonePickedUp",
		"tvOn",
		"foodOnTable",
		"personAtDoor",
		"personEating",
		"personDrinking",
		"IP", //7
		"Port",//8
		"preference",
		"speed",//10
		"visitingSpace",
		"location",
		"psgPowerLevel",
		"action",
		"mood",//15
		"isBusy",
	};
	
	public static List<String> attributesList = arrayToList(attributesArray);
	
	/**
	 * The list of events currently supported by the PSG simulator.
	 * An event attribute only has two possible values: TRUE or FALSE.
	 */
	public static String [] eventsArray = {
		"personInRoom"
	};
	
	public static List<String> eventsList = arrayToList(eventsArray);
	
	// domain names
	public static String[] domainsArray = {
	"person",
	"shop",
	"office",
	"home"
	};
	public static List<String> domainsList = arrayToList(domainsArray);
	// constructor
	
	// methods
	/**
	 * This function examines whether a given name for a context attribute is valid.
	 * @param name a given name
	 * @return TRUE if the input is a valid name for some context attribute; otherwise FALSE
	 */
	public static boolean checkAttribute(String attribute) {
		return attributesList.equals(attribute);
	}
	
	
	/**
	 * This function examines whether a given name for a context domain is valid.
	 * @param name a given name
	 * @return TRUE if the input is the valid name for a context domain; otherwise FALSE
	 */
	public static boolean checkDomain(String domain) {
		return domainsList.contains(domain);
	}  // checkDomain
	
	
	/**
	 * This function gets the list of context attributes in a context domain.
	 * @param domain name of the context domain
	 * @return the list of attributes in the domain
	 */
	private static List<String> getAttributes(String domain) {
		domain = domain.toLowerCase();
		List<String> attrs = new ArrayList<String>();
		if(domainsList.indexOf(domain) == 0) {
			attrs.add(attributesArray[0]);
			attrs.add(attributesArray[9]);
			attrs.add(attributesArray[10]);
			attrs.add(attributesArray[11]);
			attrs.add(attributesArray[12]);
			attrs.add(attributesArray[13]);
			attrs.add(attributesArray[14]);
			attrs.add(attributesArray[15]);
			attrs.add(attributesArray[16]);
			attrs.add(eventsArray[0]);
			//attrs[4] = attributesArray[7];
		} else if (domainsList.indexOf(domain) == 1)	{
			attrs.add(attributesArray[0]);
			attrs.add(attributesArray[2]);
			attrs.add(attributesArray[4]);
			attrs.add(attributesArray[5]);
			attrs.add(attributesArray[6]);
//			attrs[5] = eventsList[1];
//			attrs[6] = eventsList[2];
		} else if (domainsList.indexOf(domain) == 2)	{
			attrs.add(attributesArray[0]);
			attrs.add(attributesArray[1]);
			attrs.add(attributesArray[2]);
			attrs.add(eventsArray[0]);
//			attrs[4] = eventsArray[2];
		} else if (domainsList.indexOf(domain) == 3)	{
			attrs.add(attributesArray[0]);
			attrs.add(attributesArray[1]);
			attrs.add(attributesArray[2]);
			attrs.add(attributesArray[3]);
			attrs.add(attributesArray[4]);
			attrs.add(attributesArray[5]);
			attrs.add(attributesArray[6]);
			attrs.add(eventsArray[0]);
			attrs.add(attributesArray[7]);
			attrs.add(attributesArray[8]);
		}
		/*for (int i=0;i<attrs.length;i++)
			System.out.println(attrs[i]);
		System.exit(0);*/
		return attrs;
	}  // getAttributes	
	
	
	/**
	 * This function gets the list of events defined in a context domain.
	 * @param domain name of the context domain
	 * @return the list of events in the domain
	 */
	private static List<String> getEvents (String domain) {
		domain = domain.toLowerCase();
		List<String> events = new ArrayList<String>();
		
		if (domainsList.indexOf(domain) == 0) {
			events.add(eventsArray[2]);
		} else if (domainsList.indexOf(domain) == 1) {
			events.add(eventsArray[1]);
			events.add(eventsArray[2]);		
		} else if (domainsList.indexOf(domain) == 2) {
			events.add(eventsArray[0]);
			events.add(eventsArray[2]);		
		} else if (domainsList.indexOf(domain) == 3) {
			events.add(eventsArray[0]);
		}
		return events;
	}  // getEvents
	
	

	
	/**
	 * This function examines whether a given attribute is valid for a context domain.
	 * @param domain name of a context domain
	 * @param attrName name of a context attribute in the domain
	 * @return TRUE if the attribute is valid for the domain; otherwise FALSE
	 */
	public static boolean checkAttribute(String domain, String attrName) {
		if (!checkDomain(domain)) return false;
		
		List<String> attrs = getAttributes(domain);
		if(attrs.contains(attrName)) {
			return true;
		} else {
			return false;
		}
	}	// checkName
	
	/**
	 * This function examines whether a given name represents a valid event in a context domain.
	 * @param domain name of a context domain
	 * @param name a given name
	 * @return TRUE if the event is valid for the domain; otherwise FALSE
	 */
	public static boolean checkEvent(String domain, String name) {
		List<String> events = getEvents(domain);
		
		if(events.contains(name)) {
			return true;
		} else {
			return false;
		}
	}	// checkEvent
	
	public static List<String> arrayToList(String[] array) {
		List<String> list = new Vector<String>();
		for(String s:array) {
			list.add(s);
		}
		return list;
	}
	
}
