package de.dis;

import de.dis.data.Apartment;
import de.dis.data.Estate;
import de.dis.data.EstateAgent;
import de.dis.data.House;
import de.dis.data.Person;
import de.dis.data.PurchaseContract;
import de.dis.data.TenancyContract;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Hauptklasse
 */
public class Main {
	/**
	 * Startet die Anwendung
	 */
	public static void main(String[] args) {
		showMainMenu();
	}
	
	
	/**
	 * Menus
	 */

	/**
	 * Zeigt das Hauptmenü
	 */
	public static void showMainMenu() {
		//Menüoptionen
		final int MENU_MAKLER = 0;
		final int MENU_ESTATE = 1;
		final int MENU_CONTRACT = 2;
		final int QUIT = 3;
		
		//Erzeuge Menü
		Menu mainMenu = new Menu("Hauptmenü");
		mainMenu.addEntry("Makler-Verwaltung", MENU_MAKLER);
		mainMenu.addEntry("Anwesen-Verwaltung", MENU_ESTATE);
		mainMenu.addEntry("Vertrag-Verwaltung", MENU_CONTRACT);
		mainMenu.addEntry("Beenden", QUIT);
		
		//Verarbeite Eingabe
		while(true) {
			int response = mainMenu.show();
			
			switch(response) {
				case MENU_MAKLER:
					showMaklerMenu();
					break;
				case MENU_ESTATE:
					showEstateMenu();
					break;
				case MENU_CONTRACT:
					showContractMenu();
					break;
				case QUIT:
					return;
			}
		}
	}
	
	/**
	 * Zeigt die Maklerverwaltung
	 */
	public static void showMaklerMenu() {
		//Menüoptionen
		final int NEW_MAKLER = 0;
		final int ACCOUNT_LOESCHEN = 1;
		final int BACK = 2;

		// Passwort prüfen
		if (FormUtil.readString("Passwort").equals("secret")) {
			//Maklerverwaltungsmenü
			Menu maklerMenu = new Menu("Makler-Verwaltung");
			maklerMenu.addEntry("Neuer Makler", NEW_MAKLER);
			maklerMenu.addEntry("Account löschen", ACCOUNT_LOESCHEN);
			maklerMenu.addEntry("Zurück zum Hauptmenü", BACK);
			
			//Verarbeite Eingabe
			while(true) {
				int response = maklerMenu.show();
				
				switch(response) {
					case NEW_MAKLER:
						newMakler();
						break;
					case ACCOUNT_LOESCHEN:
						removeMakler();
						break;
					case BACK:
						return;
				}
			}
		} else {
			System.out.println("Falsches Passwort!");
			return;
		}
	}

	/**
	 * Legt einen neuen Makler an, nachdem der Benutzer
	 * die entprechenden Daten eingegeben hat.
	 */
	public static void newMakler() {
		EstateAgent m = new EstateAgent();
		
		m.setName(FormUtil.readString("Name"));
		m.setAddress(FormUtil.readString("Adresse"));
		m.setLogin(FormUtil.readString("Login"));
		m.setPassword(FormUtil.readString("Passwort"));
		m.save();
		
		System.out.println("Makler mit der ID "+m.getId()+" wurde erzeugt.");
	}
	
	public static void removeMakler() {
		EstateAgent m = EstateAgent.load(Integer.parseInt(FormUtil.readString("Id")));
		
		m.delete();
		
		System.out.println("Makler mit der ID "+m.getId()+" wurde gelöscht.");
	}


	/*
	 * Anwesen Verwaltung
	 */
	public static void showEstateMenu() {
		//Menüoptionen
		final int CREATE_ESTATE = 0;
		final int DELETE_ESTATE = 1;
		final int UPDATE_ESTATE = 2;
		final int BACK = 3;

		String username = FormUtil.readString("username");
		String password = FormUtil.readString("password");

		EstateAgent ea = EstateAgent.checkCredentials(username, password);
		if (ea != null) {
			//Estate Menu
			Menu estateMenu = new Menu("Anwesen-Verwaltung");
			estateMenu.addEntry("Neues Anwesen", CREATE_ESTATE);
			estateMenu.addEntry("Anwesen löschen", DELETE_ESTATE);
			estateMenu.addEntry("Anwesen bearbeiten", UPDATE_ESTATE);
			estateMenu.addEntry("Zurück zum Hauptmenü", BACK);
			
			//Verarbeite Eingabe
			while(true) {
				int response = estateMenu.show();
				
				switch(response) {
					case CREATE_ESTATE:
						createEstate(ea.getId());
						break;
					case DELETE_ESTATE:
						deleteEstate();
						break;
					case UPDATE_ESTATE:
						updateEstate(ea.getId());
						break;
					case BACK:
						return;
				}
			}
		} else {
			System.out.println("Falscher login name / passwort");
			return;
		}
	}

