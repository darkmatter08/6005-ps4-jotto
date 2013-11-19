package model;

import java.net.*;
import java.io.*;

/**
 * // TODO Write specifications for your JottoModel!
 */
public class JottoModel {
    
    final private String GAME_URL;
    private String lastGuessCorrectPos;
    private String lastGuessCommonResult;
    
    /**
     * Constructor to select random puzzle
     */
    public JottoModel() {
        this((int) (Math.random() * Integer.MAX_VALUE));
    }
    
    /**
     * Constructor to select a predetermined puzzle
     * @param puzzle int representing a puzzle number (on server)
     */
    JottoModel(int puzzle) {
        String BASE_URL = "http://courses.csail.mit.edu/6.005/jotto.py?";
        String PUZZLE_QS = "puzzle=";
        String GUESS_QS = "&guess=";
        GAME_URL = BASE_URL + PUZZLE_QS + new Integer(puzzle).toString() + GUESS_QS;
    }
    
    /**
     * Tests a guess against the real Jotto word. Can check the results of the
     *  guess via the getLastGuessCorrectPos() and the getLastGuessCommonResult()
     * @param String guess the guess to be tested against the real value
     * @throws IOException 
     */
    public void makeGuess(String guess) throws IOException {
        URL finalURL = new URL(GAME_URL + guess);
        URLConnection uc = finalURL.openConnection();
        uc.setDoOutput(true);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(
                uc.getInputStream()));
        String getBack = in.readLine();
        String results[] = getBack.split(" ");
        String result0 = results[0];
        if (result0.equals("guess")){
            lastGuessCommonResult = results[1];
            lastGuessCorrectPos = results[2];
            return;
        }
        else if (result0.equals("error")) {
            String result1 = results[1].substring(0, 1);
            if (result1.equals("0"))
                throw new IOException("Ill-formatted request");
            else if (result1.equals("1"))
                throw new PuzzleIdException("Non-number puzzle ID");
            else if (result1.equals("2"))
                throw new InvalidGuessException("Invalid guess. Length of guess != 5 or guess is not a dictionary word.");
        }
        
        // Should never reach here
    }
    
    /**
     * Call after makeGuess() has been called at least once
     * @return String representing the number of letters from the last guess 
     *  in the correct position. 
     */
    public String getLastGuessCorrectPos() {
        if (lastGuessCorrectPos == null)
            throw new RuntimeException("incorrect call order");
        return lastGuessCorrectPos;
    }
    
    /**
     * Call after makeGuess() has been called at least once
     * @return String representing the number of common letters from the last 
     *  guess.
     */
    public String getLastGuessCommonResult() {
        if (lastGuessCorrectPos == null)
            throw new RuntimeException("incorrect call order");
        return lastGuessCommonResult;
    }
}

/**
 * PuzzleIDException represents an exception relating strictly to an invalid puzzle ID
 * Package-Private since it's only used by classes in this package, and 
 * written inline since it relates closely to the makeGuess() method in JottoModel
 * @author jains
 *
 */
class PuzzleIdException extends IOException { 
    PuzzleIdException(String s) { 
        super(s);
    }
}

/**
 * InvalidGuessException represents an exception relating strictly to an invalid 
 * guess passed into makeGuess(). Extends RuntimeException since the guess's validity
 * is the responsbility of the client. 
 * Package-Private since it's only used by classes in this package, and 
 * written inline since it relates closely to the makeGuess() method in JottoModel
 * @author jains
 *
 */
class InvalidGuessException extends RuntimeException{
    InvalidGuessException(String s) {
        super(s);
    }
}