package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Apartment {
    private int id = -1;
	private int floor;
	private int rent;
    private int rooms;
    private boolean balcony;
    private int estateId;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getFloor() {
		return floor;
	}
	
	public void setFloor(int floors) {
		this.floor = floors;
	}
	
	public int getRent() {
		return rent;
	}
	
	public void setRent(int rent) {
		this.rent = rent;
	}
	
	public int getRooms() {
		return rooms;
	}
	
	public void setRooms(int rooms) {
		this.rooms = rooms;
	}
	
    public boolean getBalcony() {
        return balcony;
    }

    public void setBalcony(boolean balcony) {
        this.balcony = balcony;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

	/**
	 * Lädt einen Makler aus der Datenbank
	 * @param id ID des zu ladenden Maklers
	 * @return Makler-Instanz
	 */
	public static Apartment load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM apartment WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Apartment a = new Apartment();
				a.setId(id);
				a.setFloor(rs.getInt("floor"));
				a.setRent(rs.getInt("rent"));
				a.setRooms(rs.getInt("rooms"));
				a.setBalcony(rs.getBoolean("balcony"));
				a.setEstateId(rs.getInt("estateid"));

				rs.close();
				pstmt.close();
				return a;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Speichert den Makler in der Datenbank. Ist noch keine ID vergeben
	 * worden, wird die generierte Id von der DB geholt und dem Model übergeben.
	 */
	public void save() {
		// Hole Verbindung
		Connection con = DbConnectionManager.getInstance().getConnection();

		try {
			// FC<ge neues Element hinzu, wenn das Objekt noch keine ID hat.
			if (getId() == -1) {
				// Achtung, hier wird noch ein Parameter mitgegeben,
				// damit spC$ter generierte IDs zurC<ckgeliefert werden!
				String insertSQL = "INSERT INTO apartment(floor, rent, rooms, balcony, estateid) VALUES (?, ?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setInt(1, getFloor());
				pstmt.setInt(2, getRent());
				pstmt.setInt(3, getRooms());
				pstmt.setBoolean(4, getBalcony());
				pstmt.setInt(5, getEstateId());
				pstmt.executeUpdate();

				// Hole die Id des engefC<gten Datensatzes
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					setId(rs.getInt(1));
				}

				rs.close();
				pstmt.close();
			} else {
				// Falls schon eine ID vorhanden ist, mache ein Update...
				String updateSQL = "UPDATE house SET floor = ?, rent = ?, rooms = ?, balcony = ?, estateid = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setInt(1, getFloor());
				pstmt.setInt(2, getRent());
				pstmt.setInt(3, getRooms());
				pstmt.setBoolean(4, getBalcony());
				pstmt.setInt(5, getEstateId());
                pstmt.setInt(6, getId());
				pstmt.executeUpdate();

				pstmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void delete () {
		try {
			Connection con = DbConnectionManager.getInstance().getConnection();

			String deleteQuery = "DELETE FROM apartment WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(deleteQuery);

			// Set the id parameter (1st parameter in the query)
			pstmt.setInt(1, getId());

			// Execute the delete operation
			pstmt.executeUpdate();

			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

    public static ArrayList<Apartment> getAllAparments(int agentid) {
        ArrayList<Apartment> result = new ArrayList<>(); 
        
        try {
			Connection con = DbConnectionManager.getInstance().getConnection();

            String getAllQuery = "SELECT * FROM Apartment";
            PreparedStatement pstmt = con.prepareStatement(getAllQuery);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String getEstate = "SELECT estateagentid FROM estate where id = ?";
                PreparedStatement st = con.prepareStatement(getEstate);
                st.setInt(1, rs.getInt("estateid"));
                ResultSet res = st.executeQuery();

				if (res.next() && res.getInt("estateagentid") == agentid) {
					Apartment a = new Apartment();
					a.setId(rs.getInt("id"));
                    a.setFloor(rs.getInt("floor"));
                    a.setRent(rs.getInt("rent"));
                    a.setRooms(rs.getInt("rooms"));
                    a.setBalcony(rs.getBoolean("balcony"));
                    a.setEstateId(rs.getInt("estateid"));
                	result.add(a);
				}				
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    } 

    public void display() {
        System.out.println("Apartment{id=" + id + ", Floor='" + floor + "', Rent=" + rent + ", Rooms='" + rooms + ", Balcony=" + balcony + "}");
    }
}