	public static void createEstate (int estateagentid) {
		final int CREATE_HOUSE = 0;
		final int CREATE_APARTMENT = 1;
		final int BACK = 2;
		
		//Contract Menu
		Menu createEstateMenu = new Menu("Anwesen anlegen");
		createEstateMenu.addEntry("Haus anlegen", CREATE_HOUSE);
		createEstateMenu.addEntry("Wohnung anlegen", CREATE_APARTMENT);
		createEstateMenu.addEntry("Zurück zur Anwesen-Verwaltung", BACK);
		
		//Verarbeite Eingabe
		while(true) {
			int response = createEstateMenu.show();
			
			switch(response) {
				case CREATE_HOUSE:
				 	createHouse(estateagentid);
					break;
				case CREATE_APARTMENT:
					createApartment(estateagentid);
					break;
				case BACK:
					return;
			}
		}
		
	}

	public static void deleteEstate () {
		Estate e = Estate.load(FormUtil.readInt("Id"));
		
		e.delete();

		System.out.println("Anwesen mit der ID "+e.getId()+" wurde gelöscht.");
	}


	public static void createHouse(int estateagentid) {
		Estate e = new Estate();

		e.setCity(FormUtil.readString("City"));
		e.setPostalcode(FormUtil.readInt("Postalcode"));
		e.setStreet(FormUtil.readString("Street"));
		e.setStreetnumber(FormUtil.readInt("Streetnumber"));
		e.setSquarearea(Float.parseFloat(FormUtil.readString("Squarearea")));
		e.setEstateAgentId(estateagentid);
		e.save();
		
		House h = new House();
		h.setFloors(FormUtil.readInt("Floors"));
		h.setPrice(FormUtil.readInt("Price"));
		h.setGarden(Boolean.parseBoolean(FormUtil.readString("Garden (1/0)")));
		h.setEstateId(e.getId());
		h.save();

		System.out.println("Anwesen mit der ID "+e.getId()+" wurde erzeugt.");
		System.out.println("Haus mit der ID "+h.getId()+" wurde erzeugt.");
	}

	public static void createApartment(int estateagentid) {
		Estate e = new Estate();

		e.setCity(FormUtil.readString("City"));
		e.setPostalcode(FormUtil.readInt("Postalcode"));
		e.setStreet(FormUtil.readString("Street"));
		e.setStreetnumber(FormUtil.readInt("Streetnumber"));
		e.setSquarearea(Float.parseFloat(FormUtil.readString("Squarearea")));
		e.setEstateAgentId(estateagentid);
		e.save();
		
		Apartment a = new Apartment();
		a.setFloor(FormUtil.readInt("Floor"));
		a.setRent(FormUtil.readInt("Rent"));
		a.setRooms(FormUtil.readInt("Rooms"));
		a.setBalcony(Boolean.parseBoolean(FormUtil.readString("Balcony (1/0)")));
		a.setEstateId(e.getId());
		a.save();

		System.out.println("Anwesen mit der ID "+e.getId()+" wurde erzeugt.");
		System.out.println("Apartment mit der ID "+a.getId()+" wurde erzeugt.");
	}

	
	public static void updateEstate (int agentid) {
		final int CHANGE_ESTATE = 0;
		final int CHANGE_HOUSE = 1;
		final int CHANGE_APARTMENT = 2;
		final int BACK = 3;	

		//Estate Menu
		Menu updateMenu = new Menu("Info ändern");
		updateMenu.addEntry("Anwesen ändern", CHANGE_ESTATE);
		updateMenu.addEntry("Haus ändern", CHANGE_HOUSE);
		updateMenu.addEntry("Apartment ändern", CHANGE_APARTMENT);
		updateMenu.addEntry("Zurück zur Anwesen-Verwaltung", BACK);


		//Verarbeite Eingabe
		while(true) {
			int response = updateMenu.show();
			
			switch(response) {
				case CHANGE_ESTATE:
				 	changeEstate(agentid);
					break;
				case CHANGE_HOUSE:
					changeHouse(agentid);
					break;
				case CHANGE_APARTMENT:
					changeApartment(agentid);
				case BACK:
					return;
			}
		}
		
	}

