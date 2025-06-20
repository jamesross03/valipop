package uk.ac.standrews.cs.valipop.utils.addressLookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for Open Street Map API.
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class OpenStreetMapAPI {

    public static long lastAPIRequestTime = System.currentTimeMillis();
    public static long requestGapMillis = 1000;

    public static int requestsSinceLastPause = 0;
    public static int requestCap = 3600;

    public static void rateLimiter(Cache cache) throws InterruptedException, IOException {

        long wait = requestGapMillis - (System.currentTimeMillis() - lastAPIRequestTime);

        if (wait > 0) {
            Thread.sleep(wait);
        }

        requestsSinceLastPause++;

        if(requestsSinceLastPause >= requestCap / 12) {
            cache.writeToFile();

            Thread.sleep(1000 * 60 * 5);
            System.out.println("Prec. 5 min pause");
            requestsSinceLastPause = 0;
        }

        lastAPIRequestTime = System.currentTimeMillis();
    }

    public static Area getAreaFromAPI(double lat, double lon, Cache cache) throws IOException, InvalidCoordSet, InterruptedException, APIOverloadedException, URISyntaxException {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("format", "json");
        parameters.put("lat", String.valueOf(lat));
        parameters.put("lon", String.valueOf(lon));
        parameters.put("zoom", "16");

        URL url = new URI("https://nominatim.openstreetmap.org/reverse.php?" + getParamsString(parameters)).toURL();

        StringBuffer content = callAPI(url, cache);

        return Area.makeArea(content.toString(), cache);
    }

    public static Place getPlaceFromAPI(long placeId, Cache cache) throws IOException, InterruptedException, APIOverloadedException, URISyntaxException {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("format", "json");
        parameters.put("place_id", String.valueOf(placeId));

        URL url = new URI("https://nominatim.openstreetmap.org/details.php?" + getParamsString(parameters)).toURL();

        StringBuffer content = callAPI(url, cache);

        return Place.makePlace(content.toString());
    }

    private static StringBuffer callAPI(URL url, Cache cache) throws IOException, InterruptedException, APIOverloadedException {

        rateLimiter(cache);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(30000);
        con.setReadTimeout(30000);
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

        System.out.println(con.toString());

        try {
            con.getResponseCode();
        } catch (java.io.IOException e) {
            throw new APIOverloadedException();
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        con.disconnect();
        return content;
    }

    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
