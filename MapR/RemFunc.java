package MapR;

import java.io.Serializable;
import java.util.function.Consumer;


@FunctionalInterface
public interface RemFunc<T> extends Consumer<T>, Serializable 
{

}
