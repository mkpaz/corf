package org.telekit.base.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.domain.exception.TelekitException;
import org.telekit.base.i18n.BaseMessages;
import org.telekit.base.i18n.I18n;
import org.telekit.base.service.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JacksonYamlSerializer<T> implements Serializer<T> {

    private final YAMLMapper mapper;
    private final TypeReference<T> typeRef;

    public JacksonYamlSerializer(YAMLMapper mapper, TypeReference<T> typeRef) {
        this.mapper = mapper;
        this.typeRef = typeRef;
    }

    @Override
    public void serialize(OutputStream outputStream, T value) {
        try {
            mapper.writeValue(outputStream, value);
        } catch (IOException e) {
            throw new TelekitException(I18n.t(BaseMessages.MGG_UNABLE_TO_SAVE_DATA_TO_FILE));
        }
    }

    @Override
    public T deserialize(InputStream inputStream) {
        try {
            return mapper.readValue(inputStream, typeRef);
        } catch (IOException e) {
            throw new TelekitException(I18n.t(BaseMessages.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE));
        }
    }
}
