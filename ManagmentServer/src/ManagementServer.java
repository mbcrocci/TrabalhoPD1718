import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
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
    static final int MULTICAST_PORT = 4001;
    
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_USER = "user";
    static final String DB_PASS = "password";
    
    private Socket client;
    private Connection conn;
    
    private HeartbeatReceiver heartbeatReceiver;
   
    // sockets de todos os clientes para permitir mandar mmensagens
    private ArrayList<Socket> clients;
    
    private MulticastSocket multicastSocket;
    
    public ManagementServer(
            Socket client, Connection conn,
            // Referencia para o receiver para obter o servidor de jogo actual 
            HeartbeatReceiver hb,
            ArrayList<Socket> clients) {
        this.client = client;
        this.conn = conn;
        this.heartbeatReceiver = hb;
        this.clients = clients;
        
        try {
            this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
            
        } catch (IOException e) {
            System.out.println("Impossivel iniciar multicast socket");
        }
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
    
    private void createUnconfirmedPair(String username, String pairusername) throws SQLException {
        String query = "INSERT INTO pairs (user1, user2, confirmed) VALUES (?, ?, ?);";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, pairusername);
        pstmt.setBoolean(3, false);
    }
    
    private void confirmPair(String username, String pairusername) throws SQLException {
        String query = "UPDATE pairs SET confirmed = ?"
                + "WHERE (user1 = ? AND user2 = ?)"
                + "OR (user1 = ? AND user2 = ?;";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        pstmt.setString(2, username);
        pstmt.setString(3, pairusername);
        pstmt.setString(4, pairusername);
        pstmt.setString(5, username);
        
        pstmt.executeUpdate();
    }
    
    private void denyPair(String username, String pairusername) throws SQLException {
        String query = "DELETE FROM pairs"
                + "WHERE (user1 = ? AND user2 = ?)"
                + "OR (user1 = ? AND user2 = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        pstmt.setString(2, username);
        pstmt.setString(3, pairusername);
        pstmt.setString(4, pairusername);
        pstmt.setString(5, username);
        
        pstmt.executeUpdate();
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
    
    private ArrayList<String> getAllAddresses() throws SQLException {
        ArrayList<String> addrs = new ArrayList<>();
        
        String query = "SELECT address FROM user WHERE authenticated = ?;";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) addrs.add(rs.getString(1));
        
        return addrs;
    }
    
    private Socket getClientSocket(String addr) {
        Socket s = null;
        for (Socket c: clients)
            if (s.getInetAddress().toString().equals(addr)) {
                s = c;
                break;
            }
        return s;
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
                    try {
                        String addr = getUserAddress(request.getPairUsername());
                        if (addr == null) {
                            response = "Pairusername nao encontrado.";
                        
                        } else {
                            createUnconfirmedPair(request.getUsername(), request.getPairUsername());
                            
                            // perguntar ao outro utilizador se quer formar par
                            Socket s = getClientSocket(addr);
                            if (s != null) {
                                UserRequest rq = new UserRequest(UserRequest.ASK_PAIR_REQUEST);
                                rq.setUsername(request.getUsername());
                                rq.setPairUsername(request.getPairUsername());

                                ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                                reqOut.writeObject(rq);
                            }
                        }
                    } catch (SQLException e) {
                        response = "Impossivel criar par";
                    }
                    break;
                
                case UserRequest.ACCEPT_REQUEST:
                    try {
                        confirmPair(request.getUsername(), request.getPairUsername());
                        response = "Par criado com " + request.getPairUsername() + ". Pode iniciar o jogo.";
                        
                        // Notificar o solicitador que foi aceitado.
                        String addr = getUserAddress(request.getPairUsername());
                        Socket s = getClientSocket(addr);
                        if (s != null) {
                            String msg = request.getUsername() + " aceitou o seu pedido para criar par. Pode iniciar o jogo.";
                            ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                            reqOut.writeObject(msg);
                        }
                    } catch (SQLException e) {
                        response = "Impossivel confirmar o par.";
                    }
                    break;
                case UserRequest.DENY_REQUEST:
                    try {
                        denyPair(request.getUsername(), request.getPairUsername());
                        response = "Par com " + request.getPairUsername() +" negado.";
                        
                        // Notificar o solicitador que foi negado.
                        String addr = getUserAddress(request.getPairUsername());
                        Socket s = getClientSocket(addr);
                        if (s != null) {
                            String msg = request.getUsername() + " negou o seu pedido para criar par.";
                            ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                            reqOut.writeObject(msg);
                        }
                    } catch (SQLException e) {
                        response = "Impossivel negar o par.";
                    }
                    break;
                case UserRequest.PLAYER_MESSAGE_REQUEST:
                    try {
                        String address = getUserAddress(request.getPairUsername());
                        
                        if (address == null) {
                            response = "Jogador nao encontrado.";
                        } else {
                            String msg = request.getUsername() + ": @" + request.getPairUsername() + request.getMessage();
                            Socket s = getClientSocket(address);
                            if (s != null) {
                                ObjectOutputStream msgOut = new ObjectOutputStream(s.getOutputStream());
                                msgOut.writeObject(msg);
                            }
                        }
                    } catch (SQLException e) {
                        response = "Problema no sql ao procurar jogador.";
                    }
                    
                    break;
                case UserRequest.MESSAGE_REQUEST:
                    try {
                    ArrayList<String> addrs = getAllAddresses();
                    
                    for (Socket s: clients)
                        if (addrs.contains(s.getInetAddress().toString())) {
                            String msg = request.getUsername() + ": " + request.getMessage();
                            ObjectOutputStream msgOut = new ObjectOutputStream(s.getOutputStream());
                            msgOut.writeObject(msg);
                        }
                    break;
                    } catch(SQLException e) {
                        response = "Problema no sql ao aceder a base de dados.";
                    }
                }
                
                output.writeObject(response);

            } catch (IOException | ClassNotFoundException e) {

            }
        }
    }
}
