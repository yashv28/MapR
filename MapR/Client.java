package MapR;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Client 
{

    public static void main(String[] args) throws Exception 
    {

        ClientApp.connect(args);

        System.in.read();
    }

    public static void connect(String[] args) throws Exception 
    {

        String domain = "localhost";
        Long N = 1200000L;

        if (args.length > 0) 
        {
            domain = args[0];
        }

        if (args.length > 1) 
        {
            N = Long.valueOf(args[1]);
        }

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        ClientManager clientManager = ClientManager.createClient();
        clientManager.getProperties().put("org.glassfish.tyrus.incomingBufferSize", 1024 * 1024 * 100 * 999999999);

        URI uri = new URI("ws://" + domain + ":8080/socket");
        Session client = clientManager.connectToServer(FactorialWorkerSocket.class, uri);

        Map map = new HashMap<>();
        map.put("start", null);
        map.put("N", N);

        client.getAsyncRemote().sendObject(map);
    }
}
