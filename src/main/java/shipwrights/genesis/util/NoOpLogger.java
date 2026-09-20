package shipwrights.genesis.util;

import org.slf4j.helpers.NOPLogger;

public class NoOpLogger extends NOPLogger {

    public static final NoOpLogger INSTANCE = new NoOpLogger();

    protected NoOpLogger() {
        super();
    }
}