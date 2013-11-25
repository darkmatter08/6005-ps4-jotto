package model;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class JottoModelTest {
    
    /**
     * Testing Strategy:
     *  - Test basic connection to server (2x)
     *  - Test invalid puzzle number
     *  - Test random puzzle instantiation
     *  - Test valid guesses
     *  - Test getLastGuessCorrectPos() and getLastGuessCommonResult()
     *  - Test invalid guess - non length 5 string
     *  - Test guess with *
     */
    
    @Test
    public void connectToServerTest() {
        JottoModel jm = new JottoModel(16952);
        try {
            jm.makeGuess("cargo");
            assertEquals(jm.getLastGuessCommonResult(), "5");
            assertEquals(jm.getLastGuessCorrectPos(), "5");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        jm = new JottoModel(2015);
        try {
            jm.makeGuess("rucks");
            assertEquals(jm.getLastGuessCommonResult(), "5");
            assertEquals(jm.getLastGuessCorrectPos(), "5");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test(expected = PuzzleIdException.class)
    public void invalidPuzzleTest() throws PuzzleIdException, IOException{
        JottoModel jm = new JottoModel(-1);
        jm.makeGuess("rucks");
    }
    
    @Test
    public void randomPuzzleTest() {
        JottoModel jm = new JottoModel();
        try {
            jm.makeGuess("rucks");
        } catch (PuzzleIdException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void validGuessTest() {
        JottoModel jm = new JottoModel(16952);
        try {
            jm.makeGuess("maybe");
            assertEquals(jm.getLastGuessCommonResult(), "1");
            assertEquals(jm.getLastGuessCorrectPos(), "1");
            jm.makeGuess("cargo");
            assertEquals(jm.getLastGuessCommonResult(), "5");
            assertEquals(jm.getLastGuessCorrectPos(), "5");
        } catch (InvalidGuessException e) {
            fail(e.getMessage());  
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    
    @Test
    public void invalidGuessTest() {
        JottoModel jm = new JottoModel(16952);
        try {
            jm.makeGuess("carog");
        } catch (InvalidGuessException e) {
            // Expect this since guess is not a dictionary word.  
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        try {
            jm.makeGuess("kicar");
        } catch (InvalidGuessException e) {
            // Expect this since guess is not a dictionary word.  
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void invalidGuessLengthAndRecoveryTest() {
        JottoModel jm = new JottoModel(16952);
        try {
            jm.makeGuess("cog");
        } catch (InvalidGuessException e) {
            // Expect this since guess is not a dictionary word.  
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        try {
            jm.makeGuess("maybe");
            assertEquals(jm.getLastGuessCommonResult(), "1");
            assertEquals(jm.getLastGuessCorrectPos(), "1");
        } catch (InvalidGuessException e) {
            e.printStackTrace();
            fail(e.getMessage());  
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void guessWithAsteriskTest() {
        JottoModel jm = new JottoModel(16952);
        try {
            jm.makeGuess("may*e");
            assertEquals(jm.getLastGuessCommonResult(), "1");
            assertEquals(jm.getLastGuessCorrectPos(), "1");
        } catch (InvalidGuessException e) {
            e.printStackTrace();
            fail(e.getMessage());  
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
