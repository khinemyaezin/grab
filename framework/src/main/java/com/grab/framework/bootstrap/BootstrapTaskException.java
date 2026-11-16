package com.grab.framework.bootstrap;

/**
 * Failure raised by a {@link BootstrapTask}. Permanent failures (e.g. a security manifest digest conflict) are not
 * retried: the task fails immediately and, if required, keeps the replica out of rotation.
 */
public class BootstrapTaskException extends RuntimeException {

    private final boolean permanent;

    private BootstrapTaskException(String message, boolean permanent) {
        super(message);
        this.permanent = permanent;
    }

    public static BootstrapTaskException permanent(String message) {
        return new BootstrapTaskException(message, true);
    }

    public static BootstrapTaskException transientFailure(String message) {
        return new BootstrapTaskException(message, false);
    }

    public boolean isPermanent() {
        return permanent;
    }
}
