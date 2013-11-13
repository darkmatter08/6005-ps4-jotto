package ui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * // TODO Write specifications for your JottoGUI class.
 * // Remember to name all your components, otherwise autograder will give a zero.
 * // Remember to use the objects newPuzzleButton, newPuzzleNumber, puzzleNumber,
 * // guess, and guessTable in your GUI!
 */
public class JottoGUI extends JFrame {

    // remember to use these objects in your GUI:
    private final JButton newPuzzleButton;
    private final JTextField newPuzzleNumber;
    private final JLabel puzzleNumber;
    private final JTextField guess;
    private final JTable guessTable;

    public JottoGUI() {
        newPuzzleButton = new JButton();
        newPuzzleButton.setName("newPuzzleButton");
        newPuzzleNumber = new JTextField();
        newPuzzleNumber.setName("newPuzzleNumber");
        puzzleNumber = new JLabel();
        puzzleNumber.setName("puzzleNumber");
        guess = new JTextField();
        guess.setName("guess");
        guessTable = new JTable();
        guessTable.setName("guessTable");
        
        // TODO Problem 2, 3, 4, and 5
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JottoGUI main = new JottoGUI();

                main.setVisible(true);
            }
        });
    }
}
