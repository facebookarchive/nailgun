package com.martiansoftware.nailgun;

public interface NGHeartbeatListener {

    /**
     * Called by an internal nailgun thread when the server receives a heartbeat from the client.
     * This can normally be implemented as a no-op handler and is primarily useful for debugging.
     * {@link NGClientListener}s can be registered using {@link NGContext.registerHeartbeatListener}.
     */
    public void heartbeatReceived(long intervalMillis);
}
