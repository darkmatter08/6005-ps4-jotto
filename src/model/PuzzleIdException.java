package model;

import java.io.IOException;

/**
 * PuzzleIDException represents an exception relating strictly to an invalid puzzle ID
 * Package-Private since it's only used by classes in this package, and 
 * written inline since it relates closely to the makeGuess() method in JottoModel
 * @author jains
 *
 */
public class PuzzleIdException extends IOException { 
    public PuzzleIdException(String s) { 
        super(s);
    }
}