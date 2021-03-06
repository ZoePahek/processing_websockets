package websockets;

import java.lang.reflect.Method;
import java.net.URI;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import processing.core.PApplet;

/**
 * 
 * @author Lasse Steenbock Vestergaard
 *
 * Class for creating websocket client connections to any websocket server. Sub-protocols have not yet been implemented, and it's therefore only possible to connect to regular websocket servers. 
 *
 */
public class WebsocketClient {
	private Method webSocketEvent;
	private Method webSocketEventBinary;
	private Method webSocketOnError;
	private WebsocketClientEvents socket;

	/**
	 *
	 * Initiating the client connection
	 *
	 * @param parent Processing's PApplet object
	 * @param endpointURI The URI to connect to Ex. ws://localhost:8025/john
	 */
	public WebsocketClient(PApplet parent, String endpointURI) {
		this(parent, parent, endpointURI);
	}

	/**
	 * More flexible constructor in case you don't want callbacks called
	 * in your PApplet but in a different class. Use this if you are
	 * instantiating WebsocketClient in a class.
	 *
	 * @param parent Processing's PApplet object
	 * @param callbacks The object implementing .webSocketEvent()
	 * @param endpointURI The URI to connect to Ex. ws://localhost:8025/john
	 */
	public WebsocketClient(PApplet parent, Object callbacks, String endpointURI) {
		parent.registerMethod("dispose", this);

		try {
			webSocketEvent = callbacks.getClass().getMethod("webSocketEvent", String.class);
			webSocketOnError = callbacks.getClass().getMethod("webSocketOnError", Throwable.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			webSocketEventBinary = callbacks.getClass().getMethod("webSocketEvent", byte[].class, int.class, int.class);
		} catch (Exception e){
			// This Method is optional. Ignore if it doesn't exist.
		}

		WebSocketClient client = null;

		// Commented out because deprecated.
//		if(endpointURI.startsWith("wss")) {
//			SslContextFactory ssl = new SslContextFactory();
//			client = new WebSocketClient(ssl);
//		} else {
//			client = new WebSocketClient();
//		}
		client = new WebSocketClient();

		try {
			socket = new WebsocketClientEvents(callbacks, webSocketEvent,
					webSocketEventBinary, webSocketOnError);
			client.start();
			URI echoUri = new URI(endpointURI);
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			client.connect(socket, echoUri, request);
		} catch (Throwable t) {
			throw new RuntimeException();
		}

		// This is causing the sketch to hang if the connection fails.
//		try {
//			socket.getLatch().await();
//		} catch (InterruptedException e) {
//			// Ignore.
//		}
	}

	/**
	 *
	 * Send message to the websocket server. At a later stage it should be possible to send messages to specific clients connected to the same server
	 *
	 * @param message The message to send
	 */
	public void sendMessage(String message){
		socket.sendMessage(message);
	}

	public void sendMessage(byte[] data){
		socket.sendMessage(data);
	}
	
	public void dispose(){
		// Anything in here will be called automatically when 
	    // the parent sketch shuts down. For instance, this might
	    // shut down a thread used by this library.
	}
}
