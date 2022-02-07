package com.gunmetal.azure.servicebus;

import com.gunmetal.azure.servicebus.controller.AzureServiceBusQueueSieve;
import com.gunmetal.azure.servicebus.model.AzureServiceBusQueueSieveConfiguration;

public class SieveApplication 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String connectionString = "Endpoint=sb://azure-servicebus-sieve.servicebus.windows.net/;SharedAccessKeyName=access;SharedAccessKey=gDDzuASMdFovy42BeXoFZF/X6pQW2IGhYHu5j1Lse9s=;EntityPath=sieve_queue";
        String queueName = "sieve_queue";
        String filter = "{message.body=xyz}";
        int timeoutInMiliseconds = 60 * 1000;
        AzureServiceBusQueueSieveConfiguration azureServiceBusQueueSieveConfiguration = new AzureServiceBusQueueSieveConfiguration(connectionString, queueName, timeoutInMiliseconds, filter);
        AzureServiceBusQueueSieve azureServiceBusQueueSieve = new AzureServiceBusQueueSieve(azureServiceBusQueueSieveConfiguration);
        
        azureServiceBusQueueSieve.receiveFilteredMessages(azureServiceBusQueueSieveConfiguration)
            .subscribe(message -> {
                System.out.println("message received by client-" + message.getBody().toString());
            });   
    }
}
