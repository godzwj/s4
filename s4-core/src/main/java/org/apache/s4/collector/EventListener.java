/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.s4.collector;

import static org.apache.s4.util.MetricsName.S4_CORE_METRICS;
import static org.apache.s4.util.MetricsName.S4_EVENT_METRICS;
import static org.apache.s4.util.MetricsName.generic_listener_msg_in_ct;
import org.apache.s4.listener.EventHandler;
import org.apache.s4.logger.Monitor;
import org.apache.s4.processor.AsynchronousEventProcessor;
import org.apache.s4.processor.PEContainer;

import org.apache.log4j.Logger;

public class EventListener implements EventHandler {
    private static Logger logger = Logger.getLogger(EventListener.class);
    private int eventCount = 0;
    private AsynchronousEventProcessor eventProcessor;
    private org.apache.s4.listener.EventListener rawListener;
    private Monitor monitor;

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    public void setPeContainer(PEContainer peContainer) {
        this.eventProcessor = peContainer;
    }

    public void setEventProcessor(AsynchronousEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public void setRawListener(org.apache.s4.listener.EventListener rawListener) {
        this.rawListener = rawListener;
    }

    public org.apache.s4.listener.EventListener getRawListener() {
        return this.rawListener;
    }

    public int getEventCount() {
        return eventCount;
    }

    public EventListener() {

    }

    public void init() {
        rawListener.addHandler(this);
    }

    public void processEvent(EventWrapper eventWrapper) {
        try {
            synchronized (this) {
                eventCount++;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("STEP 3 (EventListener): peContainer.addEvent - "
                        + eventWrapper.getEvent().toString());
            }
            eventProcessor.queueWork(eventWrapper);

            if (monitor != null) {
                monitor.increment(generic_listener_msg_in_ct.toString(),
                                  1,
                                  S4_EVENT_METRICS.toString(),
                                  "et",
                                  eventWrapper.getStreamName());
                monitor.increment(generic_listener_msg_in_ct.toString(),
                                  1,
                                  S4_CORE_METRICS.toString());
            }
        } catch (Exception e) {
            logger.error("Exception in processEvent on thread "
                    + Thread.currentThread().getId(), e);
        }
    }
}
