/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.collections_utils;

/**
 *
 * @author douglas
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class IteratorWrapper<E> implements Iterator<E> {

    /** The current iterator */
    private Iterator<Iterator<E>> currentIter;
    /** The current item in that iterator */
    private Iterator<E> current;

    public  IteratorWrapper(Iterator<E>... iterators) {
        // note UNSAFE constructor here
        this(Arrays.asList(iterators));
    }

    public IteratorWrapper(Collection<Iterator<E>> iterators) {
        // note UNSAFE constructor here
        currentIter = iterators.iterator();
        current = currentIter.next();
    }

    private Iterator<E> getIterator() {
        while (!current.hasNext() && currentIter.hasNext()) {
            current = currentIter.next();
        }

        return current;
    }

    public boolean hasNext() {
        return getIterator().hasNext();
    }

    public void remove() {
        getIterator().remove();
    }

    public E next() {
        return getIterator().next();
    }
}
