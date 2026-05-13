package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.api;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.util.TimeZone;

@QuarkusMain
public class PISIIAAssistantApp {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Quarkus.run(args);
    }
}