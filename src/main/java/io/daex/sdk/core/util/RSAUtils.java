package io.daex.sdk.core.util;

import java.net.URL;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class RSAUtils {


    public static String sortQuery(URL url) {
        String query = null;
        SortedMap<String, String> query_pairs = splitQuery(url);
        query = flat(query_pairs);

        return query;
    }

    public static SortedMap<String, String> splitQuery(URL url) {
        SortedMap<String, String> query_pairs = new TreeMap<>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
/*        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
        }*/
        Stream.of(pairs).forEach(pair -> {
            String[] args = pair.split("=");
            query_pairs.put(args[0], args.length == 2? args[1]: "");
        });
        return query_pairs;
    }

    public static String flat(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }


}
