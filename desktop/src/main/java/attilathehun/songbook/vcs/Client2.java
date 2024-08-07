package attilathehun.songbook.vcs;

import attilathehun.songbook.environment.SettingsManager;
import com.google.gson.Gson;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is used for communication with the remote server over HTTP.
 */
public class Client2 {
    private static final Logger logger = LogManager.getLogger(Client.class);
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_ZIP = "application/zip; charset=utf-8";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String JSON_EXTENSION = ".json";

    public static final int ACCESS_DENIED = 401;

    /**
     * Sends an HTTP request with the specified parameters and returns a pre-parsed response.
     *
     * @param url the url to send the request to
     * @param method the HTTP method
     * @param token the authentication token
     * @param data the request data; {@link File} will be transmitted as byte stream, {@link String} will be sent as-is and anything else will be attempted to
     *             encode as JSON
     * @param metadata will be assigned as an attachment name in the content-type header
     * @return the result containing the response code, response body, error stream and path to the local copy of the response body
     * @throws Exception
     */
    public Result http(final String url, final String method, final String token, final Object data, final String metadata) throws Exception  {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("invalid url");
        }

        if (method == null || !(method.equals(HTTP_GET) || method.equals(HTTP_POST) || method.equals(HTTP_PUT) || method.equals(HTTP_DELETE))) {
            throw new IllegalArgumentException("invalid method");
        }

        final URL endpoint = new URI(url).toURL();
        final HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        if (token != null && token.length() != 0) {
            conn.setRequestProperty(AUTHORIZATION_HEADER, String.format(AUTHORIZATION_HEADER_VALUE, token));
        }

        if (data != null) {
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            if (data instanceof File) {
                if (((File) data).getName().endsWith(ZIP_EXTENSION)) {
                    conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_ZIP);
                } else if (((File) data).getName().endsWith(JSON_EXTENSION)) {
                    conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
                } else {
                    conn.setRequestProperty(CONTENT_TYPE_HEADER, "application/custom; charset=utf-8");
                }
                if (metadata != null && metadata.length() != 0) {
                    conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", metadata));
                } else {
                    conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", ((File) data).getName()));
                }

                final OutputStream outputStream = conn.getOutputStream();
                final InputStream fileInputStream = new FileInputStream((File) data);
                outputStream.write(fileInputStream.readAllBytes());
                outputStream.flush();
                outputStream.close();

            } else {

                conn.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON);
                if (metadata != null && metadata.length() != 0) {
                    conn.setRequestProperty(CONTENT_DISPOSITION_HEADER, String.format("attachment; filename=%s", metadata));
                }

                final OutputStream outputStream = conn.getOutputStream();
                if (data instanceof String) {
                    outputStream.write(((String) data).getBytes(StandardCharsets.UTF_8));
                } else {
                    final String jsonRequestBody = new Gson().toJson(data);
                    outputStream.write(jsonRequestBody.getBytes(StandardCharsets.UTF_8));
                }
                outputStream.flush();
                outputStream.close();

            }
        }

        byte[] response = null;
        String filename = null;

        if (conn.getResponseCode() < 400) { // prevent {@link HttpURLConnection#getInputStream()} from throwing exception
            if (conn.getHeaderFields().get(CONTENT_DISPOSITION_HEADER) != null) {
                filename = conn.getHeaderFields().get(CONTENT_DISPOSITION_HEADER).get(0).replace("attachment; filename=", "")
                        .replace("attachment;filename=", "");
                final InputStream in = conn.getInputStream();
                filename = Paths.get(SettingsManager.getInstance().getValue("VCS_CACHE_PATH"), filename).toString();
                Files.copy(in, Path.of(filename), StandardCopyOption.REPLACE_EXISTING);
                in.close();

            } else {
                response = (conn.getInputStream() == null) ? null : conn.getInputStream().readAllBytes();
            }
        }

        byte[] error = (conn.getErrorStream() == null) ? null : conn.getErrorStream().readAllBytes();
        conn.disconnect();
        return new Result(conn.getResponseCode(), response, error, filename);
    }

    /**
     * This class represents the result of the HTTP request. Depending on the nature of the request and the corresponding response, the fields may or may not
     * be defined (we call this being null in Java). If the response had a body, this body will be stored in {@param responseCode}. If something went wring with
     * the connection, the error message will be stored in {@param error}. If the response contained an attachment, this attachment is saved locally rather than
     * stored in {@param response} and the path to the local copy will be stored in {@param metadata}. Any of these fields can be null.
     *
     * @param responseCode the HTTP response code
     * @param response the HTTP response body or null
     * @param error the connection error information on null
     * @param metadata attachment file path or null
     */
    public record Result(int responseCode, byte[] response, byte[] error, String metadata) {
       /* public Result(int responseCode, byte[] response, byte[] error, String metadata) {
            this.responseCode = responseCode;
            this.response = response;
            this.error = error;
            this.metadata = metadata;
            System.out.println(new Gson().toJson(this));
        }*/
    }

    /**
     * Sends an HTTP request with custom request body and headers.
     *
     * @param url the address to send the request to
     * @param method the HTTP method
     * @param token the authentication token
     * @param body the request body
     * @param headers the request headers (will be parsed through {@link #parseHeaders(String)})
     * @return the connection object (closed)
     * @throws Exception you bet
     */
    public HttpURLConnection httpWithHeaders(final String url, final String method, final String token, final String body, final String headers) throws Exception  {
        if (url == null || url.length() == 0) {
            throw new IllegalArgumentException("invalid url");
        }

        if (method == null || !(method.equals(HTTP_GET) || method.equals(HTTP_POST) || method.equals(HTTP_PUT) || method.equals(HTTP_DELETE))) {
            throw new IllegalArgumentException("invalid method");
        }

        final URL endpoint = new URI(url).toURL();
        final HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        if (token != null && token.length() != 0) {
            conn.setRequestProperty(AUTHORIZATION_HEADER, String.format(AUTHORIZATION_HEADER_VALUE, token));
        }
        for (final Pair<String, String> header : parseHeaders(headers)) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        if (body != null && body.length() != 0) {
            conn.setDoOutput(true);
            final OutputStream outputStream = conn.getOutputStream();
            outputStream.write((body).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
        }
        conn.connect();
        conn.disconnect();

       return conn;
    }

    /**
     * <p></p>Parses a string into a {@link Collection} of {@link Pair} elements of header names and header value. The collection may be empty.</p>
     * <p></p>The headers are expected to be separated by line breaks and header names from header values are expected to be separated by semicolons (;).</p>
     *
     * @param headers the string to parse
     * @return collection of the headers where header names are the keys of the pair element and header values are the values of the pair element
     */
    private Collection<Pair<String, String>> parseHeaders(final String headers) {
        final Collection<Pair<String, String>> parsedHeaders = new ArrayList<>();
        if (headers == null || headers.length() == 0) {
            return parsedHeaders;
        }
        for (final String line : headers.split("\n")) {
            try {
                final String headerName = line.substring(0, line.indexOf(";"));
                final String headerValue = line.substring(line.indexOf(";") + 1);
                parsedHeaders.add(new Pair<>(headerName, headerValue));
            } catch (final Exception e) {
                logger.error("could not parse header: " + line);
                logger.error(e.getMessage(), e);
            }
        }

        return parsedHeaders;
    }

}
