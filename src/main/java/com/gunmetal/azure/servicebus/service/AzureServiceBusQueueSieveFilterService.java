package com.gunmetal.azure.servicebus.service;

import java.util.Map;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AzureServiceBusQueueSieveFilterService {
    
    public static boolean checkMessageonFilter(ServiceBusReceivedMessage message, String filter) {

        filter = filter.split("[{]")[1];
        // System.out.println(filter);
        filter = filter.split("[}]")[0];
        // System.out.println(filter);
        filter = filter.split("message.")[1];
        // System.out.println(filter);
        String filter_key = filter.split("=")[0];
        // System.out.println(filter_key);
        String filter_value = filter.split("=")[1];
        // System.out.println(filter_value);
        
        if(filter_key.equals("body"))
        {
            String messageBody = message.getBody().toString();
            if(messageBody.equals(filter_value))
                return true;
        }
        else if(isNestedBodyFilter(filter_key))
        {
            try {
                Map<String, Object> message_object = (Map<String, Object>) new ObjectMapper().readValue(message.toString(), Map.class);
                String[] filter_arr = filter_key.split("[.]");
                for(int i=1; i<filter_arr.length; i++)
                {
                    if(i == filter_arr.length -1)
                    {
                        if(message_object.get(filter_arr[i]).equals(filter_value))
                            return true;
                    }
                    else
                    {
                        message_object = (Map<String, Object>) message_object.get(filter_arr[i]);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private static boolean isNestedBodyFilter(String filter_key) {
        if(filter_key.split("[.]")[0].equals("body") && filter_key.split("[.]").length > 0)
            return true;
        return false;
    }

}
