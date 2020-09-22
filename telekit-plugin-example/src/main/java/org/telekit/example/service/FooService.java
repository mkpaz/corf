package org.telekit.example.service;

import org.telekit.base.Messages;
import org.telekit.example.ExampleMessageKeys;

public class FooService {

    public String hello() {
        return Messages.get(ExampleMessageKeys.HELLO);
    }
}
