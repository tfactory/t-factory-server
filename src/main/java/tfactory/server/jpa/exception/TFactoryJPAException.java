package tfactory.server.jpa.exception;

/**
 * Generic exception for tFactory JPA module.
 * Created by aalopez on 4/23/16.
 */
public class TFactoryJPAException extends Exception {

    /**
     * Type of exception.
     */
    private TFactoryExceptionCodeEnum exceptionCode;

    /**
     * Creates an instance of the exception with the specified exception code, but {@code null} as its detail message.
     *
     * @param code Exception code
     */
    public TFactoryJPAException(TFactoryExceptionCodeEnum code) {
        super();
        this.exceptionCode = code;
    }

    /**
     * Constructs a new exception with the specified exception code and detail message.
     *
     * @param code    the exception code.
     * @param message the detail message.
     */
    public TFactoryJPAException(TFactoryExceptionCodeEnum code, String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified exception code, detail message and
     * cause.
     *
     * @param code    the exception code.
     * @param message the detail message.
     * @param cause   the cause.
     */
    public TFactoryJPAException(TFactoryExceptionCodeEnum code, String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified exception code, cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt>.
     *
     * @param code  the exception code.
     * @param cause the cause.
     */
    public TFactoryJPAException(TFactoryExceptionCodeEnum code, Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified exception code, detail message,
     * cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param code               the exception code.
     * @param message            the detail message.
     * @param cause              the cause.
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled.
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable.
     */
    protected TFactoryJPAException(TFactoryExceptionCodeEnum code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Gets the exception code associated to this exception.
     *
     * @return Exception code.
     */
    public TFactoryExceptionCodeEnum getExceptionCode() {
        return exceptionCode;
    }
}
