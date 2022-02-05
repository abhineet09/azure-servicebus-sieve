package com.gunmetal.azure.servicebus;

import java.util.Map;

import com.gunmetal.azure.servicebus.controller.AzureServiceBusQueueSieve;
import com.gunmetal.azure.servicebus.model.AzureServiceBusQueueSieveConfiguration;

import org.reactivestreams.Publisher;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

public class SieveApplication 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String connectionString = "";
        String queueName = "";
        Long timeout = 10L;
        AzureServiceBusQueueSieveConfiguration azureServiceBusQueueSieveConfiguration = new AzureServiceBusQueueSieveConfiguration(connectionString, queueName, timeout);
        AzureServiceBusQueueSieve.subscribe(azureServiceBusQueueSieveConfiguration).subscribe(message -> {System.out.println(message);});   
    }
}
