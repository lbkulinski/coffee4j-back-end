package com.coffee4j.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import schema.generated.tables.Vessel;

/**
 * The REST controller used to interact with the Coffee4j vessel data.
 *
 * @author Logan Kulinski, lbkulinski@gmail.com
 * @version May 20, 2022
 */
@RestController
@RequestMapping("/api/vessel")
public final class VesselController {
    /**
     * The {@code vessel} table of the {@link VesselController} class.
     */
    private static final Vessel VESSEL;

    /**
     * The {@link Logger} of the {@link VesselController} class.
     */
    private static final Logger LOGGER;

    static {
        VESSEL = Vessel.VESSEL;

        LOGGER = LogManager.getLogger();
    } //static
}