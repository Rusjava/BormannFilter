/*
 * Copyright (C) 2015 Samsung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package borrmannfilter;

import org.apache.commons.math3.complex.*;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class XrayCrystall {

    private double d, den, theta, L, M, sphi, cphi, sphi1, cphi1;
    private Complex f;

    /**
     * Coefficient for the electrical susceptibility calculation
     */
    public final static double COEF = 6.02e23 * 2.81794e-15 / 2 / Math.PI;

    public XrayCrystall() {
        d = 1.92e-10;
        den = 2000;
        theta = 0;
        L = 0.001;
        M = 0.02809;
        this.f = new Complex(-14.321, 0.494);
    }

    /**
     * Returning real part of the scattering factor
     *
     * @return
     */
    public double getF1() {
        return f.getReal();
    }

    /**
     * Setting real part of the scattering factor
     *
     * @param f1
     */
    public void setF1(double f1) {
        f = new Complex(-f1, f.getImaginary());
    }

    /**
     * Returning imaginary part of the scattering factor
     *
     * @return
     */
    public double getF2() {
        return f.getImaginary();
    }

    /**
     * Setting imaginary part of the scattering factor
     *
     * @param f2
     */
    public void setF2(double f2) {
        f = new Complex(f.getReal(), f2);
    }

    /**
     * Returning inter-plane distance of the crystal
     *
     * @return
     */
    public double getD() {
        return d;
    }

    /**
     * Setting inter-plane distance of the crystal
     *
     * @param d
     */
    public void setD(double d) {
        this.d = d;
    }

    /**
     * Returning density of the crystal
     *
     * @return
     */
    public double getDen() {
        return den;
    }

    /**
     * Setting density of the crystal
     *
     * @param den
     */
    public void setDen(double den) {
        this.den = den;
    }

    /**
     * Returning cutting angle of the crystal
     *
     * @return
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Setting cutting angle of the crystal
     *
     * @param theta
     */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * Returning thickness of the crystal
     *
     * @return
     */
    public double getThickness() {
        return L;
    }

    /**
     * Setting thickness of the crystal
     *
     * @param L
     */
    public void setL(double L) {
        this.L = L;
    }

    /**
     * Returning the diffraction angle
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public double getDiffAngle(double phi, double wl) {
        return Math.asin(Math.sin(phi) + wl / d * Math.sin(theta));
    }

    /**
     * Returning the amplitude Bragg reflectivity coefficient
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public Complex getReflectivity(double phi, double wl) {
        Complex K1, K2, ex, R0, R, g, B, eps0, omega1, omega2, sq;
        double qplus, qminus, rq, q, k2;
        B = f.multiply(COEF * wl * wl * den / M);
        eps0 = B.add(1);
        k2 = Math.pow(2 * Math.PI / wl, 2);
        q = Math.PI / d * Math.cos(theta);
        sphi = Math.sin(phi);
        cphi = Math.sqrt(1 - sphi * sphi);
        sphi1 = sphi + wl / d * Math.sin(theta);
        cphi1 = Math.sqrt(1 - sphi1 * sphi1);
        qplus = 2 * q * cphi / (cphi + cphi1);
        qminus = 2 * q * cphi1 / (cphi + cphi1);
        rq = Math.sqrt(qplus / qminus);
        omega1 = eps0.subtract(sphi * sphi).multiply(k2 / qplus / 2).subtract(qplus / 2);
        omega2 = eps0.subtract(sphi1 * sphi1).multiply(k2 / qminus / 2).subtract(qminus / 2);
        g = omega1.add(omega2).divide(k2).multiply(Math.sqrt(qplus * qminus / 2)).divide(B);
        sq = Complex.ONE.subtract(g.pow(2)).sqrt();
        R0 = Complex.I.multiply(sq).subtract(g);
        ex = B.multiply(sq).multiply(2 * k2 / Math.sqrt(qplus * qminus)).multiply(-L).exp();
        if (ex.isNaN() || ex.isInfinite()) {
            ex = new Complex(0);
        }
        K2 = R0.multiply(rq);
        K1 = R0.divide(rq);
        R=ex.subtract(1).divide(ex.multiply(K1).multiply(K2).subtract(1)).
                multiply(K2);
        if (R.abs() > 1) {
            sq = sq.negate();
            //R0 = Complex.I.multiply(sq).subtract(g);
            ex = B.multiply(sq).multiply(2 * k2 / Math.sqrt(qplus * qminus)).multiply(-L).exp();
            if (ex.isNaN() || ex.isInfinite()) {
                ex = new Complex(0);
            }
            //K2 = R0.multiply(rq);
            //K1 = R0.divide(rq);
            R=ex.subtract(1).divide(ex.multiply(K1).multiply(K2).subtract(1)).
            multiply(K2);
        } 
        
        return R;
    }

    /**
     * Returning the intensity Bragg reflectivity coefficient
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getIReflectivity(double phi, double wl) {
        Complex r = getReflectivity(phi, wl);
        return r.conjugate().multiply(r).getReal() * cphi1 / cphi;
    }
}
