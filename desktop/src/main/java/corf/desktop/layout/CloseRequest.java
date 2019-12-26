package corf.desktop.layout;

import org.jetbrains.annotations.Nullable;
import corf.base.desktop.Dimension;

public record CloseRequest(int exitCode, @Nullable Dimension windowSize) { }
