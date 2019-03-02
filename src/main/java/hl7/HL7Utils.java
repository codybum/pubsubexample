package hl7;

import ca.uhn.fhir.context.FhirContext;
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
import org.hl7.fhir.r4.model.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;


public class HL7Utils {


    private FhirContext ctxR4;

    public HL7Utils() {
        ctxR4 = FhirContext.forR4();
    }

    public Observation getObservationBySNOMED(String snomedId, String value) {
        Observation observation = null;
        try {

            observation = new Observation();
            observation.setStatus(Observation.ObservationStatus.FINAL);
            CodeableConcept codeableConcept = new CodeableConcept();
            Coding coding = new Coding();
            coding.setSystem("http://terminology.hl7.org/CodeSystem/observation-category");
            coding.setDisplay("Vital Signs");
            coding.setCode("vital-signs");
            List<CodeableConcept> codeableConceptsList = new ArrayList<>();
            observation.setCategory(codeableConceptsList);

            switch(snomedId)
            {
                case "271649006": //Systolic blood pressure
                    observation
                            .getCode()
                            .addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("271649006")
                            .setDisplay("Systolic blood pressure");

                    observation.setValue(
                            new Quantity()
                                    .setValue(Long.parseLong(value))
                                    .setUnit("mmHg")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("mm[Hg]"));
                    break;
                case "271650006": //Diastolic blood pressure
                    observation
                            .getCode()
                            .addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("271650006")
                            .setDisplay("Diastolic blood pressure");

                    observation.setValue(
                            new Quantity()
                                    .setValue(Long.parseLong(value))
                                    .setUnit("mmHg")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("mm[Hg]"));
                    break;

                case "6797001": //Mean blood pressure
                    observation
                            .getCode()
                            .addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("6797001")
                            .setDisplay("Mean blood pressure");

                    observation.setValue(
                            new Quantity()
                                    .setValue(Long.parseLong(value))
                                    .setUnit("mmHg")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("mm[Hg]"));
                    break;

                case "386725007": //Body temperature
                    observation
                            .getCode()
                            .addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("386725007")
                            .setDisplay("Body temperature");

                    observation.setValue(
                            new Quantity()
                                    .setValue(Long.parseLong(value))
                                    .setUnit("C")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("C"));
                    break;


                case "78564009": //Pulse rate
                    observation
                            .getCode()
                            .addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("78564009")
                            .setDisplay("Pulse rate");

                    observation.setValue(
                            new Quantity()
                                    .setValue(Long.parseLong(value))
                                    .setUnit("bpm")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("bpm"));
                    break;

                case "431314004": //SpO2
                    observation
                            .getCode()
                            .addCoding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("431314004")
                            .setDisplay("SpO2");

                    observation.setValue(
                            new Quantity()
                                    .setValue(Long.parseLong(value))
                                    .setUnit("%")
                                    .setSystem("http://unitsofmeasure.org")
                                    .setCode("%"));
                    break;



                default:
                    System.out.println("no match");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return observation;
    }

    public String getFIHRfromV2(MSH msh, PID pid, OBX obx) {
        String FIHRString = null;
        try {


            Patient patient = new Patient();
            // Add an MRN (a patient identifier)
            Identifier id = patient.addIdentifier();
            id.setSystem("http://example.com/fictitious-mrns");
            id.setValue(pid.getPatientID().getIDNumber().getValue());

            // Add a name
            HumanName name = patient.addName();
            name.setUse(HumanName.NameUse.OFFICIAL);
            name.setFamily(pid.getPatientName()[0].getFamilyName().getSurname().encode());
            name.addGiven(pid.getPatientName()[0].getGivenName().encode());

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
            Date date       = format.parse (msh.getDateTimeOfMessage().encode());

            Observation observation = getObservationBySNOMED(obx.getObservationIdentifier().getIdentifier().encode(),obx.getObservationValue(0).encode());
            DateTimeType edate = new DateTimeType();
            edate.setValue(date);
            observation.setEffective(edate);

            // The observation refers to the patient using the ID, which is already
            observation.setSubject(new Reference(patient.getId()));

            // Create a bundle that will be used as a transaction
            Bundle bundle = new Bundle();
            bundle.setType(Bundle.BundleType.MESSAGE);

            // Add the patient as an entry. This entry is a POST with an
            // If-None-Exist header (conditional create) meaning that it
            // will only be created if there isn't already a Patient with
            // the identifier 12345
            bundle.addEntry()
                    .setFullUrl(patient.getId())
                    .setResource(patient)
                    .getRequest()
                    .setUrl("Patient")
                    .setIfNoneExist("identifier=http://acme.org/mrns|12345");
            //.setMethod(HTTPVerbEnum.POST);

            // Add the observation. This entry is a POST with no header
            // (normal create) meaning that it will be created even if
            // a similar resource already exists.
            bundle.addEntry()
                    .setResource(observation)
                    .getRequest()
                    .setUrl("Observation");
            //.setMethod(HTTPVerbEnum.POST);


            FIHRString = ctxR4.newXmlParser().encodeResourceToString(bundle);
            //String encoded = ctxR4.newXmlParser().encodeResourceToString(bundle);
            //System.out.println(encoded);

            //String encoded = ctxR4.newXmlParser().encodeResourceToString(patient);
            //System.out.println(encoded);


        } catch (Exception ex){
            ex.printStackTrace();
        }
        return FIHRString;
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

            System.out.println("Patient ID:\t" + patientId + "\t" +"Date:\t" + date);
            System.out.println("--------------------------------------------------------------");
            List<ORU_R01_OBSERVATION> observations = ORUmsg.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATIONAll();

            for(ORU_R01_OBSERVATION oru_r01_observation : observations) {

                OBX obx = oru_r01_observation.getOBX();

                System.out.println("Result:\t" + obx.getObservationValue(0).encode() + "\t" + "Observation Type:\t" + obx.getObservationIdentifier().getText());

            }
            System.out.println("");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public List<String> parseORURecordList(String recordString) {
        List<String> oruStringList = null;
        try {

            HapiContext context = new DefaultHapiContext();

            PipeParser parser = context.getPipeParser();
            parser.setValidationContext(new NoValidation());

            Message hapiMsg = null;
            try {
                // The parse method performs the actual parsing
                hapiMsg = parser.parse(recordString);
            } catch (EncodingNotSupportedException e) {
                e.printStackTrace();

            } catch (HL7Exception e) {
                e.printStackTrace();

            }

            if(hapiMsg != null) {

                oruStringList = new ArrayList<>();

                ORU_R01 ORUmsg = (ORU_R01) hapiMsg;
                MSH msh = ORUmsg.getMSH();

                PID pid = ORUmsg.getPATIENT_RESULT().getPATIENT().getPID();
                String patientId = pid.getPatientID().getIDNumber().getValue();

                List<ORU_R01_OBSERVATION> observations = ORUmsg.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATIONAll();

                for (ORU_R01_OBSERVATION oru_r01_observation : observations) {

                    OBX obx = oru_r01_observation.getOBX();
                    /*
                    String obx_ident = obx.getObservationIdentifier().getIdentifier().encode();
                    String obx_time = obx.getDateTimeOfTheObservation().encode();
                    String obx_value = obx.getObservationValue(0).encode();

                    String returnString = patientId + "," + obx_time + "," + obx_ident + "," + obx_value;
                    */
                    String returnString = getFIHRfromV2(msh,pid,obx);
                    System.out.println(returnString);
                    oruStringList.add(returnString);
                }
            }




        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return oruStringList;
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
