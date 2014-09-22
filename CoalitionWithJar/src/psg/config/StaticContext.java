package psg.config;

import psg.service.manager.ContextAttribute;
import psg.service.manager.ContextDataService;
import psg.service.manager.ContextDomain;

import java.net.*;


// Segregate Context Assignment from MCS API
public class StaticContext {
	// data
	
	// constructor
	
	// methods
	public static ContextDomain getShopDomain(String name, String reference, int i) {	
//		i=0;
		String domainType = "SHOP";
		String[] type = {"book", "gift", "pc"};
		String[] location = {"AmI", "NSS", "DC2"};
		String[] promotion = {"true", "false"};
		int index = i % 3;
		int indexTwo = (i/3)%3;
		ContextAttribute ca1 = new ContextAttribute("shop.name", "String", "shop" + i);
		ContextAttribute ca2 = new ContextAttribute("shop.type", "String", type[index]);
		ContextAttribute ca3 = new ContextAttribute("shop.location", "String", location[indexTwo]);
		ContextAttribute ca4 = new ContextAttribute("shop.crowdness", "String", ""+index);
		ContextAttribute ca5 = new ContextAttribute("shop.brightness", "String", ""+index);
		ContextAttribute ca6 = new ContextAttribute("shop.noiseness", "String", ""+index);
		ContextAttribute ca7 = new ContextAttribute("shop.promotion", "String", promotion[0]);
		ContextAttribute ca8 = new ContextAttribute("shop.crowdness", "String", "" + (0.1*(i%10)));
		ContextAttribute ca9 = new ContextAttribute("shop.brightness", "String", "" + (0.1*(i%10)));
		
		String domainReference = reference;//"http://localhost:13001/xmlrpc";
		String domainName = name;
		ContextDomain cdInstance = new ContextDomain(domainName, domainType, domainReference);
		cdInstance.addAttribute(ca1);
		cdInstance.addAttribute(ca2);
		cdInstance.addAttribute(ca3);
		cdInstance.addAttribute(ca4);
		cdInstance.addAttribute(ca5);
		cdInstance.addAttribute(ca6);
		cdInstance.addAttribute(ca7);
		cdInstance.addAttribute(ca8);
		cdInstance.addAttribute(ca9);
		
		return cdInstance;
	}
	public static ContextDomain getPersonDomain(String name, String reference, int i) {	
//		i=0;
		String domainType = "PERSON";
		int index = i % 3;
		int indexTwo = (i/3)%3;
		String[] preference = {"book", "gift", "pc" };
		String[] action = {"reading","shopping","sleeping"};
		String[] mood = {"happy", "sad", "normal"};
		String[] location = {"AmI", "NSS", "DC2"};
		ContextAttribute ca1 = new ContextAttribute("person.name", "String", "testpersonname");
		ContextAttribute ca2 = new ContextAttribute("person.preference", "String", preference[index]);
		ContextAttribute ca3 = new ContextAttribute("person.location", "String", location[index]);
		ContextAttribute ca4 = new ContextAttribute("person.isBusy", "String", ""+index);
		ContextAttribute ca5 = new ContextAttribute("person.speed", "String", ""+index);
		ContextAttribute ca6 = new ContextAttribute("person.action", "String", action[index]);
		ContextAttribute ca7 = new ContextAttribute("person.power", "String", ""+index);
		ContextAttribute ca8 = new ContextAttribute("person.mood", "String", mood[index]);
		
		String domainReference = reference;//"http://localhost:13001/xmlrpc";
		String domainName = name;
		ContextDomain cdInstance = new ContextDomain(domainName, domainType, domainReference);
		cdInstance.addAttribute(ca1);
		cdInstance.addAttribute(ca2);
		cdInstance.addAttribute(ca3);
		cdInstance.addAttribute(ca4);
		cdInstance.addAttribute(ca5);
		cdInstance.addAttribute(ca6);
		cdInstance.addAttribute(ca7);
		cdInstance.addAttribute(ca8);
		
		return cdInstance;
	}	
	
