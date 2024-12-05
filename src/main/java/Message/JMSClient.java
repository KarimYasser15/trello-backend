package Message;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;

@Startup
@Singleton
public class JMSClient {
	
	@Resource(mappedName = "java:/jms/queue/DLQ")
	private Queue helloWorldQueue;
	
	@Inject
	JMSContext context;
	
	public void sendMessage(String msg)
	{
		try
		{
			JMSProducer producer = context.createProducer();
			
			TextMessage message = context.createTextMessage(msg);
			
			producer.send(helloWorldQueue, message);
			
			System.out.println("Sent Message:\n" + msg);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}
