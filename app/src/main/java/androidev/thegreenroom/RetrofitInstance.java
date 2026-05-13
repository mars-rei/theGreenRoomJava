package androidev.thegreenroom;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// creates and manages retrofit instance - singleton pattern
public class RetrofitInstance {
    private static final String BASE_URL = "https://app.ticketmaster.com/discovery/v2/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static TicketmasterApi getApiInterface() {
        return getRetrofitInstance().create(TicketmasterApi.class);
    }
}