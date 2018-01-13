
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class User {
    public static final int TIMEOUT = 10000;
        
    public static String readCommand() throws IOException {
        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
        
        String cmd;
        
        System.out.print(">> ");
        cmd = buff.readLine();
        
        return cmd;
    }
    
    public static UserRequest parseCommand(String cmd) {
        // serparar o comando por palavras
        String []cmdArgs = cmd.split(" ");
        
        // Comando e o cmdArgs[0] e o resto sao os seu argumentos
        String command = cmdArgs[0];
        
        UserRequest request = null;
        
        // TODO: permitir variacoes dos comandos
        
        if (command.equalsIgnoreCase("register")) {
            if (cmdArgs.length != 3) {
                System.out.println("Comando errado. Sintaxe: register <username> <password>");
                return null;
            }
            
            request = new UserRequest((UserRequest.REGISTER_REQUEST));
            request.setUsername(cmdArgs[1]);
            request.setPassword(cmdArgs[2]);
            
        } else if (command.equalsIgnoreCase("autenticar")) {
            if (cmdArgs.length != 3) {
                System.out.println("Comando errado. Sintaxe: autenticar <username> <password>");
                return null;
            }
            
            request = new UserRequest(UserRequest.AUTHENTICATE_REQUEST);
            request.setUsername(cmdArgs[1]);
            request.setPassword(cmdArgs[2]);
            
        } else if (command.equalsIgnoreCase("mostralista")) {
            if (cmdArgs.length != 1) {
                System.out.println("Demasiados argumentos. Serao ignorados.");
            }
            
            request = new UserRequest(UserRequest.SHOW_PLAYER_LIST_REQUEST);
            
        } else if (command.equalsIgnoreCase("pedirpar")) {
            if (cmdArgs.length != 2) {
                System.out.println("Comando errado. Sintaxe: pedirpar <pairusername>");
                return null;
            }
            
            request = new UserRequest(UserRequest.PAIR_REQUEST);
            request.setPairUsername(cmdArgs[1]);
            
            
        } else if (command.equalsIgnoreCase("aceitar")) {
            if (cmdArgs.length != 2) {
                System.out.println("Comando errado. Sintaxe: aceitar <username>");
                return null;
            }
            
            request = new UserRequest(UserRequest.ACCEPT_REQUEST);
            request.setUsername(cmdArgs[1]);
            
        } else if (command.equalsIgnoreCase("rejeitar")) {
            if (cmdArgs.length != 2) {
                System.out.println("Comando errado. Sintaxe: rejeitar <username>");
                return null;
            }
            
            request = new UserRequest(UserRequest.DENY_REQUEST);
            request.setUsername(cmdArgs[1]);
        }
        
        return request;
    }
     
    public static void main(String[] args) {
        String managementAddress;
        int managementPort;
        
        Socket managementServerSocket = null;
        Socket gameServerSocket = null;
        
        ObjectInputStream mngObjIn = null;
        ObjectOutputStream mngObjOut = null;
        
        if (args.length != 2) {
            System.out.println("Sintaxe: java User <ip servidor gestao> <porto servidor gestao>.");
            
            BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
            
            try {
                System.out.print("Introduza o ip do servidor de gestao: ");
                managementAddress = buff.readLine();

                System.out.print("Introduza o porto do servidor de gestao: ");
                managementPort = Integer.parseInt(buff.readLine());
            } catch (IOException ex) {
                System.out.println("[ERRO] Impossivel ler da consola.");
                return;
            }
            
        } else {
            managementAddress = args[0];
            managementPort = Integer.parseInt(args[1]);
        }
        
        
        try {
            try {
            managementServerSocket = new Socket(managementAddress, managementPort);
            
            managementServerSocket.setSoTimeout(TIMEOUT);
            
            mngObjIn = new ObjectInputStream(managementServerSocket.getInputStream());
            mngObjOut = new ObjectOutputStream(managementServerSocket.getOutputStream());
            
            
            String cmd = readCommand();
            
            UserRequest request = parseCommand(cmd);    
            
            if (request != null)
                mngObjOut.writeObject(request);
            
            mngObjOut.flush();
            
            Object mngResponse = mngObjIn.readObject();
            
            // Inicializar objects possivei de resposta;
            String mngStringResponse = null;
            UserRequest mngPairRequest = null;
            
            if (mngResponse != null) {
                if (mngResponse instanceof String) {
                    mngStringResponse = (String) mngResponse;

                    System.out.println(mngStringResponse);

                // O servidor de gestao pede confirmacao ou negacao de um pedido de par
                } else if (mngResponse instanceof UserRequest) {
                    mngPairRequest = (UserRequest) mngResponse;
                    
                    // Ter a certeza que esta a pedir par.
                    if (mngPairRequest.getType() == UserRequest.ASK_PAIR_REQUEST) {
                        System.out.print(mngPairRequest.getUsername()
                                + " pede para fazer par consigo. Aceita? (y/n): ");
                        
                        BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
                        String conf = buff.readLine();
                        
                        // Bastaria so mudar o tipo do request recido no entanto
                        // a resposta em texto que o servidor mandara ficara errada.
                        // Logo criamos um novo UserRequest com o tipo desejado
                        // e invertem-se os usernames. 
                        UserRequest r = new UserRequest();
                        r.setUsername(mngPairRequest.getPairUsername());
                        r.setPairUsername(mngPairRequest.getUsername());
                        
                        if (conf.equalsIgnoreCase("y"))
                            r.setType(UserRequest.ACCEPT_REQUEST);
                        
                        else if (conf.equalsIgnoreCase("n"))
                            r.setType(UserRequest.DENY_REQUEST);
                        
                        mngObjOut.writeObject(r);
                    }
                }
            }
            
            } catch (UnknownHostException ex) {
                System.out.println("[ERRO] Impossivel ligar ao servidor de gestao.");
                return;

            } catch (IOException e) {
                System.out.println("[ERRO] Ocorreu um erro no acesso ao socket.");
            } catch (ClassNotFoundException e) {
                System.out.println("[ERRO] Foi recebido um objecto invalido.");
            }
        } finally {
            if (managementServerSocket != null)
                try { managementServerSocket.close(); } catch (IOException e) {}
            
            if (gameServerSocket != null)
                try { gameServerSocket.close(); } catch (IOException e) {}
        }
    }
}
