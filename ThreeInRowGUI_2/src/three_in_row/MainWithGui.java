package three_in_row;

import three_in_row.logic.ObservableGame;
import three_in_row.ui.gui.ThreeInRowView;

public class MainWithGui 
{

    public static void main(String[] args)
    {                
        ThreeInRowView GUI = new ThreeInRowView(new ObservableGame());
    }
    
}
