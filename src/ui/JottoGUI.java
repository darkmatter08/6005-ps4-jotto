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
 * JottoGUI provides a GUI to play the JottoGame that's represented JottoModel. 
 * A user can select a puzzle by it's puzzle number, or he can play a random puzzle
 * His guesses and their result - the number of characters in common and the number 
 * of characters in the correct position - will appear in a table for the user to see.
 * The table records all his guesses, making it easy for him to reference his past 
 * guesses and their results. 
 * If he wins, a window will alert him, as well as appearing on the feedback table. 
 * If he makes an invalid submission, such as entering a non-dictionary word or 
 * entering a word not of length 5, he will see an error window, as well as recording
 * the invalid guess on the table. 
 * 
 * He can use the keyboard or the mouse to interact with the UI.
 * 
 * It remains responsive even when there is a slow server connection, via multithreading.
 * The user can continue to make guesses even when the server is slow, and the UI will
 * update when the server returns the result.
 * 
 * THREAD SAFETY: 
 * The blocking code in the program is the call to the JottoModel model.makeGuess() 
 *  as it may wait to return from the server. This implementation deals with this 
 *  problem by spawning a new thread for each guess request by the user. Because 
 *  JottoModel is not threadsafe (my design decision) since it updates it's instance 
 *  fields after each makeGuess() request completes, this implementation creates
 *  a new instance of JottoModel for each thread, whose lifetime is the same as the
 *  thread. The JottoModel that is stored in the JottoGUI isn't used to make requests
 *  to the server - rather it is used to keep track of which puzzle the user is 
 *  currently playing. 
 *  
 * The data that could be modified concurrently is the JottoTableModel. However, 
 *  there is no need to lock on that object since each thread modifies it's own row 
 *  only. Each thread 'claims' it's row because it only modifies the row it added. 
 * The only time this could be in conflict is when there is a long running makeGuess() 
 *  request occurring (i.e. with *) and meanwhile a user loads a new puzzle and makes 
 *  a short guess. Then these two threads could have the same row. However, every 
 *  thread enforces the claiming condition by checking that the puzzle that the guess 
 *  was made on is the same as the current guess. With that check, there can only be
 *  1 thread that claims each row. Thus no need for locking the data - and the 
 *  implementation is thread safe. 
 * 
 * @author jains
 */
public class JottoGUI extends JFrame {

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
                int rowIndex = tm.addGuessRow(userGuess);
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
                        tm.editGuessRow(rowIndex, modelForGuess.getLastGuessCommonResult(), 
                                modelForGuess.getLastGuessCorrectPos());
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
        addGuessRow("Guess", "Common Characters", "Correct Positions");
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
     * Adds in a new row
     * @param c0 col0 String
     * @param c1 col1 String
     * @param c2 col2 String
     * @return int index of row added
     */
    public int addGuessRow(String c0, String c1, String c2) {
        List<String> row = new ArrayList<String>();
        row.add(c0);
        if (c1 != null)
            row.add(c1);
        else
            row.add("");
        if (c2 != null)
            row.add(c2);
        else
            row.add("");
        data.add(row);
        return data.size()-1;
    }
    
    /**
     * Shortcut method for addGuessRow, only 1 or 2 entries
     * @param guess col0 String
     * @return int index of row added
     */
    public int addGuessRow(String guess) {
        return addGuessRow(guess, null, null);
    }
    
    public void editGuessRow(int index, String c1, String c2) {
        // check if that row still exists, and if so, update it, otherwise 
        // do nothing for this edit request
        if (data.size() > index) {
            if (c1 != null)
                data.get(index).set(1, c1); 
            else
                data.get(index).set(1, "");
            if (c2 != null)
                data.get(index).set(2, c2);
            else
                data.get(index).set(2, "");
        } 
    }
    
    public void editGuessRow(int index, String c1) {
        editGuessRow(index, c1, null);
    }
    
    /**
     * Deletes all table data except the header row
     */
    public void clearTable() {
        data = new ArrayList<List<String>>();
        addGuessRow("Guess", "Common Characters", "Correct Positions");
    }
}

