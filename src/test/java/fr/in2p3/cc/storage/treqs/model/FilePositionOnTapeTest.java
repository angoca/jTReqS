package fr.in2p3.cc.storage.treqs.model;

/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
 *                  CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
 *
 * This software is a computer program whose purpose is to schedule, sort
 * and submit file requests to the hierarchical storage system HPSS.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 *
 */

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.NullParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * FilePositionOnTapeTest.cpp
 * 
 * @version Nov 10, 2009
 * @author gomez
 */
public class FilePositionOnTapeTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilePositionOnTapeTest.class);
    private File file = null;
    private String fileName = "filename";
    private Tape tape = null;
    private String tapeName = "tapename";

    @After
    public void tearDown() {
        Configurator.destroyInstance();
    }

    public FilePositionOnTapeTest() {
        this.file = new File(fileName, new User("username"), 400);
        this.tape = new Tape(tapeName, new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
    }

    /**
     * Tests a fpot with a null file.
     */
    @Test
    public void test01Constructor() {
        int position = 100;
        Calendar timestamp = new GregorianCalendar(2010, 07, 01, 10, 30, 05);

        boolean failed = false;
        try {
            new FilePositionOnTape(null, timestamp, position, tape);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests a valid timestamp for metadata.
     * 
     * @throws ProblematicConfiguationFileException
     * @throws NullParameterException
     *             Never.
     */
    @Test
    public void test01MetadataNotOutdated()
            throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue("MAIN", "MAX_METADATA_AGE", "100");

        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 100, tape);
        fpot.setMetadataTimestamp(new GregorianCalendar());

        boolean outdated = fpot.isMetadataOutdated();

        Assert.assertTrue("No outdated metadata", !outdated);
    }

    /**
     * Tests the method setTapeRef giving a null value.
     * 
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     * @throws NullParameterException
     *             Never
     */
    @Test
    public void test01nullTapeRef() throws ProblematicConfiguationFileException {
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 100, tape);

        boolean failed = false;
        try {
            fpot.setTape(null);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests the toString.
     * 
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    @Test
    public void test01toString() throws ProblematicConfiguationFileException {
        int position = 100;
        Calendar timestamp = new GregorianCalendar(2010, 07, 01, 10, 30, 05);
        FilePositionOnTape fpot = new FilePositionOnTape(file, timestamp,
                position, tape);

        String actual = fpot.toString();

        String expected = "FilePositionOnTape{ MAX_METADATA_AGE: "
                + FilePositionOnTape.MAX_METADATA_AGE + ", file: " + fileName
                + ", metadataAge: " + timestamp.getTimeInMillis()
                + ", position: " + position + ", tape: " + tapeName + "}";

        Assert.assertEquals("toString", expected, actual);
    }

    /**
     * Tests a fpot with a null timestamp.
     */
    @Test
    public void test02Constructor() {
        int position = 100;

        boolean failed = false;
        try {
            new FilePositionOnTape(file, null, position, tape);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Test an outdated timestamp for metadata.
     * 
     * @throws InterruptedException
     *             Never.
     * @throws ProblematicConfiguationFileException
     */
    @Test
    public void test02MetadataOutdated() throws InterruptedException,
            ProblematicConfiguationFileException {
        Configurator.getInstance().setValue("MAIN", "MAX_METADATA_AGE", "1");

        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 100, tape);
        fpot.setMetadataTimestamp(new GregorianCalendar());
        LOGGER.info("Sleeping thread for 2 seconds");
        Thread.sleep(2000);

        boolean outdated = fpot.isMetadataOutdated();

        Assert.assertTrue("Outdated metadata", outdated);

    }

    /**
     * Tests the method setFileRef giving a null value.
     * 
     * @throws ProblematicConfiguationFileException
     *             Never
     */
    @Test
    public void test02nullFileRef() throws ProblematicConfiguationFileException {
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 100, tape);

        boolean failed = false;
        try {
            fpot.setFile(null);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests a fpot with a negative position.
     */
    @Test
    public void test03Constructor() {
        int position = -100;
        Calendar timestamp = new GregorianCalendar(2010, 07, 01, 10, 30, 05);

        boolean failed = false;
        try {
            new FilePositionOnTape(file, timestamp, position, tape);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests a fpot witha null tape.
     */
    @Test
    public void test04Constructor() {
        int position = 100;
        Calendar timestamp = new GregorianCalendar(2010, 07, 01, 10, 30, 05);

        boolean failed = false;
        try {
            new FilePositionOnTape(file, timestamp, position, null);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests no configuration value.
     * 
     * @throws ConfigNotFoundException
     *             Never.
     * @throws ProblematicConfiguationFileException
     */
    @Test
    public void test05Constructor() throws ConfigNotFoundException,
            ProblematicConfiguationFileException {
        Configurator.getInstance().deleteValue("MAIN", "MAX_METADATA_AGE");

        int position = 100;
        Calendar timestamp = new GregorianCalendar(2010, 07, 01, 10, 30, 05);

        new FilePositionOnTape(file, timestamp, position, tape);
    }
}
