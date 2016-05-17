/**
 * Copyright (C) 2015 Cesar Hernandez. (https://github.com/tfactory)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        this.exceptionCode = code;
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
        this.exceptionCode = code;
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
        this.exceptionCode = code;
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
        this.exceptionCode = code;
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
