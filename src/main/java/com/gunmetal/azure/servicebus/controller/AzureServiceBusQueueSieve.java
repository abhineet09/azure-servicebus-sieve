package com.gunmetal.azure.servicebus.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.gunmetal.azure.servicebus.model.AzureServiceBusQueueSieveConfiguration;
import com.gunmetal.azure.servicebus.service.AzureServiceBusQueueSieveFilterService;

import reactor.core.publisher.Flux;

public class AzureServiceBusQueueSieve {

    private static AzureServiceBusQueueSieveConfiguration azureServiceBusQueueSieveConfiguration = null;
    private static Boolean SUBSCRIBER_HOOKED = false;
    private static List<ServiceBusReceivedMessage> serviceBusReceivedMessageList = new ArrayList<ServiceBusReceivedMessage>();
    private static int front = 0;
    private static long initTime;

    public AzureServiceBusQueueSieve(AzureServiceBusQueueSieveConfiguration receivedAzureServiceBusQueueSieveConfiguration)
    {
        SUBSCRIBER_HOOKED = true;
        azureServiceBusQueueSieveConfiguration = receivedAzureServiceBusQueueSieveConfiguration;
    }

    public static Flux<ServiceBusReceivedMessage> receiveFilteredMessages(AzureServiceBusQueueSieveConfiguration receivedAzureServiceBusQueueSieveConfiguration) {
        initTime = System.currentTimeMillis();
        return Flux.create(sink -> {
            while(SUBSCRIBER_HOOKED)
            {
                receiveMessages();
                if(front < serviceBusReceivedMessageList.size())
                {
                    ServiceBusReceivedMessage serviceBusReceivedMessage = serviceBusReceivedMessageList.get(front);
                    if(serviceBusReceivedMessage != null)
                    {
                        front++;
                        sink.next(serviceBusReceivedMessage);
                    }
                }
            }
            System.out.println("Subscriber is being closed.");
            sink.complete();
        });
    }


    private static void receiveMessages()
    {
        CountDownLatch countdownLatch = new CountDownLatch(1);
        ServiceBusReceiverClient receiverClient = new ServiceBusClientBuilder()
                    .connectionString(azureServiceBusQueueSieveConfiguration.connectionString)
                    .receiver()
                    .disableAutoComplete()
                    .queueName(azureServiceBusQueueSieveConfiguration.queueName)
                    .buildClient();
        IterableStream<ServiceBusReceivedMessage> serviceBusReceivedMessageInflux = (IterableStream<ServiceBusReceivedMessage>) receiverClient.receiveMessages(9999);
        for(ServiceBusReceivedMessage serviceBusReceivedMessage : serviceBusReceivedMessageInflux)
        {
            if(processMessage(serviceBusReceivedMessage))
            {
                receiverClient.complete(serviceBusReceivedMessage);
            }
        }
        try {
            countdownLatch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiverClient.close();
        if(System.currentTimeMillis() - initTime > azureServiceBusQueueSieveConfiguration.timeoutInMiliseconds ){
            SUBSCRIBER_HOOKED = false;
        }
    }

    private static Boolean processMessage(ServiceBusReceivedMessage message) {
        if(checkSubscriberFilter(message, azureServiceBusQueueSieveConfiguration.filter))
        {
            serviceBusReceivedMessageList.add(message);
            return true;
        }
        else
        {
            return false;
        }
        
    }  

    private static boolean checkSubscriberFilter(ServiceBusReceivedMessage message, String filter) {
        return AzureServiceBusQueueSieveFilterService.checkMessageonFilter(message, filter);
    }

}
