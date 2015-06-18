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
public class XrayCrystal {

    private double d, den, theta, L, M;
    private Complex f;

    /**
     * Coefficient for the electrical susceptibility calculation
     */
    public final static double COEF = 6.02e23 * 2.81794e-15 / Math.PI;

    public XrayCrystal() {
        d = 1.92e-10;
        den = 2000;
        theta = 0;
        L = 0.00025;
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
    public double getL() {
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
     * Returning molar mass of the crystal material
     *
     * @return
     */
    public double getM() {
        return M;
    }

    /**
     * Setting molar mass of the crystal material
     *
     * @param M
     */
    public void setM(double M) {
        this.M = M;
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
     * Returning the sinus of diffraction angle
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public double getDiffAngleSin(double phi, double wl) {
        return Math.sin(phi) + wl / d * Math.sin(theta);
    }

    /**
     * Returning the amplitude Bragg reflectivity coefficient
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public Complex getBraggReflectivity(double phi, double wl) {
        Complex K1, K2, ex, R0, g, B, eps0, omega1, omega2, sq;
        double qplus, qminus, rq, q, k2, sphi, sphi1, cphi, cphi1;
        B = f.multiply(COEF * wl * wl * den / M);
        eps0 = B.add(1);
        k2 = Math.pow(2 * Math.PI / wl, 2);
        q = Math.PI / d * Math.cos(theta);
        sphi = Math.sin(phi);
        cphi = Math.cos(phi);
        sphi1 = getDiffAngleSin(phi, wl);
        cphi1 = Math.sqrt(1 - sphi1 * sphi1);
        qplus = 2 * q * cphi / (cphi + cphi1);
        qminus = 2 * q * cphi1 / (cphi + cphi1);
        rq = Math.sqrt(qplus / qminus);
        omega1 = eps0.subtract(sphi * sphi).multiply(k2 / qplus / 2).subtract(qplus / 2);
        omega2 = eps0.subtract(sphi1 * sphi1).multiply(k2 / qminus / 2).subtract(qminus / 2);
        g = omega1.add(omega2).multiply(Math.sqrt(qplus * qminus) / k2).divide(B);
        sq = Complex.ONE.subtract(g.pow(2)).sqrt();
        R0 = Complex.I.multiply(sq).subtract(g);
        K2 = R0.multiply(rq);
        K1 = R0.divide(rq);
        ex = B.multiply(sq).multiply(k2 / Math.sqrt(qplus * qminus)).multiply(-L).exp();
        if (ex.isNaN()) {
            return K2;
        }
        if (ex.isInfinite()) {
            return K1.reciprocal();
        }
        return ex.subtract(1).divide(ex.subtract(K1.multiply(K2).reciprocal())).divide(K1);
    }

    /**
     * Returning the intensity Bragg reflectivity coefficient
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getBraggIReflectivity(double phi, double wl) {
        Complex r = getBraggReflectivity(phi, wl);
        return r.conjugate().multiply(r).getReal() * Math.cos(getDiffAngle(phi, wl)) / Math.cos(phi);
    }

    /**
     * Returning the amplitude Bragg transmittivity coefficient
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public Complex getBraggTransmittivity(double phi, double wl) {
        Complex K1, K2, ex, ex1, R0, g, B, eps0, omega1, omega2, sq, rec;
        double qplus, qminus, rq, q, k2, sphi, sphi1, cphi, cphi1;
        B = f.multiply(COEF * wl * wl * den / M);
        eps0 = B.add(1);
        k2 = Math.pow(2 * Math.PI / wl, 2);
        q = Math.PI / d * Math.cos(theta);
        sphi = Math.sin(phi);
        cphi = Math.cos(phi);
        sphi1 = getDiffAngleSin(phi, wl);
        cphi1 = Math.sqrt(1 - sphi1 * sphi1);
        qplus = 2 * q * cphi / (cphi + cphi1);
        qminus = 2 * q * cphi1 / (cphi + cphi1);
        rq = Math.sqrt(qplus / qminus);
        omega1 = eps0.subtract(sphi * sphi).multiply(k2 / qplus / 2).subtract(qplus / 2);
        omega2 = eps0.subtract(sphi1 * sphi1).multiply(k2 / qminus / 2).subtract(qminus / 2);
        g = omega1.add(omega2).multiply(Math.sqrt(qplus * qminus) / k2).divide(B);
        sq = Complex.ONE.subtract(g.pow(2)).sqrt();
        R0 = Complex.I.multiply(sq).subtract(g);
        K2 = R0.multiply(rq);
        K1 = R0.divide(rq);
        ex = B.multiply(sq).multiply(k2 / Math.sqrt(qplus * qminus)).multiply(-L).exp();
        ex1 = B.multiply(sq).multiply(k2 / Math.sqrt(qplus * qminus)).
                subtract(Complex.I.multiply(omega1.subtract(omega2))).multiply(-L / 2).exp();
        if (ex.isNaN()) {
            return K2.multiply(K1).subtract(1).multiply(ex1).negate();
        }
        if (ex.isInfinite() || ex1.isNaN()) {
            return Complex.ZERO;
        }
        rec = K1.multiply(K2).reciprocal();
        return Complex.ONE.subtract(rec).multiply(ex1).divide(ex.subtract(rec));
    }

    /**
     * Returning the intensity Bragg transmittivity coefficient
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getBraggITransmittivity(double phi, double wl) {
        Complex t = getBraggTransmittivity(phi, wl);
        return t.conjugate().multiply(t).getReal();
    }

    /**
     * Returning the width of the Bragg reflection band in wavelength units
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getWavelengthWidth(double phi, double wl) {
        Complex B = f.multiply(COEF * wl * wl * den / M);
        double c = Math.pow(2 * d / wl, 2);
        double sn = Math.sin(theta);
        return c * B.abs() * wl / Math.sqrt(Math.abs(1 - c * Math.pow(sn, 2)))
                * Math.abs(Math.cos(theta) - Math.sqrt(c - 1) * sn);
    }

    /**
     * Returning the width of the Bragg reflection band in angle units
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getAngleWidth(double phi, double wl) {
        return getWavelengthWidth(phi, wl) / wl / Math.sqrt(Math.pow(2 * d / wl, 2));
    }

    /**
     * Returning the amplitude Laue reflectivity coefficient
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public Complex getLaueReflectivity(double phi, double wl) {
        Complex K1, K2, K1K2, ex, ex1, R0, g, B, eps0, omega1, omega2, sq;
        double qplus, qminus, rq, q, k2, sphi, sphi1, cphi, cphi1;
        B = f.multiply(COEF * wl * wl * den / M);
        eps0 = B.add(1);
        k2 = Math.pow(2 * Math.PI / wl, 2);
        q = Math.PI / d * Math.cos(theta);
        sphi = Math.sin(phi);
        cphi = Math.cos(phi);
        sphi1 = getDiffAngleSin(phi, wl);
        cphi1 = -Math.sqrt(1 - sphi1 * sphi1);
        qplus = 2 * q * cphi / (cphi + cphi1);
        qminus = 2 * q * cphi1 / (cphi + cphi1);
        rq = Math.sqrt(-qplus / qminus);
        omega1 = eps0.subtract(sphi * sphi).multiply(k2 / qplus / 2).subtract(qplus / 2);
        omega2 = eps0.subtract(sphi1 * sphi1).multiply(k2 / qminus / 2).subtract(qminus / 2);
        g = omega1.add(omega2).multiply(Math.sqrt(-qplus * qminus) / k2).divide(B);
        sq = Complex.ONE.add(g.pow(2)).sqrt();
        R0 = sq.add(g);
        K2 = R0.multiply(-rq);
        K1 = R0.divide(rq);
        ex = Complex.I.multiply(B).multiply(sq).multiply(k2 / Math.sqrt(-qplus * qminus)).multiply(L).exp();
        ex1 = B.multiply(sq).multiply(k2 / Math.sqrt(-qplus * qminus)).
                subtract(omega1.subtract(omega2)).multiply(Complex.I).multiply(-L / 2).exp();
        K1K2=K1.multiply(K2);
        if (ex.isNaN()) {
            return ex1.divide(K1K2.subtract(1)).multiply(K2).negate();
        }
        if (ex1.isNaN()) {
            return Complex.ZERO;
        }
        return ex.subtract(1).divide(K1K2.subtract(1)).multiply(K2).multiply(ex1);
    }
    
    /**
     * Returning the intensity Laue reflectivity coefficient
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getLaueIReflectivity(double phi, double wl) {
        Complex r = getLaueReflectivity(phi, wl);
        return r.conjugate().multiply(r).getReal() * Math.cos(getDiffAngle(phi, wl)) / Math.cos(phi);
    }
    
    /**
     * Returning the amplitude Laue transmittivity coefficient
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public Complex getLaueTransmittivity(double phi, double wl) {
        Complex K1, K2, K1K2, ex, ex1, R0, g, B, eps0, omega1, omega2, sq;
        double qplus, qminus, rq, q, k2, sphi, sphi1, cphi, cphi1;
        B = f.multiply(COEF * wl * wl * den / M);
        eps0 = B.add(1);
        k2 = Math.pow(2 * Math.PI / wl, 2);
        q = Math.PI / d * Math.cos(theta);
        sphi = Math.sin(phi);
        cphi = Math.cos(phi);
        sphi1 = getDiffAngleSin(phi, wl);
        cphi1 = -Math.sqrt(1 - sphi1 * sphi1);
        qplus = 2 * q * cphi / (cphi + cphi1);
        qminus = 2 * q * cphi1 / (cphi + cphi1);
        rq = Math.sqrt(-qplus / qminus);
        omega1 = eps0.subtract(sphi * sphi).multiply(k2 / qplus / 2).subtract(qplus / 2);
        omega2 = eps0.subtract(sphi1 * sphi1).multiply(k2 / qminus / 2).subtract(qminus / 2);
        g = omega1.add(omega2).multiply(Math.sqrt(-qplus * qminus) / k2).divide(B);
        sq = Complex.ONE.add(g.pow(2)).sqrt();
        R0 = sq.add(g);
        K2 = R0.multiply(-rq);
        K1 = R0.divide(rq);
        ex = Complex.I.multiply(B).multiply(sq).multiply(k2 / Math.sqrt(-qplus * qminus)).multiply(L).exp();
        ex1 = B.multiply(sq).multiply(k2 / Math.sqrt(-qplus * qminus)).
                subtract(omega1.subtract(omega2)).multiply(Complex.I).multiply(-L / 2).exp();
        K1K2=K1.multiply(K2);
        if (ex.isNaN()) {
            return ex1.divide(K1K2.subtract(1)).negate();
        }
        if (ex1.isNaN()) {
            return Complex.ZERO;
        }   
        return ex.multiply(K1K2).subtract(1).divide(K1K2.subtract(1)).multiply(ex1);
    }
    
    /**
     * Returning the intensity Laue transmittivity coefficient
     *
     * @param phi
     * @param wl
     * @return
     */
    public double getLaueITransmittivity(double phi, double wl) {
        Complex t = getLaueTransmittivity(phi, wl);
        return t.conjugate().multiply(t).getReal();
    }
    
    /**
     * Return Shadow rays modified by passing through the crystal
     * @param oldRay
     * @param sPol modify s-polarization
     * @param pPol modify p-polarization
     * @return
     */
    public double[] rayConversion (double [] oldRay, Boolean sPol, Boolean pPol) {
        return oldRay;
    }
}
