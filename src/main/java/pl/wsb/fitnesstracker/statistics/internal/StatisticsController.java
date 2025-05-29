package pl.wsb.fitnesstracker.statistics.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.statistics.api.StatisticsService;

import java.util.List;

/**
 * REST controller for Statistics API.
 */
@RestController
@RequestMapping("/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final StatisticsMapper statisticsMapper;
    
    /**
     * Returns all statistics.
     *
     * @return A list of all statistics
     */
    @GetMapping
    List<StatisticsDto> findAllStatistics() {
        return statisticsService.findAllStatistics()
                .stream()
                .map(statisticsMapper::toDto)
                .toList();
    }
    
    /**
     * Returns statistics for a specific user.
     *
     * @param userId The ID of the user
     * @return The statistics for the user, or 404 if not found
     */
    @GetMapping("/user/{userId}")
    ResponseEntity<StatisticsDto> getStatisticsByUserId(@PathVariable Long userId) {
        return statisticsService.getStatisticsByUserId(userId)
                .map(statisticsMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Returns statistics with calories burned greater than the specified value.
     *
     * @param calories The minimum number of calories
     * @return A list of statistics with calories burned greater than the specified value
     */
    @GetMapping("/calories")
    List<StatisticsDto> findStatisticsWithCaloriesGreaterThan(@RequestParam int calories) {
        return statisticsService.findStatisticsWithCaloriesGreaterThan(calories)
                .stream()
                .map(statisticsMapper::toDto)
                .toList();
    }
    
    /**
     * Creates new statistics.
     *
     * @param statisticsDto The statistics data
     * @return The created statistics
     */
    @PostMapping
    ResponseEntity<StatisticsDto> createStatistics(@RequestBody CreateStatisticsDTO statisticsDto) {
        Statistics persisted = statisticsService.createStatistics(
                statisticsMapper.toEntity(statisticsDto),
                statisticsDto.getUserId()
        );
        StatisticsDto dto = statisticsMapper.toDto(persisted);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
    
    /**
     * Updates existing statistics.
     *
     * @param statisticsId The ID of the statistics to update
     * @param statisticsDto The updated statistics data
     * @return The updated statistics
     */
    @PutMapping("/{statisticsId}")
    StatisticsDto updateStatistics(
            @PathVariable Long statisticsId,
            @RequestBody CreateStatisticsDTO statisticsDto
    ) {
        Statistics updated = statisticsService.updateStatistics(
                statisticsMapper.toEntity(statisticsDto),
                statisticsId,
                statisticsDto.getUserId()
        );
        return statisticsMapper.toDto(updated);
    }
    
    /**
     * Deletes statistics.
     *
     * @param statisticsId The ID of the statistics to delete
     * @return 204 No Content if successful
     */
    @DeleteMapping("/{statisticsId}")
    ResponseEntity<Void> deleteStatistics(@PathVariable Long statisticsId) {
        statisticsService.deleteStatistics(statisticsId);
        return ResponseEntity.noContent().build();
    }
}