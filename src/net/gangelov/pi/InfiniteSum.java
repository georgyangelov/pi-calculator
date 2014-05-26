package net.gangelov.pi;

import java.math.BigDecimal;

/**
 * This interface should be implemented to allow abstract calculation of sums.
 * The objects implementing this should be stateless and thread-safe so that any two instances (or one instance)
 * can be used to compute the same indices (and they should return the same value).
 *
 * One instance of this interface's implementing class should be able to handle multiple threads using it.
 *
 * The basic idea is that an infinite sum can be calculated in two steps:
 *  1. Calculate a partial term based on the previous partial term. This is a way to speed-up the computation
 *      because the terms which we are computing almost always have some common part that can easily be adjusted
 *      (multiplied by something) to get a part of the next term.
 *
 *      Suppose f is a function which has this property: f(i) = f(i-1)*d(i).
 *      For example the terms of the sum with common term f(i)*g(i) can be calculated faster using the above property.
 *      This is essentially what the `nextPartialTerm` method should do - calculate f(i) based on f(i-1).
 *
 *  2. As the g(i) function (from the above example) doesn't have this property there should be a way
 *      for the actual terms to be computed and this is what the `calculateTerm` method should do.
 *
 *      In the above example it should just compute g(i) and multiply it with `term`.
 *
 * Based on this separation of properties, and due to the fact that f(i) = f(i-k)*d(i-k+1)*...*d(i-1)*d(i)
 * we can calculate the sum in parallel using this interface by splitting it in multiple parts. This is effectively
 * calculating the sum from a different starting partial term f(k) and, when done, multiplying the resulting sum
 * by f(k) to obtain this part's sum.
 *
 * The term indices start at 0!
 */
public interface InfiniteSum {

    /**
     * Returns the next partial term of the sum.
     *
     * @param lastPartialTerm The previous partial term of the sum.
     * @param termIndex The current term index.
     *
     * @return The partial term for termIndex.
     */
    BigDecimal nextPartialTerm(BigDecimal lastPartialTerm, int termIndex);

    /**
     * Calculates the actual term that should be added to the resulting sum.
     *
     * @param partialTerm The partial term generated by the above method.
     * @param index The index of the current term.
     *
     * @return The actual (non-partial) term to sum.
     */
    BigDecimal calculateTerm(BigDecimal partialTerm, int index);

    /**
     * Returns the preferred precision that should be used for calculations up to the given maximum number of terms.
     *
     * @param numTerms The maximum number of terms.
     *
     * @return The preferred precision (in number of decimal digits).
     */
    int preferredPrecision(int numTerms);

}