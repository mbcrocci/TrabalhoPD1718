
package three_in_row.ui.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import three_in_row.logic.ObservableGame;
import three_in_row.logic.states.AwaitBeginning;

/** Painel que apresenta as varias opcoes de configuracao e permite iniciar o game
 *  Observa o game para se tornar invisivel/visivel conforme um game esteja em curso ou nao.
 * @author JMSousa (base)
 *
 */

class StartOptionPanel extends JPanel implements Observer
{
    ObservableGame game;
    
    JButton start=new JButton("Start");
    PlayerNameBox player1Name;		
    PlayerNameBox player2Name;

    StartOptionPanel(ObservableGame g)
    {
        game=g;
        game.addObserver(this);
        
        setBackground(Color.GREEN);
        setupComponents();
        setupLayout();
        
        setVisible(game.getState() instanceof AwaitBeginning);
    }


   private void setupLayout()
    {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        start.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        add(Box.createVerticalStrut(10));
        add(start);
        
        player1Name.setMinimumSize(new Dimension(120,20));
        player2Name.setMinimumSize(new Dimension(120,20));
        
        player1Name.setAlignmentX(Component.CENTER_ALIGNMENT);
        player2Name.setAlignmentX(Component.CENTER_ALIGNMENT);
        player1Name.setOpaque(false);
        player2Name.setOpaque(false);
        
        add(Box.createVerticalStrut(10));
        add(player1Name);
        
        add(Box.createVerticalStrut(10));
        add(player2Name);
        
        validate();
    }
 
    
    private void setupComponents()
    {
        player1Name = new PlayerNameBox(game,1);	
        player2Name = new PlayerNameBox(game,2);

        start.addActionListener(new ActionListener(){        
            @Override
            public void actionPerformed(ActionEvent ev){
                game.setPlayerName(1, player1Name.getText());
                game.setPlayerName(2, player2Name.getText());
                game.startGame();
            }
        });
        
    }

    @Override
    public void update(Observable o, Object arg)
    {
        setVisible(game.getState() instanceof AwaitBeginning);
    }
	
}
