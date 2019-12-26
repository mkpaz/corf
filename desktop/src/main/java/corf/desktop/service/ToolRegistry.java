package corf.desktop.service;

import org.apache.commons.lang3.StringUtils;
import corf.base.plugin.Tool;
import corf.desktop.tools.base64.Base64ConverterTool;
import corf.desktop.tools.filebuilder.FileBuilderTool;
import corf.desktop.tools.httpsender.HttpSenderTool;
import corf.desktop.tools.ipcalc.IPv4CalcTool;
import corf.desktop.tools.passgen.PasswordGeneratorTool;
import corf.desktop.tools.seqgen.SequenceGeneratorTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ToolRegistry {

    private static final List<Tool<?>> BUILTIN_TOOLS = List.of(
            new Base64ConverterTool(),
            new FileBuilderTool(),
            new HttpSenderTool(),
            new IPv4CalcTool(),
            new PasswordGeneratorTool(),
            new SequenceGeneratorTool()
    );

    private final List<Tool<?>> tools = new ArrayList<>();

    public ToolRegistry() {
        tools.addAll(BUILTIN_TOOLS);
    }

    public List<Tool<?>> getAll() {
        return new ArrayList<>(tools);
    }

    public void register(Tool<?> tool) {
        if (StringUtils.isBlank(tool.id())) {
            throw new IllegalArgumentException("Tool ID can not be blank.");
        }

        tools.add(tool);
    }

    public void unregister(Class<? extends Tool<?>> c) {
        tools.removeIf(tool -> Objects.equals(tool.getClass(), c));
    }

    public void unregister(String id) {
        Objects.requireNonNull(id, "id");
        tools.removeIf(tool -> Objects.equals(id, tool.id()));
    }
}
