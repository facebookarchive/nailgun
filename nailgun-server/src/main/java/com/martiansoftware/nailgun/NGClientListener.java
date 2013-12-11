package com.martiansoftware.nailgun;

public interface NGClientListener {

    /**
     * Called by an internal nailgun thread when the server detects that the nailgun client has disconnected.
     * {@link NGClientListener}s can be registered using {@link NGContext.registerClientListener}. If
     * clientDisconnected throws an InterruptedException nailgun interrupts the main session thread.
     */
    public void clientDisconnected() throws InterruptedException;
}
