package MapR;

import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.util.HashMap;

public class ServerApp 
{

    public static void main(String[] args) throws DeploymentException, IOException 
    {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("org.glassfish.tyrus.incomingBufferSize", 1024*1024*100*999999999);
        Server server = new Server("0.0.0.0", 8080, "/", properties, FactorialServerSocket.class);
        server.start();
        System.out.println("MasterStarted");
        System.in.read();
    }
}