	public static void changeEstate(int agentid) {
		final int CHANGE_CITY = 0;
		final int CHANGE_POSTALCODE = 1;
		final int CHANGE_STREET = 2;
		final int CHANGE_STREETNUMBER = 3;
		final int CHANGE_SQUAREAREA = 4;
		final int BACK = 5;

		ArrayList<Estate> allEstates = new ArrayList<>();
		allEstates = Estate.getAllEstates(agentid);

		for (Estate e : allEstates) {
			e.display();
		}

		Estate e = Estate.load(FormUtil.readInt("Id zum bearbeiten"));

		Menu updateEstateMenu = new Menu("Info ändern");
		updateEstateMenu.addEntry("City ändern", CHANGE_CITY);
		updateEstateMenu.addEntry("Postalcode ändern", CHANGE_POSTALCODE);
		updateEstateMenu.addEntry("Street ändern", CHANGE_STREET);
		updateEstateMenu.addEntry("Streetnumber ändern", CHANGE_STREETNUMBER);
		updateEstateMenu.addEntry("Squarearea ändern", CHANGE_SQUAREAREA);
		updateEstateMenu.addEntry("Zurück zur Anwesen Verwaltung", BACK);

		while(true) {
			int response = updateEstateMenu.show();
			
			switch(response) {
				case CHANGE_CITY:
					e.setCity(FormUtil.readString("new City"));
					break;
				case CHANGE_POSTALCODE:
					e.setPostalcode(FormUtil.readInt("new Postalcode"));
					break;
				case CHANGE_STREET:
					e.setStreet(FormUtil.readString("new Street"));
					break;
				case CHANGE_STREETNUMBER:
					e.setStreetnumber(FormUtil.readInt("new Streetnumber"));
					break;
				case CHANGE_SQUAREAREA:
					e.setSquarearea(Float.parseFloat(FormUtil.readString("new Squarearea")));
					break;
				case BACK:
					return;
			}
			e.save();

			System.out.println("Anwesen mit der ID" + e.getId() + " wurde geändert.");
		}
	}

	public static void changeHouse(int agentid) {
		final int CHANGE_FLOORS = 0;
		final int CHANGE_PRICE = 1;
		final int CHANGE_GARDEN = 2;
		final int BACK = 3;

		ArrayList<House> allHouses = new ArrayList<>();
		allHouses = House.getAllHouses(agentid);

		for (House h : allHouses) {
			h.display();
		}

		House h = House.load(FormUtil.readInt("Id zum bearbeiten"));

		Menu updateEstateMenu = new Menu("Info ändern");
		updateEstateMenu.addEntry("Floors ändern", CHANGE_FLOORS);
		updateEstateMenu.addEntry("Price ändern", CHANGE_PRICE);
		updateEstateMenu.addEntry("Garden ändern", CHANGE_GARDEN);
		updateEstateMenu.addEntry("Zurück zur Anwesen Verwaltung", BACK);

		while(true) {
			int response = updateEstateMenu.show();
			
			switch(response) {
				case CHANGE_FLOORS:
					h.setFloors(FormUtil.readInt("new Floor"));
					break;
				case CHANGE_PRICE:
					h.setPrice(FormUtil.readInt("new Postalcode"));
					break;
				case CHANGE_GARDEN:
					h.setGarden(Boolean.parseBoolean(FormUtil.readString("new Street")));
					break;
				case BACK:
					return;
			}
			h.save();

			System.out.println("Haus mit der ID" + h.getId() + " wurde geändert.");
		}
	}

	public static void changeApartment(int agentid) {
		final int CHANGE_FLOOR = 0;
		final int CHANGE_RENT = 1;
		final int CHANGE_ROOMS = 2;
		final int CHANGE_BALCONY = 3;
		final int BACK = 4;

		ArrayList<Apartment> allApartments = new ArrayList<>();
		allApartments = Apartment.getAllAparments(agentid);

		for (Apartment a : allApartments) {
			a.display();
		}

		Apartment a = Apartment.load(FormUtil.readInt("Id zum bearbeiten"));

		Menu updateEstateMenu = new Menu("Info ändern");
		updateEstateMenu.addEntry("Floor ändern", CHANGE_FLOOR);
		updateEstateMenu.addEntry("Rent ändern", CHANGE_RENT);
		updateEstateMenu.addEntry("Rooms ändern", CHANGE_ROOMS);
		updateEstateMenu.addEntry("Balcony ändern", CHANGE_BALCONY);
		updateEstateMenu.addEntry("Zurück zur Anwesen Verwaltung", BACK);

		while(true) {
			int response = updateEstateMenu.show();
			
			switch(response) {
				case CHANGE_FLOOR:
					a.setFloor(FormUtil.readInt("new Floor"));
					break;
				case CHANGE_RENT:
					a.setRent(FormUtil.readInt("new Rent"));
					break;
				case CHANGE_ROOMS:
					a.setRooms(FormUtil.readInt("new Rooms"));
					break;
				case CHANGE_BALCONY:
					a.setBalcony(Boolean.parseBoolean(FormUtil.readString("new Balcony")));
					break;
				case BACK:
					return;
			}
			a.save();

			System.out.println("Apartment mit der ID" + a.getId() + " wurde geändert.");
		}
	}
	
