package im.wangchao.http.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Description  : Timeout.废弃，不处理该 Annotation</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/31.</p>
 * <p>Time         : 下午1:13.</p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Deprecated
public @interface Timeout {
    int value();
}
