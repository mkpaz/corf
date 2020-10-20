package org.telekit.base.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.i18n.BaseMessageKeys;
import org.telekit.base.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JacksonXmlSerializer<T> implements Serializer<T> {

    private XmlMapper mapper;
    private TypeReference<T> typeRef;

    public JacksonXmlSerializer(XmlMapper mapper, TypeReference<T> typeRef) {
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