	public static void showContractMenu() {
		final int CONTRACT_OVERVIEW = 0;
		final int CREATE_CONTRACT = 1;
		final int ADD_PERSONS = 3;
		final int BACK = 4;
		
		//Contract Menu
		Menu contractMenu = new Menu("Vertrag-Verwaltung");
		contractMenu.addEntry("Übersicht Verträge", CONTRACT_OVERVIEW);
		contractMenu.addEntry("Vertrag erstellen", CREATE_CONTRACT);
		contractMenu.addEntry("Personen hinzufügen", ADD_PERSONS);
		contractMenu.addEntry("Zurück zum Hauptmenü", BACK);
		
		//Verarbeite Eingabe
		while(true) {
			int response = contractMenu.show();
			
			switch(response) {
				case CONTRACT_OVERVIEW:
				 	contractOverview();
					break;
				case CREATE_CONTRACT:
					createContract();
					break;
				case ADD_PERSONS:
					addPerson();
					break;
				case BACK:
					return;
			}
		}
	}
	
	public static void contractOverview() {
		ArrayList<TenancyContract> allTenancyContracts = new ArrayList<>();
		allTenancyContracts = TenancyContract.getAllContracts();

		ArrayList<PurchaseContract> allPurchaseContracts = new ArrayList<>();
		allPurchaseContracts = PurchaseContract.getAllContracts();

		for (TenancyContract tc : allTenancyContracts) {
			tc.display();
		}
		for (PurchaseContract pc : allPurchaseContracts) {
			pc.display();
		}
	}

	public static void addPerson() {
		Person p = new Person();
		
		p.setFirstName(FormUtil.readString("First Name"));
		p.setLastName(FormUtil.readString("Last Name"));
		p.setHomeAddress(FormUtil.readString("Home Address"));
		p.save();
		
		System.out.println("Person mit der ID "+p.getId()+" wurde erzeugt.");
	}

	public static void createContract() {
		final int CREATE_TENANCY_CONTRACT = 1;
		final int CREATE_PURCHASE_CONTRACT = 2;
		final int BACK = 3;

		//Estate Menu
		Menu createContractMenu = new Menu("Vertrag erstellen");
		createContractMenu.addEntry("Mietvertrag anlegen", CREATE_TENANCY_CONTRACT);
		createContractMenu.addEntry("Kaufvertrag anlegen", CREATE_PURCHASE_CONTRACT);
		createContractMenu.addEntry("Zurück zur Vertrag Verwaltung", BACK);
		
		while (true) {
			int response = createContractMenu.show();
			
			switch(response) {
				case CREATE_TENANCY_CONTRACT:
					createTenancyContract();
					break;
				case CREATE_PURCHASE_CONTRACT:
				 	createPurchaseContract();
					break;
				case BACK:
					return;
			}
		}
	}

	public static void createTenancyContract() {
		TenancyContract tc = new TenancyContract();
		
		tc.setStartDate(LocalDate.parse(FormUtil.readString("Start Date")));
		tc.setDuration(FormUtil.readInt("Duration"));
		tc.setAdditionalCosts(Float.parseFloat(FormUtil.readString("Additional Cost")));
		tc.setContractNumber(FormUtil.readInt("Contract Number"));
		tc.save();
		
		System.out.println("Mietvertrag mit der ID "+tc.getId()+" wurde erzeugt.");
	}

	public static void createPurchaseContract() {
		PurchaseContract pc = new PurchaseContract();
		
		pc.setInstallmentsNumber(FormUtil.readInt("Installments Rate"));
		pc.setInterestRate(Float.parseFloat(FormUtil.readString("Interest Rate")));
		pc.setContractNumber(FormUtil.readInt("Contract Number"));
		pc.save();
		
		System.out.println("Kaufvertrag mit der ID "+pc.getId()+" wurde erzeugt.");
	}
	
	public static void signContract() {
		
	}

}
