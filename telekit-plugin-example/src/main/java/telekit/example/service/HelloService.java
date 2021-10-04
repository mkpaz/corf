package telekit.example.service;

import telekit.base.i18n.I18n;
import telekit.example.i18n.ExampleMessages;

public class HelloService {

    public String hello() {
        return I18n.t(ExampleMessages.HELLO);
    }
}
