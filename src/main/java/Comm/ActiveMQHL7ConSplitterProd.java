package Comm;

import hl7.HL7Utils;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class ActiveMQHL7ConSplitterProd {

    private HL7Utils hl7Utils;

    public ActiveMQHL7ConSplitterProd() {
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

            // Create the destination for source HL7
            Destination source_destination = session.createTopic("HL7.VITALS");

            // Create the destination for source HL7
            Destination split_destination = session.createTopic("HL7.VITAL");

            MessageProducer producer = session.createProducer(split_destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);



            // Create a MessageConsumer from the Session to the Topic or Queue
            MessageConsumer consumer = session.createConsumer(source_destination);

            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {

                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            String hl7ORUString = textMessage.getText();

                            //System.out.println("ActiveMQHL7Consumer(): [Received]");
                            //hl7Utils.parseORURecord(hl7ORUString);
                            List<String> oruStringList = hl7Utils.parseORURecordList(hl7ORUString);

                            for(String oruString : oruStringList) {
                                //System.out.println(oruString);
                                TextMessage out_message = session.createTextMessage(oruString);
                                producer.send(out_message);
                            }
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


