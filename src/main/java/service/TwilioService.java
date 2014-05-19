package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

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
	private String accountSID = "";
	private String authToken = "";
	private String destNumber = "+15555555555";
	private String srcNumber = "+15555555555";
	
	private final static Logger LOGGER = Logger.getLogger(TwilioService.class.getName()); 
		
	private static TwilioService instance = null;
	private TwilioRestClient client;
	
	private TwilioService() {
		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		
		try {
			Properties properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
			accountSID = properties.getProperty("twilio_account_ssid");
			authToken = properties.getProperty("twilio_auth_token");
			destNumber = properties.getProperty("twilio_dest_number");
			srcNumber = properties.getProperty("twilio_src_number");
		} catch (Exception e) {
			LOGGER.warning("No config.properties file found, using defaults");
		}
		LOGGER.info("Set Twilio account SID to " + accountSID);
		LOGGER.info("Set Twilio authentication token to " + authToken);
		LOGGER.info("Set Twilio destination phone number to " + destNumber);
		LOGGER.info("Set Twilio source phone number to " + srcNumber);
		client = new TwilioRestClient(accountSID, authToken);
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
	    params.add(new BasicNameValuePair("To", destNumber));
	    params.add(new BasicNameValuePair("From", srcNumber));	     
	     
	    try {
		    MessageFactory messageFactory = client.getAccount().getMessageFactory();
		    messageFactory.create(params);
	    } catch (TwilioRestException ex) {
	    	// Handle silently -- the SMS won't send, but we won't report an error.
	    }
	}
}
