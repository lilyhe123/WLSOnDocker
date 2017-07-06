package samples;

import weblogic.protocol.Identity;
import weblogic.security.HMAC;

public class Const {
	  public final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";
	  public final static String JNDI_FACTORY_SAF="weblogic.jms.safclient.jndi.InitialContextFactoryImpl";
	  
	  public final static String cfClass = "weblogic.jndi.WLInitialContextFactory";  

      public final static String userName="system";
      public final static String password="gumby1234";	  
      
	  public final static String cfName = "cf1";
	  public final static String xacfName = "weblogic.jms.XAConnectionFactory";	  
	  public final static String url = "t3://m1:8003,m2:8005";
	  public final static String queueName = "queue1";
	  public final static String topicName = "topic";
	  
}
