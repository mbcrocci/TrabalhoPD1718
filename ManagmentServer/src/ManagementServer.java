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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

public class ManagementServer implements Runnable {
    static final int TIMEOUT = 60000;
    static final int MANAGEMENT_PORT = 4001;
    
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
        
        System.out.println("Objecto para atender cliente criada...");
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
        
        db_url ="jdbc:mysql://" + databaseAddress + "/trabalhopdg10";       
        // Fazer ligacao a base de dados
        try {
            Class.forName(JDBC_DRIVER);
            
            System.out.println("Connecting to database..."); 
            databaseConn = DriverManager.getConnection(db_url, DB_USER, DB_PASS);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return;

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Impossivel ligar a base de dados.");
            return;
        }
        
        // Cria a socket que vai correr o ManagementServer
        try {
            socket = new ServerSocket(MANAGEMENT_PORT);
            
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
        try {
            while (true) {
                Socket client = null;

                // aceitar uma nova ligacao
                client = socket.accept(); 

                System.out.println("Novo cliente...");
                
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
                System.out.println("Thread para atender cliente criada...");
            }
        } catch (IOException e) {
            System.out.println("[ERRO] Impossivel aceitar ligacao.");
            e.printStackTrace();
        }
    }
    
    private void registerUser(String username, String password) throws SQLException {
        String query = "INSERT INTO users (username, password, address, port, authenticated) VALUES (?, ?, ?, ?, ?);";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, client.getInetAddress().toString());
        pstmt.setInt(4, client.getPort());
        pstmt.setBoolean(5, false);
        
