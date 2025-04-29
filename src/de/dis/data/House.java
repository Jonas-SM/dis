package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class House {
    private int id = -1;
	private int floors;
	private float price;
    private boolean garden;
    private int estateId;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getFloors() {
		return floors;
	}
	
	public void setFloors(int floors) {
		this.floors = floors;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public boolean getGarden() {
		return garden;
	}
	
	public void setGarden(boolean garden) {
		this.garden = garden;
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
	public static House load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM house WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				House h = new House();
				h.setId(id);
				h.setFloors(rs.getInt("floors"));
				h.setPrice(rs.getFloat("price"));
				h.setGarden(Boolean.parseBoolean(rs.getString("garden")));
				h.setEstateId(rs.getInt("estateid"));

				rs.close();
				pstmt.close();
				return h;
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
				String insertSQL = "INSERT INTO House(floors, price, garden, estateId) VALUES (?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setInt(1, getFloors());
				pstmt.setFloat(2, getPrice());
				pstmt.setBoolean(3, getGarden());
				pstmt.setInt(4, getEstateId());
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
				String updateSQL = "UPDATE house SET floors = ?, price = ?, garden = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setInt(1, getFloors());
				pstmt.setFloat(2, getPrice());
				pstmt.setBoolean(3, getGarden());
				pstmt.setInt(4, getId());
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

			String deleteQuery = "DELETE FROM house WHERE id = ?";
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

    public static ArrayList<House> getAllHouses(int agentid) {
        ArrayList<House> result = new ArrayList<>(); 
        
        try {
			Connection con = DbConnectionManager.getInstance().getConnection();

            String getAllQuery = "SELECT * FROM House";
            PreparedStatement pstmt = con.prepareStatement(getAllQuery);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String getEstate = "SELECT estateagentid FROM estate where id = ?";
                PreparedStatement st = con.prepareStatement(getEstate);
                st.setInt(1, rs.getInt("estateid"));
                ResultSet res = st.executeQuery();

				if (res.next() && res.getInt("estateagentid") == agentid) {
					House h = new House();
					h.setId(rs.getInt("id"));
                    h.setFloors(rs.getInt("floors"));
                    h.setPrice(rs.getFloat("price"));
                    h.setGarden(Boolean.parseBoolean(rs.getString("garden")));
                    h.setEstateId(rs.getInt("estateid"));
                	result.add(h);
				}				
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    } 

    public void display() {
        System.out.println("House{id=" + id + ", Floors='" + floors + "', price=" + price + ", Garden='" + garden + "}");
    }
}
