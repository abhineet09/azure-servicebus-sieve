package com.gunmetal.azure.servicebus.model;

public class AzureServiceBusQueueSieveConfiguration {
    
    public long timeoutInMiliseconds;
    public String connectionString;
    public String queueName;
    public String filter;

    public AzureServiceBusQueueSieveConfiguration(String connectionString2, String queueName2, long timeout2, String filter2) {
        this.connectionString = connectionString2;
        this.queueName = queueName2;
        this.timeoutInMiliseconds = timeout2;
        this.filter = filter2;
    }

}
