/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of DLCM.
 *
 *  DLCM is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DLCM is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DLCM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* StructureListener.java
 *
 * Used to allow other classes to be notified if a structure is added, removed,
 * or changed
 */

package dlcm;


/**
 *
 * @author David McCoy
 *
 * Structure Change callback functions
 */
public interface StructureListener {

    /**
     * Notification that the structure is about to change
     */
    public void structureChanging();

    /**
     * Notification that the structure has changed
     */
    public void structureChanged();

}
