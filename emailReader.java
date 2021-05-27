import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class emailReader {
	//save directory location for attached files.
	private String directory;
	public void setDirectoryLocation(String dirLocation) { this.directory = dirLocation; }
	//main method to pass into download email params
	
	public static void main(String[]args) {
		String host = "pop.gmail.com";
		final String user = "EXAMPLE_USERNAME";
		final String pass = "EXAMPLE_PASSWORD";
		final String port = "995"; 
		String directory = "ENTER DIRECTORY PATH HERE";
		emailReader reader = new emailReader();		
		reader.setDirectoryLocation(directory);
		reader.downloadEmail(host, user, pass, port);
	}
	//download email method
	public void downloadEmail(String host, String user, String pass, String port) {
		Properties props = new Properties();
		
		//server settings
		props.put("mail.pop3.host", host);
		props.put("mail.pop3.port", port);
		//SSL
		props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.pop3.socketFactory.fallback", "false");
		props.setProperty("mail.pop3.socketFactory.port", String.valueOf(port));
		Session sess = Session.getDefaultInstance(props);
		try {
			// message store
			Store store = sess.getStore("pop3");
			store.connect(user, pass);
			// open inbox folder
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
		
			// get new messages from server
			Message[] msgArr = inbox.getMessages();
			for(int i = 0; i < msgArr.length; i++)
			{
				Message msg = msgArr[i];
				Address[] fromAddress = msg.getFrom();
				String from = fromAddress[0].toString();
				String subject = msg.getSubject();
				String date = msg.getSentDate().toString();
				String contentType = msg.getContentType();
				String content = "";
				// store attachment file names each separated by a ','
				String attachFiles = "";
				if(contentType.contains("multipart")) {
					//possible attachments in content
					Multipart multiPart = (Multipart) msg.getContent();
					int numParts = multiPart.getCount();
					for(int counter = 0; counter < numParts; counter++)
					{
						MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(counter);
						if(Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
							// attachment
							String fileName = part.getFileName();
							attachFiles += fileName+", ";
							part.saveFile(directory + File.separator + fileName);
						//likely msg content section
						}else content = part.getContent().toString();
						
					}
					//remove extra comma at the end of attachFiles string
					if(attachFiles.length() > 1) attachFiles = attachFiles.substring(0, attachFiles.length()-2);
				}else if(contentType.contains("text/plain") || contentType.contains("text/html")){
					//put html and plain text content into content string
					Object contentObj = msg.getContent();
					if(content != null)content  = contentObj.toString();
				}
				//print details
				System.out.println("Message Number ("+(i+1)+"):\n From: "+from+"\n Subject: "+subject+"\n Date"+date+"\n Message: "+content+"\n Attachments: "+attachFiles);
			}
			//disconnect from message store
			inbox.close(false);
			store.close();
		}catch(NoSuchProviderException e) {
			System.out.println("No provider for pop3.");
			e.printStackTrace();
		}catch(MessagingException e) {
			System.out.println("Could not connect to message store");
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
