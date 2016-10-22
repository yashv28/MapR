package MapR;

import com.google.common.collect.Lists;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;


@ServerEndpoint(
        value = "/soket",
        encoders = ObjectSerializer.class,
        decoders = ObjectSerializer.class)
public class FactorialServerSocket 
{

    private static Long workStartTimeMillis = null;
    private static List<BigInteger> subFactoriels = Collections.synchronizedList(new ArrayList<>());
    private static int workDoneCount = 0;


    @OnMessage
    public void onmessage(Map message, Session session) throws IOException 
    {

        if (message.containsKey("start")) {
            dispatchWorks(session, (Long) message.get("N"));
        }

        if (message.containsKey("chunk")) {
            collectChunks(message, session);
        }

    }

    private void dispatchWorks(Session session, Long N) throws IOException 
    {

        if (workStartTimeMillis == null) {
            workStartTimeMillis = System.currentTimeMillis();
        }

        List<Long> numberList = LongStream.rangeClosed(1, N).boxed().collect(Collectors.toList());

        Set<Session> allSessions = session.getOpenSessions();

        List<List<Long>> numberChunkedList = Lists.partition(numberList, (numberList.size() / allSessions.size()));

        workDoneCount = ((numberChunkedList.size() % allSessions.size()) == 0) ? allSessions.size() : allSessions.size() + 1;

        Iterator<Session> allSessionsIterator = allSessions.iterator();

        for (List<Long> numberChunks : numberChunkedList) 
        {

            ArrayList<Long> chunk = new ArrayList<>(numberChunks);
            RemFunc<Session> remoteFunction = (serverSession) -> 
            {
                BigInteger subFactorial = chunk
                        .parallelstream()
                        .map(BigInteger::valueOf)
                        .reduce(BigInteger.ONE, (first, second) -> first.multiply(second));

                Map data = new HashMap();
                data.put("chunk", subFactorial);

                serverSession.getAsyncRemote().sendObject(data);
            };


            Map data = new HashMap();
            data.put("chunk", remoteFunction);

            if (allSessionsIterator.hasNext())
            {
                Session next = allSessionsIterator.next();
                next.getAsyncRemote().sendObject(data);
            } 
            else 
            {
                session.getAsyncRemote().sendObject(data);
            }

        }
    }

    private void collectChunks(Map message, Session session) 
    {

        subFactoriels.add((BigInteger) message.get("chunk"));

        if (subFactoriels.size() == workDoneCount) 
        {

            BigInteger factorielResult = subFactoriels.stream()
                    .reduce(BigInteger.ONE, (first, second) -> first.multiply(second));

            long workerEndTimeMillis = System.currentTimeMillis();
            long workerCompleteTime = workerEndTimeMillis - workStartTimeMillis;


            Set<Session> allSessions = session.getOpenSessions();

            Map map = new HashMap();
            map.put("totalWorker", allSessions.size());
            map.put("completeTime", workerCompleteTime);
            String resultAsString = factorielResult.toString();
            resultAsString = resultAsString.length() > 10 ? resultAsString.substring(0, 10) : resultAsString;
            map.put("result", resultAsString.concat("..."));

            for (Session e : allSessions) 
            {
                e.getAsyncRemote().sendObject(map);
            }

            workStartTimeMillis = null;
            subFactoriels.clear();
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
