package org.telekit.base.preferences;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.Set;

public interface Theme {

    String getName();

    Set<String> getStylesheets();

    boolean isLight();

    InputStream getInterfaceFont(FontWeight weight, FontPosture posture);

    InputStream getMonospaceFont(FontWeight weight);

    InputStream getDocumentFont(FontWeight weight, FontPosture posture);
}
