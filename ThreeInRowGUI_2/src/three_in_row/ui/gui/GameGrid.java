
package three_in_row.ui.gui;

import java.awt.GridLayout;
import javax.swing.JPanel;
import three_in_row.logic.Constants;
import three_in_row.logic.ObservableGame;

/** Grelha de celulas... E' apenas um contentor
 * 
 * @author JMSousa (base)
 *
 */
class GameGrid extends JPanel implements Constants
{
    ObservableGame game;
    
    GameGrid(ObservableGame g)
    {
        game = g;        
        setupLayout();
    }

//    void setupLayout()
//    {
//        JPanel p;
//        
//        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        
//        for(int i=0 ; i<DIM ; i++){            
//            p = new JPanel();
//            
//            for(int j=0 ; j<DIM ; j++){
//                GameCell cell = new GameCell(game,i,j);
//                p.add(cell);
//            }
//            
//            add(p);            
//        }
//        
//    }

     void setupLayout()
    {
        JPanel p = new JPanel();
        
        p.setLayout(new GridLayout(DIM, DIM ,10,10));
        
        for(int i=0 ; i<DIM ; i++){            
   
            
            for(int j=0 ; j<DIM ; j++){
                GameCell cell = new GameCell(game,i,j);
                p.add(cell);
            }
            
                   
        }
          add(p);   
    }

}

