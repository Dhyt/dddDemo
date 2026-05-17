package com.ddd.common.domain;

import java.io.Serializable;

/**
 * 值对象基类标记接口。
 *
 * DDD: 值对象通过属性值来定义自身，没有唯一标识，是不可变的。
 *      两个值对象如果所有属性值相等，则视为同一个对象。
 *      equals() 和 hashCode() 必须基于所有属性实现。
 */
public interface ValueObject extends Serializable {
}
