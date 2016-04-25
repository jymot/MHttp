package im.wangchao.http.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * <p>Description  : RootURL.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/31.</p>
 * <p>Time         : 上午10:17.</p>
 */
@Retention(CLASS)
@Target(FIELD)
public @interface RootURL {
    /**
     * request root address
     */
    String value();
}
