package hamburg.dbis.persistence;

import java.util.ArrayList;
import java.util.Hashtable;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;


public class PersistenceManager {

    static final private PersistenceManager _manager;

    // TODO Add class variables if necessary
    public int lastUsedTransactionId = 0;
    private AtomicInteger lastLSN = new AtomicInteger();
    ConcurrentHashMap<Integer, String> buffer = new ConcurrentHashMap<>();
    public String logFilePath = "C:/Users/Elliot/Documents/hh-master/dis/ex04/Sheet_05_ExampleProject/hamburg/dbis/persistence/log/log.txt";
    public String pageStorePath = "C:/Users/Elliot/Documents/hh-master/dis/ex04/Sheet_05_ExampleProject/hamburg/dbis/persistence/user/";
    Set<Integer> finishedTIDs = new HashSet<>();

    static {
        try {
            _manager = new PersistenceManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PersistenceManager() {
        // TODO Get the last used transaction id from the log (if present) at startup
        lastUsedTransactionId = getLastUsedTransactionId();
        // TODO Initialize class variables if necessary
    }

    public void persistFinishedTransactions() {
        for (Map.Entry<Integer, String> entry : buffer.entrySet()) {
            // get taid
            int taid = Integer.parseInt(entry.getValue().split(",")[0]);

            // check if transaction is already finished
            if (finishedTIDs.contains(taid)) {
                //remove taid
                String line = entry.getValue().split(",")[1] + "," + entry.getValue().split(",")[2]; 
                // write to persistent memory
                writePersistentEntry(entry.getKey(), line);

                // remove taid from finished transactions and buffer
                buffer.remove(entry.getKey());
            }
        }
    }

    public synchronized int getLastUsedTransactionId() {
        Path path = Paths.get(logFilePath);
        int lastUsedTID = lastUsedTransactionId;

        if (lastUsedTID == 0) {
            if (Files.exists(path)) {
                try {
                    List<String> lines = Files.readAllLines(path);
                    
                    for (String line : lines) {
                        String[] parts = line.split(",");
                        
                        lastUsedTID = Integer.max(lastUsedTID, Integer.parseInt(parts[1]));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } 
        return lastUsedTID;
    }

    public synchronized void writeLogEntry(String line) {
        try {
            Files.writeString(
                Paths.get(logFilePath),
                line + "\n",
                StandardOpenOption.CREATE,     // Create file if it doesn't exist
                StandardOpenOption.APPEND      // Append to the end
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writePersistentEntry(int pageid, String line) {
        try {
            Files.writeString(
                Paths.get(pageStorePath + "Page" + pageid),
                line,
                StandardOpenOption.CREATE,     // Create file if it doesn't exist
                StandardOpenOption.APPEND      // Append to the end
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkBuffer() {
        if (buffer.size() > 5) {
            persistFinishedTransactions();
        }
    }

    static public PersistenceManager getInstance() {
        return _manager;
    }

    public synchronized int beginTransaction() {

        // TODO return a valid transaction id to the client
        int tid = lastUsedTransactionId + 1;
        lastUsedTransactionId = tid;

        return tid;
    }

    public void commit(int taid) {
        // TODO handle commits
        finishedTIDs.add(taid);

        // create log entry
        int lsn = lastLSN.incrementAndGet();
        String logEntry = lsn + "," + taid + "," + "EOT";

        writeLogEntry(logEntry);
    }

    public void write(int taid, int pageid, String data) {
        // TODO handle writes of Transaction taid on page pageid with data
        int lsn = lastLSN.incrementAndGet();
        String logEntry = lsn + "," + taid + "," + pageid + "," + data;
        writeLogEntry(logEntry);
        
        // will override entry if there is already one for the same pageid
        buffer.put(pageid, taid + "," + lsn + "," + data);
        
        checkBuffer();
    }
}
