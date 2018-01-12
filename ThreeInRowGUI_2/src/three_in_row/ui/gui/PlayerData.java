
package three_in_row.ui.gui;

import three_in_row.logic.Player;
import three_in_row.logic.ObservableGame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;


/** Painel que apresenta os dados do jogador:
 * indica o nome, conteudo da mao, player activo e apresenta mensagem de vitoria se adequado.
 * 
 * @author JMSousa (base)
 *
 */

class PlayerData extends JPanel implements Observer
{
    JPanel tokensInHand;
    JLabel victoryMessage;
    JLabel name;
    
    ObservableGame game;
    int playerNumber;

    PlayerData(ObservableGame game, int playerNumber)
    {
        this.game=game;
        this.game.addObserver(this);
        this.playerNumber=playerNumber;

        /** Painel que apresenta o conteudo da mao do player
         * E' definido como um classe interna anonima (redefinindo paintComponent).
        */
        tokensInHand = new JPanel(){		

            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                Player player = (playerNumber==1 ? game.getPlayer1() : game.getPlayer2());
                
                int availableTokens = player.getNumAvailableTokens();
                int width=(getWidth()/3);
                int height=getHeight();
                int side=(width>height)?height:width;
                int margin=5;

                for(int i=0;i<availableTokens;i++){
                    g.drawImage(GameCell.getPlayerIcon(player,game), (i*side)+margin,margin,side-2*margin,side-2*margin,this);
                }
                g.drawRect(0,0,getWidth()-1,getHeight()-1);
            }

        };		

        victoryMessage=new JLabel("WINNER !!");
        
        name=new JLabel();
        
        setupLayout();
    }

    private void setupLayout()
    {
        Player player = (playerNumber==1 ? game.getPlayer1() : game.getPlayer2());
        
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        
        setPreferredSize(new Dimension(200,80));
                
        name.setText(player.getName());
        name.setAlignmentX(CENTER_ALIGNMENT);
        name.setAlignmentY(CENTER_ALIGNMENT);
        
        tokensInHand.setAlignmentY(CENTER_ALIGNMENT);
        tokensInHand.setBackground(Color.WHITE);
        
        victoryMessage.setVisible(false);
        victoryMessage.setAlignmentX(CENTER_ALIGNMENT);
        victoryMessage.setFont(new Font("Dialog",Font.BOLD,30));
 
        add(name);
        add(victoryMessage);
        add(tokensInHand);

        if(game.getCurrentPlayer()==player){
            setBackground(Color.YELLOW);
        }else{
            setBackground(Color.gray);		
        }
    }

   @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);

        Player player = (playerNumber==1 ? game.getPlayer1() : game.getPlayer2());
        
        if(game.getCurrentPlayer()==player){
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(8));  //espessura da linha
            g2.setColor(Color.GREEN);
            g2.drawRect(0,0,getWidth(),getHeight());   
        }
    }

   
@Override
	public void update(Observable arg0, Object arg1) {
        
        Player player = (playerNumber==1 ? game.getPlayer1() : game.getPlayer2());
        
        name.setText(player.getName());
        
        if(game==null){
            return;
        }
        
        if(game.getCurrentPlayer()==player){
            setBackground(Color.YELLOW);
        }else{
            setBackground(Color.gray);
        }
                
        if(game.isOver()){
            tokensInHand.setVisible(!game.hasWon(player));
            victoryMessage.setVisible(game.hasWon(player));
        }else{
            tokensInHand.setVisible(true); 
            victoryMessage.setVisible(false);   
        } 
    }
		
}
