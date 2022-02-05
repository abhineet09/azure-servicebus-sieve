package com.gunmetal.azure.servicebus.model;

public class AzureServiceBusQueueSieveConfiguration {
    
    public Long timeout;
    public String connectionString;
    public String queueName;
    public AzureServiceBusQueueSieveFilter filter;

    public AzureServiceBusQueueSieveConfiguration(String connectionString2, String queueName2, Long timeout2, AzureServiceBusQueueSieveFilter filter2) {
        this.connectionString = connectionString2;
        this.queueName = queueName2;
        this.timeout = timeout2;
        this.filter = filter2;
    }

}
