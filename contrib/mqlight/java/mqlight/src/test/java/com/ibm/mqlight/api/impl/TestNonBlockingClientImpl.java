/*
 *   <copyright 
 *   notice="oco-source" 
 *   pids="5725-P60" 
 *   years="2015" 
 *   crc="1438874957" > 
 *   IBM Confidential 
 *    
 *   OCO Source Materials 
 *    
 *   5724-H72
 *    
 *   (C) Copyright IBM Corp. 2015
 *    
 *   The source code for the program is not published 
 *   or otherwise divested of its trade secrets, 
 *   irrespective of what has been deposited with the 
 *   U.S. Copyright Office. 
 *   </copyright> 
 */

package com.ibm.mqlight.api.impl;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import com.ibm.mqlight.api.ClientOptions;
import com.ibm.mqlight.api.ClientState;
import com.ibm.mqlight.api.DestinationAdapter;
import com.ibm.mqlight.api.Promise;
import com.ibm.mqlight.api.SendOptions;
import com.ibm.mqlight.api.callback.CallbackService;
import com.ibm.mqlight.api.endpoint.Endpoint;
import com.ibm.mqlight.api.endpoint.EndpointPromise;
import com.ibm.mqlight.api.endpoint.EndpointService;
import com.ibm.mqlight.api.timer.TimerService;

public class TestNonBlockingClientImpl {

    private class StubEndpointService implements EndpointService {
        @Override public void lookup(EndpointPromise promise) {}
        @Override public void onSuccess(Endpoint endpoint) {}
    }
    
    private class StubCallbackService implements CallbackService {
        @Override public void run(Runnable runnable, Object orderingCtx, Promise<Void> promise) {}
    }
    
    private class StubTimerService implements TimerService {
        @Override public void schedule(long delay, Promise<Void> promise) {}
        @Override public void cancel(Promise<Void> promise) {}
    }
    
//    private class StubDestinationListener<T> implements DestinationListener<T> {
//        @Override public void onMessage(NonBlockingClient client, T context, Delivery delivery) {}
//        @Override public void onMalformed(NonBlockingClient client, T context, MalformedDelivery delivery) {}
//        @Override public void onUnsubscribed(NonBlockingClient client, T context, String topicPattern, String share) {}
//    }

    @Test public void autoGeneratedClientId() {
        StubEndpointService endpointService = new StubEndpointService();
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();
        NonBlockingClientImpl client = new NonBlockingClientImpl(endpointService, callbackService, component, timerService, ClientOptions.builder().build(), null, null);
        assertTrue("Expected auto generated client ID to start with string 'AUTO_'", client.getId().startsWith("AUTO_"));
    }
    
    @Test public void endpointServiceReportsFatalFailure() {
        StubEndpointService endpointService = new StubEndpointService() {
            @Override public void lookup(EndpointPromise promise) {
                promise.setFailure(new Exception());
            }
        };
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();
        NonBlockingClientImpl client = new NonBlockingClientImpl(endpointService, callbackService, component, timerService, ClientOptions.builder().build(), null, null);
        assertEquals("Client should have transitioned into stopping state, ", ClientState.STOPPING, client.getState());
    }
    