	public static ContextDomain getOfficeDomain(String name, String reference, int i) {	
		String domainType = "OFFICE";
		int index = i % 3;
		String[] location = {"NUS", "IDMI", "vivo"};
		ContextAttribute ca1 = new ContextAttribute("office.name", "String", "office"+i);
		ContextAttribute ca2 = new ContextAttribute("office.projector", "String", ""+index);
		ContextAttribute ca3 = new ContextAttribute("office.location", "String", location[index]);
		ContextAttribute ca4 = new ContextAttribute("office.isMeeting", "String", ""+index);
		ContextAttribute ca5 = new ContextAttribute("office.lightOn", "String", ""+index);
		ContextAttribute ca6 = new ContextAttribute("office.sound", "String", ""+index);
		
		String domainReference = reference;//"http://localhost:13001/xmlrpc";
		String domainName = name;
		ContextDomain cdInstance = new ContextDomain(domainName, domainType, domainReference);
		cdInstance.addAttribute(ca1);
		cdInstance.addAttribute(ca2);
		cdInstance.addAttribute(ca3);
		cdInstance.addAttribute(ca4);
		cdInstance.addAttribute(ca5);
		cdInstance.addAttribute(ca6);
		
		return cdInstance;
	}	
	
	public static ContextDomain getHomeDomain(String reference, int i) {	
		String domainType = "HOME";
		int index = i % 3;
		String[] location = {"Clementi", "West_Coast", "vivo"};
		ContextAttribute ca1 = new ContextAttribute("home.name", "String", "home"+i);
		ContextAttribute ca2 = new ContextAttribute("home.temperature", "String", ""+index);
		ContextAttribute ca3 = new ContextAttribute("home.location", "String", location[index]);
		ContextAttribute ca4 = new ContextAttribute("home.isOccupied", "String", ""+index);
		ContextAttribute ca5 = new ContextAttribute("home.lightOn", "String", ""+index);
		ContextAttribute ca6 = new ContextAttribute("home.noiseness", "String", ""+index);
		
		String domainReference = reference;//"http://localhost:13001/xmlrpc";
		String domainName = domainReference;
		ContextDomain cdInstance = new ContextDomain(domainName, domainType, domainReference);
		cdInstance.addAttribute(ca1);
		cdInstance.addAttribute(ca2);
		cdInstance.addAttribute(ca3);
		cdInstance.addAttribute(ca4);
		cdInstance.addAttribute(ca5);
		cdInstance.addAttribute(ca6);
		
		return cdInstance;
	}	
	
	public static ContextDomain getClinicDomain(String reference, int i) {	
		String domainType = "CLINIC";
		int index = i % 3;
		String[] location = {"NUS", "IDMI", "vivo"};
		ContextAttribute ca1 = new ContextAttribute("clinic.name", "String", "clinic"+i);
		ContextAttribute ca2 = new ContextAttribute("clinic.type", "String", ""+index);
		ContextAttribute ca3 = new ContextAttribute("clinic.location", "String", location[index]);
		ContextAttribute ca4 = new ContextAttribute("clinic.isOpen", "String", ""+index);
		ContextAttribute ca5 = new ContextAttribute("clinic.brightness", "String", ""+index);
		ContextAttribute ca6 = new ContextAttribute("clinic.noiseness", "String", ""+index);
		
		String domainReference = reference;//"http://localhost:13001/xmlrpc";
		String domainName = domainReference;
		ContextDomain cdInstance = new ContextDomain(domainName, domainType, domainReference);
		cdInstance.addAttribute(ca1);
		cdInstance.addAttribute(ca2);
		cdInstance.addAttribute(ca3);
		cdInstance.addAttribute(ca4);
		cdInstance.addAttribute(ca5);
		cdInstance.addAttribute(ca6);
		
		return cdInstance;
	}	
	
}
