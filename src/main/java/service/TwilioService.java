package service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;

/**
 * A singleton for communicating with the Twilio service
 * @author Jake Shamash
 */
public class TwilioService {
	// TODO move to config file
	private static final String ACCOUNT_SID = "AC013d2d273f8193d60cb7654672f5aab0";
	private static final String AUTH_TOKEN = "fd40f8ee74bd6ec026bead17925d5c5d";
	private static final String DEST_NUMBER = "+15148652279";
	private static final String SRC_NUMBER = "+14387938511";
		
	private static TwilioService instance = null;
	private TwilioRestClient client;
	
	private TwilioService() {
		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
	}

	public static TwilioService getInstance() {
		if (instance == null)
			instance = new TwilioService();
		return instance;
	}
	
	/**
	 * Send a message to the specified number via Twilio
	 * @param message The message to send
	 */
	public void sendMessage(String message) {
		// Build a filter for the MessageList
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("Body", message));
	    params.add(new BasicNameValuePair("To", DEST_NUMBER));
	    params.add(new BasicNameValuePair("From", SRC_NUMBER));	     
	     
	    try {
		    MessageFactory messageFactory = client.getAccount().getMessageFactory();
		    messageFactory.create(params);
	    } catch (TwilioRestException ex) {
	    	// Handle silently -- the SMS won't send, but we won't report an error.
	    }
	}
}
