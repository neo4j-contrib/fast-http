package org.neo4j.smack.serialization.strategy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.neo4j.server.rest.repr.ExtensionInjector;
import org.neo4j.server.rest.repr.OutputFormat;
import org.neo4j.server.rest.repr.Representation;
import org.neo4j.server.rest.repr.RepresentationFormatRepository;
import org.neo4j.smack.serialization.AbstractNonStreamingSerializationStrategy;
import org.neo4j.smack.serialization.SerializationException;
import org.neo4j.smack.serialization.Serializer;

/**
 * @author mh
 * @since 27.11.11
 */
public class RepresentationSerializationStrategy extends AbstractNonStreamingSerializationStrategy<Representation> {

    RepresentationFormatRepository repository = new RepresentationFormatRepository(new ExtensionInjector() {
        @Override
        public Map<String, List<String>> getExensionsFor(Class<?> aClass) {
            return Collections.emptyMap();
        }
    });

    @Override
    public void serialize(Representation representation, Serializer out) throws SerializationException {
        try {
            final OutputFormat format = createOutputFormat();
            final String result = format.format(representation);
            out.putRaw(result);
        } catch (Exception e) {
            throw new SerializationException("Error serializing representation " + representation, e);
        }
    }

    private OutputFormat createOutputFormat() throws URISyntaxException {
        final URI uri = new URI("http://localhost:7473/db/data"); // TODO
        return repository.outputFormat(Arrays.asList(MediaType.APPLICATION_JSON_TYPE), uri);
    }
}
