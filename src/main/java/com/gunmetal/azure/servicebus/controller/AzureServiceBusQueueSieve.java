package com.gunmetal.azure.servicebus.controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.gunmetal.azure.servicebus.model.AzureServiceBusQueueSieveConfiguration;

import reactor.core.publisher.Flux;

public class AzureServiceBusQueueSieve {

    private static Boolean SUBSCRIBER_HOOKED = false;
    private static LinkedListServiceBusReceivedMessageQueue linkedListServiceBusReceivedMessageQueue = new LinkedListServiceBusReceivedMessageQueue();

    public static Flux<ServiceBusReceivedMessage> subscribe(AzureServiceBusQueueSieveConfiguration azureServiceBusQueueSieveConfiguration) {
        SUBSCRIBER_HOOKED = true;
        receiveMessages(azureServiceBusQueueSieveConfiguration);
        return Flux.create(sink -> {
            sink.next(deliverMessages());
            if(!SUBSCRIBER_HOOKED)
            {
                sink.complete();
            }
        });
    }


    private static ServiceBusReceivedMessage deliverMessages()
    {
        while(SUBSCRIBER_HOOKED)
        {
            if(!linkedListServiceBusReceivedMessageQueue.isQueueEmpty())
            {
                return linkedListServiceBusReceivedMessageQueue.deque();
            }
        }
        return null;
    }

    // handles received messages
    static void receiveMessages(AzureServiceBusQueueSieveConfiguration azureServiceBusQueueSieveConfiguration)
    {
        try 
        {
            CountDownLatch countdownLatch = new CountDownLatch(1);

            // Create an instance of the processor through the ServiceBusClientBuilder
            ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(azureServiceBusQueueSieveConfiguration.connectionString)
                .processor()
                .queueName(azureServiceBusQueueSieveConfiguration.queueName)
                .processMessage(AzureServiceBusQueueSieve::processMessage)
                .processError(context -> processError(context, countdownLatch))
                .buildProcessorClient();

            System.out.println("Starting the processor");
            processorClient.start();

            TimeUnit.SECONDS.sleep(azureServiceBusQueueSieveConfiguration.timeout);
            System.out.println("Stopping and closing the processor");
            processorClient.close();
            SUBSCRIBER_HOOKED = false;
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
            	
    } 

    private static void processMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
            message.getSequenceNumber(), message.getBody());
        
        if(checkSubscriberFilter(message))
        {
            linkedListServiceBusReceivedMessageQueue.enqueue(message);
        }
        
    }  

    private static boolean checkSubscriberFilter(ServiceBusReceivedMessage message) {
        return false;
    }

    private static void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath());
    
        if (!(context.getException() instanceof ServiceBusException)) {
            System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }
    
        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();
    
        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
            || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
            || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
                reason, exception.getMessage());
    
            countdownLatch.countDown();
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            System.out.printf("Message lock lost for message: %s%n", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                // Choosing an arbitrary amount of time to wait until trying again.
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Unable to sleep for period of time");
            }
        } else {
            System.out.printf("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
                reason, context.getException());
        }
    } 

}