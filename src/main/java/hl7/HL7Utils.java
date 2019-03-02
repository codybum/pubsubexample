package hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;


public class HL7Utils {


    public HL7Utils() {

    }

    public String generateRecord() {
        String recordString = null;

        try {
            LocalDateTime ldt = LocalDateTime.now();
            String localtimeString = DateTimeFormatter.ofPattern("yyyyMMddhhmmss", Locale.ENGLISH).format(ldt);

            int randomSBP = ThreadLocalRandom.current().nextInt(80, 200 + 1);
            int randomDBP = ThreadLocalRandom.current().nextInt(40, 130 + 1);
            int randomMBP = ThreadLocalRandom.current().nextInt(80, 110 + 1);
            int bodyTemp = ThreadLocalRandom.current().nextInt(33, 40 + 1);
            int pulse = ThreadLocalRandom.current().nextInt(50, 180 + 1);
            int bloodO2 = ThreadLocalRandom.current().nextInt(85, 99 + 1);


            recordString = "MSH|^~\\&|VSM002|MIRTH_CONNECT|HIS001|MIRTH_CONNECT|" + localtimeString + "||ORU^R01|MSG0000002|P|2.5|||NE|NE|CO|8859/1|ES-CO\r" +
                    "PID||87345125|87345125^^^^CC||NATHALIA^ORTEGA||19821029|F\r" +
                    "OBR|1||VS12350000|28562-7^Vital Signs^LN\r" +
                    "OBX|1|NM|271649006^Systolic blood pressure^SNOMED-CT||" + randomSBP + "|mm[Hg]|90-120|N|||F|||" + localtimeString + "\r" +
                    "OBX|2|NM|271650006^Diastolic blood pressure^SNOMED-CT||" + randomDBP + "|mm[Hg]|60-80|N|||F|||" + localtimeString + "\r" +
                    "OBX|3|NM|6797001^Mean blood pressure^SNOMED-CT||" + randomMBP + "|mm[Hg]|92-96|N|||F|||" + localtimeString + "\r" +
                    "OBX|4|NM|386725007^Body temperature^SNOMED-CT||" + bodyTemp + "|C|37|N|||F|||" + localtimeString + "\r" +
                    "OBX|5|NM|78564009^Pulse rate^SNOMED-CT||" + pulse + "|bpm|60-100|N|||F|||" + localtimeString + "\r" +
                    "OBX|6|NM|431314004^SpO2^SNOMED-CT||" + bloodO2 + "|%|94-100|N|||F|||" + localtimeString + "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return recordString;
    }


    public void parseORURecordPrintable(String recordString) {
        try {

            HapiContext context = new DefaultHapiContext();

            PipeParser parser = context.getPipeParser();
            parser.setValidationContext(new NoValidation());

            Message hapiMsg;
            try {
                // The parse method performs the actual parsing
                hapiMsg = parser.parse(recordString);
            } catch (EncodingNotSupportedException e) {
                e.printStackTrace();
                return;
            } catch (HL7Exception e) {
                e.printStackTrace();
                return;
            }

            ORU_R01 ORUmsg = (ORU_R01)hapiMsg;
            MSH msh = ORUmsg.getMSH();

            PID pid = ORUmsg.getPATIENT_RESULT().getPATIENT().getPID();
            String patientId = pid.getPatientID().getIDNumber().getValue();

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
            Date date       = format.parse (msh.getDateTimeOfMessage().encode());

            System.out.println("Patient ID:\t\t" + patientId);
            System.out.println("Record Date:\t" + date);

            List<ORU_R01_OBSERVATION> observations = ORUmsg.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATIONAll();

            for(ORU_R01_OBSERVATION oru_r01_observation : observations) {

                OBX obx = oru_r01_observation.getOBX();

                System.out.println("Result:\t" + obx.getObservationValue(0).encode() + "\t" + "Observation Type:" + obx.getObservationIdentifier().getText());

            }
            System.out.println("");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void parseORURecord(String recordString) {
        try {

            HapiContext context = new DefaultHapiContext();

            PipeParser parser = context.getPipeParser();
            parser.setValidationContext(new NoValidation());

            Message hapiMsg;
            try {
                // The parse method performs the actual parsing
                hapiMsg = parser.parse(recordString);
            } catch (EncodingNotSupportedException e) {
                e.printStackTrace();
                return;
            } catch (HL7Exception e) {
                e.printStackTrace();
                return;
            }

            ORU_R01 ORUmsg = (ORU_R01)hapiMsg;
            MSH msh = ORUmsg.getMSH();

            PID pid = ORUmsg.getPATIENT_RESULT().getPATIENT().getPID();
            String patientId = pid.getPatientID().getIDNumber().getValue();

            System.out.println("patientId: " + patientId);

            //System.out.println(pid.getPatientID().getIDNumber().getValue());
            //System.out.println(pid.getPatientName()[0].getGivenName() + " " + pid.getPatientName()[0].getFamilyName().getSurname());

            //String msgType = msh.getMessageType().getMessageCode().getVersion();
            //String msgTrigger = msh.getMessageType().getTriggerEvent().getValue();



            List<ORU_R01_OBSERVATION> observations = ORUmsg.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATIONAll();

            for(ORU_R01_OBSERVATION oru_r01_observation : observations) {

                OBX obx = oru_r01_observation.getOBX();

                String obx_ident = obx.getObservationIdentifier().getIdentifier().encode();
                String obx_time = obx.getDateTimeOfTheObservation().encode();
                String obx_value = obx.getObservationValue(0).encode();

                System.out.println(obx_time + "," + obx_ident + "," + obx_value);

                //System.out.println(obx.getObservationIdentifier());
                //System.out.println(obx.getObservationIdentifier().getIdentifier());
                //System.out.println(obx.getObservationIdentifier().getText());
                //System.out.println(obx.getObservationIdentifier().getNameOfCodingSystem());

                //System.out.println(obx.getDateTimeOfTheObservation());

                //System.out.println(obx.getObservationValue(0).encode());

            /*
            String[] rangeString = obx.getReferencesRange().getValue().split("-");
            int low = Integer.parseInt(rangeString[0]);
            int high = Integer.parseInt(rangeString[1]);
            */
            }




        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
