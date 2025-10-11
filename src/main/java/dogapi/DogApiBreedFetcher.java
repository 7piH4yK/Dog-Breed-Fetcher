package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://dog.ceo/api/";
    private static final String MESSAGE = "message";
    private static final String STATUS = "status";
    private static final String SUCCESS_STATUS = "success";

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedFetcher.BreedNotFoundException {
        Request request = new Request.Builder()
                .url(String.format("%sbreed/%s/list", API_URL, breed))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new BreedNotFoundException("Empty API response for: " + breed);
            }
            JSONObject responseBody = new JSONObject(response.body().string());
            String status = responseBody.getString(STATUS);
            if (!SUCCESS_STATUS.equals(status)) {
                throw new BreedNotFoundException(responseBody.optString(MESSAGE, "Breed not found"));
            }
            JSONArray breeds = responseBody.getJSONArray(MESSAGE);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < breeds.length(); i++) {
                result.add(breeds.getString(i));
            }
            return result;
        } catch (IOException | JSONException e) {
            throw new BreedNotFoundException("Failed to fetch sub-breeds for: " + breed);
        }
    }
}