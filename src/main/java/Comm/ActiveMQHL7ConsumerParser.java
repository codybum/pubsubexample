package Comm;

import hl7.HL7Utils;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActiveMQHL7ConsumerParser {

    private HL7Utils hl7Utils;

    public ActiveMQHL7ConsumerParser() {
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

            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {

                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            String hl7ORUString = textMessage.getText();

                            System.out.println("ActiveMQHL7Consumer(): [Received]");
                            hl7Utils.parseORURecordPrintable(hl7ORUString);


                        }


                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


            //            session.close();
            //connection.close();
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

}


