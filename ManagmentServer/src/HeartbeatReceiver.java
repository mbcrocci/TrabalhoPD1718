import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HeartbeatReceiver implements Runnable {

    // Contem o Address do servidor de jogo que mandou o heartbeat
    // e o timepoint em que foi mandado.
    protected class Heartbeat {
        public String gameServerAddress;

        // Quando o Heartbeat foi criado
        private Long timepoint;
        
        // Quando chega a 3 o Receiver apaga da lista
        private int counter;
        
        public Heartbeat(String gameServerAddress) {
            this.gameServerAddress = gameServerAddress;
            this.timepoint = System.currentTimeMillis();
            this.counter = 0;
        }
        
        public String getGameServerAddress() {
           return this.gameServerAddress;
        }
        
        public int getCounter() {
            return this.counter;
        }
        
        public Long getTimePoint() {
            return this.timepoint;
        }
        
        public void updateTimePoint() {
            this.timepoint = System.currentTimeMillis();
        }
        
        public void resetCounter() {
            this.counter = 0;
        }
        
        public void tickCounter() {
            this.counter++;
        }
          
        // Dois heartbeast sao iguais quando o têm o mesmo gameServerAddress
        // pois provêm do mesmo local
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            
            Heartbeat h;
            if (o instanceof Heartbeat)
                h = (Heartbeat) o;
            
            else return false;
            
            return h.getGameServerAddress().equals(this.gameServerAddress);
        }

        // Foi gerado automaticamente pelo Netbeans
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Objects.hashCode(this.gameServerAddress);
            return hash;
        }
        
    }
    
   // Porto do receiver
    public static final int HEARTBEAT_PORT = 4001;
    
    // Porto do receiver
    public static final int HEARTBEAT_SIZE = 256;
    
    // Intervalo de tempo entre HeartBeat;
    public static final int HEARTBEAT_TIME = 3000; // 3s
    
    // Mensagem para pedir o ip da base de dados
    public static final String HEARTBEAT_REQUEST = "dbaddress";
    
    
    private String databaseAddress;
    
    private DatagramSocket socket;
    private DatagramPacket packet;
    
    private ArrayList<Heartbeat> gameServers;
    
    private Timer timer;
    
    public HeartbeatReceiver(String databaseAddress) throws SocketException {
        this.socket = null;
        this.packet = null;
        
        this.databaseAddress = databaseAddress;
        this.socket = new DatagramSocket(HEARTBEAT_PORT);
        
        this.gameServers = new ArrayList<>();
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                gameServers.forEach((server) -> {
                    server.tickCounter();
                });
            }
        }, HEARTBEAT_TIME);
        
        System.out.println("HeartbeatReceiver criado.");
    }
    
    private void cleanGameServerList() {
        for (Heartbeat h : this.gameServers)
            if (h.getCounter() >= 3) 
                this.gameServers.remove(h);
    }
    
    public String getCurrentGameServer() {
        return this.gameServers.get(0).getGameServerAddress();
    }
    
    public void closeSocket() {
        if (socket != null)
            socket.close();
    }
       
    @Override
    public void run() {
        System.out.println("HeartbeatReceiver a receber...");
        
        while (true) {
            try {
                if (socket == null) continue;
                
                packet = new DatagramPacket(new byte[HEARTBEAT_SIZE], HEARTBEAT_SIZE);
                
                socket.receive(packet);
                System.out.println("Heartbeat recebido...");
                
                String request = new String(packet.getData(), 0, packet.getLength());
                
                if (request == null) continue;
                if (!request.equalsIgnoreCase(HEARTBEAT_REQUEST)) continue;
                
                
                Heartbeat heartbeat = new Heartbeat(packet.getAddress().toString());
                
                // Ver se ja foi recebido um heartbeat deste servidor
                // se sim da update ao heartbeat ja existente
                if (gameServers.contains(heartbeat)) {
                    int index = gameServers.indexOf(heartbeat);
                    Heartbeat server = gameServers.get(index);
                    server.updateTimePoint();
                    server.resetCounter();
                
                } else {
                    gameServers.add(heartbeat);
                }
                
                // Responder ao gameServer que mandou o heartbeat
                packet.setData(this.databaseAddress.getBytes());
                packet.setLength(this.databaseAddress.length());             
                socket.send(packet);
                
            } catch (IOException e) {
                System.out.println("[ERRO] Problema a receber heartbeat.");
            }
        }
    }
}
