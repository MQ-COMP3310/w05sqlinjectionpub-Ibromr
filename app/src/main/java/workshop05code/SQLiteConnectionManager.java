package workshop05code;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//Import for logging exercise
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SQLiteConnectionManager {
    //Start code logging exercise
    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(SQLiteConnectionManager.class.getName());
    //End code logging exercise
    
    private String databaseURL = "";

    private static final String WORDLE_DROP_TABLE_STRING = "DROP TABLE IF EXISTS wordlist;";
    
    /*
     * String concatenation (`+`) creates multiple intermediate `String` objects, which can be inefficient. 
     * Text blocks are optimized at compile time.
     */
    private static final String WORDLE_CREATE_STRING = """
            CREATE TABLE wordlist (
                id integer PRIMARY KEY,
                word text NOT NULL
            );
            """;
    

    private static final String VALID_WORDS_DROP_TABLE_STRING = "DROP TABLE IF EXISTS validWords;";

    /*
     * String concatenation (`+`) creates multiple intermediate `String` objects, which can be inefficient. 
     * Text blocks are optimized at compile time.
     */
    private static final String VALID_WORDS_CREATE_STRING = """
            CREATE TABLE validWords (
                id integer PRIMARY KEY,
                word text NOT NULL
            );
            """;
    /**
     * Set the database file name in the sqlite project to use
     *
     * @param fileName the database file name
     */
    public SQLiteConnectionManager(String filename) {
        databaseURL = "jdbc:sqlite:sqlite/" + filename;

    }

    /**
     * Connect to a sample database
     *
     * @param fileName the database file name
     */
    public void createNewDatabase(String fileName) {

        try (Connection conn = DriverManager.getConnection(databaseURL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();

                // A log level guard to ensure it only executes 
                // if the appropriate log level is enabled.
               if (logger.isLoggable(Level.INFO)) {
                   logger.log(Level.INFO, "The driver name is {0}", meta.getDriverName());
               }
                logger.log(Level.INFO,"A new database has been created.");

            }
        } catch (SQLException e) {

            // A log level guard to ensure it only executes 
            // if the appropriate log level is enabled.
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }
    }

    /**
     * Check that the file has been cr3eated
     *
     * @return true if the file exists in the correct location, false otherwise. If
     *         no url defined, also false.
     */
    public boolean checkIfConnectionDefined() {
        if ("".equals(databaseURL)) {  // Positioning literals first in String comparisons
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL)) {
                if (conn != null) {
                    return true;
                }
            } catch (SQLException e) {
                // A log level guard to ensure it only executes 
                // if the appropriate log level is enabled.
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, e.getMessage());
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Create the table structures (2 tables, wordle words and valid words)
     *
     * @return true if the table structures have been created.
     */
    public boolean createWordleTables() {
        if ("".equals(databaseURL)) { // Positioning literals first in String comparisons
            return false;
        } else {
            try (Connection conn = DriverManager.getConnection(databaseURL);
                    Statement stmt = conn.createStatement()) {
                stmt.execute(WORDLE_DROP_TABLE_STRING);
                stmt.execute(WORDLE_CREATE_STRING);
                stmt.execute(VALID_WORDS_DROP_TABLE_STRING);
                stmt.execute(VALID_WORDS_CREATE_STRING);
                return true;

            } catch (SQLException e) {
                // A log level guard to ensure it only executes 
                // if the appropriate log level is enabled.
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, e.getMessage());
                }
                return false;
            }
        }
    }

    /**
     * Take an id and a word and store the pair in the valid words
     * 
     * @param id   the unique id for the word
     * @param word the word to store
     */
    public void addValidWord(int id, String word) {     // Need fix check notes

        String sql = "INSERT INTO validWords(id,word) VALUES(?, ?);";
                                                            //'" + id + "','" + word + "'
        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, id);
                pstmt.setString(2, word);

                pstmt.executeUpdate();
                
        } catch (SQLException e) {
            // A log level guard to ensure it only executes 
            // if the appropriate log level is enabled.
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage());
            }
        }

    }

    /**
     * Possible weakness here?
     * 
     * @param guess the string to check if it is a valid word.
     * @return true if guess exists in the database, false otherwise
     */
    public boolean isValidWord(String guess) {
        String sql = "SELECT count(id) as total FROM validWords WHERE word like ?;";

        try (Connection conn = DriverManager.getConnection(databaseURL);
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, guess);
            try (ResultSet resultRows = stmt.executeQuery()) { // Object is closed after use by wrapping it in a try-with-resources block.
                if (resultRows.next()) {
                    int result = resultRows.getInt("total");
                    return result >= 1;
                }
            }

            return false;

        } catch (SQLException e) {
            // A log level guard to ensure it only executes 
            // if the appropriate log level is enabled.
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage());
            }
            return false;
        }

    }
}
