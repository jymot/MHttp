package im.wangchao.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Description  : Callback.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 15/10/17.</p>
 * <p>Time         : 下午6:02.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface Callback {
}
