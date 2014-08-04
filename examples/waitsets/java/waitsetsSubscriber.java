/*******************************************************************************
 (c) 2005-2014 Copyright, Real-Time Innovations, Inc.  All rights reserved.
 RTI grants Licensee a license to use, modify, compile, and create derivative
 works of the Software.  Licensee has the right to distribute object form only
 for use with RTI products.  The Software is provided "as is", with no warranty
 of any type, including any warranty for fitness for any purpose. RTI is under
 no obligation to maintain or support the Software.  RTI shall not be liable for
 any incidental or consequential damages arising out of the use or inability to
 use the software.
 ******************************************************************************/

/* waitsetsSubscriber.java

   A publication of data of type waitsets

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type waitsets automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription on the same domain used for with the command
       java waitsetsSubscriber <domain_id> <sample_count>

   (3) Start the publication with the command
       java waitsetsPublisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS. 
       
   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.
              
                                   
   Example:
        
       To run the example application on domain <domain_id>:
            
       Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
       Java.                       
       
        On UNIX systems: 
             add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
             variable
                                         
        On Windows systems:
             add %NDDSHOME%\lib\<arch> to the 'Path' environment variable
                        

       Run the Java applications:
       
        java -Djava.ext.dirs=$NDDSHOME/class waitsetsPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class waitsetsSubscriber <domain_id>  
       
       
modification history
------------ -------   
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class waitsetsSubscriber {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------
    
    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }
        
        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }
        
        
        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */
        
        // --- Run --- //
        subscriberMain(domainId, sampleCount);
    }
    
    
    
    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------
    
    // --- Constructors: -----------------------------------------------------
    
    private waitsetsSubscriber() {
        super();
    }
    
    
    // -----------------------------------------------------------------------
    
    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Topic topic = null;
        DataReaderListener listener = null;
        waitsetsDataReader reader = null;

        try {

            // --- Create participant --- //
    
            /* To customize participant QoS, use
               the configuration file
               USER_QOS_PROFILES.xml */    
            participant = DomainParticipantFactory.TheParticipantFactory.
                create_participant(
                    domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }                         

            // --- Create subscriber --- //
    
            /* To customize subscriber QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (subscriber == null) {
                System.err.println("create_subscriber error\n");
                return;
            }     
                
            // --- Create topic --- //
        
            /* Register type before creating topic */
            String typeName = waitsetsTypeSupport.get_type_name(); 
            waitsetsTypeSupport.register_type(participant, typeName);
    
            /* To customize topic QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            topic = participant.create_topic(
                "Example waitsets",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     
        
            // --- Create reader --- //

            
            /* To customize data reader QoS, use
               the configuration file USER_QOS_PROFILES.xml */
            reader = (waitsetsDataReader)
                subscriber.create_datareader(
                    topic, Subscriber.DATAREADER_QOS_DEFAULT, null,
                    StatusKind.STATUS_MASK_NONE);
            if (reader == null) {
                System.err.println("create_datareader error\n");
                return;
            }          
            
            
            /* If you want to change the DataReader's QoS programmatically 
             * rather than using the XML file, you will need to add the 
             * following lines to your code and comment out the 
             * create_datareader call above.
             *
             * In this case, we reduce the liveliness timeout period to trigger 
             * the StatusCondition DDS_LIVELINESS_CHANGED_STATUS
             */
            
            /*
            DataReaderQos datareader_qos = new DataReaderQos();
            subscriber.get_default_datareader_qos(datareader_qos);

            datareader_qos.liveliness.lease_duration.sec = 2;
            datareader_qos.liveliness.lease_duration.nanosec = 0;
        
            reader = (waitsetsDataReader)
                    subscriber.create_datareader(
                        topic, datareader_qos, null,
                        StatusKind.STATUS_MASK_NONE);
            if (reader == null) {
                System.err.println("create_datareader error\n");
                return;
            }
            */

            /* Create read condition
             * ---------------------
             * Note that the Read Conditions are dependent on both incoming
             * data as well as sample state. Thus, this method has more
             * overhead than adding a DDS_DATA_AVAILABLE_STATUS StatusCondition.
             * We show it here purely for reference
             */
            ReadCondition read_condition = reader.create_readcondition(
            		SampleStateKind.NOT_READ_SAMPLE_STATE,
            		ViewStateKind.ANY_VIEW_STATE,
            		InstanceStateKind.ANY_INSTANCE_STATE);

            /* Get status conditions
             * ---------------------
             * Each entity may have an attached Status Condition. To modify the
             * statuses we need to get the reader's Status Conditions first.
             */
            StatusCondition status_condition = reader.get_statuscondition();

            /* Set enabled statuses
             * --------------------
             * Now that we have the Status Condition, we are going to enable the
             * statuses we are interested in: DDS_SUBSCRIPTION_MATCHED_STATUS 
             * and DDS_LIVELINESS_CHANGED_STATUS.
             */
            status_condition.set_enabled_statuses(
                    StatusKind.SUBSCRIPTION_MATCHED_STATUS |
                    StatusKind.LIVELINESS_CHANGED_STATUS);

            /* Create and attach conditions to the WaitSet
             * -------------------------------------------
             * Finally, we create the WaitSet and attach both the Read 
             * Conditions and the Status Condition to it.
             */
            WaitSet waitset = new WaitSet();            
            waitset.attach_condition(read_condition);
            waitset.attach_condition(status_condition);

            
            // --- Wait for data --- //

            Duration_t duration = new Duration_t(1, 500000000);

            for (int count = 0;
                 (sampleCount == 0) || (count < sampleCount);
                 ++count) {
                ConditionSeq active_conditions = new ConditionSeq();
                try {
                    /* wait() blocks execution of the thread until one or more 
                     * attached Conditions become true, or until a 
                     * user-specified timeout expires.
                     */
                    waitset.wait(active_conditions, duration);
                } catch (RETCODE_TIMEOUT e) {
                    /* We get to timeout if no conditions were triggered */
                    System.out.print("Wait timed out!! No conditions were"
                            + " triggered\n");
                    continue;
                }
                /* Get the number of active conditions */
                System.out.print("Got" + active_conditions.size() + 
                		" active conditions\n");
                for (int i = 0; i < active_conditions.size(); ++i) {
                    /* Now we compare the current condition with the Status
                     * Conditions and the Read Conditions previously defined. If
                     * they match, we print the condition that was triggered.*/

                    /* Compare with Status Conditions */
                    if (active_conditions.get(i) == status_condition) {
                        /* Get the status changes so we can check which status
                         * condition triggered. */
                        int triggeredmask = reader.get_status_changes();

                        /* Liveliness changed */
                        if ((triggeredmask & 
                        		StatusKind.LIVELINESS_CHANGED_STATUS) != 0) {
                            LivelinessChangedStatus st = 
                            		new LivelinessChangedStatus();
                            reader.get_liveliness_changed_status(st);
                            System.out.print("Liveliness changed => "
                                    + "Active writers = "
                                             + st.alive_count + "\n");
                        }
                        
                        /* Subscription matched */
                        if ((triggeredmask & 
                        		StatusKind.SUBSCRIPTION_MATCHED_STATUS) !=0) {
                            SubscriptionMatchedStatus st = 
                            		new SubscriptionMatchedStatus();
                            reader.get_subscription_matched_status(st);
                            System.out.print("Subscription matched => "
                                    + "Cumulative matches = "
                                             + st.total_count + "\n");
                        }
                    } 
                    /* Compare with Read Conditions */
                    else if (active_conditions.get(i) == read_condition) {
                        /* Current conditions match our conditions to read data,
                         * so we can read data just like we would do in any 
                         * other example. */
                    	waitsetsSeq data_seq = new waitsetsSeq();
                        SampleInfoSeq info_seq = new SampleInfoSeq();

                        /* You may want to call take_w_condition() or
                         * read_w_condition() on the Data Reader. This way you 
                         * will use the same status masks that were set on the 
                         * Read Condition. This is just a suggestion, you can 
                         * always use read() or take() like in any other 
                         * example.
                         */
                        boolean follow = true;
                        while (follow) {
                            try{
                                reader.take_w_condition(
                                    data_seq, info_seq,
                                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                                    read_condition);
        
                                for (int j = 0; j < info_seq.size(); ++j) {
                                    if (!((SampleInfo)
                                            info_seq.get(j)).valid_data) {
                                        System.out.print("Got metadata\n");
                                        continue;
                                    }
                                    
                                    waitsets data = (waitsets)data_seq.get(j);
                                    System.out.println("   x: " + data.x);
        
                                }
                            } catch (RETCODE_NO_DATA noData) {
                                /* When there isn't data, the subscriber stop to
                                 * take samples
                                 */
                                follow = false;
                            } finally {
                                reader.return_loan(data_seq, info_seq);
                            }
                        }
                    }
                }       
            }
        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                    delete_participant(participant);
            }
            /* RTI Connext provides the finalize_instance()
               method for users who want to release memory used by the
               participant factory singleton. Uncomment the following block of
               code for clean destruction of the participant factory
               singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
        
}


        