package bg.sofia.uni.fmi.mjt.battleships.server.controller;

import bg.sofia.uni.fmi.mjt.battleships.common.cookie.SessionCookie;
import bg.sofia.uni.fmi.mjt.battleships.common.request.ClientRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ControllerRespondArgs {

    public static final List<Class> argsClasses = List.of(
        ClientRequest.class,
        List.class,
        String.class
    );

    public List<Object> argsValues;

    public ControllerRespondArgs() {
        this.argsValues = new ArrayList<>();
    }

    public ControllerRespondArgs(List<Object> args) {
        this.argsValues = args;
    }
}
