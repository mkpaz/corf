package org.telekit.example.service;

import org.telekit.base.i18n.Messages;
import org.telekit.example.MessageKeys;

public class HelloService {

    public String hello() {
        return Messages.get(MessageKeys.HELLO);
    }
}
