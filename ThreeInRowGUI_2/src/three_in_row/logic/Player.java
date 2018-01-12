package three_in_row.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Constants, Serializable
{

    private GameData game;
    private String name;
    private List<Token> availableTokens = new ArrayList<>();
    private boolean hasWon;

    public Player(String name, GameData j) 
    {
        this.name = name;
        this.game = j;
        hasWon = false;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }

    public boolean getHasWon() 
    {
        return hasWon;
    }

    public void setHasWon(boolean hasWon) 
    {
        this.hasWon = hasWon;
    }

    public void getNewTokens() 
    {
        availableTokens.clear();        
        
        for(int i=0; i<NUM_TOKENS_TURN; i++){
            availableTokens.add(new Token(this));
        }
        
        hasWon = false;
    }
    
    public List<Token> getAvailableTokens()
    {
        return availableTokens;
    }
    
    public int getNumAvailableTokens()
    {
        return availableTokens.size();
    }

    @Override
    public String toString() 
    {
        return "Player " + name + "\nAvailable tokens: " + availableTokens + "\n" + (hasWon?"HAS WON!\n":"") + "\n";
    }
    
}
