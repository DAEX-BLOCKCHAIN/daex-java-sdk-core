package io.daex.sdk.core.client;

import com.google.gson.JsonObject;
import io.daex.sdk.core.enums.ApiType;
import io.daex.sdk.core.http.*;
import io.daex.sdk.core.service.exception.*;
import io.daex.sdk.core.service.security.RSAOptions;
import io.daex.sdk.core.service.security.RSASigner;
import io.daex.sdk.core.util.RSAUtils;
import io.daex.sdk.core.util.RequestUtils;
import io.daex.sdk.core.util.ResponseUtils;
import jersey.repackaged.jsr166e.CompletableFuture;
import okhttp3.*;
import okhttp3.Headers;
import okhttp3.Request.Builder;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAEX base SDK client
 */
public class DaexClient {

    private static final Logger LOG = Logger.getLogger(DaexClient.class.getName());

    private static final String MESSAGE_ERROR_3 = "message";
    private static final String MESSAGE_ERROR_2 = "error_message";
    protected static final String MESSAGE_ERROR = "error";
    private static final String BEARER = "Bearer ";

    private String sdkName = "";
    private String endPoint;
    private OkHttpClient client;


    private boolean rsaEnabled;
    /**
     * The default headers.
     */
    protected Headers defaultHeaders = null;


    /**
     * Instantiates a new DAEX SDK client.
     *
     * @param endPoint the end point to call
     */
    public DaexClient(final String endPoint) {
        this.endPoint = endPoint;

        rsaEnabled = DaexClientConfig.getInstance().isRSAEnabled();

        client = configureHttpClient();
    }


    /**
     * Configure the {@link OkHttpClient}. This method will be called by the constructor and can be used to customize the
     * client that the service will use to perform the http calls.
     *
     * @return the {@link OkHttpClient}
     */
    protected OkHttpClient configureHttpClient() {
        return HttpClientSingleton.getInstance().createHttpClient();
    }

    /**
     * Execute the HTTP request. Okhttp3 compliant.
     *
     * @param request the HTTP request
     * @return the HTTP response
     */
    private Call createCall(final Request request) {
        return createCall(request, null);
    }

    private Call createCall(final Request request, final String jsonBody) {
        final Builder builder = request.newBuilder();

        setDefaultHeaders(builder);

        if(rsaEnabled) {
            setRSAHeaders(request, builder, jsonBody);
        }
        final Request newRequest = builder.build();
        return client.newCall(newRequest);
    }

    /**
     * Sets the default headers including User-Agent.
     *
     * @param builder the new default headers
     */
    protected void setDefaultHeaders(final Builder builder) {
        String userAgent = RequestUtils.getUserAgent(sdkName);

        if (defaultHeaders != null) {
            for (String key : defaultHeaders.names()) {
                builder.header(key, defaultHeaders.get(key));
            }
            if (defaultHeaders.get(HttpHeaders.USER_AGENT) != null) {
                userAgent += " " + defaultHeaders.get(HttpHeaders.USER_AGENT);
            }
        }
        builder.header(HttpHeaders.USER_AGENT, userAgent);
    }

    protected void setRSAHeaders(final Request request, final Builder builder ,final String jsonBody) {

        RSAOptions.Builder rsaOptionsBuilder = new RSAOptions.Builder();

        String method = request.method();
        URL url = request.url().url();
        String path = url.getPath();
        String query = url.getQuery();
        rsaOptionsBuilder.method(method);
        rsaOptionsBuilder.path(path);
        ApiType apiType = ApiType.apiTypeMap.get(path);
        rsaOptionsBuilder.rsaId(apiType.getApiId());
        rsaOptionsBuilder.privateKey(apiType.getApiPrivateKey());
        rsaOptionsBuilder.nonce(UUID.randomUUID().toString());
        rsaOptionsBuilder.timeStamp(String.valueOf(Instant.now().getEpochSecond()));
        if(query!=null){
            query = RSAUtils.sortQuery(url);
            rsaOptionsBuilder.query(query);
        }

        RequestBody requestBody = request.body();
        if("POST".equals(method) && requestBody!=null){
            if(requestBody instanceof FormBody){
                SortedMap<String,String> paraMap = new TreeMap<>();
                FormBody formBody = (FormBody)requestBody;

                for(int i=0;i<formBody.size();i++){
                    String name = formBody.encodedName(i);
                    String value = formBody.encodedValue(i);
                    paraMap.put(name,value);
                }
                String formParams = RSAUtils.flat(paraMap);
                rsaOptionsBuilder.formParams(formParams);
            } else {
                rsaOptionsBuilder.formParams(jsonBody);
            }
        }

        RSAOptions rsaOptions = rsaOptionsBuilder.build();

        String signature = RSASigner.getSignature(rsaOptions);

        builder.addHeader(HttpHeaders.X_AUTHORIZATION_NONCE, rsaOptions.getNonce());
        builder.addHeader(HttpHeaders.X_AUTHORIZATION_TIME, rsaOptions.getTimeStamp());
        builder.addHeader(HttpHeaders.X_AUTHORIZATION_RSA, signature);
    }

