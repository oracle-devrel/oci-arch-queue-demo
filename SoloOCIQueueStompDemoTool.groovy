import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.stomp.Frame;
import io.vertx.ext.stomp.StompClient;
import io.vertx.ext.stomp.StompClientConnection;
import io.vertx.ext.stomp.StompClientOptions;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Implements or provides reference code to establish a STOMP connection for OCI Queue Service
 * https://docs.oracle.com/en-us/iaas/Content/queue/messages-stomp.htm
 * 
 * The example uses vertx-stomp libraries to create a stomp client to establish connection.
 * OCI Queue service uses port 61613 for STOMP connection. 
 */
public class SoloOCIQueueStompDemoTool {
    private static final String QUEUEOCID = "QUEUEOCID";
    private static final String HOSTNAME = "DP_HOSTNAME";
    private static final String REGION = "REGION";
    private static final String ISVERBOSE = "VERBOSE";
    private static final String AUTHTOKEN = "AUTHTOKEN";
    private static final String UNAME = "USERNAME";
    private static final String LOGIN = "LOGIN";
    private static final String TENANCY = "TENANCY";
    private static final String DTG_FORMAT = "HH:mm:ss";
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DTG_FORMAT);

    private static final String ACTION_SEND = "send";
    private static final String ACTION_CONSUME = "consume";
    private static final String ACTION_TEST = "test";

    private static String action = null;
    private static Properties props = new Properties();

    private static boolean verbose = true;

    //------------

    private static String queueId = null;
    private static StompClient stompClient = null;
    private StompClientConnection stompClientConnection = null;
    private Vertx vertx = null;
    private CompletableFuture<Void> futureForAck = new CompletableFuture<>();


    /*
     * make sure we close connections cleanly
     */
    public void finalize()
    {
        log ("finalizing ...");
        if (stompClient != null)
        {
            stompClient.close();
        }
        if (vertx != null)
        {
            vertx.close();
        }
    }


    public SoloOCIQueueStompDemoTool () {
        log ("SoloOCIQueueStompDemoTool instance created");
    }

    public SoloOCIQueueStompDemoTool (StompClientConnection conn) {
        log ("SoloOCIQueueStompDemoTool instance created");
        stompClientConnection = conn;
    }

    public static void main(String[] args) {
        configure (args);
        // creates the vertx instance.
        Vertx vertx = Vertx.vertx();
        try {
            String authCode = props.getProperty(AUTHTOKEN);
            // The passcode should be base64 encoded.
            String base64EncodedPasscode = Base64.getEncoder()
            .encodeToString(authCode.getBytes(StandardCharsets.UTF_8));
            
            final StompClientOptions options = new StompClientOptions()
            .setHost(props.getProperty(HOSTNAME))
            .setPort(61613)
            .setSsl(true)
            .setLogin(props.getProperty(LOGIN))
            .setPasscode(base64EncodedPasscode);
            options.setTcpKeepAlive(true);
            
            log("Create STOMP client");
            // Creates the stomp client with error frame handler
            // and exception handler. Error frame handler is to handle the error frame
            // received
            // and exception handler is to handle the exception in case thrown.
            stompClient = StompClient.create(vertx, options)
            .errorFrameHandler(
                frame -> log("Error Frame received! message: " + frame.toString()))
            .exceptionHandler(
                throwable -> log("Exception Occurs! message: " + throwable.toString()));
            
            // establishes the connection, run the tests and join the results.
            
            switch (action)
            {
            case ACTION_TEST:
                getConnection(stompClient)
                .thenCompose(conn -> new SoloOCIQueueStompDemoTool(conn).execTest())
                .whenComplete((unused, error) -> {
                        // Error messages are already logged in respective futures
                        stompClient.close();
                    })
                .join();
                break;

            case ACTION_SEND:
                getConnection(stompClient)
                .thenCompose(conn -> new SoloOCIQueueStompDemoTool(conn).execSend())
                .whenComplete((unused, error) -> {
                        // Error messages are already logged in respective futures
                        stompClient.close();
                    })
                .join();
                break;

            case ACTION_CONSUME:
                getConnection(stompClient)
                .thenCompose(conn -> new SoloOCIQueueStompDemoTool(conn).execConsume())
                .whenComplete((unused, error) -> {
                        // Error messages are already logged in respective futures
                        stompClient.close();
                    })
                .join();
                break;
            }
            
            
        } finally {
            // close the vertx instance.
            vertx.close();
        }
    }
    
    /**
     * This gets the completable future of the stomp client connection.
     * In case of an error, the future completes exceptionally.
     */
    
    private static CompletableFuture<StompClientConnection> getConnection(StompClient stompClient) {
        CompletableFuture<StompClientConnection> future = new CompletableFuture<>();
        stompClient.connect(ar -> {
                if (ar.succeeded()) {
                    future.complete(ar.result());
                } else {
                    future.completeExceptionally(ar.cause());
                }
            });
        return future;
    }
    
    /*
     * Unsubscribes the given queue id. The queue id is the OCID of the queue to be
     * unsubscribed.
     */
    private CompletableFuture<Void> unsubscribe() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        stompClientConnection.unsubscribe(queueId,
            ar -> {
                if (ar.succeeded()) {
                    log("RECEIPT for UNSUBSCRIBE frame received!");
                    future.complete(null);
                } else {
                    log("Failure to process UNSUBSCRIBE frame!");
                    future.completeExceptionally(ar.cause());
                }
            });
        log("UNSUBSCRIBE Frame is sent!");
        return future;
    }
    
    /*
     * This closes the connection. It throws an error in case there was an error in
     * processing
     * the disconnect frame otherwise sends the receipt back for disconnect call.
     */
    private CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        stompClientConnection.disconnect(ar -> {
                if (ar.succeeded()) {
                    log("RECEIPT for DISCONNECT frame received!");
                    future.complete(null);
                } else {
                    log("Failure to process DISCONNECT frame!");
                    future.completeExceptionally(ar.cause());
                }
            });
        log("DISCONNECT Frame is sent!");
        return future;
    }
    
    /*
     * This subscribes to the queue. Queue is OCID of the queue and is stored as
     * constant in
     * Environment variables java class. It has two handlers: message frame handler
     * and receipt handler.
     */
    private CompletableFuture<Void> subscribe() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        final Map<String, String> queueSubscribeHeaders = new HashMap<>();
        queueSubscribeHeaders.put(Frame.ACK, "client-individual");
        stompClientConnection.subscribe(queueId,
            queueSubscribeHeaders,
            this::receiveMessage,
            ar -> {
                if (ar.succeeded()) {
                    log("RECEIPT for SUBSCRIBE frame received! subscription id=" + ar.result());
                    future.complete(null);
                } else {
                    log("Failure to process SUBSCRIBE frame!");
                    future.completeExceptionally(ar.cause());
                }
            });
        log("SUBSCRIBE Frame is sent!");
        return future;
    }
    
    /*
     * This sends the message to the queue. The queue is OCID of the destination
     * queue.
     */
    private CompletableFuture<Void> sendMessage() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String content = createMessage(1);
        stompClientConnection.send(queueId,
            Buffer.buffer(content),
            ar -> {
                if (ar.succeeded()) {
                    log("RECEIPT for SEND frame received for " + content);
                    future.complete(null);
                } else {
                    log("Failure to process SEND frame! cause:" + ar.cause());
                    future.completeExceptionally(ar.cause());
                }
            });
        log("Send MESSAGE");
        return future;
    }
    
    /*
     * This processes the message received on the subscribed queue.
     * It receives the ACK frame and completes the future for ACK.
     */
    private void receiveMessage(Frame frame) {
        String messageId = frame.getHeader("message-id");
        String content = frame.getBodyAsString();
        
        log("MESSAGE received, messageId=" + messageId +
                " ,size=" + content.getBytes().length + ", message="+content);
        
        stompClientConnection.ack(frame.getAck(),
            ar -> {
                if (ar.succeeded()) {
                    log("RECEIPT for ACK frame received!");
                    futureForAck.complete(null);
                } else {
                    log("Failure to process ACK frame!");
                    futureForAck.completeExceptionally(ar.cause());
                }
            });
        log("ACK frame sent");
    }

    //----------------------
    //----------------------

    /**
     * Runs the test to chain the following:
     * subscribe -> sendMessage -> getmessage -> deletemessage -> unsubscribe -> disconnect.
     */
    private CompletableFuture<Void> execTest() {
        return subscribe()
        .thenCompose(unused -> sendMessage())
        .thenCompose(unused -> futureForAck)
        .thenCompose(unused -> unsubscribe())
        .thenCompose(unused -> disconnect());
    }

    private CompletableFuture<Void> execSend() {
        return sendMessage()
        //.thenCompose(unused -> futureForAck)
        .thenCompose(unused -> disconnect());
    }

    private CompletableFuture<Void> execConsume() {
        return subscribe()
        .thenCompose(unused -> futureForAck)
        .thenCompose(unused -> unsubscribe())
        .thenCompose(unused -> disconnect());
    }

    //-----------

    private static String createMessage (int msgCtr)
    {
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(dtf);
        return  "{\"MessageNo\" : " + msgCtr + ", \"at\": " + "\""+ nowStr + "\", \"sent\" : \"from client app\"}";        

    }

    /*
     * Simple utility function - trying to set a property with a null value triggers an exception - rather than wrapping all the code
     * with the same If conditions - we've parameterized it.
     */
    static void setPropertyFromVar(String propname, String envName, Properties props)
    {
        String envVal = System.getenv(envName);
        if (envVal != null)
        {
            props.setProperty(propname, envVal);
        }
    }
    
    /*
     * To use a proper logging framework replace the calls to this method or direct the calls within this method to a logging framework
     * This doesn't use a logging framework to minize the dependencies needing to be retrieved - making it easy to deploy and use as
     * a single file application.
     */
    private static void log (String msg)
    {
        if (verbose) {System.out.println (msg);}
    }

    private static void configure (String[] args)
    {
        setPropertyFromVar (REGION, REGION, props);
        setPropertyFromVar (TENANCY, TENANCY, props);
        setPropertyFromVar (AUTHTOKEN, AUTHTOKEN, props);
        setPropertyFromVar (UNAME, UNAME, props);
        setPropertyFromVar (QUEUEOCID, QUEUEOCID, props);
        setPropertyFromVar (ISVERBOSE, ISVERBOSE, props);
        verbose =((System.getenv(ISVERBOSE) == null) || (System.getenv(ISVERBOSE).trim().equalsIgnoreCase("true")));
        
        queueId = props.getProperty(QUEUEOCID);
                
        if (props.getProperty(TENANCY) == null)
        {
            log("No tenancy set");
            System.exit(1);
        }
        if (props.getProperty(UNAME) == null)
        {
            log ("No user defined");
            System.exit(1);
        }
        if (props.getProperty(REGION) == null)
        {
            log ("No region set");
            System.exit(1);
        }
        if (props.getProperty(AUTHTOKEN) == null)
        {
            props.setProperty(AUTHTOKEN, getTokenFromFile());
        }
        if (props.getProperty(AUTHTOKEN) == null)
        {
            log ("no auth token set");
            System.exit(1);
        }

        props.setProperty(LOGIN, props.getProperty(TENANCY) + "/" + props.getProperty(UNAME)); // "YourTenancy/UserName"
        props.setProperty(HOSTNAME, "cell-1.queue.messaging."+props.get(REGION)+".oci.oraclecloud.com");
        
        if (args.length >0)
        {
            action = args[0].trim();
        }
        else
        {
            action = ACTION_TEST;
        }
        log ("action is:" + action);

    }

    
    /*
     * As token strings can have characters that can be troublesome for env vars. We have the option to
     * copy the token from a file called authtoken.txt which only contains the auth token
     * @return the auth token in the file
     */
    private static String getTokenFromFile()
    {
        String token = null;
        try
        {
            BufferedReader objReader = new BufferedReader(new FileReader("authtoken.txt"));
            token = objReader.readLine();
            log ("pulled token from file>"+token+"<");
            objReader.close();
        }
        catch (IOException ioErr)
        {
            log (ioErr.getMessage());
        }
        
        return token;
    }

    private static void displayCLI ()
    {
        System.out.println ("Options are: " + ACTION_SEND + " | " + ACTION_CONSUME + " | " + ACTION_TEST);
    }

    //--------------------
    
}