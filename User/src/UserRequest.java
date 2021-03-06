
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
    static final int SHOW_PAIR_LIST_REQUEST = 4;
    static final int PAIR_REQUEST = 5;
    static final int ACCEPT_REQUEST = 6;
    static final int DENY_REQUEST = 7;
    static final int ASK_PAIR_REQUEST = 8;
    static final int PLAYER_MESSAGE_REQUEST = 9;
    static final int MESSAGE_REQUEST = 10;
    static final int DISCONNECT_REQUEST = 11;
    
    // Tipo de request 
    private int type;
    
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
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
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
