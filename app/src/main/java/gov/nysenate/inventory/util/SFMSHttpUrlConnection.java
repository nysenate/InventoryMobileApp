package gov.nysenate.inventory.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SFMSHttpUrlConnection extends HttpURLConnection {
    private static final String USER_AGENT = "Mozilla/5.0";

    public SFMSHttpUrlConnection(URL url) {
        super(url);
        super.setRequestProperty("User-Agent", USER_AGENT);
    }

    @Override
    public void disconnect() {
        disconnect();
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public void connect() throws IOException {
        connect();
    }

    public String sendGet() throws IOException {

        HttpURLConnection httpURLConnection = (HttpURLConnection) super.url.openConnection();

        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        // optional default is GET
        httpURLConnection.setRequestMethod("GET");

        int responseCode = httpURLConnection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public void sendPost() throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) super.url.openConnection();

        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        // optional default is POST
        httpURLConnection.setRequestMethod("POST");

        int responseCode = httpURLConnection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    public int sendPost(String urlParameters) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) super.url.openConnection();

        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        // Send post request
        httpURLConnection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = httpURLConnection.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return responseCode;
    }

    public String sendPostResponse(String urlParameters) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) super.url.openConnection();

        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        // Send post request
        httpURLConnection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));

        String inputLine;

        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public BufferedReader sendPostStream(String urlParameters) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) super.url.openConnection();

        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        // Send post request
        httpURLConnection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));

        String inputLine;

        StringBuffer response = new StringBuffer();

        return in;
    }

}