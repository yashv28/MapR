package MapR;

import javax.websocket.*;
import java.util.Map;
import java.util.function.Consumer;


@ClientEndpoint(
        encoders = ObjectSerializer.class,
        decoders = ObjectSerializer.class
)
public class FactorialWorkerSocket 
{

    @OnMessage
    public void onmessage(Map message , Session session) throws Exception 
    {

        if(message.containsKey("chunk"))
        {
            Consumer<Session> task = (Consumer<Session>) message.get("chunk");
            task.accept(session);
        }

        if(message.containsKey("result"))
        {
            System.out.println(message);
        }

    }

    @OnError
    public void onerror(Throwable throwable)
    {
        throwable.printStackTrace();
    }

    @OnClose
    public void onclose(CloseReason reason)
    {
        System.out.println(reason.getCloseCode());
        System.out.println(reason.getReasonPhrase());
    }

}
