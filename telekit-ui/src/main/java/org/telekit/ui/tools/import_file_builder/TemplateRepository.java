package org.telekit.ui.tools.import_file_builder;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.telekit.base.Messages;
import org.telekit.base.Environment;
import org.telekit.base.domain.TelekitException;
import org.telekit.base.service.XMLBeanRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.telekit.ui.main.AllMessageKeys.MGG_UNABLE_TO_PARSE_CONFIG;
import static org.telekit.ui.main.AllMessageKeys.MGG_UNABLE_TO_SAVE_CONFIG;

public class TemplateRepository extends XMLBeanRepository<Template> {

    private static final String DATA_FILE_NAME = "import-file-builder.templates.xml";
    private static final Path DATA_FILE_PATH = Environment.DATA_DIR.resolve(DATA_FILE_NAME);

    public TemplateRepository(XmlMapper mapper) {
        super(mapper);
    }

    @Override
    protected Template deserialize(String xml) throws Exception {
        return mapper.readValue(xml, Template.class);
    }

    @Override
    protected List<Template> load() {
        List<Template> result = new ArrayList<>();
        if (!Files.exists(DATA_FILE_PATH)) return result;

        try {
            Templates data = mapper.readValue(DATA_FILE_PATH.toFile(), Templates.class);
            if (data.getTemplates() != null && data.getTemplates().size() > 0) {
                result.addAll(data.getTemplates());
            }
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_PARSE_CONFIG), e);
        }

        return result;
    }

    @Override
    protected void store() {
        try {
            List<Template> templates = new ArrayList<>(getAll());
            Collections.sort(templates);
            mapper.writeValue(DATA_FILE_PATH.toFile(), new Templates(templates));
        } catch (Exception e) {
            throw new TelekitException(Messages.get(MGG_UNABLE_TO_SAVE_CONFIG), e);
        }
    }

    @JacksonXmlRootElement(localName = "templates")
    public static class Templates {

        @JacksonXmlProperty(localName = "template")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Template> templates;

        public Templates() {}

        public Templates(List<Template> templates) {
            this.templates = templates;
        }

        public List<Template> getTemplates() {
            return templates;
        }

        public void setTemplates(List<Template> templates) {
            this.templates = templates;
        }
    }
}
