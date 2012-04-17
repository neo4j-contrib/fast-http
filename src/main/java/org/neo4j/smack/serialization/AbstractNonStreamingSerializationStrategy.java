package org.neo4j.smack.serialization;

public abstract class AbstractNonStreamingSerializationStrategy<T> implements
        SerializationStrategy<T> {

    public boolean isStreaming() {
        return false;
    }

    public int estimatedSerializedSizeInBytes(T value) {
        return 100;
    }
}
