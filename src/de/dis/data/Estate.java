package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Estate {
    private int id = -1;
	private String city;
	private int postalcode;
	private String street;
	private int streetnumber;
    private float squarearea;
	private int estateagentid;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public int getPostalcode() {
		return postalcode;
	}
	
	public void setPostalcode(int postalcode) {
		this.postalcode = postalcode;
	}
	
	public String getStreet() {
		return street;
	}
	
	public void setStreet(String street) {
		this.street = street;
	}
	
	public int getStreetnumber() {
		return streetnumber;
	}
	
	public void setStreetnumber(int streetnumber) {
		this.streetnumber = streetnumber;
	}

    public float getSquarearea() {
        return squarearea;
    }

    public void setSquarearea(float squarearea) {
        this.squarearea = squarearea;
    }

	public int getEstateAgentId() {
		return estateagentid;
	}

	public void setEstateAgentId(int estateagentid) {
		this.estateagentid = estateagentid;
	}
	
	/**
	 * Lädt einen Makler aus der Datenbank
	 * @param id ID des zu ladenden Maklers
	 * @return Makler-Instanz
	 */
	public static Estate load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM Estate WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Estate ts = new Estate();
				ts.setId(id);
				ts.setCity(rs.getString("city"));
				ts.setPostalcode(Integer.parseInt(rs.getString("postalcode")));
				ts.setStreet(rs.getString("street"));
				ts.setStreetnumber(Integer.parseInt(rs.getString("streetnumber")));
                ts.setSquarearea(Float.parseFloat(rs.getString("squarearea")));

				rs.close();
				pstmt.close();
				return ts;
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
				String insertSQL = "INSERT INTO Estate(city, postalcode, street, streetnumber, squarearea, estateagentid) VALUES (?, ?, ?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setString(1, getCity());
				pstmt.setInt(2, getPostalcode());
				pstmt.setString(3, getStreet());
				pstmt.setInt(4, getStreetnumber());
				pstmt.setFloat(5, getSquarearea());
				pstmt.setInt(6, getEstateAgentId());
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
				String updateSQL = "UPDATE Estate SET city = ?, postalcode = ?, street = ?, streetnumber = ?, squarearea = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setString(1, getCity());
				pstmt.setInt(2, getPostalcode());
				pstmt.setString(3, getStreet());
				pstmt.setInt(4, getStreetnumber());
				pstmt.setFloat(5, getSquarearea());
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

			String deleteQuery = "DELETE FROM estate WHERE id = ?";
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

	public static ArrayList<Estate> getAllEstates(int agentid) {
        ArrayList<Estate> result = new ArrayList<>(); 
        
        try {
			Connection con = DbConnectionManager.getInstance().getConnection();

            String getAllQuery = "SELECT * FROM Estate";
            PreparedStatement pstmt = con.prepareStatement(getAllQuery);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
				int estateagentid = rs.getInt("estateagentid");

				if (estateagentid == agentid) {
					Estate e = new Estate();
					e.setId(rs.getInt("id"));
					e.setCity(rs.getString("city"));
					e.setPostalcode(Integer.parseInt(rs.getString("postalcode")));
					e.setStreet(rs.getString("street"));
					e.setStreetnumber(Integer.parseInt(rs.getString("streetnumber")));
					e.setSquarearea(Float.parseFloat(rs.getString("squarearea")));	
                	result.add(e);
				}				
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

	public void display() {
        System.out.println("Estate{id=" + id + ", City='" + city + "', Postal Code=" + postalcode + ", Street='" + street + ", Street Number=" + streetnumber + ", Square Area=" + squarearea + "}");
    }
}
