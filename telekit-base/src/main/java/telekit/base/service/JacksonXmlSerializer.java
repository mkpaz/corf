package telekit.base.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import telekit.base.domain.exception.TelekitException;
import telekit.base.i18n.BaseMessages;
import telekit.base.i18n.I18n;
import telekit.base.service.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JacksonXmlSerializer<T> implements Serializer<T> {

    private final XmlMapper mapper;
    private final TypeReference<T> typeRef;

    public JacksonXmlSerializer(XmlMapper mapper, TypeReference<T> typeRef) {
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
