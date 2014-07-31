/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2;

import java.awt.geom.Point2D;

public class StraightLine {

    private Double m_dm;        // slope (gradient)
    private Double m_dc;        // y-intercept
    private Double m_dx;        // x-intercept (if y-intercept == NaN)
    private Double m_dAlpha;    // alpha (-180 < alpha <= 180)

    public StraightLine(Double dm, Double dc, boolean bRevert) {
    	this.m_dm = dm;
    	this.m_dc = dc;
    	this.m_dx = Double.NaN;
        if (this.m_dm.isNaN()) {
            this.m_dAlpha = Double.NaN;
        } else if (this.m_dm.isInfinite()) {
            this.m_dAlpha = CheckAlpha((Double.NEGATIVE_INFINITY == this.m_dm) ? -90.0 : 90.0);
        } else {
            this.m_dAlpha = CheckAlpha(Math.atan(dm) * 180 / Math.PI + (bRevert ? 180 : 0) );
        }
    }
    
    public StraightLine(Point2D.Double oP1, Point2D.Double oP2) {
        this.m_dm = (oP2.getY() - oP1.getY()) / (oP2.getX() - oP1.getX());
        this.m_dc = oP1.getY() - (m_dm * oP1.getX());

        if (this.m_dm.isInfinite() || this.m_dm.isNaN()) {
            this.m_dx = oP1.getX();
            this.m_dAlpha = CheckAlpha(oP1.getY() > oP2.getY() ? -90.0 : 90.0);
        } else {
            this.m_dx = Double.NaN;
            this.m_dAlpha = CheckAlpha(Math.atan((oP2.getY() - oP1.getY()) / (oP2.getX() - oP1.getX())) * 180 / Math.PI + (oP1.getX() > oP2.getX() ? 180 : 0));
        }
    }
    
    static private Double CheckAlpha(Double dAlpha) {
        if (dAlpha > 180) {
            return dAlpha - 360;
        }
        if (dAlpha <= -180) {
            return dAlpha + 360;
        }
        return dAlpha;
    }
    
    public Double getM() {
        return m_dm;
    }
    public Double getC() {
        return m_dc;
    }
    public Double getX() {
        return m_dx;
    }
    public Double getAlpha() {
        return m_dAlpha;
    }
    public boolean IsLine() {
        return !(m_dx.isNaN() || m_dx.isInfinite() || m_dAlpha.isNaN() || m_dAlpha.isInfinite())
                || !(m_dm.isNaN() || m_dm.isInfinite() || m_dc.isNaN() || m_dc.isInfinite());
    }

    public Point2D.Double GetIntersectionPoint(StraightLine oLine) {
        Double dx;
        Double dy;
        
        if (!this.IsLine() || !oLine.IsLine()) {
            // data missing
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        if (m_dm.isInfinite() || this.m_dm.isNaN()) {
            if (oLine.getM().isInfinite() || oLine.getM().isNaN()) {
                // no IntersectionPoint
                return new Point2D.Double(Double.NaN, Double.NaN);
            } else {
                dx = this.m_dx;
                dy = oLine.getM() * dx + oLine.getC();
            }
        } else if (oLine.getM().isInfinite() || oLine.getM().isNaN()) {
            dx = oLine.getX();
            dy = this.m_dm * dx + this.m_dc;
        } else {
            dx = (oLine.m_dc - this.m_dc) / (this.m_dm - oLine.m_dm);
            dy = this.m_dm * dx + this.m_dc;
        }
        
        return new Point2D.Double(dx, dy);
    }

}
