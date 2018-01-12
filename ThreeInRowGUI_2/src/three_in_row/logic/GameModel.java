package three_in_row.logic;

import three_in_row.logic.states.AwaitBeginning;
import three_in_row.logic.states.IStates;
import java.io.Serializable;

/**
 *
 * @author Jose Marinho
 */

public class GameModel  implements Serializable
{
    
    private GameData gameData;
    private IStates state; 
    
    public GameModel()
    {
        gameData = new GameData();
        setState(new AwaitBeginning(gameData));
    }

    public GameData getGameData() {
        return gameData;
    }

    public void setGameData(GameData gameData)
    {
        this.gameData = gameData;
    }

    public IStates getState()
    {
        return state;
    }
    
    private void setState(IStates state)
    {
        this.state = state;
    }        
    
     // Methods retrieve data from the game
    
    public String gridToString()
    {
        return gameData.gridToString();
    }                    

    public int getNumPlayers()
    {
        return gameData.getNumPlayers();
    }
    
    public Player getCurrentPlayer() 
    {
        return gameData.getCurrentPlayer();
    }

    public Player getNotCurrentPlayer() 
    {
        return gameData.getNotCurrentPlayer();
    }
    
    public Player getPlayer1()
    {
        return gameData.getPlayer1();
    }

    public Player getPlayer2()
    {
        return gameData.getPlayer2();
    }

    public Token getToken(int line, int column) 
    {
        return gameData.getToken(line, column);
    }
    
    public String grelhaToString()
    {
        return gameData.gridToString();
    }

    public int getNumCurrentPlayer()
    {
        return gameData.getNumCurrentPlayer();
    }

    public String getCurrentPlayerName()
    {
        return gameData.getCurrentPlayer().getName();
    }
    
    public boolean isOver() 
    {
        return gameData.isOver();
    }
    
    public boolean hasWon(Player player) 
    {
        return gameData.hasWon(player);
    }
    
    // Methods that are intended to be used by the user interfaces and that are delegated in the current state of the finite state machine 
    
    public void setNumberPlayers(int num)
    {
        setState(getState().setNumberPlayers(num));
    }

    public void setPlayerName(int num, String name) 
    {
        setState(getState().setName(num, name));
    }

    public void startGame()
    {
        setState(getState().startGame());
    }

    public void placeToken(int line, int column)
    {
        setState(getState().placeToken(line, column));
    }

    public void returnToken(int line, int column)
    {
        setState(getState().returnToken(line, column));
    }

    public void quit()
    {
        setState(getState().quit());
    }

}
