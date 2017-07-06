package samples;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Scanner;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;


public class QueueReceive
{
  private QueueConnectionFactory qconFactory;
  private QueueConnection qcon;
  private QueueSession qsession;
  private QueueReceiver qreceiver;
  private Queue queue;
  private Scanner in = new Scanner(System.in);

 /**
  * Receives message interface.
  */
  public void receiveMessages() throws Exception {
    Message msg = null;
    String msgText = "";
    int count =0;
     
    try {
      do {
    	count ++;
        msg = qreceiver.receive();
        if (msg != null) {
          if (msg instanceof TextMessage) {
            msgText = ((TextMessage)msg).getText();
          } else {
            msgText = msg.toString();
          }
          System.out.println("Message Received: "+ msgText + " from " + msg.getJMSDestination());
          System.out.print("Enter:");
          BufferedReader msgStream = new BufferedReader(new InputStreamReader(System.in));
          String line = msgStream.readLine();
          
          if (msgText.equalsIgnoreCase("quit")) {
            System.exit(0);
          }          
          msg.acknowledge();
        }
      } while(1 == 1);
    } catch (JMSException jmse) {
      System.out.println("Error receiving JMS message: "+jmse);
      System.err.println("An exception occurred: "+jmse.getMessage());
      throw jmse;
    }
  }

  /**
   * Creates all the necessary objects for receiving
   * messages from a JMS queue.
   *
   * @param   ctx	JNDI initial context
   * @param	topicName	name of queue
   * @exception NamingException operation cannot be performed
   * @exception JMSException if JMS fails to initialize due to internal error
   */
  public void init(Context ctx)
    throws NamingException, JMSException
  {
    qconFactory = (QueueConnectionFactory) ctx.lookup(Const.cfName);
    qcon = qconFactory.createQueueConnection();
    qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    queue = (Queue) ctx.lookup(Const.queueName);
    qreceiver = qsession.createReceiver(queue);
    qcon.start();
  }

  /**
   * Closes JMS objects.
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  public void close() throws JMSException {
    qreceiver.close();
    qsession.close();
    qcon.close();
  }

 /**
  * main() method.
  *
  * @param args  WebLogic Server URL
  * @exception  Exception if execution fails
  */
  public static void main(String[] args) throws Exception {
    /*if (args.length != 1) {
      System.out.println("Usage: java examples.jms.queue.QueueReceiveInTx WebLogicURL");
      return;
    }*/
    InitialContext ic = getInitialContext();
    QueueReceive qr = new QueueReceive();
    qr.init(ic);

    System.out.println("JMS Ready To Receive Message.");

    qr.receiveMessages();
    qr.close();
  }

  private static InitialContext getInitialContext()
    throws NamingException
  {
    Hashtable<String,String> env = new Hashtable<String,String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, Const.cfClass);
    env.put(Context.PROVIDER_URL, Const.url);
    return new InitialContext(env);
  }

}




