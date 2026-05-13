package androidev.thegreenroom;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// api interface for ticketmaster - will be used to search for events in the west midlands
public interface TicketmasterApi {
    @GET("events.json")
    Call<TicketmasterResponse> searchEvents(
            @Query("apikey") String apiKey,
            @Query("city") String city,
            @Query("countryCode") String countryCode,
            @Query("classificationName") String classification,
            @Query("size") int size
    );
}