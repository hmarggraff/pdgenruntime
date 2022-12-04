// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Jun 5, 2003
 * Time: 12:46:45 PM
 * To change this template use Options | File Templates.
 */
public class FIFOQueue<E>
{
    private final E[] queue;
    private final int capacity;
    private int size;
    private int head;
    private int tail;
    @SuppressWarnings("unchecked")
    public FIFOQueue(int cap)
    {
        capacity = (cap > 0) ? cap : 1;
        //noinspection unchecked
        queue = (E[])new Object[capacity];
        head = 0;
        tail = 0;
        size = 0;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public synchronized int getSize()
    {
        return size;
    }

    public synchronized boolean isEmpty()
    {
        return size == 0;
    }

    public synchronized boolean isFull()
    {
        return size == capacity;
    }

    public synchronized void add(E obj) throws InterruptedException
    {
        waitWhileFull();

        queue[head] = obj;
        head = ( head + 1) % capacity;
        size ++;

        notifyAll();
    }

    public synchronized void addEach(E[] objs) throws InterruptedException
    {
        for (E obj : objs)
        {
            add(obj);
        }
    }

    public synchronized E remove() throws InterruptedException
    {
        waitWhileEmpty();
        E obj = queue[tail];

        queue[tail] = null;

        tail = ( tail + 1 ) % capacity;

        size--;

        notifyAll();

        return obj;
    }

    @SuppressWarnings("unchecked")
    public synchronized E[] removeAll() throws InterruptedException
    {
        //noinspection unchecked
        E[] objs = (E[])new Object[size];
        for (int i = 0; i < objs.length; i++)
        {
            objs[i] = remove();
        }

        return objs;
    }

    public synchronized Object[] removeAtLeastOne() throws InterruptedException
    {
        waitWhileEmpty();
        return removeAll();
    }

    public synchronized boolean waitUntilEmpty(long msTimeout) throws InterruptedException
    {
        if ( msTimeout == 0)
        {
            waitUntilEmpty();
            return true;
        }

        long endTime = System.currentTimeMillis() + msTimeout;
        long msRemaining = msTimeout;
        while(!isEmpty() && msRemaining > 0)
        {
            wait(msRemaining);
            msRemaining = endTime - System.currentTimeMillis();
        }

        return isEmpty();
    }

    public synchronized void waitUntilEmpty() throws InterruptedException
    {
        while(!isEmpty())
        {
            wait();
        }
    }

    public synchronized void waitWhileEmpty() throws InterruptedException
    {
        while(isEmpty())
        {
            wait();
        }
    }

    public synchronized void waitUntilFull() throws InterruptedException
    {
        while(!isFull())
        {
            wait();
        }
    }

    public synchronized void waitWhileFull() throws InterruptedException
    {
        while(isFull())
        {
            wait();
        }
    }
}