    /**
     * Creates the service call.
     *
     * @param <T>       the generic type
     * @param request   the request
     * @param converter the converter
     * @return the service call
     */
    protected final <T> ServiceCall<T> createServiceCall(final Request request, final ResponseConverter<T> converter, final String jsonBody) {
        final Call call = createCall(request, jsonBody);
        return new DAEXServiceCall<>(call, converter);
    }

    protected final <T> ServiceCall<T> createServiceCall(final Request request, final ResponseConverter<T> converter) {
        final Call call = createCall(request);
        return new DAEXServiceCall<>(call, converter);
    }

    /**
     * Gets the API end point.
     *
     * @return the API end point
     */
    public String getEndPoint() {
        return endPoint;
    }

    /**
     * Gets the SDK name.
     *
     * @return the SDK name.
     */
    public String getSdkName() {
        return sdkName;
    }

    /**
     * Set the SDK name.
     * @param sdkName the SDK name.
     */
    public void setSdkName(String sdkName) {
        this.sdkName = sdkName;
    }

    /**
     * Gets the error message from a JSON response.
     *
     * <pre>
     * {
     *   code: 400
     *   error: 'bad request'
     * }
     * </pre>
     *
     * @param response the HTTP response
     * @return the error message from the JSON object
     */
    private String getErrorMessage(Response response) {
        String error = ResponseUtils.getString(response);
        try {

            final JsonObject jsonObject = ResponseUtils.getJsonObject(error);
            if (jsonObject.has(MESSAGE_ERROR)) {
                error = jsonObject.get(MESSAGE_ERROR).getAsString();
            } else if (jsonObject.has(MESSAGE_ERROR_2)) {
                error = jsonObject.get(MESSAGE_ERROR_2).getAsString();
            } else if (jsonObject.has(MESSAGE_ERROR_3)) {
                error = jsonObject.get(MESSAGE_ERROR_3).getAsString();
            }
        } catch (final Exception e) {
            // Ignore any kind of exception parsing the json and use fallback String version of response
        }

        return error;
    }

    /**
     * Sets the end point.
     *
     * @param endPoint the new end point. Will be ignored if empty or null
     */
    public void setEndPoint(final String endPoint) {
        if ((endPoint != null) && !endPoint.isEmpty()) {
            this.endPoint = endPoint.endsWith("/") ? endPoint.substring(0, endPoint.length() - 1) : endPoint;
        }
    }

