package corf.base.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import corf.base.exception.AppException;
import corf.base.i18n.M;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import static corf.base.i18n.I18n.t;

public class JacksonYamlSerializer<T> implements Serializer<T> {

    protected final YAMLMapper mapper;
    protected final TypeReference<T> typeRef;

    public JacksonYamlSerializer(YAMLMapper mapper, TypeReference<T> typeRef) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.typeRef = Objects.requireNonNull(typeRef, "typeRef");
    }

    @Override
    public void serialize(OutputStream outputStream, T value) {
        try {
            mapper.writeValue(outputStream, value);
        } catch (IOException e) {
            throw new AppException(t(M.MGG_UNABLE_TO_SAVE_DATA_TO_FILE));
        }
    }

    @Override
    public T deserialize(InputStream inputStream) {
        try {
            return mapper.readValue(inputStream, typeRef);
        } catch (IOException e) {
            throw new AppException(t(M.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE));
        }
    }
}
