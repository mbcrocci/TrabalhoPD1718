
package three_in_row.ui.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import three_in_row.logic.ObservableGame;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
/** Apresenta um label e campo de texto para alteracao do nome de um jogador..
 * 
 * @author JMSousa (base)
 *
 */
class PlayerNameBox extends JPanel
{
    int nr;
    ObservableGame game;
    JTextField nameField;

    PlayerNameBox(ObservableGame g, int playerNr)
    {	                        
        String nome;

        game = g;
        
        nr = playerNr;
        
        nome = (playerNr==1) ? g.getPlayer1().getName():g.getPlayer2().getName();
        
        nameField = new JTextField(15);
        nameField.setText(nome);        
        
        // Listen for changes in the text
        nameField.getDocument().addDocumentListener(new DocumentListener(){

            @Override
            public void changedUpdate(DocumentEvent e) {
                game.setPlayerName(nr, nameField.getText());
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {		
                game.setPlayerName(nr, nameField.getText());
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {		
                game.setPlayerName(nr, nameField.getText());
            }
	});        
       
        // <Enter> pressed
        nameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.setPlayerName(nr, nameField.getText());
                game.startGame();
            }                        
	});        
        
        setMaximumSize(new Dimension(200,40));        
        setupLayout();
        
    }

    private void setupLayout()
    {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(new JLabel("Player "+nr+" name"));
        add(nameField);
    }    

    public String getText()
    {
        return nameField.getText();
    }
    
}
