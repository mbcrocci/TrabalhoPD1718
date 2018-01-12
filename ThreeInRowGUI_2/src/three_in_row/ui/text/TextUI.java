package three_in_row.ui.text;

import three_in_row.logic.ObservableGame;
import three_in_row.logic.states.AwaitPlacement;
import three_in_row.logic.states.AwaitReturn;
import three_in_row.logic.states.AwaitBeginning;
import three_in_row.logic.states.IStates;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import three_in_row.logic.GameModel;
import three_in_row.files.FileUtility;

/** 
 * @author Jose Marinho
 *
 */

public class TextUI 
{

    private ObservableGame game;
    private boolean quit = false;

    public TextUI(ObservableGame game) 
    {
        this.game = game;
    }

    public void uiAwaitBeginning() 
    {
        Scanner sc = new Scanner(System.in);
        BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
        String option, fileName;
        char c;
        String [] words;
        int num;
        String name;
        
        System.out.println("\n=== AWAITING FOR THE BEGINNING OF THE GAME ===\n");
        System.out.println("Number of players: " + game.getNumPlayers() + "\n");
        System.out.println(game.getPlayer1() != null ? "" + game.getPlayer1() : "");
        System.out.println(game.getPlayer2() != null ? "" + game.getPlayer2() : "");
        
        
        if((game.getPlayer1()!= null && game.getPlayer1().getHasWon()) || 
           (game.getPlayer2()!= null && game.getPlayer2().getHasWon() )){
            
            System.out.println(game.gridToString());
        
        }
        
        while (true) {
            
            do{
                
                System.out.println();
                System.out.println("0 - Quit");
                System.out.println();
                System.out.println("1 - Set the number of players");
                System.out.println("2 - Set the name(s) of the player(s)");
                System.out.println("3 - Start");
                System.out.println();
                System.out.println("4 - Continue from a previously saved state");
                System.out.println();
                System.out.print("> ");

                option = sc.next();
                
                if(option.length() >= 1)
                    c = option.charAt(0);
                else
                    c = ' ';
                
            }while(c < '0' || c > '4');
            
            switch(c){
                
                case '0':
                    quit = true;
                    return;

                case '1':
                    
                    System.out.println("Number of players (fixed in this version): 2");
                    game.setNumberPlayers(2);
                    
                    return;
                
                case '2':
                    
                    if(game.getNumPlayers()<1)
                        return;
                    
                    System.out.print("Enter the number of a player [1-" + game.getNumPlayers() + "] followed by its name: ");
                    
                    try{
                        
                        option = bin.readLine().trim();
                        words = option.split(" ");
                        
                        if(words.length != 2)
                            return;
                                                
                        num = Integer.parseInt(words[0]);
                        
                        if(num < 1 || num > game.getNumPlayers())
                            return;
                        
                        name = words[1];

                        game.setPlayerName(num, name);
                        
                        System.out.println("number: " + num + "\tname: " + name);
                        
                    }catch (IOException | NumberFormatException ex) {System.out.println(ex);}
                                        
                    return;
                
                case '3': 
                    
                    System.out.println("Game started");
                    
                    game.startGame();
                    return;
                    
                case '4':
                    
                    System.out.print("File name: ");
                    
                    try{
                        
                        fileName = bin.readLine();
                        
                        if(fileName==null)
                            return;
                        
                        if(fileName.length() < 1)
                            return;
                        
                        game.setGameModel((GameModel)FileUtility.retrieveGameFromFile(new File(fileName))/*retrieveGameFromFile(fileName)*/);
                        
                    }catch(IOException | ClassNotFoundException e){
                        System.out.println("Operation failed: " + e);
                    }                    
                    
                    return;
                    
                default:
                    return;
                    
            } //switch
            
        } //while
    
    } //uiWaitBeginning    

    public void uiAWaitPlacement()
    {
        Scanner sc = new Scanner(System.in);
        
        System.out.println();
        System.out.println("\n=== AWAITING FOR THE PLACEMENT OF A TOKEN === \n");
        System.out.println(game.getPlayer1());
        System.out.println(game.getPlayer2());
        System.out.println("\nCurrent player: " + game.getCurrentPlayerName());
        System.out.println(game.gridToString());

        System.out.println("\nPlay: line [1-3]  column [1-3]\n\nSave current state and quit: 0\nQuit: -1");
        System.out.println();
        System.out.print(game.getCurrentPlayerName() + "> ");
                
        while(!sc.hasNextInt()){
            sc.next();
        }
        
        int line = sc.nextInt();
        
        if(line == 0){
                    
            try{
                
                handleSaveGameToFileOption();
                game = new ObservableGame();
                
            }catch(IOException e){            
                System.out.println("Operation failed: " + e);
            }
            
            return;
        }
        
        if (line == -1){
            game.quit();
            return;
        }
        
        while(!sc.hasNextInt()){
            sc.next();
        }
        
        int column = sc.nextInt();
        
        game.placeToken(line-1, column-1);
        
    }

    public void uiAWaitReturn() 
    {
        
        System.out.println();
        System.out.println("\n=== AWAITING FOR THE RETURNING OF A TOKEN === \n");
        System.out.println(game.getPlayer1());
        System.out.println(game.getPlayer2());
        System.out.println("\nCurrent player: " + game.getCurrentPlayerName());
        System.out.println(game.gridToString());

        System.out.println("\nReturn : line [1-3] column [1-3]\n\nSave current state and quit: 0\nQuit: -1");
        System.out.println();
        System.out.print(game.getCurrentPlayerName() + "> ");
        
        Scanner sc = new Scanner(System.in);
        
        while(!sc.hasNextInt()){
            sc.next();
        }
        
        int line = sc.nextInt();
        
         if(line == 0){            
                    
            try{
                
                handleSaveGameToFileOption();
                game = new ObservableGame();
                
            }catch(IOException e){            
                System.out.println("Operation failed: " + e);
            }
            
            return;
            
        }
        
        if (line == -1) {
            game.quit();
            return;
        }
        
        while(!sc.hasNextInt()){
            sc.next();
        }
        
        int column = sc.nextInt();
        
        game.returnToken(line-1, column-1);
        
    }
    
    private void handleSaveGameToFileOption() throws IOException
    {
        String fileName;
        
        System.out.print("File name: ");
        fileName = new BufferedReader(new InputStreamReader(System.in)).readLine();

        if(fileName==null)
            return;

        if(fileName.length() < 1)
            return;

        FileUtility.saveGameToFile(new File(fileName), game.getGameModel());            
    }
        
    public void run() 
    {

        while (!quit) {
            
            IStates state = game.getState();
           
            if (state instanceof AwaitBeginning) {
                uiAwaitBeginning();
            } else if (state instanceof AwaitPlacement) {
                uiAWaitPlacement();
            } else if (state instanceof AwaitReturn) {
                uiAWaitReturn();
            }
            
        }

    }
}
