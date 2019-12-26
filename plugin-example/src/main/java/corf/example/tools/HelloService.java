package corf.example.tools;

import corf.example.i18n.EM;

import static corf.base.i18n.I18n.t;

public final class HelloService {

    public String hello() {
        return t(EM.HELLO);
    }
}
