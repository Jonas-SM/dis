package de.dis.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;


public class TenancyContract {
    private int id = -1;
	private LocalDate startDate;
	private int duration;
	private float additionalCosts;
	private int contractNumber;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public LocalDate getStartDate() {
		return startDate;
	}
	
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public float getAdditionalCosts() {
		return additionalCosts;
	}
	
	public void setAdditionalCosts(float additionalCosts) {
		this.additionalCosts = additionalCosts;
	}
	
	public int getContractNumber() {
		return contractNumber;
	}
	
	public void setContractNumber(int contractNumber) {
		this.contractNumber = contractNumber;
	}

	/**
	 * Lädt einen Makler aus der Datenbank
	 * @param id ID des zu ladenden Maklers
	 * @return Makler-Instanz
	 */
	public static TenancyContract load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM TenancyContract WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				TenancyContract tc = new TenancyContract();
				tc.setId(id);
				tc.setStartDate(LocalDate.parse(rs.getString("startDate")));
				tc.setDuration(rs.getInt("duration"));
				tc.setAdditionalCosts(Float.parseFloat(rs.getString("additionalCosts")));
				tc.setContractNumber(rs.getInt("contractNumber"));

				rs.close();
				pstmt.close();
				return tc;
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
				String insertSQL = "INSERT INTO TenancyContract(startdate, duration, additionalcosts, contractnumber) VALUES (?, ?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setDate(1, Date.valueOf(getStartDate()));
				pstmt.setInt(2, getDuration());
				pstmt.setFloat(3, getAdditionalCosts());
				pstmt.setInt(4, getContractNumber());
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
				String updateSQL = "UPDATE TenancyContract SET startdate = ?, duration = ?, additionalcosts = ?, contractnumber = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setDate(1, Date.valueOf(getStartDate()));
				pstmt.setInt(2, getDuration());
				pstmt.setFloat(3, getAdditionalCosts());
				pstmt.setInt(4, getContractNumber());
				pstmt.setInt(5, getId());
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

			String deleteQuery = "DELETE FROM TenancyContract WHERE id = ?";
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

    public static ArrayList<TenancyContract> getAllContracts() {
        ArrayList<TenancyContract> result = new ArrayList<>(); 
        
        try {
			Connection con = DbConnectionManager.getInstance().getConnection();

            String getAllQuery = "SELECT * FROM TenancyContract";
            PreparedStatement pstmt = con.prepareStatement(getAllQuery);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TenancyContract tc = new TenancyContract();
				tc.setId(rs.getInt("id"));
				tc.setStartDate(LocalDate.parse(rs.getString("startDate")));
				tc.setDuration(rs.getInt("duration"));
				tc.setAdditionalCosts(Float.parseFloat(rs.getString("additionalCosts")));
				tc.setContractNumber(rs.getInt("contractNumber"));

                result.add(tc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void display() {
        System.out.println("TenancyContract{id=" + id + ", startDate='" + startDate + "', duration=" + duration + ", additionalCosts='" + additionalCosts + "', contractNumber='" + contractNumber + "}");
    }
}
