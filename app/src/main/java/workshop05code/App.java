package workshop05code;

import java.io.BufferedReader;
//Included for the logging exercise
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author sqlitetutorial.net
 */
public class App {
    // Start code for logging exercise


    static {
        // must set before the Logger
        // loads logging.properties from the classpath
        try {// resources\logging.properties
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());
    // End code for logging exercise
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.log(Level.INFO,"Wordle created and connected.");
        } else {
            logger.log(Level.INFO,"Not able to connect. Sorry!");
            return;
        }
        if (wordleDatabaseConnection.createWordleTables()) {
            logger.log(Level.INFO,"Wordle structures in place.");
        } else {
            logger.log(Level.INFO,"Not able to launch. Sorry!");
            return;
        }

        // log some words to valid 4 letter words from the data.txt file
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                logger.log(Level.INFO,line);
                wordleDatabaseConnection.addValidWord(i, line);
                i++;
            }

        } catch (IOException e) {
            logger.log(Level.WARNING,"Not able to load . Sorry!", e);
           
            // A log level guard to ensure it only executes 
            // if the appropriate log level is enabled.
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage());
            }
           
            return;
        }

        // let's get them to enter a word
            
        try (Scanner scanner = new Scanner(System.in)) {
            String guess = scanner.nextLine();

            while (!"q".equals(guess)) {
                 // Validate the input to be exactly 4 lowercase letters
                if (!guess.matches("^[a-z]{4}$") && logger.isLoggable(Level.WARNING)) {
                    // A log level guard to ensure it only executes 
                    // if the appropriate log level is enabled.
                    logger.log(Level.WARNING, "{0} is Invalid guess!\n", guess);
                } else if (wordleDatabaseConnection.isValidWord(guess)) {
                    logger.log(Level.INFO, "Successful login\n");
                    System.out.println("Success! It is in the list.\n");  // System.out is not too secure  I know but I could not find easier way
                    
                } else {
                    // A log level guard to ensure it only executes 
                    // if the appropriate log level is enabled.
                    if (logger.isLoggable(Level.SEVERE)) {
                        logger.log(Level.SEVERE,"{0} is invalid word. This word is NOT in the list.\n", guess);
                    }    
                }


                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Message", e);
        } 

    }
}