    @Test
    public void nullValuesIntoConstructor() {
        StubEndpointService endpointService = new StubEndpointService();
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();

        // Specifying null options, listener and context object should not throw an exception
        new NonBlockingClientImpl(endpointService, callbackService, component, timerService, null, null, null);

        // Specifying a null endpoint service should throw an exception
        try {
            new NonBlockingClientImpl(null, callbackService, component, timerService, null, null, null);
            throw new AssertionFailedError("Null endpoint service should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        
        // Specifying a null callback service should throw an exception
        try {
            new NonBlockingClientImpl(endpointService, null, component, timerService, null, null, null);
            throw new AssertionFailedError("Null callback service should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        
        // Specifying a null timer service should throw an exception
        try {
            new NonBlockingClientImpl(endpointService, callbackService, component, null, null, null, null);
            throw new AssertionFailedError("Null timer service should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void nullValuesIntoSend() {
        StubEndpointService endpointService = new StubEndpointService();
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();
        NonBlockingClientImpl client = new NonBlockingClientImpl(endpointService, callbackService, component, timerService, null, null, null);
        
        // Null properties, send options, listener and context object should be okay...
        client.send("topic", "data", null, null, null, null);
        client.send("topic", ByteBuffer.allocate(1), null, null, null, null);
        
        // Null topic should throw an exception
        try {
            client.send(null, "data", null, null, null, null);
            throw new AssertionFailedError("Null topic (send String) should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        try {
            client.send(null, ByteBuffer.allocate(1), null, null, null, null);
            throw new AssertionFailedError("Null topic (send ByteBuffer) should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        
        // Null data should throw an exception
        // Null topic should throw an exception
        try {
            client.send("topic", (String)null, null, null, null, null);
            throw new AssertionFailedError("Null data (send String) should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        try {
            client.send("topic", (ByteBuffer)null, null, null, null, null);
            throw new AssertionFailedError("Null data (send ByteBuffer) should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void messageTtlValues() {
        SendOptions.builder().setTtl(1).build();
        
        try {
            SendOptions.builder().setTtl(0).build();
            throw new AssertionFailedError("Zero TTL should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        
        try {
            SendOptions.builder().setTtl(-1).build();
            throw new AssertionFailedError("-1 TTL should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void nullValuesIntoSubscribe() {
        StubEndpointService endpointService = new StubEndpointService();
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();
        NonBlockingClientImpl client = new NonBlockingClientImpl(endpointService, callbackService, component, timerService, null, null, null);
        
        // Null subscription options, completion listener, and context should be fine...
        client.subscribe("topicPattern", null, new DestinationAdapter<Object>(){}, null, null);
        
        // Null topic pattern should throw an exception
        try {
            client.subscribe(null, null, new DestinationAdapter<Object>(){}, null, null);
            throw new AssertionFailedError("Null topic pattern should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        
        // Null destination listener should throw an exception
        try {
            client.subscribe("topicPattern", null, null, null, null);
            throw new AssertionFailedError("Null destination listener should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void nullValuesIntoUnsubscribe() {
        StubEndpointService endpointService = new StubEndpointService();
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();
        NonBlockingClientImpl client = new NonBlockingClientImpl(endpointService, callbackService, component, timerService, null, null, null);
        
        // Null share, listener and context object should be fine...
        client.unsubscribe("topicPattern", null, 0, null, null);
        client.unsubscribe("topicPattern", null, null, null);
        
        // Null topic pattern should throw an exception
        try {
            client.unsubscribe(null, null, 0, null, null);
            throw new AssertionFailedError("Null topic pattern should have thrown an exception (1)");
        } catch(IllegalArgumentException e) {
            // Expected
        }
        try {
            client.unsubscribe(null, null, null, null);
            throw new AssertionFailedError("Null topic pattern should have thrown an exception (2)");
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void nonzeroTtlIntoUnsubscribe() {
        StubEndpointService endpointService = new StubEndpointService();
        StubCallbackService callbackService = new StubCallbackService();
        MockComponent component = new MockComponent();
        StubTimerService timerService = new StubTimerService();
        NonBlockingClientImpl client = new NonBlockingClientImpl(endpointService, callbackService, component, timerService, null, null, null);
        
        try {
            client.unsubscribe("topicPattern", null, 7, null, null);
            throw new AssertionFailedError("Non-zero ttl should have thrown an exception");
        } catch(IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test
    public void topicEncoding() {
        String[][] testData = new String[][] {
            {"", "amqp:///"},
            {"/", "amqp:////"},
            {"/kittens", "amqp:////kittens"},
            {"kittens", "amqp:///kittens"},
            {"kittens/puppies", "amqp:///kittens/puppies"},
            {"kittens/puppies/", "amqp:///kittens/puppies/"},
            {"/kittens/puppies", "amqp:////kittens/puppies"},
            {"/kittens/puppies/", "amqp:////kittens/puppies/"},
            {"&", "amqp:///%26"},
            {"/&", "amqp:////%26"},
            {"&/", "amqp:///%26/"},
            {"/kittens&", "amqp:////kittens%26"},
            {"/kit&tens", "amqp:////kit%26tens"},
            {"/&kittens", "amqp:////%26kittens"},
            {"&/kittens", "amqp:///%26/kittens"},
            {"&/&kit&tens&/&pup&pies&/&", "amqp:///%26/%26kit%26tens%26/%26pup%26pies%26/%26"},
        };
        for (int i = 0; i < testData.length; ++i) {
            assertEquals("test case #"+i, testData[i][1], NonBlockingClientImpl.encodeTopic(testData[i][0]));
        }
    }
}
