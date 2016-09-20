package com.example.techjini.threadbasics.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by techjini on 19/9/16.
 */
public class NetworkRequest {
    public static final String ERROR_HTTP_CLIENT_TIMEOUT = "408";
    public static final String ERROR_IO_EXCEPTION = "http_io_exception_error";
    public static final String ERROR_UNKNOWN = "http_unknown_error";

    //
    public String callServer() {
        String result = null;
        int resCode = -1;
        try {
            //Declaring a URL Connection
            URL url = new URL(StaticUtils.API_URL);
            URLConnection urlConn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpConn.getInputStream();
                result = StaticUtils.sConvertStreamToString(in);
                return result;
            } else if (resCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
                result = ERROR_HTTP_CLIENT_TIMEOUT;
                return result;
            } else {
                result = ERROR_UNKNOWN;
                return result;
            }

        } catch (MalformedURLException e) {
            result = ERROR_UNKNOWN;
            return result;

        } catch (IOException e) {
            result = ERROR_IO_EXCEPTION;
            return result;
        }
    }
}

