/*
 * Guarda a informacao necessario sobre um cliente
 */
public class ClientInfo {
    String ipAddress;
    int port;
    
    public ClientInfo(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }
}
