package Comm;


import hl7.HL7Utils;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ActiveMQHL7Producer {

    private HL7Utils hl7Utils;

    public ActiveMQHL7Producer() {
        hl7Utils = new HL7Utils();
    }

    public void start() {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            Destination destination = session.createTopic("HL7.VITALS");

            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);


            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    try {
                        String hl7Text = hl7Utils.generateRecord();

                        // Create Text Message
                        TextMessage message = session.createTextMessage(hl7Text);

                        //get MD5 hash of message
                        String md5Hash = getMD5Checksum(hl7Text);

                        //set MD5 hash as property of message, this is just an example of setting message header
                        message.setStringProperty("md5", md5Hash);

                        // Tell the producer to send the message
                        System.out.println("ActiveMQHL7Producer(): [Sent message] \n\t" + message.hashCode() + " : " + Thread.currentThread().getName() + " " + new Date());
                        producer.send(message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            Timer timer = new Timer("Timer");

            long delay  = 1000L;
            long period = 5000L;
            timer.scheduleAtFixedRate(repeatedTask, delay, period);

        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public String getMD5Checksum(String checkString) throws Exception {
        byte[] b = checkString.getBytes(Charset.forName("UTF-8"));
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

}

