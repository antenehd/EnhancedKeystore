package fi.aalto.mss.enhancedkeystore;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Worker callback functions.
 */
public interface WorkerCallback {
    void updateClient(ITEEClient client);

    void updateContext(ITEEClient.IContext ctx);

    void updateSession(ITEEClient.ISession ses);
}
