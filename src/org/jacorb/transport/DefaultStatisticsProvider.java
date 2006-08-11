package org.jacorb.transport;

import org.jacorb.orb.giop.StatisticsProvider;


public class DefaultStatisticsProvider implements StatisticsProvider {
    
    public final long created_ = System.currentTimeMillis();
    public long messages_sent_ = 0;
    public long messages_received_ = 0;
    public long bytes_sent_ = 0;
    public long bytes_received_ = 0;

    public void messageChunkSent(int size) {
        bytes_sent_ += size;
    }

    public void flushed() {
        messages_sent_++;
    }

    public void messageReceived(int size) {
        messages_received_++;
        bytes_received_ += size;
    }

}
