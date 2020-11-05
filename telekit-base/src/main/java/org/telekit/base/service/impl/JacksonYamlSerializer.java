package org.telekit.base.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.BaseMessageKeys;
import org.telekit.base.i18n.Messages;
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
            throw new TelekitException(Messages.get(BaseMessageKeys.MGG_UNABLE_TO_SAVE_DATA_TO_FILE));
        }
    }

    @Override
    public T deserialize(InputStream inputStream) {
        try {
            return mapper.readValue(inputStream, typeRef);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(BaseMessageKeys.MGG_UNABLE_TO_LOAD_DATA_FROM_FILE));
        }
    }
}
