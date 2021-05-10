/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sins2.collections_utils;

import java.util.Iterator;

/**
 *
 * @author douglas
 */
public class EmptyIterator<T> implements Iterator<T>{

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        return null;
    }

    @Override
    public void remove() {
        
    }
    
}
