package org.telekit.example.service;

import org.telekit.base.i18n.I18n;
import org.telekit.example.i18n.ExampleMessages;

public class HelloService {

    public String hello() {
        return I18n.t(ExampleMessages.HELLO);
    }
}
