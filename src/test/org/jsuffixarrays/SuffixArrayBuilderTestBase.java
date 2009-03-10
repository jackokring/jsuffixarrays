package org.jsuffixarrays;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.PrimitiveArrays;

/**
 * A set of shared tests and preconditions that all implementations of
 * {@link ISuffixArrayBuilder} should meet.
 */
public abstract class SuffixArrayBuilderTestBase
{
    protected ISuffixArrayBuilder builder;

    protected MinMax smallAlphabet = new MinMax(-5, 5);
    protected MinMax largeAlphabet = new MinMax(-500, 500);

    @Before
    public final void prepareBuilder()
    {
        builder = getInstance();
    }

    /**
     * Subclasses must override and return a valid instance of {@link ISuffixArrayBuilder}
     * .
     */
    protected abstract ISuffixArrayBuilder getInstance();

    /**
     * Check the suffixes of <code>banana</code>.
     */
    @Test
    public void checkBanana()
    {
        assertSuffixes("banana", 
            "a",
            "ana",
            "anana",
            "banana",
            "na",
            "nana");
    }

    /**
     * Check the suffixes of <code>mississippi</code>.
     */
    @Test
    public void checkMississippi()
    {
        assertSuffixes("mississippi", 
            "i",
            "ippi",
            "issippi",
            "ississippi",
            "mississippi",
            "pi",
            "ppi",
            "sippi",
            "sissippi",
            "ssippi",
            "ssissippi");
    }

    /**
     * Create a suffix array for the same input sequence, but placed at
     * different offsets in the input array. The result should be identical
     * (not counting the offset of course).
     * <p>
     * Checks the LCP array created for the given input as well.
     */
    @Test
    public void sameResultWithArraySlice()
    {
        final ISuffixArrayBuilder builder = getInstance();

        final int sliceSize = 500;
        final int totalSize = 1000;

        final Random rnd = new Random(0x11223344);
        final MinMax alphabet = new MinMax(1, 50);

        final int [] slice = generateRandom(rnd, sliceSize, alphabet);
        final int [] total = generateRandom(rnd, totalSize, alphabet);

        int [] prevSuffixArray = null;
        int [] prevLCP = null;
        for (int i = 0; i < totalSize - slice.length - 3; i++)
        {
            int [] input = (int []) total.clone();
            System.arraycopy(slice, 0, input, i, slice.length);
            int [] clone = (int []) input.clone();

            int [] sa = builder.buildSuffixArray(input, i, slice.length);
            int [] lcp = SuffixArrays.computeLCP(input, i, slice.length, sa);

            Assert.assertArrayEquals(clone, input);

            // Normalize indexes so that they are 0-based between runs.
            for (int j = 0; j < slice.length; j++)
            {
                sa[j] -= i;
            }
            if (prevSuffixArray != null)
            {
                Assert.assertArrayEquals(prevSuffixArray, sa);
            }
            prevSuffixArray = sa;

            // Compare LCPs
            if (prevLCP != null)
            {
                Assert.assertArrayEquals(prevLCP, lcp);
            }
            prevLCP = lcp;
        }
    }    

    /**
     * Create suffix arrays for a few random sequences of integers, verify
     * invariants (every suffix array is a permutation of indices, every suffix in
     * the suffix array is lexicographically greater or equal than all its predecessors). 
     */
    @Test
    public void invariantsOnRandomSmallAlphabet()
    {
        final ISuffixArrayBuilder builder = getInstance();

        // Use constant seed so that we can repeat tests.
        final Random rnd = new Random(0x11223344);
        final int inputSize = 1000;
        final int repeats = 500;

        runRandom(builder, rnd, inputSize, repeats, smallAlphabet);
    }

    /**
     * @see #invariantsOnRandomSmallAlphabet() 
     */
    @Test
    public void invariantsOnRandomLargeAlphabet()
    {
        final ISuffixArrayBuilder builder = getInstance();

        // Use constant seed so that we can repeat tests.
        final Random rnd = new Random(0x11223344);
        final int inputSize = 1000;
        final int repeats = 500;

        runRandom(builder, rnd, inputSize, repeats, largeAlphabet);
    }

    /*
     * Run invariant checks on randomly generated data.
     */
    private void runRandom(final ISuffixArrayBuilder builder, final Random rnd,
        final int inputSize, final int repeats, final MinMax alphabet)
    {
        for (int i = 0; i < repeats; i++)
        {
            final int [] input = generateRandom(rnd, inputSize, alphabet);
            final int [] copy = (int []) input.clone();

            final int start = 0;
            final int length = input.length - 3;
            final int [] SA = builder.buildSuffixArray(input, start, length);
            
            Assert.assertArrayEquals(input, copy);
            assertPermutation(SA, length);
            assertSorted(SA, input, length);
        }
    }

    /*
     * Generate random data.
     */
    public static int [] generateRandom(Random rnd, int size, MinMax alphabet)
    {
        final int [] input = new int [size];
        for (int j = 0; j < input.length; j++) 
        {
            input[j] = rnd.nextInt(alphabet.range() + 1) + alphabet.min;
        }
        return input;
    }

    /*
     * Verify that two suffixes are less or equal.
     */
    private boolean sleq(int [] s1, int s1i, int [] s2, int s2i, int n)
    {
        do
        {
            if (s1[s1i] < s2[s2i]) return true;
            if (s1[s1i] > s2[s2i]) return false;
            s2i++;
            s1i++;

            if (s1i == n) return true;
        }
        while (true);
    }

    /*
     * Make sure suffixes in a suffix array is sorted.
     */
    private void assertSorted(int [] SA, int [] s, int n)
    {
        for (int i = 0; i < n - 1; i++)
        {
            if (!sleq(s, SA[i], s, SA[i + 1], n))
            {
                Assert.fail("Suffix " + SA[i] + ">" + SA[i + 1] + ":\n"
                    + SA[i]     + ">" + PrimitiveArrays.asList(s).subList(SA[i], n) + "\n"
                    + SA[i + 1] + ">" + PrimitiveArrays.asList(s).subList(SA[i + 1], n) + "\n"
                    + "a>" + PrimitiveArrays.asList(s));
            }
        }
    }    

    /*
     * Assert a suffix array is a permutation of indices.
     */
    private void assertPermutation(int [] SA, int length)
    {
        final boolean [] seen = new boolean [length];
        for (int i = 0; i < length; i++) seen[SA[i]] = true;
        for (int i = 0; i < length; i++) Assert.assertTrue(seen[i]);
    }

    /*
     * Assert a suffix array built for a given input contains exactly the given
     * set of suffixes, in that order. 
     */
    private void assertSuffixes(CharSequence input, CharSequence... expectedSuffixes)
    {
        final int [] suffixes = SuffixArrays.create(input, getInstance());
        Assert.assertEquals(Arrays.asList(expectedSuffixes), SuffixArrays.toString(input,
            suffixes));
    }
}
