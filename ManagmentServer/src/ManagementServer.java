import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ManagementServer implements Runnable {
    static final int TIMEOUT = 60000;
    
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_USER = "user";
    static final String DB_PASS = "password";
    
    private Socket client;
    private Connection conn;
    
    private HeartbeatReceiver heartbeatReceiver;
   
    // sockets de todos os clientes para permitir mandar mmensagens
    private ArrayList<Socket> clients;
    
    public ManagementServer(
            Socket client, Connection conn,
            // Referencia para o receiver para obter o servidor de jogo actual 
            HeartbeatReceiver hb,
            ArrayList<Socket> clients) {
        this.client = client;
        this.conn = conn;
        this.heartbeatReceiver = hb;
        this.clients = clients;
    }
    
    public static void main(String[] args) {
        String databaseAddress;
        String db_url;
        Connection databaseConn;
        ServerSocket socket;
        HeartbeatReceiver heartbeatReceiver;
        Thread hbThread;
        ArrayList<Socket> clients;
        ArrayList<Thread> clientHandlers;
        
        if (args.length != 1) {
            System.out.println("Sintaxe: java ManagementServer <ip base dados>");
            
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
        
        // Cria a socket que vai correr o ManagementServer
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
        
        // inicializa a lista dos clientes e threads correspondentes
        clients = new ArrayList<>();
        clientHandlers = new ArrayList<>();
        
        while (true) {
            Socket client = null;
            try {
                // aceitar uma nova ligacao
                client = socket.accept();
                
                client.setSoTimeout(TIMEOUT);
                
                // adicionar nova socket a lista de clientes
                clients.add(client);
                // criar uma thread que vai atender o cliente
                clientHandlers.add(new Thread(
                        new ManagementServer(
                                client, databaseConn,
                                heartbeatReceiver, clients
                        )
                ));
                clientHandlers.get(clientHandlers.size()-1).start();
                
            } catch (IOException e) {
                System.out.println("[ERRO] Impossivel aceitar ligacao.");
                e.printStackTrace();
            } 
        }
    }
    
    private void registerUser(String username, String password, String address) throws SQLException {
        String query = "INSERT INTO users (username, password, address, authenticated) VALUES (?, ?, ?, ?);";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, address);
        pstmt.setBoolean(3, false);
        
        pstmt.executeUpdate();
    }
    
    private Boolean authenticateUser(String username, String password) throws SQLException {
        String query = "UPDATE users SET athenticated = ? WHERE username = ? and password = ?";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        pstmt.setString(2, username);
        pstmt.setString(3, password);
        
       pstmt.executeUpdate();
       
       // Confir the user is authenticated
       query = "SELECT authenticated FROM users WHERE usernam = ? and password = ?;";
       pstmt = conn.prepareStatement(query);
       pstmt.setString(1, username);
       pstmt.setString(2, password);
       ResultSet rs = pstmt.executeQuery();
       
       if (rs.next())
           return rs.getBoolean(1);
       
       return false;
    }
    
    private String getPlayerList() throws SQLException {
        String query = "SELECT username, address FROM users WHERE authenticated = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        
        ResultSet rs = pstmt.executeQuery();
        String response = "";
        
        while(rs.next()) {
            response += "Username: " + rs.getString(1) + " | IPAddress: " + rs.getString(2) + "\n";
        }
        
        return response;
    }
    
    private void requestPair(String username, String pairusername) throws SQLException {
        String query = "SELECT user1, user 2 FROM pairs WHERE user1 = ? or user1 = ?;";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, pairusername);
        
        
        
    }
    
    private String getUserAddress(String username) throws SQLException {
        String query = "SELECT address FROM user WHERE username = ? AND authenticated = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setBoolean(2, true);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) return rs.getString(1);
        
        return null;
    }
    
    private void sendMulticastMessage(String msg) {
        
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());

                // Ler request
                UserRequest request = (UserRequest) input.readObject();

                String response = "";
                switch (request.getType()) {
                case UserRequest.REGISTER_REQUEST: 
                    try {
                        registerUser(request.getUsername(), request.getPassword(), client.getInetAddress().toString());
                    } catch (SQLException e) {
                        response = "Impossivel registrar utilizador.";
                    }
                    break;
                    
                case UserRequest.AUTHENTICATE_REQUEST:
                    try {
                        boolean r = authenticateUser(request.getUsername(), request.getPassword());
                        
                        if (r) response = "Utilizador autenticado com sucesso.";
                        else response = "Impossivel autenticar utilizador";
                    
                    } catch (SQLException e) {
                        response = "Impossivel autenticar utilizador. Erro no sql.";
                    }
                    break;
                    
                case UserRequest.SHOW_PLAYER_LIST_REQUEST:
                    try {
                        response = getPlayerList();
                        
                    } catch (SQLException e) {
                        response  = "Impossivel ir buscar lista de jogadores.";
                    }
                    break;
                    
                case UserRequest.PAIR_REQUEST:
                    
                    break;
                
                case UserRequest.ACCEPT_REQUEST: break;
                case UserRequest.DENY_REQUEST: break;
                case UserRequest.PLAYER_MESSAGE_REQUEST:
                    try {
                        String address = getUserAddress(request.getPairUsername());
                        
                        if (address == null) {
                            response = "Jogador nao encontrado.";
                        } else {
                            for (Socket s: clients)
                                if (s.getInetAddress().toString().equals(address)) {
                                    response = request.getUsername() + ": @" + request.getPairUsername() + request.getMessage();
                                    break; // from for loop
                                }

                        }
                    } catch (SQLException e) {
                        response = "Problema no sql ao procurar jogador.";
                    }
                    
                    break;
                case UserRequest.MESSAGE_REQUEST:
                    sendMulticastMessage(request.getMessage());
                    break;
                }
                
                output.writeObject(response);

            } catch (IOException | ClassNotFoundException e) {

            }
        }
    }
}
