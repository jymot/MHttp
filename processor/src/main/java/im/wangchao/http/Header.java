package im.wangchao.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Description  : Header.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/31.</p>
 * <p>Time         : 下午1:17.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Header {
    /**
     * the common header of request
     */
    String[] value();
}
