/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors   Andres Gomez,
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
package fr.in2p3.cc.storage.treqs.model;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Tests for FilePositionOnTape.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class FilePositionOnTapeTest {
    /**
     * Number one hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilePositionOnTapeTest.class);
    /**
     * File.
     */
    private final File file;
    /**
     * Filename.
     */
    private final String fileName;
    /**
     * Tape.
     */
    private final Tape tape;
    /**
     * Tapename.
     */
    private final String tapeName;

    /**
     * Initializes the objects.
     */
    public FilePositionOnTapeTest() {
        fileName = "filename";
        tapeName = "tapename";
        this.file = new File(fileName, 400);
        this.tape = new Tape(tapeName, new MediaType((byte) 1, "media"));
    }

    /**
     * Destroys all.
     */
    @After
    public void tearDown() {
        Configurator.destroyInstance();
    }

    /**
     * Tests a fpot with a null file.
     */
    @Test
    public void testConstructor01() {
        int position = FilePositionOnTapeTest.HUNDRED;
        User user = new User("username");

        boolean failed = false;
        try {
            new FilePositionOnTape(null, position, tape, user);
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
    public void testConstructor02() {
        int position = -FilePositionOnTapeTest.HUNDRED;
        User user = new User("username");

        boolean failed = false;
        try {
            new FilePositionOnTape(file, position, tape, user);
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
    public void testConstructor03() {
        int position = FilePositionOnTapeTest.HUNDRED;
        User user = new User("username");

        boolean failed = false;
        try {
            new FilePositionOnTape(file, position, null, user);
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
     * Tests a fpot with a null user.
     */
    @Test
    public void testConstructor04() {
        int position = FilePositionOnTapeTest.HUNDRED;
        User user = null;

        boolean failed = false;
        try {
            new FilePositionOnTape(file, position, tape, user);
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
     * @throws KeyNotFoundException
     *             Never.
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testConstructor05() throws KeyNotFoundException,
            ProblematicConfiguationFileException {
        Configurator.getInstance().deleteValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE);

        int position = FilePositionOnTapeTest.HUNDRED;

        new FilePositionOnTape(file, position, tape, new User("username"));
    }

    /**
     * Tests a valid timestamp for metadata.
     *
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testMetadataNotOutdated01()
            throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, "100");

        FilePositionOnTape fpot = new FilePositionOnTape(file,
                FilePositionOnTapeTest.HUNDRED, tape, new User("username"));

        boolean outdated = fpot.isMetadataOutdated();

        Assert.assertTrue("No outdated metadata", !outdated);
    }

    /**
     * Test an outdated timestamp for metadata.
     *
     * @throws InterruptedException
     *             Never.
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testMetadataOutdated02() throws InterruptedException,
            ProblematicConfiguationFileException {
        Configurator.getInstance().setValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, "1");

        FilePositionOnTape fpot = new FilePositionOnTape(file,
                FilePositionOnTapeTest.HUNDRED, tape, new User("username"));
        LOGGER.info("Sleeping thread for 2 seconds");
        Thread.sleep(2000);

        boolean outdated = fpot.isMetadataOutdated();

        Assert.assertTrue("Outdated metadata", outdated);

    }

    /**
     * Tests the toString.
     *
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testToString01() throws ProblematicConfiguationFileException {
        int position = FilePositionOnTapeTest.HUNDRED;
        String username = "username";
        User user = new User(username);

        FilePositionOnTape fpot = new FilePositionOnTape(file, position, tape,
                user);

        String actual = fpot.toString();

        String expectedPrefix = "FilePositionOnTape{ "
                + Constants.MAX_METADATA_AGE + ": "
                + DefaultProperties.MAX_METADATA_AGE + ", file: " + fileName
                + ", metadataAge: ";
        String expectedSuffix = ", position: " + position + ", requester: "
                + username + ", tape: " + tapeName + "}";

        Assert.assertTrue("toString prefix", actual.startsWith(expectedPrefix));
        Assert.assertTrue("toString sufix", actual.endsWith(expectedSuffix));
    }
}
