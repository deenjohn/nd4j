package org.nd4j.parameterserver.distributed.transport;

import lombok.NonNull;
import org.nd4j.parameterserver.distributed.conf.Configuration;
import org.nd4j.parameterserver.distributed.enums.NodeRole;
import org.nd4j.parameterserver.distributed.messages.VoidMessage;

/**
 * Transport interface describes Client -> Shard, Shard -> Shard, Shard -> Client communication
 *
 * @author raver119@gmail.com
 */
public interface Transport {


    void init(Configuration configuration, NodeRole role, String localIp);


    /**
     * This method accepts message for delivery, routing is applied according on message type
     *
     * @param message
     */
    void sendMessage(VoidMessage message);

    /**
     * This method accepts message from network
     *
     * @param message
     */
    void receiveMessage(VoidMessage message);

    /**
     * This method takes 1 message from "incoming messages" queue, blocking if queue is empty
     *
     * @return
     */
    VoidMessage takeMessage();
}