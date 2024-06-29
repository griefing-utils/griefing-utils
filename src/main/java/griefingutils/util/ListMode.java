package griefingutils.util;

import java.util.Collection;

public enum ListMode {
    Whitelist,
    Blacklist;

    public <T> boolean contains(Collection<T> collection, T element) {
        if (this == Whitelist) return collection.contains(element);
        else return !collection.contains(element);
    }
}
