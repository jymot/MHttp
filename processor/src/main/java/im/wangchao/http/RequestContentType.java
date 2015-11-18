package im.wangchao.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * <p>Description  : RequestContentType.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/31.</p>
 * <p>Time         : 下午1:09.</p>
 */
@Retention(CLASS)
@Target(FIELD)
public @interface RequestContentType {
    /**
     * request Content-Type
     */
    String value();
}
