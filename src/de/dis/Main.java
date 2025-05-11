package de.dis;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    static Connection c1;
    static Connection c2;
    static Connection i1;

    public static void main(String[] args) throws SQLException, InterruptedException {

        c1 = setup_new_connection();
        c1.setAutoCommit(false);
        //c1.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        //c1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        c2 = setup_new_connection();
        c2.setAutoCommit(false);
        //c2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        //c2.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);


        // Setup
        i1 = setup_new_connection();
        Statement cs = i1.createStatement();
        cs.execute("DROP TABLE if exists dissheet3;" +
                "CREATE TABLE dissheet3 (" +
                "id integer primary key," +
                "name VARCHAR(50));" +
                "INSERT INTO dissheet3 (id, name) VALUES (1, 'Goofy'),(2, 'Donald'),(3, 'Tick')," +
                "                                  (4, 'Trick'),(5, 'Track');");
        i1.close();
        
        
        //S1 = r1(x) w2(x) c2 w1(x) r1(x) c1
        List<RunnableOperation> operations = new ArrayList<>(Arrays.asList(

                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
                new RunnableOperation(c2, 'c', "COMMIT;"),
                new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;"),
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c1, 'c', "COMMIT;"))
        );
        
        //executeSequence(operations);

        // S2

        //S2 = r1(x) w2(x) c2 r1(x) c1
        List<RunnableOperation> operations2 = new ArrayList<>(Arrays.asList(

                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
                new RunnableOperation(c2, 'c', "COMMIT;"),
                new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
                new RunnableOperation(c1, 'c', "COMMIT;"))
        );

        //executeSequence(operations2);

        // S3 = r2(x) w1(x) w1(y) c1 r2(y) w2(x) w2(y) c2
        List<RunnableOperation> operations3 = new ArrayList<>(Arrays.asList(

            new RunnableOperation(c2, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
            new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
            new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = 'Maus' WHERE id = 2;"),
            new RunnableOperation(c1, 'c', "COMMIT;"),
            new RunnableOperation(c2, 'r', "SELECT name FROM dissheet3 WHERE id = 2;"),
            new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Test' WHERE id = 1;"),
            new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'qwe' WHERE id = 2;"),
            new RunnableOperation(c2, 'c', "COMMIT;"))
            
        );


        executeSequence(operations3);

        // S1'
        List<RunnableOperation> operations1_ss2pl = new ArrayList<>(Arrays.asList(
                    
            new RunnableOperation(c1, 'r', "SELECT * FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
            new RunnableOperation(c2, 'r', "SELECT * FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
            new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
            new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
            new RunnableOperation(c2, 'c', "COMMIT;"),
            new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = name || ' + Max' WHERE id = 1;"),
            new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
            new RunnableOperation(c1, 'c', "COMMIT;"))
            
        );

        // S2'
        List<RunnableOperation> operations2_ss2pl = new ArrayList<>(Arrays.asList(

            new RunnableOperation(c1, 'r', "SELECT * FROM dissheet3 WHERE id = 1 FOR SHARE;"),
            new RunnableOperation(c2, 'r', "SELECT * FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
            new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
            new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
            new RunnableOperation(c2, 'c', "COMMIT;"),
            new RunnableOperation(c1, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
            new RunnableOperation(c1, 'c', "COMMIT;"))
            
        );

        // S3' 
        List<RunnableOperation> operations3_ss2pl = new ArrayList<>(Arrays.asList(

            new RunnableOperation(c2, 'r', "SELECT * FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
            new RunnableOperation(c2, 'r', "SELECT * FROM dissheet3 WHERE id = 2 FOR UPDATE;"),
            new RunnableOperation(c1, 'r', "SELECT * FROM dissheet3 WHERE id = 1 FOR UPDATE;"),
            new RunnableOperation(c1, 'r', "SELECT * FROM dissheet3 WHERE id = 2 FOR UPDATE;"),
            new RunnableOperation(c2, 'r', "SELECT name FROM dissheet3 WHERE id = 1;"),
            new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = 'Mickey' WHERE id = 1;"),
            new RunnableOperation(c1, 'w', "UPDATE dissheet3 SET name = 'Maus' WHERE id = 2;"),
            new RunnableOperation(c1, 'c', "COMMIT;"),
            new RunnableOperation(c2, 'r', "SELECT name FROM dissheet3 WHERE id = 2;"),
            new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'Test' WHERE id = 1;"),
            new RunnableOperation(c2, 'w', "UPDATE dissheet3 SET name = 'qwe' WHERE id = 2;"),
            new RunnableOperation(c2, 'c', "COMMIT;"))
 
                        
        );

    }

    public static void setup () {
        try {
            
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
        
    }

    public static void executeSequence (List<RunnableOperation> operations) {
        
        try {
            ExecutorService executor_t1 = Executors.newFixedThreadPool(1);
            ExecutorService executor_t2 = Executors.newFixedThreadPool(1);
            for (RunnableOperation op : operations) {

                if (op.c == c1)
                    executor_t1.execute(op);

                if (op.c == c2)
                    executor_t2.execute(op);

                Thread.sleep(250);  // Sleep, so the threads in both pools get executed in the desired order
            }
            executor_t1.shutdown();
            executor_t2.shutdown();

            while (!executor_t1.isTerminated() && !executor_t2.isTerminated()) {
                Thread.sleep(1000);
                System.out.println("Waiting for threads");
            }

            System.out.println("Finished all threads");


            // GET Table at the end
            Connection i2 = setup_new_connection();
            Statement cs2 = i2.createStatement();
            ResultSet rs = cs2.executeQuery("SELECT id, name FROM dissheet3 ORDER BY id");
            while (rs.next())
                System.out.println(Integer.toString(rs.getInt("id")) + "," + rs.getString("name"));
            cs2.close();
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }

        
    }

    public static Connection setup_new_connection() {

        try {
            // Holen der Einstellungen aus der db.properties Datei
            Properties properties = new Properties();
            FileInputStream stream = new FileInputStream(new File("db.properties"));
            properties.load(stream);
            stream.close();

            String jdbcUser = properties.getProperty("jdbc_user");
            String jdbcPass = properties.getProperty("jdbc_pass");
            String jdbcUrl = properties.getProperty("jdbc_url");
            // Verbindung zur Datenbank herstellen
            return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

