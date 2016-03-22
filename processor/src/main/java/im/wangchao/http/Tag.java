package im.wangchao.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Description  : Tag.</p>
 * <p/>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 16/3/22.</p>
 * <p>Time         : 上午10:58.</p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface Tag {
}
