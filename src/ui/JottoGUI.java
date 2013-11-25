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
    private final JLabel guessLabel;
    
    private final JottoTableModel tm;
    private JottoModel model;

    public JottoGUI() {
        super("Jotto on csail.mit");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cp = this.getContentPane();
        GroupLayout layout = new GroupLayout(cp);
        cp.setLayout(layout);
        
        model = new JottoModel();
        
        newPuzzleButton = new JButton();
        newPuzzleButton.setName("newPuzzleButton");
        newPuzzleButton.setText("New Puzzle");
        
        newPuzzleNumber = new JTextField();
        newPuzzleNumber.setName("newPuzzleNumber");
        
        puzzleNumber = new JLabel();
        puzzleNumber.setName("puzzleNumber");
        puzzleNumber.setText("Puzzle # " + model.getPuzzleId());
        
        guess = new JTextField(5);
        guess.setName("guess");
        
        guessLabel = new JLabel("Type a guess here:");
        guessLabel.setLabelFor(guess);
        
        tm = new JottoTableModel();
        guessTable = new JTable(tm);
        guessTable.setName("guessTable");
        //guessTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // REGISTER LISTENERS

        newPuzzleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newPuzzleButtonHelper();
            }
        });
        
        newPuzzleNumber.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newPuzzleButtonHelper();
            }
        });
        
        guess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleGuessHelper();
            }
        });
        
        //TODO lock on tableModel  
        
        buildLayout(layout);
    }
    
    /**
     * Method to build Swing layout via the GroupLayout class
     * @param the GroupLayout to layout, associated with a contentPane
     */
    private void buildLayout(GroupLayout layout) {
        // LAYOUT 
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(puzzleNumber)
                    .addComponent(newPuzzleButton)
                    .addComponent(newPuzzleNumber)
            ).addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE) 
                    .addComponent(guessLabel)
                    .addComponent(guess)
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
            
        onTableUpdate();
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
    
    /**
     *  When the newPuzzleButton is pressed OR enter is typed on the newPuzzleNumber
     *  text field, check the contents of the newPuzzleNumber field. If it's empty, 
     *  then load a random puzzle. Otherwise load the puzzle number from the newPuzzleNumber 
     */
    private void newPuzzleButtonHelper() {
        String puzzleString = newPuzzleNumber.getText();
        newPuzzleNumber.setText("");
        if (puzzleString == null || puzzleString.equals(""))
            model = new JottoModel();
        else {
            try{
                int puzzle = Integer.parseInt(puzzleString);
                if (puzzle >= 0) 
                    model = new JottoModel(puzzle);
                else // bad input
                    throw new NumberFormatException("Puzzle number must be greater than 0");
            } catch (NumberFormatException ne) {
                showDialog("Bad Puzzle Number Input", "JottoGame Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        puzzleNumber.setText("Puzzle # " + model.getPuzzleId());
        tm.clearTable();
        onTableUpdate();
    }
    
    /**
     * When enter is pressed on the guess JTextField, make a guess on the 
     * JottoModel with the contents of the textField. Display errors on the 
     * table when a guess is invalid by catching the exceptions makeGuess throws.
     * 
     * If the guess is correct, alert the user with a new window and by placing it
     * in the table, then reset the game. 
     * 
     * Spawn a new thread for each guess so that the GUI doesn't hang and the user
     * can still make additional guesses. Make sure before each update to the table,
     * the user is still playing the same game. 
     */
    private void handleGuessHelper() {
        new Thread() {
            public void run() {
                JottoModel modelForGuess = new JottoModel(model.getPuzzleId());
                String userGuess = guess.getText();
                guess.setText(""); // reset box to empty
                int rowIndex = tm.addGuessRow(userGuess, null);
                onTableUpdate();
                
                try {
                    // blocking step - subsequent edits to the table must check 
                    // if we're still playing the same game. 
                    modelForGuess.makeGuess(userGuess);
                } catch (InvalidGuessException ige) {
                    // Checking if we're still in the same game.
                    if (modelForGuess.getPuzzleId() == model.getPuzzleId()) { 
                        tm.editGuessRow(rowIndex, "Invalid Guess");
                        onTableUpdate();
                        showDialog(ige.getMessage(), "JottoGame Message", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                } catch (PuzzleIdException pie){
                    showDialog(pie.getMessage() + "\r\n A new puzzle has been loaded", 
                            "JottoGame Error", JOptionPane.ERROR_MESSAGE);
                    model = new JottoModel();
                    tm.clearTable();
                    return;
                } catch (IOException e1) {
                    showDialog(e1.getMessage(), "JottoGame Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String correctPos = modelForGuess.getLastGuessCorrectPos();
                String commonResult = modelForGuess.getLastGuessCommonResult();
                
                // Checking if we're still in the same game.
                if (modelForGuess.getPuzzleId() == model.getPuzzleId()) {
                    // Winning condition check
                    if (correctPos.equals("5") && commonResult.equals("5")) {
                        tm.editGuessRow(rowIndex, "You Win!");
                        showDialog("You win! The secret was: " + userGuess + "!", 
                                "JottoGame Message", JOptionPane.PLAIN_MESSAGE);
                    } else{
                        tm.editGuessRow(rowIndex, modelForGuess.getLastGuessCorrectPos(), 
                                modelForGuess.getLastGuessCommonResult());
                    }
                    onTableUpdate();
                }
            }
        }.start();
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
        if (commonResult != null)
            row.add(commonResult);
        else
            row.add("");
        if (correctPos != null)
            row.add(correctPos);
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
        // check if that row still exists, and if so, update it, otherwise 
        // do nothing for this edit request
        if (data.size() > index) {
            data.get(index).set(2, correctPos);
            if (commonResult != null)
                data.get(index).set(1, commonResult); 
            else
                data.get(index).set(1, "");
        } 
    }
    
    public void editGuessRow(int index, String col2) {
        editGuessRow(index, null, col2);
    }
    
    /**
     * Deletes all table data except the header row
     */
    public void clearTable() {
        data = new ArrayList<List<String>>();
        addGuessRow("Guess", "Correct Positions", "Common Characters");
    }
}

