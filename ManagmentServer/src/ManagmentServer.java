

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManagmentServer implements Runnable {
    static final int TIMEOUT = 60000;
    
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_USER = "user";
    static final String DB_PASS = "password";
    
    private Socket client;
    private Connection conn;
    
    private HeartbeatReceiver heartbeatReceiver;
    
    // Lista de clientes autenticados. Sincroniza com a funcao main()
    private ArrayList<String> authClients;
    
    public ManagmentServer(
            Socket client, Connection conn,
            // Referencia para o receiver para obter o servidor de jogo actual 
            HeartbeatReceiver hb,
            ArrayList<String> authClients) {
        this.client = client;
        this.conn = conn;
        this.heartbeatReceiver = hb;
    }
    

    public static void main(String[] args) {
        String databaseAddress;
        String db_url;
        Connection databaseConn;
        ServerSocket socket;
        HeartbeatReceiver heartbeatReceiver;
        Thread hbThread;
        ArrayList<Thread> clientHandlers;
        ArrayList<String> authClients;
        
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
        
        db_url ="jdbc:mysql://" + databaseAddress + "/trabalhopd";       
        // Fazer ligacao a base de dados
        try {
            Class.forName(JDBC_DRIVER);
            
            System.out.println("Connecting to database..."); 
            databaseConn = DriverManager.getConnection(db_url, DB_USER, DB_PASS);
            
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return;

        } catch (SQLException ex) {
            System.out.println("[ERROR] Impossivel ligar a base de dados.");
            return;
        }
        
        // Cria a socket que vai correr o ManagmentServer
        try {
            socket = new ServerSocket();
            
        } catch (IOException e) {
            System.out.println("[ERRO] Impossivel criar serversocket.");
            e.printStackTrace();
            return;
        }
        
        // Criar o HeartbeatReceiver que vai responder as ligacoes UDP dos servidores de jogo
        try {
            heartbeatReceiver = new HeartbeatReceiver(databaseAddress);
            
        } catch (SocketException e) {
            System.out.println("[ERRO] Nao foi possivel criar um Heartbeat Receiver.");
            return;
        }
        
        // Comecar o HeartbeatReceiver numa thread e inicia-a
        hbThread = new Thread(heartbeatReceiver);
        hbThread.start();
        
        // inicializa a lista das threads de clientes e a dos ips dos que ja estao atenticados
        clientHandlers = new ArrayList<>();
        authClients = new ArrayList<>();
        
        while (true) {
            Socket client = null;
            try {
                // aceitar uma nova ligacao
                client = socket.accept();
                
                client.setSoTimeout(TIMEOUT);
                
                // criar uma thread que vai atender o cliente
                clientHandlers.add(new Thread(
                        new ManagmentServer(
                                client, databaseConn,
                                heartbeatReceiver, authClients
                        )
                ));
                clientHandlers.get(clientHandlers.size()-1).start();
                
            } catch (IOException e) {
                System.out.println("[ERRO] Impossivel aceitar ligacao.");
                e.printStackTrace();
            } 
        }
    }
    
    @Override
    public void run() {
        try {
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();
            
            
            // Ler request
            
            // Responder ao request
            
            
        } catch (Exception e) {
        }
    }
}
