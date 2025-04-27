package de.dis.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PurchaseContract {
    private int id = -1;
	private int installmentsNumber;
	private float interestRate;
	private int contractNumber;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getInstallmentsNumber() {
		return installmentsNumber;
	}
	
	public void setInstallmentsNumber(int installmentsNumber) {
		this.installmentsNumber = installmentsNumber;
	}
	
	public float getInterestRate() {
		return interestRate;
	}
	
	public void setInterestRate(float interestRate) {
		this.interestRate = interestRate;
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
	public static PurchaseContract load(int id) {
		try {
			// Hole Verbindung
			Connection con = DbConnectionManager.getInstance().getConnection();

			// Erzeuge Anfrage
			String selectSQL = "SELECT * FROM PurchaseContract WHERE id = ?";
			PreparedStatement pstmt = con.prepareStatement(selectSQL);
			pstmt.setInt(1, id);

			// Führe Anfrage aus
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				PurchaseContract pc = new PurchaseContract();
				pc.setId(id);
				pc.setInstallmentsNumber(rs.getInt("installmentsNumber"));
				pc.setInterestRate(rs.getFloat("interestRate"));
				pc.setContractNumber(rs.getInt("contractNumber"));

				rs.close();
				pstmt.close();
				return pc;
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
				String insertSQL = "INSERT INTO PurchaseContract(installmentsNumber, interestRate, contractNumber) VALUES (?, ?, ?)";

				PreparedStatement pstmt = con.prepareStatement(insertSQL,
						Statement.RETURN_GENERATED_KEYS);

				// Setze Anfrageparameter und fC<hre Anfrage aus
				pstmt.setInt(1, getInstallmentsNumber());
				pstmt.setFloat(2, getInterestRate());
				pstmt.setInt(3, getContractNumber());
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
				String updateSQL = "UPDATE PurchaseContract SET installmentsNumber = ?, interestRate = ?, contractNumber = ? WHERE id = ?";
				PreparedStatement pstmt = con.prepareStatement(updateSQL);

				// Setze Anfrage Parameter
				pstmt.setInt(1, getInstallmentsNumber());
				pstmt.setFloat(2, getInterestRate());
				pstmt.setInt(3, getContractNumber());
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

			String deleteQuery = "DELETE FROM PurchaseContract WHERE id = ?";
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

    public static ArrayList<PurchaseContract> getAllContracts() {
        ArrayList<PurchaseContract> result = new ArrayList<>(); 
        
        try {
			Connection con = DbConnectionManager.getInstance().getConnection();

            String getAllQuery = "SELECT * FROM PurchaseContract";
            PreparedStatement pstmt = con.prepareStatement(getAllQuery);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                PurchaseContract pc = new PurchaseContract();
				pc.setId(rs.getInt("id"));
				pc.setInstallmentsNumber(rs.getInt("installmentsNumber"));
				pc.setInterestRate(rs.getFloat("interestRate"));
				pc.setContractNumber(rs.getInt(("contractNumber")));
				pc.setContractNumber(rs.getInt("contractNumber"));

                result.add(pc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void display() {
        System.out.println("PurchaseContract{id=" + id + ", installmentsNumber='" + installmentsNumber + "', interestRate=" + interestRate + ", contractNumber='" + contractNumber + "}");
    }
}
