/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package gov.nasa.gsfc.seadas;

import org.esa.beam.framework.ui.application.ApplicationDescriptor;
import org.esa.beam.visat.VisatApp;

/**
 * The <code>SeadasApp</code> class represents the SeaDAS UI application.
 *
 * @author Don
 */
public class SeadasApp extends VisatApp {

    /**
     * Constructs the SeaDAS UI application instance. The constructor does not start the application nor does it perform any GUI
     * work.
     *
     * @param applicationDescriptor The application descriptor.
     */
    public SeadasApp(ApplicationDescriptor applicationDescriptor) {
        super(applicationDescriptor);
        // SystemUtils.BEAM_HOME_PAGE = "http://seadas.gsfc.nasa.gov/";
    }

}
