package net.es.oscars.nsibridge.client.cli.output;

/**
 * Interface for outputting reserve results
 *
 */
public interface ReserveOutputter {

    void outputConfirmed(String connectionId);

    void outputTimeout(String connectionId);

    void outputFailed(String connectionId);

}