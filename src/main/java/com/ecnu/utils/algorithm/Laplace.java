package com.ecnu.utils.algorithm;

import org.apache.commons.math3.distribution.LaplaceDistribution;

/**
 * Laplace
 * 差分隐私要用到。
 *
 * @author Wenyan Liu
 */
public class Laplace {

    public static LaplaceDistribution laplace;

    /**
     * Laplace pdf with location mu and scale beta.
     *
     * @param mu   location of the laplace function
     * @param beta scale of the laplace function
     * @return Laplace function value
     */
    private static double pdf(double mu, double beta) {
        laplace = new LaplaceDistribution(mu, beta);
        return laplace.sample();
    }

    /**
     * Laplace pdf related to Exponential pdf.
     *
     * @param beta scale of the laplace pdf
     * @return Laplace function value of <code>mu</code> = 0 and the given <code>beta</code>
     */
    public static double pdf(double beta) {
        return pdf(0, beta);
    }

    /**
     * Default Laplace pdf.
     *
     * @return Laplace function value of <code>mu</code> = 0 and <code>beta</code> = 1.
     */
    public static double pdf() {
        return pdf(0, 1);
    }
}
