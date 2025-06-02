package hamburg.dbis.recovery;

import hamburg.dbis.persistence.PersistenceManager;

import java.util.HashSet;
import java.util.List;
import java.nio.file.*;
import java.util.Set;
import java.io.IOException;


public class RecoveryManager {

    static final private RecoveryManager _manager;

    // TODO Add class variables if necessary
    public String logFilePath = "C:/Users/Elliot/Documents/hh-master/dis/ex04/Sheet_05_ExampleProject/hamburg/dbis/persistence/log/log.txt";
    public String pageStorePath = "C:/Users/Elliot/Documents/hh-master/dis/ex04/Sheet_05_ExampleProject/hamburg/dbis/persistence/user/";

    Set<Integer> winners = new HashSet<>();

    static {
        try {
            _manager = new RecoveryManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private RecoveryManager() {
        // TODO Initialize class variables if necessary
    }

    static public RecoveryManager getInstance() {
        return _manager;
    }

    public void startRecovery() {
        // TODO

        try {
            List<String> lines = Files.readAllLines(Paths.get(logFilePath));
            
            // determine winners
            for (String line : lines) {
                if (line.contains("EOT")) {
                    String[] parts = line.split(",");
                    int taid = Integer.parseInt(parts[1]);
                    winners.add(taid);
                }    
            }
            System.out.println("Winners: " + winners);

            // redo phase
            for (String line : lines) {
                String[] parts = line.split(",");
                int taid = Integer.parseInt(parts[1]);
                int lsn_log = Integer.parseInt(parts[0]);
                
                if (!(line.contains("EOT")) && winners.contains(taid)) {
                    int pageid = Integer.parseInt(parts[2]);
                    String data = parts[3];

                    // get lsn from the referenced page
                    int lsn_page = getLSNFromPage(pageid);

                    if (lsn_page < lsn_log) {
                        writeToMemory(pageid, lsn_log + "," + data);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getLSNFromPage(int pageid) {
        int lsn = 0;

        try {
            String firstLine = Files.lines(Paths.get(pageStorePath + "Page" + pageid)).findFirst().orElse(null);

            lsn = Integer.parseInt(firstLine.split(",")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lsn;
    }

    private void writeToMemory(int pageid, String data) {
        try {
            Files.writeString(
                Paths.get(pageStorePath + "Page" + pageid),
                data,
                StandardOpenOption.CREATE,     // Create file if it doesn't exist
                StandardOpenOption.TRUNCATE_EXISTING  // Overwrite if exists            
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
