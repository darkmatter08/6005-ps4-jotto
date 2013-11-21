package model;

/**
 * InvalidGuessException represents an exception relating strictly to an invalid 
 * guess passed into makeGuess(). Extends RuntimeException since the guess's validity
 * is the responsbility of the client. 
 * Package-Private since it's only used by classes in this package, and 
 * written inline since it relates closely to the makeGuess() method in JottoModel
 * @author jains
 *
 */
public class InvalidGuessException extends RuntimeException{
    public InvalidGuessException(String s) {
        super(s);
    }
}