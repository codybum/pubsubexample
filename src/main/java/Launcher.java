import Comm.*;
import hl7.HL7Utils;

public class Launcher {



    public static void main(String[] argv) {

        try {

            String mode = "3";
            if(argv.length > 0) {
                mode = argv[0];
            }

            switch(mode)
            {
                case "1":
                    //mode=1
                    //Send:     Generate HL7 Message and push it on a topic
                    //Receive:  Pull HL7 message off a topic and print out the message
                    modeOne();
                    break;
                case "2":
                    //mode=2
                    //Send:     Generate HL7 Message and push it on a topic
                    //Receive:  Pull HL7 message off a topic and print out the message
                    //Parse:    Parse incoming HL7 message and print observations
                    modeTwo();
                    break;
                case "3":
                    //mode=3
                    //Send:     Generate HL7 Message and push it on a topic
                    //Receive:  Pull HL7 message off a topic and print out the message
                    //Parse:    Parse incoming HL7 message and print observations
                    //Split:    Split observations into seperate records
                    //Convert:  Convert observations into their own FIHR records
                    //Send:     Send newly converted records to a new topic
                    //Receive:  Listen for new records on new topic (will implement CEP parser soon)
                    modeThree();
                    break;
                default:
                    System.out.println("no match");
            }



        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void modeOne() {
        ActiveMQHL7Consumer activeMQHL7Consumer = new ActiveMQHL7Consumer();
        activeMQHL7Consumer.start();

        ActiveMQHL7Producer activeMQHL7Producer = new ActiveMQHL7Producer();
        activeMQHL7Producer.start();
    }

    private static void modeTwo() {
        ActiveMQHL7ConsumerParser activeMQHL7ConsumerParser = new ActiveMQHL7ConsumerParser();
        activeMQHL7ConsumerParser.start();

        ActiveMQHL7Producer activeMQHL7Producer = new ActiveMQHL7Producer();
        activeMQHL7Producer.start();
    }

    private static void modeThree() {


        //read observations and send alerts
        ActiveMQHL7ConCEP activeMQHL7ConCEP = new ActiveMQHL7ConCEP();
        activeMQHL7ConCEP.start();

        //get message and chop it up based on observations
        ActiveMQHL7ConSplitterProd activeMQHL7ConSplitterProd = new ActiveMQHL7ConSplitterProd();
        activeMQHL7ConSplitterProd.start();

        //send original messages
        ActiveMQHL7Producer activeMQHL7Producer = new ActiveMQHL7Producer();
        activeMQHL7Producer.start();

    }

}
