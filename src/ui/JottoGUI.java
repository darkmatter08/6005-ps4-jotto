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
                    puzzleNumber.setText("Puzzle Number: " + " Random!");
                } else {
                    try{
                        int puzzle = Integer.parseInt(puzzleString);
                        model = new JottoModel(puzzle);
                        puzzleNumber.setText("Puzzle Number: " + Integer.toString(puzzle));
                    } catch (NumberFormatException ne) {
                        // show an error window
                        ne.printStackTrace();
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
                try {
                    model.makeGuess(guess.getText());
                } catch (InvalidGuessException ige) {
                    ige.printStackTrace();
                    // update table to show thread. 
                } catch (PuzzleIdException pie){
                    pie.printStackTrace();
                    // show error window
                    // update table to clear
                }catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                tm.addGuessRow(guess.getText(), model.getLastGuessCorrectPos(), model.getLastGuessCommonResult());
                guess.setText(""); // reset box to empty
                onTableUpdate(); // resize JTable
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
    
    public void onTableUpdate() {
        this.pack();
        this.setSize(400, 300);
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
     * @return int index of row
     */
    public int addGuessRow(String guess, String correctPos, String commonResult) {
        List<String> row = new ArrayList<String>();
        row.add(guess);
        row.add(correctPos);
        row.add(commonResult);
        data.add(row);
        return data.size()-1;
    }
    
    public int addGuessRow(String guess, int correctPos, int commonResult) {
        return addGuessRow(guess, Integer.toString(correctPos), Integer.toString(commonResult));
    }
    
    //public void editGuessRow(int index) {}
}
