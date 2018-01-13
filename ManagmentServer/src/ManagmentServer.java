

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

public class ManagmentServer implements Runnable {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_USER = "user";
    static final String DB_PASS = "password";
    
    List<Socket> gameServers;
    Connection conn;
    
    HeartbeatReceiver heartbeatReceiver;
    
    public ManagmentServer(Connection conn, String dataseAddress) throws SocketException {
        this.conn = conn;
        this.heartbeatReceiver = new HeartbeatReceiver(dataseAddress);
    }
    

    public static void main(String[] args) {
        
        String databaseAddress;
        if (args.length != 1) {
            System.out.println("Sintaxe: java ManagmentServer <ip base dados>");
            
            BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Introduza o ip da base de dados: ");
            
            try {
                databaseAddress = buff.readLine();
                
                if (databaseAddress.isEmpty())
                    return;
                
            } catch (IOException e) {
                System.out.println("[ERRO] problema ao ler da consola.");
                return;
            }
        } else {
            databaseAddress = args[0];
        }
        
        String db_url ="jdbc:mysql://" + databaseAddress + "/trabalhopd";
        Connection conn;
                
        // Fazer ligacao a base de dados
        try {
            Class.forName(JDBC_DRIVER);
            
            System.out.println("Connecting to database..."); 
            conn = DriverManager.getConnection(db_url, DB_USER, DB_PASS);
            
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return;
        } catch (SQLException ex) {
            System.out.println("[ERROR] Connection to database failed.");
            return;
        }
        
        ServerSocket socket;
        try {
            socket = new ServerSocket();
        } catch (IOException e) {
            System.out.println("[ERROR] ServerSocket criation failed");
            e.printStackTrace();
            return;
        }
        
        ManagmentServer mServer = new ManagmentServer(conn);
        while (true) {
            // Aceitar ligacao de clientes
            
            Socket s = null;
            try {
                s = socket.accept();
            } catch (IOException e) {
                System.out.println("[ERROR] Couldn't accept connection.");
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void run() {
        
    }
}
