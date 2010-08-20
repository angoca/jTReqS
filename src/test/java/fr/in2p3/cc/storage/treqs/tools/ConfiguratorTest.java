package fr.in2p3.cc.storage.treqs.tools;

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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * TReqSConfigTest.cpp
 *
 * @version 2010-07-27
 * @author gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public class ConfiguratorTest {

    @After
    public void tearDown() {
        Configurator.destroyInstance();
    }

    /**
     * Tests to get, then set a value and retrieve it.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void tes01setValue() throws TReqSException {
        String sec = "test1";
        String key = "keytest";
        String value = "valuetest";

        boolean failed = false;
        try {
            Configurator.getInstance().getValue(sec, key);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ConfigNotFoundException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }

        Configurator.getInstance().setValue(sec, key, value);

        String actual = Configurator.getInstance().getValue(sec, key);

        String expected = value;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to set a value and retrieve the same.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void tes02setValue() throws TReqSException {
        String sec = "test1";
        String key = "keytest";
        String value = "valuetest";

        Configurator.getInstance().setValue(sec, key, value);

        String actual = Configurator.getInstance().getValue(sec, key);

        String expected = value;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to delete an inexistant value.
     */
    @Test
    public void test01DeleteValue() {
        String sec = "test1";
        String key = "keytest";
        Configurator.getInstance().deleteValue(sec, key);
    }

    /**
     * Tests the configuration file by default.
     */
    @Test
    public void test01getFileName() {
        String actual = Configurator.getInstance().getConfFilename();

        String expected = "treqs.conf.properties";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test01getValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().getValue(null, "KEY");
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
     * Tests an inexistant value from the default properties.
     *
     * @throws ConfigNotFoundException
     */
    @Test
    public void test01loadDefaultsInexistant() throws ConfigNotFoundException {
        boolean failed = false;
        try {
            Configurator.getInstance().getValue("UNKNOWN", "KEY");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ConfigNotFoundException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test01readFile() throws TReqSException {
        boolean failed = false;
        try {
            Configurator.getInstance().setConfFilename("INEXISTANT_FILE");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ProblematicConfiguationFileException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set an invalid configuration file.
     */
    @Test
    public void test01setFileName() {
        boolean failed = false;
        try {
            Configurator.getInstance().setConfFilename(null);
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
     * Tests to delete an existent value.
     *
     * @throws ProblematicConfiguationFileException
     *             Never
     */
    @Test
    public void test02DeleteValue() throws ProblematicConfiguationFileException {
        String sec = "test1";
        String key = "keytest";
        String value = "valuetest";
        Configurator.getInstance().setValue(sec, key, value);
        Configurator.getInstance().deleteValue(sec, key);
    }

    @Test
    public void test02getValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().getValue("", "KEY");
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
     * Tests to set an invalid configuration file.
     */
    @Test
    public void test02setFileName() {
        boolean failed = false;
        try {
            Configurator.getInstance().setConfFilename("");
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

    @Test
    public void test03deleteValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue(null, "KEY");
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

    @Test
    public void test03getValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().getValue("SEC", null);
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

    @Test
    public void test03setFileName() throws ProblematicConfiguationFileException {
        Configurator.getInstance().setConfFilename("treqs.conf.properties");
    }

    @Test
    public void test03setValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue(null, "KEY", "VALUE");
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

    @Test
    public void test04deleteValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue("", "KEY");
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

    @Test
    public void test04getValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().getValue("SEC", "");
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

    @Test
    public void test04setValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("", "KEY", "VALUE");
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

    @Test
    public void test05deleteValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue("SEC", null);
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

    @Test
    public void test05setValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", null, "VALUE");
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

    @Test
    public void test06deleteValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue("SEC", "");
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

    @Test
    public void test06setValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", "", "VALUE");
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

    @Test
    public void test07setValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", "KEY", null);
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

    @Test
    public void test08setValue() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", "KEY", "");
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

}