    /**
     * Set the default headers to be used on every HTTP request.
     *
     * @param headers name value pairs of headers
     */
    public void setDefaultHeaders(final Map<String, String> headers) {
        if (headers == null) {
            defaultHeaders = null;
        } else {
            defaultHeaders = Headers.of(headers);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder().append(sdkName).append(" [");

        if (endPoint != null) {
            builder.append("endPoint=").append(endPoint);
        }

        return builder.append(']').toString();
    }


    /**
     * Process service call.
     *
     * @param <T>       the generic type
     * @param converter the converter
     * @param response  the response
     * @return the t
     */
    protected <T> T processServiceCall(final ResponseConverter<T> converter, final Response response) {
        if (response.isSuccessful()) {
            return converter.convert(response);
        }

        // There was a Client Error 4xx or a Server Error 5xx
        // Get the error message and create the exception
        final String error = getErrorMessage(response);
        LOG.log(Level.SEVERE, response.request().method() + " " + response.request().url().toString() + ", status: "
                + response.code() + ", error: " + error);

        switch (response.code()) {
            case HttpStatus.BAD_REQUEST: // HTTP 400
                throw new BadRequestException(error != null ? error : "Bad Request", response);
            case HttpStatus.UNAUTHORIZED: // HTTP 401
                throw new UnauthorizedException("Unauthorized: Access is denied due to invalid credentials. "
                        + "Tip: Did you set the Endpoint?", response);
            case HttpStatus.FORBIDDEN: // HTTP 403
                throw new ForbiddenException(error != null ? error : "Forbidden: Service refuse the request", response);
            case HttpStatus.NOT_FOUND: // HTTP 404
                throw new NotFoundException(error != null ? error : "Not found", response);
            case HttpStatus.NOT_ACCEPTABLE: // HTTP 406
                throw new ForbiddenException(error != null ? error : "Forbidden: Service refuse the request", response);
            case HttpStatus.CONFLICT: // HTTP 409
                throw new ConflictException(error != null ? error : "", response);
            case HttpStatus.REQUEST_TOO_LONG: // HTTP 413
                throw new RequestTooLargeException(error != null ? error
                        : "Request too large: " + "The request entity is larger than the server is able to process", response);
            case HttpStatus.UNSUPPORTED_MEDIA_TYPE: // HTTP 415
                throw new UnsupportedException(error != null ? error : "Unsupported Media Type", response);
            case HttpStatus.TOO_MANY_REQUESTS: // HTTP 429
                throw new TooManyRequestsException(error != null ? error : "Too many requests", response);
            case HttpStatus.INTERNAL_SERVER_ERROR: // HTTP 500
                throw new InternalServerErrorException(error != null ? error : "Internal Server Error", response);
            case HttpStatus.SERVICE_UNAVAILABLE: // HTTP 503
                throw new ServiceUnavailableException(error != null ? error : "Service Unavailable", response);
            default: // other errors
                throw new ServiceResponseException(response.code(), error, response);
        }
    }


    /**
     * Defines implementation for modifying and executing service calls.
     *
     * @param <T> the generic type
     */
    class DAEXServiceCall<T> implements ServiceCall<T> {
        private Call call;
        private ResponseConverter<T> converter;

        DAEXServiceCall(Call call, ResponseConverter<T> converter) {
            this.call = call;
            this.converter = converter;
        }

        @Override
        public ServiceCall<T> addHeader(String name, String value) {
            Builder builder = call.request().newBuilder();
            builder.header(name, value);
            call = client.newCall(builder.build());
            return this;
        }

        @Override
        public T execute() {
            try {
                Response response = call.execute();
                return processServiceCall(converter, response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public io.daex.sdk.core.http.Response<T> executeWithDetails() throws RuntimeException {
            try {
                Response httpResponse = call.execute();
                T responseModel = processServiceCall(converter, httpResponse);
                return new io.daex.sdk.core.http.Response<>(responseModel, httpResponse);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void enqueue(final ServiceCallback<? super T> callback) {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        callback.onResponse(processServiceCall(converter, response));
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            });
        }

        @Override
        public void enqueueWithDetails(final ServiceCallbackWithDetails<T> callback) {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        T responseModel = processServiceCall(converter, response);
                        callback.onResponse(new io.daex.sdk.core.http.Response<>(responseModel, response));
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                }
            });
        }

        @Override
        public CompletableFuture<T> rx() {
            final CompletableFuture<T> completableFuture = new CompletableFuture<T>();

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    completableFuture.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        completableFuture.complete(processServiceCall(converter, response));
                    } catch (Exception e) {
                        completableFuture.completeExceptionally(e);
                    }
                }
            });

            return completableFuture;
        }

        @Override
        public CompletableFuture<io.daex.sdk.core.http.Response<T>> rxWithDetails() {
            final CompletableFuture<io.daex.sdk.core.http.Response<T>> completableFuture
                    = new CompletableFuture<>();

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    completableFuture.completeExceptionally(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        T responseModel = processServiceCall(converter, response);
                        completableFuture.complete(new io.daex.sdk.core.http.Response<>(responseModel, response));
                    } catch (Exception e) {
                        completableFuture.completeExceptionally(e);
                    }
                }
            });

            return completableFuture;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            if (!call.isExecuted()) {
                final Request r = call.request();
                LOG.warning(r.method() + " request to " + r.url() + " has not been sent. Did you forget to call execute()?");
            }
        }
    }
}
