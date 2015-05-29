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

    private double d, den, theta;
    private Complex f;

    public XrayCrystall() {
        this.f = new Complex(0, 0);
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
        f = new Complex(f1, f.getImaginary());
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
     * Returning inter-plane distance of the scattering factor
     *
     * @return
     */
    public double getD() {
        return d;
    }

    /**
     * Setting inter-plane distance of the scattering factor
     *
     * @param d
     */
    public void setD(double d) {
        this.d = d;
    }

    /**
     * Returning density of the scattering factor
     *
     * @return
     */
    public double getDen() {
        return den;
    }

    /**
     * Setting density of the scattering factor
     *
     * @param den
     */
    public void setDen(double den) {
        this.den = den;
    }

    /**
     * Returning cutting angle of the scattering factor
     *
     * @return
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Setting cutting angle of the scattering factor
     *
     * @param theta
     */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * Returning diffraction angle
     *
     * @param phi incidence angle
     * @param wl wavelength
     * @return
     */
    public double getDiffAngle(double phi, double wl) {
        return Math.asin(Math.sin(phi) + wl / d * Math.sin(theta));
    }
}
