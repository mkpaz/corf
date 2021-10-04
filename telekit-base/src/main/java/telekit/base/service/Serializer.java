package telekit.base.service;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer<T> {

    void serialize(OutputStream outputStream, T value);

    T deserialize(InputStream inputStream);
}
