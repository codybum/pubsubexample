import Comm.ActiveMQHL7Consumer;
import Comm.ActiveMQHL7ConsumerParser;
import Comm.ActiveMQHL7Producer;

public class Launcher {



    public static void main(String[] argv) {

        try {


            String mode = "2";
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

}
