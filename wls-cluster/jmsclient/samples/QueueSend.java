package samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

public class QueueSend
{	
  private QueueConnectionFactory qconFactory;
  private QueueConnection qcon;
  private QueueSession qsession;
  private QueueSender qsender;
  private weblogic.jms.extensions.WLMessageProducer queueProducer;
  private Queue queue;
  private TextMessage msg;
  static private int index = 0;
  private static int cmd = 0; //1: UOO, 2: UOW
  private static boolean loop = false;

  /**
   * Sends a message to a JMS queue.
   *
   * @param message  message to be sent
   * @exception JMSException if JMS fails to send message due to internal error
   */
  public void send(String message) throws JMSException {
    if (cmd == 0) {
    	System.out.println("Msg sending: "+message+"\n");
    	msg.setText(message);
    	qsender.send(msg);
    } else if (cmd == 1) {
    	msg.setText(message);
    	queueProducer.send(msg);
    	 System.out.println("JMS Message Sent in UOO: "+message+"\n");
    } else if (cmd == 2) {
    	index ++;    
        msg.setStringProperty("JMS_BEA_UnitOfWork","myUOW");
        msg.setIntProperty("JMS_BEA_UnitOfWorkSequenceNumber",index);
        if (message.equalsIgnoreCase("quit")) {
        	msg.setBooleanProperty("JMS_BEA_IsUnitOfWorkEnd",true);
        }
        msg.setText(message);
        qsender.send(msg, DeliveryMode.PERSISTENT,7,0);
    	System.out.println("JMS Message Sent in UOW: "+message+"\n");
    }
   
  }

  /**
   * Closes JMS objects.
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  public void close() throws JMSException {
    qsender.close();
    qsession.close();
    qcon.close();
  }
  
  private static void loopSend(QueueSend qs) throws JMSException, InterruptedException {
	  for (int i=0; i<10; i++) {
		 String msg = "loopmsg-partition2-" + i;
		 qs.send(msg);
		 Thread.sleep(10);
	  }
  }

  private static void readAndSend(QueueSend qs)
    throws IOException, JMSException
  {
    BufferedReader msgStream = new BufferedReader(new InputStreamReader(System.in));
    String line=null;
    boolean quitNow = false;
    do {
      System.out.print("Enter message (\"quit\" to quit): \n");
      line = msgStream.readLine();
      if (line != null && line.trim().length() != 0) {
        qs.send(line);       
        quitNow = line.equalsIgnoreCase("quit");
      }
    } while (! quitNow);

  }
  
  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      System.out.println("arg[0] = " + args[0]);
      if (args[0].equalsIgnoreCase("UOO")) {
        cmd = 1;        
      } else if (args[0].equalsIgnoreCase("UOW")) {
        cmd = 2;
      } else if (args[0].equalsIgnoreCase("LOOP")) {
        loop = true;
      } else {
        System.out.println("QueueSend <option>");
        System.out.println("<option> can be UOO or UOW or LOOP");
        System.exit(0);
      }
    }
    System.out.println("cmd = " + cmd);
       
    
    InitialContext ic = getInitialContext();   
    QueueSend qs = new QueueSend();
    qs.init(ic);
    if (loop) {
      loopSend(qs);
    } else {
      readAndSend(qs);
    }
    qs.close();
  }

  private static InitialContext getInitialContext()
    throws Exception
  {
    Hashtable<String,String> env = new Hashtable<String,String>();

    //for non_SAF
    //env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jms.WrappedInitialContextFactory");
    env.put(Context.INITIAL_CONTEXT_FACTORY, Const.cfClass);
    env.put(Context.PROVIDER_URL, Const.url);
    //env.put(Context.SECURITY_PRINCIPAL, "mtadmin");
    //env.put(Context.SECURITY_CREDENTIALS, "Welcome1");
    env.put(Context.SECURITY_PRINCIPAL, Const.userName);
    env.put(Context.SECURITY_CREDENTIALS, Const.password);
    return new InitialContext(env);
  }
  
  public void init(Context ctx)
  throws Exception
  {
    System.out.println("lookup CF begin");
    qconFactory = (QueueConnectionFactory) ctx.lookup(Const.cfName);
    System.out.println("lookup CF end");
    queue = (Queue) ctx.lookup(Const.queueName);
    System.out.println("lookup queue end");    
    
    //System.out.println(((JMSConnectionFactory)qconFactory).getRemoteDelegate().toString());
    qcon = qconFactory.createQueueConnection();
    System.out.println("connetion is created on server: " + ((weblogic.jms.client.WLConnectionImpl) qcon).getWLSServerName());
    qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    System.out.println(qsession.getClass().toString());    
    
    if (cmd == 1) {
    queueProducer = (weblogic.jms.extensions.WLMessageProducer) qsession.createProducer(queue);
    queueProducer.setUnitOfOrder();
    System.out.println("UOO name: " + queueProducer.getUnitOfOrder());
    }
  
    qsender = qsession.createSender(queue);
  
    msg = qsession.createTextMessage();
    qcon.start();
  }

}

