import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import three_in_row.logic.ObservableGame;


public class GameServer implements Runnable {
    
    private String selfHostname;
    private int selfPort;
    
    private String databaseAdress;
    private DatagramSocket udpSocket;
    private Socket serverSocket;
    
    private List<ObservableGame> gameList;
    
    
    
    public GameServer() {
        
    }
    
    public static void main(String []args) {
        if (args.length != 2) {
            System.out.println("Sintaxe: java GameServer <ip servidor de gestao> <porto servidor de gestao>");
            
            BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
          
            try {
                System.out.print("Introduza o ip do servidor de gestao: ");
                String ipAddress = buff.readLine();

                System.out.print("Introduza a porta do servidor de gestao: ");
                int port = Integer.parseInt(buff.readLine());

                if (ipAddress.isEmpty() || port <= 0) {
                    System.out.println("Dados invalidos. Programa terminado.");
                    return;
                }
            } catch (IOException e) {
                System.out.println("[ERROR] Problema ao tentar ler consola");
                return;
            }
        }
        
        ServerSocket serverSocket = null;
        
        try {
            Socket s = serverSocket.accept();
            
            
        } catch (IOException e) {
            
        }             
    }
    
    @Override
    public void run() {
        while (true) {
            
        }
    }

    public void handleHeartbeat() {
        
    }
    
}
