package pl.wsb.fitnesstracker.statistics.internal;

// Spring Framework imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Java standard imports
import java.util.List;

// Lombok imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Application imports
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.statistics.api.StatisticsService;

/**
 * REST Controller for managing statistics-related operations.
 * 
 * This controller provides endpoints for retrieving, creating, updating, and deleting
 * statistics records in the fitness tracking system. It follows RESTful principles
 * and uses JSON for data exchange.
 * 
 * @author FitnessTracker Team
 * @version 2.0
 */
@RestController
@RequestMapping("/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    // ========== Dependencies ==========

    /** Service for statistics operations */
    private final StatisticsService statisticsService;

    /** Mapper for converting between entity and DTO objects */
    private final StatisticsMapper statisticsMapper;

    // ========== Query Endpoints (GET) ==========

    /**
     * Retrieves all statistics records from the system.
     * 
     * @return A list of all statistics records
     */
    @GetMapping
    public List<StatisticsDto> retrieveAllStatistics() {
        log.info("Received request to retrieve all statistics");

        List<StatisticsDto> statistics = statisticsService.findAllStatistics()
                .stream()
                .map(statisticsMapper::toDto)
                .toList();

        log.info("Returning {} statistics records", statistics.size());
        return statistics;
    }

    /**
     * Retrieves statistics for a specific user.
     * 
     * @param userId The ID of the user whose statistics to retrieve
     * @return The statistics for the user, or 404 if not found
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<StatisticsDto> retrieveStatisticsByUser(@PathVariable Long userId) {
        log.info("Received request to retrieve statistics for user ID: {}", userId);

        return statisticsService.getStatisticsByUserId(userId)
                .map(statistics -> {
                    log.info("Found statistics for user ID: {}", userId);
                    return statisticsMapper.toDto(statistics);
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.info("No statistics found for user ID: {}", userId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Retrieves statistics with calories burned greater than the specified threshold.
     * 
     * @param calories The minimum number of calories
     * @return A list of statistics with calories burned greater than the specified value
     */
    @GetMapping("/calories")
    public List<StatisticsDto> retrieveStatisticsAboveCalorieThreshold(@RequestParam int calories) {
        log.info("Received request to retrieve statistics with calories greater than: {}", calories);

        List<StatisticsDto> statistics = statisticsService.findStatisticsWithCaloriesGreaterThan(calories)
                .stream()
                .map(statisticsMapper::toDto)
                .toList();

        log.info("Returning {} statistics records with calories greater than {}", statistics.size(), calories);
        return statistics;
    }

    // ========== Command Endpoints (POST, PUT, DELETE) ==========

    /**
     * Creates a new statistics record in the system.
     * 
     * @param statisticsDto The statistics data transfer object containing all necessary information
     * @return ResponseEntity containing the created statistics with HTTP status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<StatisticsDto> registerNewStatistics(@RequestBody CreateStatisticsDTO statisticsDto) {
        log.info("Received request to create new statistics for user ID: {}", statisticsDto.getUserId());

        // Convert DTO to entity and persist
        Statistics statisticsEntity = statisticsMapper.toEntity(statisticsDto);
        Statistics persistedStatistics = statisticsService.createStatistics(
                statisticsEntity,
                statisticsDto.getUserId()
        );

        // Convert persisted entity back to DTO
        StatisticsDto responseDto = statisticsMapper.toDto(persistedStatistics);
        log.info("Successfully created statistics with ID: {}", responseDto.getId());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * Updates an existing statistics record in the system.
     * 
     * @param statisticsId The ID of the statistics to update
     * @param statisticsDto The statistics data transfer object containing updated information
     * @return The updated statistics data
     */
    @PutMapping("/{statisticsId}")
    public StatisticsDto modifyExistingStatistics(
            @PathVariable Long statisticsId,
            @RequestBody CreateStatisticsDTO statisticsDto
    ) {
        log.info("Received request to update statistics ID: {} for user ID: {}", 
                statisticsId, statisticsDto.getUserId());

        // Convert DTO to entity and update
        Statistics statisticsEntity = statisticsMapper.toEntity(statisticsDto);
        Statistics updatedStatistics = statisticsService.updateStatistics(
                statisticsEntity,
                statisticsId,
                statisticsDto.getUserId()
        );

        // Convert updated entity back to DTO
        StatisticsDto responseDto = statisticsMapper.toDto(updatedStatistics);
        log.info("Successfully updated statistics with ID: {}", responseDto.getId());

        return responseDto;
    }

    /**
     * Deletes a statistics record from the system.
     * 
     * @param statisticsId The ID of the statistics to delete
     * @return ResponseEntity with HTTP status 204 (No Content) if successful
     */
    @DeleteMapping("/{statisticsId}")
    public ResponseEntity<Void> removeStatistics(@PathVariable Long statisticsId) {
        log.info("Received request to delete statistics with ID: {}", statisticsId);

        statisticsService.deleteStatistics(statisticsId);
        log.info("Successfully deleted statistics with ID: {}", statisticsId);

        return ResponseEntity.noContent().build();
    }
}
