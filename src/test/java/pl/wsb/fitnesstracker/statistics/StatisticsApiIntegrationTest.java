package pl.wsb.fitnesstracker.statistics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.wsb.fitnesstracker.IntegrationTest;
import pl.wsb.fitnesstracker.IntegrationTestBase;
import pl.wsb.fitnesstracker.statistics.api.Statistics;
import pl.wsb.fitnesstracker.statistics.internal.StatisticsRepository;
import pl.wsb.fitnesstracker.user.api.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;

@IntegrationTest
@Transactional
@AutoConfigureMockMvc(addFilters = false)
class StatisticsApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StatisticsRepository statisticsRepository;


    private static User generateUser() {
        return new User(randomUUID().toString(), randomUUID().toString(), now(), randomUUID().toString());
    }

    private static Statistics generateStatistics(User user) {
        return new Statistics(user, 10, 100.0, 8000);
    }

    private Statistics persistStatistics(Statistics statistics) {
        return statisticsRepository.save(statistics);
    }

    @Test
    void shouldReturnAllStatistics_whenGettingAllStatistics() throws Exception {
        User user = existingUser(generateUser());
        Statistics statistics = persistStatistics(generateStatistics(user));

        mockMvc.perform(get("/v1/statistics").contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].user.id").value(user.getId()))
                .andExpect(jsonPath("$[0].user.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$[0].user.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$[0].user.email").value(user.getEmail()))
                .andExpect(jsonPath("$[0].totalTrainings").value(statistics.getTotalTrainings()))
                .andExpect(jsonPath("$[0].totalDistance").value(statistics.getTotalDistance()))
                .andExpect(jsonPath("$[0].totalCaloriesBurned").value(statistics.getTotalCaloriesBurned()));
    }

    @Test
    void shouldReturnStatisticsForUser_whenGettingStatisticsByUserId() throws Exception {
        User user = existingUser(generateUser());
        Statistics statistics = persistStatistics(generateStatistics(user));

        mockMvc.perform(get("/v1/statistics/user/{userId}", user.getId()).contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.totalTrainings").value(statistics.getTotalTrainings()))
                .andExpect(jsonPath("$.totalDistance").value(statistics.getTotalDistance()))
                .andExpect(jsonPath("$.totalCaloriesBurned").value(statistics.getTotalCaloriesBurned()));
    }

    @Test
    void shouldReturnStatisticsWithCaloriesGreaterThan_whenGettingStatisticsWithCaloriesGreaterThan() throws Exception {
        User user1 = existingUser(generateUser());
        User user2 = existingUser(generateUser());

        Statistics statistics1 = new Statistics(user1, 10, 100.0, 8000);
        Statistics statistics2 = new Statistics(user2, 5, 50.0, 4000);

        persistStatistics(statistics1);
        persistStatistics(statistics2);

        mockMvc.perform(get("/v1/statistics/calories").param("calories", "5000").contentType(MediaType.APPLICATION_JSON))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].user.id").value(user1.getId()))
                .andExpect(jsonPath("$[0].totalCaloriesBurned").value(8000))
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    @Test
    void shouldCreateStatistics_whenCreatingStatistics() throws Exception {
        User user = existingUser(generateUser());

        String requestBody = """
                {
                    "userId": %d,
                    "totalTrainings": 10,
                    "totalDistance": 100.0,
                    "totalCaloriesBurned": 8000
                }
                """.formatted(user.getId());

        mockMvc.perform(post("/v1/statistics").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andDo(log())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.totalTrainings").value(10))
                .andExpect(jsonPath("$.totalDistance").value(100.0))
                .andExpect(jsonPath("$.totalCaloriesBurned").value(8000));
    }

    @Test
    void shouldUpdateStatistics_whenUpdatingStatistics() throws Exception {
        User user = existingUser(generateUser());
        Statistics statistics = persistStatistics(generateStatistics(user));

        String requestBody = """
                {
                    "userId": %d,
                    "totalTrainings": 15,
                    "totalDistance": 150.0,
                    "totalCaloriesBurned": 12000
                }
                """.formatted(user.getId());

        mockMvc.perform(put("/v1/statistics/{statisticsId}", statistics.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(log())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.user.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.totalTrainings").value(15))
                .andExpect(jsonPath("$.totalDistance").value(150.0))
                .andExpect(jsonPath("$.totalCaloriesBurned").value(12000));
    }

    @Test
    void shouldDeleteStatistics_whenDeletingStatistics() throws Exception {
        User user = existingUser(generateUser());
        Statistics statistics = persistStatistics(generateStatistics(user));

        mockMvc.perform(delete("/v1/statistics/{statisticsId}", statistics.getId()))
                .andDo(log())
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/statistics/{statisticsId}", statistics.getId()))
                .andExpect(status().isMethodNotAllowed());
    }
}