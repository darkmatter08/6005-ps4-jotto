 package ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import model.*;

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
    
    private final GroupLayout layout;
    private final JLabel guessLabel;
    
    private JottoModel model = new JottoModel();

    public JottoGUI() {
        super("Jotto on csail.mit");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cp = this.getContentPane();
        layout = new GroupLayout(cp);
        cp.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        newPuzzleButton = new JButton();
        newPuzzleButton.setName("newPuzzleButton");
        newPuzzleButton.setText("New Puzzle");
        
        newPuzzleNumber = new JTextField();
        newPuzzleNumber.setName("newPuzzleNumber");
        //newPuzzleNumber.setText("puzzle number");
        
        puzzleNumber = new JLabel();
        puzzleNumber.setName("puzzleNumber");
        puzzleNumber.setText("Puzzle Number: Random!");
        
        guess = new JTextField(5);
        guess.setName("guess");
        
        guessLabel = new JLabel("Type a guess here:");
        guessLabel.setLabelFor(guess);
        
//        Object[] headers = {"Guess", "Correct Position", "Common Characters"};
//        Object[][] data = {{"blank0"}, {"blank1"}, {"blank2"}};
        final JottoTableModel tm = new JottoTableModel();
        guessTable = new JTable(tm);
        guessTable.setName("guessTable");
        //guessTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // TODO Problem 2, 3, 4, and 5
        
        // REGISTER LISTENERS

        /*
         *  When the newPuzzleButton is pressed, check the contents of
         *  the newPuzzleNumber field. If it's empty, then load a random
         *  puzzle. Otherwise load the puzzle number from the newPuzzleNumber 
         */
        newPuzzleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String puzzleString = newPuzzleNumber.getText();
                if (puzzleString == null || puzzleString.equals("")) {
                    model = new JottoModel();
                    puzzleNumber.setText("Puzzle Number: Random!");
                    tm.clearTable();
                } else {
                    try{
                        int puzzle = Integer.parseInt(puzzleString);
                        if (puzzle >= 0) {
                            model = new JottoModel(puzzle);
                            puzzleNumber.setText("Puzzle Number: " + Integer.toString(puzzle));
                            tm.clearTable();
                        }
                        else { // bad input
                            model = new JottoModel();
                            puzzleNumber.setText("Puzzle Number: " + " Random!");
                        }
                        
                    } catch (NumberFormatException ne) {
                        showDialog("Bad Puzzle Number Input", "JottoGame Error", JOptionPane.ERROR_MESSAGE);
                        ne.printStackTrace();
                        newPuzzleNumber.setText("");
                        return;
                    }
                } 
            }
        });
        
        /*
         * When enter is pressed on the guess JTextField, make a guess on the 
         * JottoModel with the contents of the textField. Display errors on the 
         * table when a guess is invalid by catching the errors makeGuess throws.
         * 
         * If the guess is correct, alert the user and reset the game. 
         */
        guess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    public void run() {
                        JottoModel newModel = new JottoModel(model.getPuzzleId());
                        String userGuess = guess.getText();
                        guess.setText(""); // reset box to empty
                        int index = tm.addGuessRow(userGuess, null);
                        try {
                            newModel.makeGuess(userGuess);
                        } catch (InvalidGuessException ige) {
                            ige.printStackTrace();
                            tm.editGuessRow(index, "Invalid Guess");
                            onTableUpdate();
                            showDialog(ige.getMessage(), "JottoGame Message", JOptionPane.ERROR_MESSAGE);
                            //tm.addGuessRow("Invalid Guess", null);
                            return;
                        } catch (PuzzleIdException pie){
                            pie.printStackTrace();
                            showDialog(pie.getMessage() + "\r\n A new puzzle has been loaded", 
                                    "JottoGame Error", JOptionPane.ERROR_MESSAGE);
                            model = new JottoModel();
                            tm.clearTable();
                            return;
                        }catch (IOException e1) {
                            showDialog(e1.getMessage(), 
                                    "JottoGame Error", JOptionPane.ERROR_MESSAGE);
                            e1.printStackTrace();
                            return;
                        }
                        String correctPos = newModel.getLastGuessCorrectPos();
                        String commonResult = newModel.getLastGuessCommonResult();
                        // Winning condition check
                        if (correctPos.equals("5") && commonResult.equals("5")) {
                            showDialog("You win! The secret was: " + userGuess + "!", "JottoGame Message", JOptionPane.PLAIN_MESSAGE);
                            tm.editGuessRow(index, "You Win!");
                        }
                        else
                            tm.editGuessRow(index, newModel.getLastGuessCorrectPos(), newModel.getLastGuessCommonResult());
                        onTableUpdate(); // resize JTable
                        }
                }.start();
            }
        });
        
        
        // LAYOUT 
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(puzzleNumber)
                    .addComponent(newPuzzleButton)
                    .addComponent(newPuzzleNumber)
            ).addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE) // TODO remove for table fix
                    .addComponent(guessLabel)
                    .addComponent(guess)
                    //.addComponent(guessTable)
            ).addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(guessTable)
            )
        );
        
        layout.setHorizontalGroup(
            layout.createParallelGroup()
            .addGroup(
                layout.createParallelGroup()
                .addGroup(
                    // top of menu group
                    layout.createSequentialGroup()
                        .addComponent(puzzleNumber)
                        .addComponent(newPuzzleButton)
                        .addComponent(newPuzzleNumber)
                ).addGroup(
                    // bottom of menu group
                    layout.createSequentialGroup()
                        .addComponent(guessLabel)
                        .addComponent(guess)
                )
            ).addGroup(
                // Table
                layout.createSequentialGroup()
                    .addComponent(guessTable)
            )
        );
        
        this.pack();
    }
    
    /**
     * Updates window and component sizes, called after adding a row
     *  to the guessTable. 
     */
    private void onTableUpdate() {
        this.pack();
        this.setSize(400, 300);
    }
    
    /**
     * Shows a dialog to the user.
     * @param message String message to be shown
     * @param title String title to be shown in the titlebar
     * @param messageType int representing messageType. Must be from JOptionPane.*
     */
    private void showDialog(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JottoGUI main = new JottoGUI();
                main.setSize(400, 300);
                main.setVisible(true);
            }
        });
    }
}

