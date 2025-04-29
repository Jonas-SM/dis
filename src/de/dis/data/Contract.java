package de.dis.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;


public class Contract {
    private int contractnumber = -1;
	private LocalDate creationdate;
	private String place;
	
	public int getContractNumber() {
		return contractnumber;
	}
	
	public void setContractNumber(int contractnumber) {
		this.contractnumber = contractnumber;
	}
	
	public LocalDate getCreationDate() {
		return creationdate;
	}
	
	public void setCreationDate(LocalDate creationdate) {
		this.creationdate = creationdate;
	}
	
	public String getPlace() {
		return place;
	}
	
	public void setPlace(String place) {
		this.place = place;
	}
	
	/**
	 * Lädt einen Makler aus der Datenbank
	 * @param id ID des zu ladenden Maklers
	 * @return Makler-Instanz
	 */
	public static Contract load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM contract WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Contract c = new Contract();
				c.setContractNumber(rs.getInt("contractnumber"));
				c.setPlace(rs.getString("rent"));
                c.setCreationDate(LocalDate.parse(rs.getString("creationDate")));

				rs.close();
				pstmt.close();
				return c;
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
			if (getContractNumber() == -1) {
				// Achtung, hier wird noch ein Parameter mitgegeben,
				// damit spC$ter generierte IDs zurC<ckgeliefert werden!
				String insertSQL = "INSERT INTO contract(creationdate, place) VALUES (?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setDate(1, Date.valueOf(getCreationDate()));
				pstmt.setString(2, getPlace());
				pstmt.executeUpdate();

				// Hole die Id des engefC<gten Datensatzes
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					setContractNumber(rs.getInt(1));
				}

				rs.close();
				pstmt.close();
			} else {
				// Falls schon eine ID vorhanden ist, mache ein Update...
				String updateSQL = "UPDATE contract SET creationdate = ?, place = ? WHERE contractnumber = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setDate(1, Date.valueOf(getCreationDate()));
				pstmt.setString(2, getPlace());
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

			String deleteQuery = "DELETE FROM contract WHERE contractnumber = ?";
			PreparedStatement pstmt = con.prepareStatement(deleteQuery);

			// Set the id parameter (1st parameter in the query)
			pstmt.setInt(1, getContractNumber());

			// Execute the delete operation
			pstmt.executeUpdate();

			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
