// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

public class BucketListOfDoubles
{
	public static final int bucketSize = 256;
	protected double[][] data;
	protected int size;

	public BucketListOfDoubles()
	{
		data = new double[4][];
	}

	void assureSize(int sz)
	{
		final int bucketsRequired = sz / bucketSize - data.length + 1;
		if (data.length < bucketsRequired)
		{
			double[][] tData = new double[data.length + Math.max(bucketsRequired, 4)][];
			System.arraycopy(data, 0, tData, 0, data.length);
			data = tData;
		}
		double[] b = data[sz / bucketSize];
		if (b == null)
			data[sz / bucketSize] = new double[bucketSize];
	}

	protected void addImpl(double o)
	{
		if (size == data.length * bucketSize)
		{
			double[][] tData = new double[data.length + 4][];
			System.arraycopy(data, 0, tData, 0, data.length);
			data = tData;
		}
		int bPos = size % bucketSize;
		double[] bucket;
		if (bPos == 0)
		{
			bucket = new double[bucketSize];
			data[size / bucketSize] = bucket;
		}
		else
			bucket = data[size / bucketSize];
		bucket[bPos] = o;
		size++;
	}

	protected double getImpl(int ix)
	{
		int bPos = ix % bucketSize;
		int bNo = ix / bucketSize;
		return data[bNo][bPos];
	}

	protected void setImpl(int index, double o)
	{
		int bPos = index % bucketSize;
		double[] bucket = data[index / bucketSize];
		bucket[bPos] = o;
	}

	public void add(double o)
	{
		addImpl(o);
	}

	public double get(int ix)
	{
		return getImpl(ix);
	}

	public void set(int index, double o)
	{
		assureSize(index);
		setImpl(index, o);
	}

	public void truncate(int newSize)
	{
		if (newSize >= size)
			return;
		int b = newSize / bucketSize;
		for (int i = b + 1; i < size / bucketSize; i++)
		{
			data[i] = null;
		}
		size = newSize;
	}

	public int size()
	{
		return size;
	}

	public void add(BucketListOfDoubles r)
	{
		if (r.size() <= 0)
			return;
		int lFull = size % bucketSize;
		int lat = size / bucketSize;
		final int rBucks = (r.size - 1) / bucketSize;
		if (data.length < lat + rBucks + 2)
		{
			double[][] tData = new double[lat + rBucks + 3][];
			System.arraycopy(data, 0, tData, 0, data.length);
			data = tData;
		}
		if (lFull == 0)
		{
			for (int i = 0; i <= rBucks; i++)
			{
				data[lat] = new double[bucketSize];
				System.arraycopy(r.data[i], 0, data[lat], 0, bucketSize);
				lat++;
			}
		}
		else
		{
			int lFree = bucketSize - lFull;
			int rem = r.size;
			int rat = 0;
			while (rem >= bucketSize)
			{
				double[] rb = r.data[rat];
				System.arraycopy(rb, 0, data[lat], lFull, lFree);
				lat++;
				if (lat == data.length)
				{
					double[][] tData = new double[data.length + 4][];
					System.arraycopy(data, 0, tData, 0, data.length);
					data = tData;
				}
				data[lat] = new double[bucketSize];
				System.arraycopy(rb, lFree, data[lat], 0, lFull);
				rat++;
				rem -= bucketSize;
			}
			if (rem > 0)
			{
				System.arraycopy(r.data[rat], 0, data[lat], lFull, Math.min(rem, lFree));
				if (rem > lFree)
				{
					++lat;
					data[lat] = new double[bucketSize];
					System.arraycopy(r.data[rat], lFree, data[lat], 0, rem - lFree);
				}
			}
		}
		size += r.size;
	}

	public void clear()
	{
		for (int i = 0; i < size / bucketSize; i++)
		{
			data[i] = null;
		}
		size = 0;
	}
}
