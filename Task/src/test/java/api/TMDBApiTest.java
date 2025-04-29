package api;


import client.CustomHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.testng.Assert.*;

public class TMDBApiTest {

    static String apiKey = "YOUR_API_KEY";
    static String sessionId = "YOUR_SESSION_ID";
    static String accountId = "YOUR_ACCOUNT_ID";

    static String movieId;
    static String listId;

    ObjectMapper mapper = new ObjectMapper();

    @Test(priority = 1)
    public void searchMovie() throws Exception {
        String url = "https://api.themoviedb.org/3/search/movie?query=Inception&api_key=" + apiKey;
        HttpResponse<String> response = CustomHttpClient.sendRequest("GET", url, Map.of(), null);
        assertEquals(response.statusCode(), 200);

        JsonNode root = mapper.readTree(response.body());
        assertTrue(root.get("results").size() > 0);

        for (JsonNode movie : root.get("results")) {
            if (movie.get("title").asText().contains("Inception")) {
                movieId = movie.get("id").asText();
                break;
            }
        }
        assertNotNull(movieId);
    }

    @Test(priority = 2, dependsOnMethods = "searchMovie")
    public void getMovieDetails() throws Exception {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;
        HttpResponse<String> response = CustomHttpClient.sendRequest("GET", url, Map.of(), null);
        assertEquals(response.statusCode(), 200);

        JsonNode root = mapper.readTree(response.body());
        assertEquals(root.get("original_language").asText(), "en");
        assertTrue(root.get("title").asText().contains("Inception"));
    }

    @Test(priority = 3, dependsOnMethods = "getMovieDetails")
    public void createListAndAddMovie() throws Exception {
        // Create list
        String createListUrl = "https://api.themoviedb.org/3/list?api_key=" + apiKey + "&session_id=" + sessionId;
        String listPayload = """
                {
                  "name": "Test List",
                  "description": "List for automation",
                  "language": "en"
                }
                """;
        HttpResponse<String> createListResponse = CustomHttpClient.sendRequest("POST", createListUrl, Map.of("Content-Type", "application/json"), listPayload);
        JsonNode listNode = mapper.readTree(createListResponse.body());
        listId = listNode.get("list_id").asText();
        assertNotNull(listId);

        // Add movie to list
        String addMovieUrl = "https://api.themoviedb.org/3/list/" + listId + "/add_item?api_key=" + apiKey + "&session_id=" + sessionId;
        String moviePayload = "{\"media_id\":" + movieId + "}";
        HttpResponse<String> addMovieResponse = CustomHttpClient.sendRequest("POST", addMovieUrl, Map.of("Content-Type", "application/json"), moviePayload);
        assertEquals(addMovieResponse.statusCode(), 201);
    }

    @Test(priority = 4, dependsOnMethods = "createListAndAddMovie")
    public void verifyMovieListHasOneMovie() throws Exception {
        String url = "https://api.themoviedb.org/3/list/" + listId + "?api_key=" + apiKey;
        HttpResponse<String> response = CustomHttpClient.sendRequest("GET", url, Map.of(), null);
        JsonNode list = mapper.readTree(response.body());

        assertEquals(list.get("items").size(), 1);
        assertEquals(list.get("items").get(0).get("title").asText(), "Inception");
    }

    @Test(priority = 5, dependsOnMethods = "verifyMovieListHasOneMovie")
    public void cleanupList() throws Exception {
        String deleteListUrl = "https://api.themoviedb.org/3/list/" + listId + "?api_key=" + apiKey + "&session_id=" + sessionId;
        CustomHttpClient.sendRequest("DELETE", deleteListUrl, Map.of(), null);
    }
}

