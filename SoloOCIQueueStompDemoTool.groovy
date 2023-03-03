import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.stomp.Frame;
import io.vertx.ext.stomp.StompClient;
import io.vertx.ext.stomp.StompClientConnection;
import io.vertx.ext.stomp.StompClientOptions;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SoloOCIQueueStompDemoTool {
    private static final String QUEUEOCID = "QUEUEOCID";
    private static final String REGION = "REGION";
    private static final String ISVERBOSE = "VERBOSE";
    private static final String AUTHTOKEN = "AUTHTOKEN";
    private static final String UNAME = "USERNAME";
    private static final String TENANCY = "TENANCY";
    static final String DTG_FORMAT = "HH:mm:ss";

    private static final String ACTION_SEND = "send";
    private static final String ACTION_CONSUME = "consume";
    private static final String ACTION_TEST = "test";

    private StompClient stompClient = null;
    private Vertx vertx = null;
    private int sentCount = 0;
    private static boolean verbose = true;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DTG_FORMAT);
    private static Properties props = new Properties();


    private StompClientConnection stompClientConnection = null;
    private CompletableFuture<Void> futureForAck = new CompletableFuture<>();

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

    public void initialize (Properties props)  {  
        // creates the vertx instance.

        vertx = Vertx.vertx();
        final String tenancy = props.getProperty(TENANCY);
        final String user = props.getProperty(UNAME);
        final String region = props.getProperty(REGION);
        String authtoken = props.getProperty(AUTHTOKEN);

        if (tenancy == null)
        {
            log("No tenancy set");
            System.exit(1);
        }
        if (user == null)
        {
            log ("No user defined");
            System.exit(1);
        }
        if (region == null)
        {
            log ("No region set");
            System.exit(1);
        }
        if (authtoken == null)
        {
            authtoken = getTokenFromFile();
        }
        if (authtoken == null)
        {
            log ("no auth token set");
            System.exit(1);
        }

        try {
            String login = tenancy + "/" + user;
            String dp_hostname ="cell-1.queue.messaging."+region+".oci.oraclecloud.com";
            // as per https://docs.oracle.com/en-us/iaas/Content/queue/messages.htm#messages__messages-endpoint
            // _DP_HOSTNAME is the message endpoint availabe for the queue on the details
            // page in OCI console.

            log (dp_hostname + " -- " + login + " -- " + authtoken);

            // The passcode should be base64 encoded.
            String base64EncodedPasscode = Base64.getEncoder()
            .encodeToString(authtoken.getBytes(StandardCharsets.UTF_8));

            final StompClientOptions options = new StompClientOptions()
            .setHost(dp_hostname)
            .setPort(61613)
            .setSsl(true)
            .setLogin(login) // "YourTenancy/UserName"
            .setPasscode(base64EncodedPasscode);
            options.setTcpKeepAlive(true);

            log("Create STOMP client");
            // Creates the stomp client with error frame handler 
            // and exception handler. Error frame handler is to handle the error frame received
            // and exception handler is to handle the exception in case thrown.
            stompClient = StompClient.create(vertx, options)
            .errorFrameHandler(frame -> log("Error Frame received! message: " + frame.toString()))
            .exceptionHandler(throwable -> log("Exception Occurs! message: " + throwable.toString()));

            // establishes the connection, run the tests and join the results.
            getConnection(stompClient)
            //.thenCompose(conn -> new Stomp(conn).execTest())
            .whenComplete((unused, error) -> {
                    // Error messages are already logged in respective futures
                    stompClient.close();
                    stompClient = null;
                    log ("connection failure " + error.toString());
                })
            .join();

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

    /*
     * Unsubscribes the given queue id. The queue id is the OCID of the queue to be unsubscribed. 
     */
    private CompletableFuture<Void> unsubscribe() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        stompClientConnection.unsubscribe(props.getProperty(QUEUEOCID),
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
     * This closes the connection. It throws an error in case there was an error in processing
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
     * This subscribes to the queue. Queue is OCID of the queue and is stored as constant in
     * Environment variables java class. It has two handlers: message frame handler and receipt handler.
     */
    private CompletableFuture<Void> subscribe() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        final Map<String, String> queueSubscribeHeaders = new HashMap<>();
        queueSubscribeHeaders.put(Frame.ACK, "client-individual");
        stompClientConnection.subscribe(props.getProperty(QUEUEOCID),
            queueSubscribeHeaders,
            this::receiveMessage,
            ar -> {
                if(ar.succeeded()) {
                    log("RECEIPT for SUBSCRIBE frame received! subscription id=" + ar.result());
                    future.complete(null);
                } else {
                    System.out.println("Failure to process SUBSCRIBE frame!");
                    future.completeExceptionally(ar.cause());
                }
            });
        log("SUBSCRIBE Frame is sent!");
        return future;
    }

    /*
     * This sends the message to the queue. The queue is OCID of the destination queue.
     */
    private CompletableFuture<Void> sendMessage() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        LocalDateTime now = LocalDateTime.now();

        String content = "{\"MessageNo\" : " + sentCount + ", \"at\": " + "\""+ now.format(dtf) + "\", \"sent\" : \"from client app\"}";        

        stompClientConnection.send(props.getProperty(QUEUEOCID),
            Buffer.buffer(content),
            ar -> {
                if(ar.succeeded()) {
                    log("RECEIPT for SEND frame received!");
                    future.complete(null);
                } else {
                    log("Failure to process SEND frame! cause:" + ar.cause());
                    future.completeExceptionally(ar.cause());
                }
            });
        log("Sent MESSAGE! " + content);
        return future;
    }

    /*
     * This processes the message received on the subscribed queue. 
     * It receives the ACK frame and completes the future for ACK. 
     */
    private void receiveMessage(Frame frame) {
        String messageId = frame.getHeader("message-id");
        String content = frame.getBodyAsString();

        System.out.println("MESSAGE received, messageId=" + messageId +
                " ,size=" + content.getBytes().length + " message=" + content);

        stompClientConnection.ack(frame.getAck(),
            ar -> {
                if(ar.succeeded()) {
                    log("RECEIPT for ACK frame received!");
                    futureForAck.complete(null);
                } else {
                    log("Failure to process ACK frame!");
                    futureForAck.completeExceptionally(ar.cause());
                }
            });
        log("ACK frame sent");
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

    public static void main(String[] args) {
        String action = null;
        setPropertyFromVar (REGION, REGION, props);
        setPropertyFromVar (TENANCY, TENANCY, props);
        setPropertyFromVar (AUTHTOKEN, AUTHTOKEN, props);
        setPropertyFromVar (UNAME, UNAME, props);
        setPropertyFromVar (QUEUEOCID, QUEUEOCID, props);
        setPropertyFromVar (ISVERBOSE, ISVERBOSE, props);
        verbose =((System.getenv(ISVERBOSE) == null) || (System.getenv(ISVERBOSE).trim().equalsIgnoreCase("true")));

        SoloOCIQueueStompDemoTool stomper = new SoloOCIQueueStompDemoTool();

        stomper.initialize (props);

        if (args.length >0)
        {
            action = args[0].trim();
        }
        else
        {
            action = ACTION_SEND;
        }
        log ("action is:" + action);

        switch (action)
        {
        case ACTION_SEND:
            stomper.sendMessage();
            break;

        case ACTION_CONSUME:
            stomper.subscribe();
            break;
        case ACTION_TEST:
            stomper.execTest();
            break;
                
        default:
            System.out.println ("Options are: " + ACTION_SEND + " | " + ACTION_CONSUME + " | " + ACTION_TEST);
        }
        stomper.disconnect();        

    }
}