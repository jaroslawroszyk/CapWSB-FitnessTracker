package pl.wsb.fitnesstracker.statistics.api;

import pl.wsb.fitnesstracker.exception.api.NotFoundException;

/**
 * Exception thrown when statistics with a given ID cannot be found.
 */
public class StatisticsNotFoundException extends NotFoundException {

    /**
     * Creates a new StatisticsNotFoundException with a message indicating that statistics with the given ID were not found.
     *
     * @param statisticsId The ID of the statistics that could not be found
     */
    public StatisticsNotFoundException(Long statisticsId) {
        super("Statistics with ID " + statisticsId + " not found");
    }
}