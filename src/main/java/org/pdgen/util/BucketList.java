// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;


public class BucketList<E> {

    public static final int bucketSize = 20;
    protected Object[][] data;
    protected int size;

    public BucketList() {
        data = new Object[4][];
    }

    protected void addImpl(E o) {
        if (size == data.length * bucketSize) {
            Object[][] tData = new Object[data.length + 4][];
            System.arraycopy(data, 0, tData, 0, data.length);
            data = tData;
        }
        int bPos = size % bucketSize;
        Object[] bucket;
        if (bPos == 0) {
            bucket = new Object[bucketSize];
            data[size / bucketSize] = bucket;
        } else
            bucket = data[size / bucketSize];
        bucket[bPos] = o;
        size++;
    }

    @SuppressWarnings("unchecked")
    protected E getImpl(int ix) {
        int bPos = ix % bucketSize;
        int bNo = ix / bucketSize;
        //noinspection unchecked
        return (E) data[bNo][bPos];
    }

    private void setImpl(int ix, E o) {
        int bPos = ix % bucketSize;
        Object[] bucket = data[ix / bucketSize];
        bucket[bPos] = o;
        size = Math.max(ix + 1, size);
    }

    public void addObject(E o) {
        addImpl(o);
    }

    public E getObject(int ix) {
        return getImpl(ix);
    }

    public void truncate(int newSize) {
        if (newSize == 0)
            clear();
        if (newSize < size) {
            int oldBuckets = size / bucketSize;
            int newBuckets = newSize / bucketSize;
            final int oPos;
            if (oldBuckets == newBuckets)
                oPos = size % bucketSize;
            else
                oPos = bucketSize;
            int bPos = newSize % bucketSize;
            Object[] bucket = data[newBuckets];
            for (int i = bPos; i < oPos; i++) {
                bucket[i] = null;
            }
            for (int i = newBuckets + 1; i < oldBuckets; i++) {
                data[i] = null;
            }
            size = newSize;
        }
    }

    public void set(int index, E o) {
        assureSize(index);
        setImpl(index, o);
    }

    private void assureSize(int sz) {
        final int bucketsRequired = sz / bucketSize - data.length + 1;
        if (data.length < bucketsRequired) {
            Object[][] tData = new Object[data.length + Math.max(bucketsRequired, 4)][];
            System.arraycopy(data, 0, tData, 0, data.length);
            data = tData;
        }
        Object[] b = data[sz / bucketSize];
        if (b == null)
            data[sz / bucketSize] = new Object[bucketSize];
    }


    public int size() {
        return size;
    }

    public void add(BucketList<? extends E> r) {
        if (r.size() <= 0)
            return;
        int lFull = size % bucketSize;
        int lat = size / bucketSize;
        final int rBucks = (r.size - 1) / bucketSize;
        if (data.length < lat + rBucks + 2) {
            Object[][] tData = new Object[lat + rBucks + 3][];
            System.arraycopy(data, 0, tData, 0, data.length);
            data = tData;
        }
        if (lFull == 0) {

            for (int i = 0; i <= rBucks; i++) {
                data[lat] = new Object[bucketSize];
                System.arraycopy(r.data[i], 0, data[lat], 0, bucketSize);
                lat++;
            }
        } else {
            int lFree = bucketSize - lFull;
            int rem = r.size;
            int rat = 0;
            while (rem >= bucketSize) {
                Object[] rb = r.data[rat];
                System.arraycopy(rb, 0, data[lat], lFull, lFree);
                lat++;
                if (lat == data.length) {
                    Object[][] tData = new Object[data.length + 4][];
                    System.arraycopy(data, 0, tData, 0, data.length);
                    data = tData;
                }
                data[lat] = new Object[bucketSize];
                System.arraycopy(rb, lFree, data[lat], 0, lFull);
                rat++;
                rem -= bucketSize;
            }
            if (rem > 0) {
                System.arraycopy(r.data[rat], 0, data[lat], lFull, Math.min(rem, lFree));
                if (rem > lFree) {
                    ++lat;
                    data[lat] = new Object[bucketSize];
                    System.arraycopy(r.data[rat], lFree, data[lat], 0, rem - lFree);
                }
            }
        }
        size += r.size;
    }

    public void clear() {
        for (int i = 0; i < size / bucketSize; i++) {
            data[i] = null;
        }
        size = 0;
    }
}
