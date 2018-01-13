
import java.io.Serializable;

/*
    Esta classe representa um request feito pelo o utilizador
    ao servidor de gestao.
    Set username and password para pedidos de registo e autenticacao.
    Set username ou pairIPAddress para pedir par.
*/
public class UserRequest implements Serializable{
    static final long serialVersionUID = 1L;
    
    // Diferentes tipos de request;
    static final int REGISTER_REQUEST = 1;
    static final int AUTHENTICATE_REQUEST = 2;
    static final int SHOW_PLAYER_LIST_REQUEST = 3;
    static final int PAIR_REQUEST = 4;
    static final int ACCEPT_REQUEST = 5;
    static final int DENY_REQUEST = 6;
    static final int PLAYER_MESSAGE_REQUEST = 7;
    static final int MESSAGE_REQUEST = 8; // multicast
    
    // Tipo de request 
    private int type;
    
    private String ipAddress;
    
    // Register and Authenticate
    private String username;
    private String password;
    
    private String pairUsername;
    
    private String msg;
    
    public UserRequest() {
        
    }
    
    public UserRequest(int type) {
        this.type = type;
    }
    
    public UserRequest(int type, String ipAddress) {
        this.type = type;
        this.ipAddress = ipAddress;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String ipAddress() {
        return ipAddress;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPairUsername() {
        return this.pairUsername;
    }
    
    public void setPairUsername(String pairUsername) {
        this.pairUsername = pairUsername;
    }
    
    public String getMessage() {
        return this.msg;
    }
    
    public void setMessage(String msg) {
        this.msg = msg;
    }
}
