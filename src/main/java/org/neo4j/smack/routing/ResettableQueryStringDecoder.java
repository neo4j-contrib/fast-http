package org.neo4j.smack.routing;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.smack.http.HttpTokens;

// Modified verion of Nettys QueryStringDecoder,
// lets you reuse the same decoder.
// Can probably be optimized further..
public class ResettableQueryStringDecoder {

        private Charset charset;
        private String uri;
        private String path;
        private Map<String, List<String>> params;

        /**
         * Returns the decoded path string of the URI.
         */
        public String getPath() {
            if (path == null) {
                int pathEndPos = uri.indexOf('?');
                if (pathEndPos < 0) {
                    path = uri;
                }
                else {
                    return path = uri.substring(0, pathEndPos);
                }
            }
            return path;
        }

        /**
         * Returns the decoded key-value parameter pairs of the URI.
         */
        public Map<String, List<String>> getParameters() {
            if (params == null) {
                int pathLength = getPath().length();
                if (uri.length() == pathLength) {
                    return Collections.emptyMap();
                }
                params = decodeParams(uri.substring(pathLength + 1));
            }
            return params;
        }
        
        public void resetWith(String uri) {
            reset(uri, HttpTokens.DEFAULT_CHARSET);
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the
         * specified charset.
         */
        public void reset(String uri, Charset charset) {
            if (uri == null) {
                throw new NullPointerException("uri");
            }
            if (charset == null) {
                throw new NullPointerException("charset");
            }

            this.uri = uri;
            this.charset = charset;
        }

        private Map<String, List<String>> decodeParams(String s) {
            Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
            String name = null;
            int pos = 0; // Beginning of the unprocessed region
            int i;       // End of the unprocessed region
            char c = 0;  // Current character
            for (i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                if (c == '=' && name == null) {
                    if (pos != i) {
                        name = decodeComponent(s.substring(pos, i), charset);
                    }
                    pos = i + 1;
                } else if (c == '&') {
                    if (name == null && pos != i) {
                        // We haven't seen an `=' so far but moved forward.
                        // Must be a param of the form '&a&' so add it with
                        // an empty value.
                        addParam(params, decodeComponent(s.substring(pos, i), charset), "");
                    } else if (name != null) {
                        addParam(params, name, decodeComponent(s.substring(pos, i), charset));
                        name = null;
                    }
                    pos = i + 1;
                }
            }

            if (pos != i) {  // Are there characters we haven't dealt with?
                if (name == null) {     // Yes and we haven't seen any `='.
                    addParam(params, decodeComponent(s.substring(pos, i), charset), "");
                } else {                // Yes and this must be the last value.
                    addParam(params, name, decodeComponent(s.substring(pos, i), charset));
                }
            } else if (name != null) {  // Have we seen a name without value?
                addParam(params, name, "");
            }

            return params;
        }

        private static String decodeComponent(String s, Charset charset) {
            if (s == null) {
                return "";
            }

            try {
                return URLDecoder.decode(s, charset.name());
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedCharsetException(charset.name());
            }
        }

        private static void addParam(Map<String, List<String>> params, String name, String value) {
            List<String> values = params.get(name);
            if (values == null) {
                values = new ArrayList<String>(1);  // Often there's only 1 value.
                params.put(name, values);
            }
            values.add(value);
        }
}