/**
 * Table Model for the guessTable. Dynamically sized. 
 * @author jains
 *
 */
class JottoTableModel extends AbstractTableModel {
    private List<List<String>> data = new ArrayList<List<String>>();
    
    public JottoTableModel(){
        super();
        addGuessRow("Guess", "Correct Positions", "Common Characters");
    }
    
    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return data.get(0).size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }
    
    /**
     * Shortcut method for addGuessRow, only 1 or 2 entries
     * @param guess col1 String
     * @param col2 String, ok to be null if you only want 1 entry
     * @return int index of row added
     */
    public int addGuessRow(String guess, String col2) {
        return addGuessRow(guess, col2, null);
    }
    
    /**
     * Adds in a new row
     * @param guess col1 String
     * @param correctPos col2 String
     * @param commonResult col3 String
     * @return int index of row added
     */
    public int addGuessRow(String guess, String correctPos, String commonResult) {
        List<String> row = new ArrayList<String>();
        row.add(guess);
        if (correctPos != null)
            row.add(correctPos);
        else
            row.add("");
        if (commonResult != null)
            row.add(commonResult);
        else
            row.add("");
        data.add(row);
        return data.size()-1;
    }
    
    /**
     * Shortcut method for addGuessRow
     * @param guess col1 String
     * @param correctPos col2 int
     * @param commonResult col3 int
     * @return int index of row added
     */
    public int addGuessRow(String guess, int correctPos, int commonResult) {
        return addGuessRow(guess, Integer.toString(correctPos), Integer.toString(commonResult));
    }
    
    public void editGuessRow(int index, String correctPos, String commonResult) {
        data.get(index).set(1, correctPos);
        if (commonResult != null)
            data.get(index).set(2, commonResult); // 
        else
            data.get(index).set(2, "");
    }
    
    public void editGuessRow(int index, String col2) {
        editGuessRow(index, col2, null);
    }
    
    /**
     * Deletes all table data except the header row
     */
    public void clearTable() {
        for (int i = 1; i < data.size(); i++) { // TODO does it check the condition before running the loop? - yes
            data.remove(i);
        }
    }
}
