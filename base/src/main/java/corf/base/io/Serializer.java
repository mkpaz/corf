package corf.base.io;

import java.io.InputStream;
import java.io.OutputStream;

/** Generic serializer interface. */
public interface Serializer<T> {

    void serialize(OutputStream outputStream, T value);

    T deserialize(InputStream inputStream);
}
