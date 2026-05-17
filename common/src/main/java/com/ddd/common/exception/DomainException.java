package com.ddd.common.exception;

/**
 * 领域异常基类 — 所有业务规则违反都抛出此异常或其子类。
 *
 * DDD: 领域异常是 Ubiquitous Language 的一部分，异常信息应使用业务术语描述。
 *      让调用方（Application 层）能够理解业务层面发生了什么问题。
 */
public class DomainException extends RuntimeException {
    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public DomainException(String message) {
        this("DOMAIN_ERROR", message);
    }

    public String getCode() {
        return code;
    }
}
