package semestrovka.module.entities;

import java.util.Collection;

public interface ICustomProductCollection<T> extends Collection<T> {
    int getCurrentIndex();

    int getCurrentProductCount();

    double getCurrentPriceForProducts();

    boolean removeProductById(int id);
}
