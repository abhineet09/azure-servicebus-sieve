package com.gunmetal.azure.servicebus.service;

import java.util.Map;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AzureServiceBusQueueSieveFilterService {
    
    public static boolean checkMessageonFilter(ServiceBusReceivedMessage message, String filter) {

        filter = filter.split("[{]")[1];
        // System.out.println(filter);
        filter = filter.trim();
        filter = filter.split("[}]")[0];
        // System.out.println(filter);
        filter = filter.trim();
        filter = filter.split("message\\.")[1];
        // System.out.println(filter);
        filter = filter.trim();
        String filter_key = filter.split("=")[0];
        // System.out.println(filter_key);
        filter_key = filter_key.trim();
        String filter_value = filter.split("=")[1];
        // System.out.println(filter_value);
        filter_value = filter_value.trim();
        
        if(filter_key.equals("body"))
        {
            String messageBody = message.getBody().toString();
            messageBody = messageBody.trim();
            if(messageBody.equals(filter_value))
                return true;
        }
        else if(isNestedBodyFilter(filter_key))
        {
            try {
                Map<String, Object> message_object = (Map<String, Object>) new ObjectMapper().readValue(message.getBody().toString(), Map.class);
                String[] filter_arr = filter_key.split("[.]");
                for(int i=1; i<filter_arr.length; i++)
                {
                    if(i == filter_arr.length -1)
                    {
                        if(((String) message_object.get(filter_arr[i])).trim().equals(filter_value))
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
        else if(filter_key.equals("correlationId"))
        {
            if(message.getCorrelationId() != null && message.getCorrelationId().equals(filter_value))
                return true;
        }
        else if(filter_key.equals("contentType"))
        {
            if(message.getContentType() != null && message.getContentType().equals(filter_value.toLowerCase()))
                return true;
        }
        else if(filter_key.equals("messageId"))
        {
            if(message.getMessageId() != null && message.getMessageId().equals(filter_value))
                return true;
        }
        else if(filter_key.equals("subject"))
        {
            if(message.getSubject() != null && message.getSubject().equals(filter_value))
                return true;
        }
        else if(isCustomPropertiesFilter(filter_key))
        {
            filter_key = filter_key.split("[.]")[2];
            Map<String, Object> message_custom_properties = message.getApplicationProperties();
            if(message_custom_properties.containsKey(filter_key))
            {
                if(filter_value.equals(message_custom_properties.get(filter_key).toString().trim()))
                    return true;
            }
        }
        return false;
    }

    private static boolean isNestedBodyFilter(String filter_key) {
        if(filter_key.split("[.]").length > 0 && filter_key.split("[.]")[0].equals("body"))
            return true;
        return false;
    }

    private static boolean isCustomPropertiesFilter(String filter_key)
    {
        if(filter_key.split("[.]").length > 0 && filter_key.split("[.]")[0].equals("custom") && filter_key.split("[.]")[1].equals("properties"))
            return true;
        return false;
    }

}
