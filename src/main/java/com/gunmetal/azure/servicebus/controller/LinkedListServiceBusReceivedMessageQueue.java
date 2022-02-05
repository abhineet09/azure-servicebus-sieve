package com.gunmetal.azure.servicebus.controller;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

public class LinkedListServiceBusReceivedMessageQueue {

    Node front, rear;
    private int queueSize;

    public LinkedListServiceBusReceivedMessageQueue()
    {
        front = null;
        rear = null;
        queueSize = 0;
    }

    private class Node{
        ServiceBusReceivedMessage data;
        Node next;
    }

    public Boolean isQueueEmpty()
    {
        if(queueSize == 0)
        {
            return true;
        }
        return false;
    }

    public void enqueue(ServiceBusReceivedMessage message)
    {
        Node oldRear = rear;  
        rear = new Node();  
        rear.data = message;  
        rear.next = null;  
        if (isQueueEmpty())  
        {  
            front = rear;  
        }  
        else  {  
            oldRear.next = rear;  
        }  
        queueSize++;  
    }

    public ServiceBusReceivedMessage deque()
    {
        ServiceBusReceivedMessage outMessage = front.data;  
        front = front.next;  
        if (isQueueEmpty())  
        {  
            rear = null;  
        }  
        queueSize--;   
        return outMessage;  
    }

}