        pstmt.executeUpdate();
    }
    
    private Boolean authenticateUser(String username, String password) throws SQLException {
        String query = "UPDATE users SET address = ?, port = ?, authenticated = ?  WHERE username = ? and password = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, client.getInetAddress().toString());
        pstmt.setInt(2, client.getPort());
        pstmt.setBoolean(3, true);
        pstmt.setString(4, username);
        pstmt.setString(5, password);
        
       pstmt.executeUpdate();
       
       // Confir the user is authenticated
       query = "SELECT authenticated FROM users WHERE username = ? and password = ?;";
       pstmt = conn.prepareStatement(query);
       pstmt.setString(1, username);
       pstmt.setString(2, password);
       ResultSet rs = pstmt.executeQuery();
       
       if (rs.next())
           return rs.getBoolean(1);
       
       return false;
    }
    
    private String getPlayerList() throws SQLException {
        String query = "SELECT username, address, port FROM users WHERE authenticated = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        
        ResultSet rs = pstmt.executeQuery();
        String response = "";
        
        while(rs.next()) {
            response += "Username: " + rs.getString(1) 
                    + " | Address: " + rs.getString(2) + ":" + rs.getString(3) + "\n";
        }
        
        return response;
    }
    
    private String getPairsList() throws SQLException {
        String query = "SELECT user1, user2 FROM pairs WHERE confirmed = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        
        ResultSet rs = pstmt.executeQuery();
        String response = "";
        
        while (rs.next()) {
            response += "Par: [" + rs.getString(1) + ", " + rs.getString(2) + "]\n";
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
    
    private Pair<String, Integer> getUserAddress(String username) throws SQLException {
        String query = "SELECT address, port FROM users WHERE username = ? AND authenticated = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setBoolean(2, true);
        
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) return new Pair<>(rs.getString(1), rs.getInt(2));
        
        return null;
    }
    
    private ArrayList<Pair<String, Integer>> getAllAddresses() throws SQLException {
        ArrayList<Pair<String, Integer>> addrs = new ArrayList<>();
        
        String query = "SELECT address, port FROM user WHERE authenticated = ?;";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, true);
        
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) addrs.add(new Pair<>(rs.getString(1), rs.getInt(2)));
        
        return addrs;
    }
    
    private Socket getClientSocket(String username) throws SQLException {
        Pair<String, Integer> pair = getUserAddress(username);
        
        Socket s = null;
        for (Socket c: clients)
            if (c.getInetAddress().toString().equals(pair.getKey())
                    && c.getPort() == pair.getValue()) {
                s = c;
                break;
            }
        return s;
    }
    
    private void disconnectClient() throws SQLException {
        String query = "UPDATE users SET authenticated = ? "
                + "WHERE address = ? AND port = ?;";
        
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setBoolean(1, false);
        pstmt.setString(2, client.getInetAddress().toString());
        pstmt.setInt(3, client.getPort());
        
        pstmt.executeUpdate();
    }
    
    @Override
    public void run() {
        System.out.println("A atender cliente...");
        ObjectInputStream input;
        ObjectOutputStream output;
        while (true) {
            try {
                input = new ObjectInputStream(client.getInputStream());
            } catch (IOException e) {
                try {
                    disconnectClient();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                continue;
            }

            // Ler request
            UserRequest request = null;
            try {
                 request = (UserRequest) input.readObject();
            } catch (Exception e) {
                System.out.println("Problema a ler request");
            }
            
            String response = "";
            
            if (request != null)
            switch (request.getType()) {
            case UserRequest.REGISTER_REQUEST: 
                try {
                    registerUser(request.getUsername(), request.getPassword());
                    response = "Utilizador registado.";
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "Impossivel registrar utilizador.";
                }
                break;

            case UserRequest.AUTHENTICATE_REQUEST:
                try {
                    boolean r = authenticateUser(request.getUsername(), request.getPassword());

                    if (r) response = "Utilizador autenticado com sucesso.";
                    else response = "Impossivel autenticar utilizador";

                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "Impossivel autenticar utilizador. Erro no sql.";
                }
                break;

            case UserRequest.SHOW_PLAYER_LIST_REQUEST:
                try {
                    response = getPlayerList();

                } catch (SQLException e) {
                    e.printStackTrace();
                    response  = "Impossivel ir buscar lista de jogadores.";
                }
                break;
                
            case UserRequest.SHOW_PAIR_LIST_REQUEST:
                try {
                    response = getPairsList();
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                    response  = "Impossivel ir buscar lista de pares.";
                }
                break;

            case UserRequest.PAIR_REQUEST:
                try {
                    createUnconfirmedPair(request.getUsername(), request.getPairUsername());

                    // perguntar ao outro utilizador se quer formar par
                    Socket s = getClientSocket(request.getPairUsername());
                    if (s != null) {
                        UserRequest rq = new UserRequest(UserRequest.ASK_PAIR_REQUEST);
                        rq.setUsername(request.getUsername());
                        rq.setPairUsername(request.getPairUsername());

                        try {
                            ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                            reqOut.writeObject(rq);
                            reqOut.flush();

                        } catch (IOException e) {
                            System.out.println("Immpossivel mandar pedido de pair");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "Impossivel criar par";
                }
                break;

            case UserRequest.ACCEPT_REQUEST:
                try {
                    confirmPair(request.getUsername(), request.getPairUsername());
                    response = "Par criado com " + request.getPairUsername() + ". Pode iniciar o jogo.";

                    // Notificar o solicitador que foi aceitado.
                    Socket s = getClientSocket(request.getPairUsername());
                    if (s != null) {
                        String msg = request.getUsername() + " aceitou o seu pedido para criar par. Pode iniciar o jogo.";
                        try {
                            ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                            reqOut.writeObject(msg);
                            reqOut.flush();
                        } catch (IOException e) {
                            System.out.println("Immpossivel aceitar pedido de pair");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "Impossivel confirmar o par.";
                }
                break;
            case UserRequest.DENY_REQUEST:
                try {
                    denyPair(request.getUsername(), request.getPairUsername());
                    response = "Par com " + request.getPairUsername() +" negado.";

                    // Notificar o solicitador que foi negado.
                    Socket s = getClientSocket(request.getPairUsername());
                    if (s != null) {
                        String msg = request.getUsername() + " negou o seu pedido para criar par.";
                        try {
                            ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                            reqOut.writeObject(msg);
                            reqOut.flush();
                        } catch (IOException e) {
                            System.out.println("Immpossivel rejeitar pedido de pair");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "Impossivel negar o par.";
                }
                break;
            case UserRequest.PLAYER_MESSAGE_REQUEST:
                try {
                    String msg = request.getUsername() + ": @" + request.getPairUsername() + request.getMessage();
                    Socket s = getClientSocket(request.getPairUsername());
                    if (s != null) {
                        try {
                            ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                            reqOut.writeObject(msg);
                            reqOut.flush();
                        } catch (IOException e) {
                            System.out.println("Immpossivel aceitar pedido de pair");
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "Problema no sql ao procurar jogador.";
                }

                break;
            case UserRequest.MESSAGE_REQUEST:
                try {
                    ArrayList<Pair<String, Integer>> addrs = getAllAddresses();
                    for (Socket s: clients)
                        if (addrs.contains( new Pair<>(s.getInetAddress().toString(), s.getPort()) )){
                            String msg = request.getUsername() + ": " + request.getMessage();
                            try {
                                ObjectOutputStream reqOut = new ObjectOutputStream(s.getOutputStream());
                                reqOut.writeObject(msg);
                                reqOut.flush();
                            } catch (IOException e) {
                                System.out.println("Impossivel aceitar pedido de pair");
                            }
                        }
                } catch(SQLException e) {
                    e.printStackTrace();
                    response = "Problema no sql ao aceder a base de dados.";
                }
                break;
                
            case UserRequest.DISCONNECT_REQUEST:
                try {
                    disconnectClient();
                    response = "Disconnected...";
                } catch (SQLException e) {
                    e.printStackTrace();
                    response = "problema no sql ao aceder a base de dados.";
                }
                break;
            }
                
            try {
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(response);
                output.flush();
            } catch(IOException e) {
                System.out.println("Impossivel mandar resposta.");
                try {
                    disconnectClient();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
