package org.telekit.base.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.BaseMessageKeys;
import org.telekit.base.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JacksonYamlSerializer<T> implements Serializer<T> {

    private YAMLMapper mapper;
    private TypeReference<T> typeRef;

    public JacksonYamlSerializer(YAMLMapper mapper, TypeReference<T> typeRef) {
        this.mapper = mapper;
        this.typeRef = typeRef;
    }

    @Override
    public void serialize(OutputStream outputStream, T value) {
        try {
            mapper.writeValue(outputStream, value);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(BaseMessageKeys.MSG_GENERIC_IO_ERROR));
        }
    }

    @Override
    public T deserialize(InputStream inputStream) {
        try {
            return mapper.readValue(inputStream, typeRef);
        } catch (IOException e) {
            throw new TelekitException(Messages.get(BaseMessageKeys.MSG_GENERIC_IO_ERROR));
        }
    }
}
