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
package fr.in2p3.cc.storage.treqs.hsm.hpssJNI;

/**
 * Definition of the HPSS Error codes.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public enum HPSSErrorCode {
    /**
     * Initialization OK.
     */
    HPSS_E_NOERROR((byte) 0),
    /**
     * (hpss_Open) One of the following conditions occurred:
     * <ul>
     * <li>Search permission is denied on a component of the path prefix.</li>
     * <li>The file exists and the permissions specified by <code>Oflag</code>
     * are denied.</li>
     * <li>The file does not exist and write permission is denied for the parent
     * directory of the file to be created.</li>
     * <li>O_TRUNC is specified and write permission is denied.</li>
     * </ul>
     * <p>
     * (hpss_FileGetXAttributes) Search permission is denied for a component of
     * the path prefix.
     */
    HPSS_EACCES((byte) -13),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EAGAIN((byte) -11),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EBADF((byte) -9),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EBADMSG((byte) -77),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EBUSY((byte) -16),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ECHILD((byte) -10),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ECONN((byte) -50),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EDEADLK((byte) -45),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EDOM((byte) -33),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EDQUOT((byte) -88),
    /**
     * (hpss_Open) O_CREAT and O_EXCL are set and the named file exists.
     */
    HPSS_EEXIST((byte) -17),
    /**
     * (hpss_Open) The Path parameter is NULL.
     * <p>
     * (hpss_FileGetXAttributes) The Path or AttrOut parameter is NULL.
     */
    HPSS_EFAULT((byte) -14),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EFBIG((byte) -27),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EINTR((byte) -4),
    /**
     * (hpss_Open) Oflag is not valid, or one or more values input in the
     * HintsIn parameter is invalid.
     * <p>
     * (hpss_FileGetXAttributes) BitfileID is NULL.
     */
    HPSS_EINVAL((byte) -22),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     * <p>
     * This error appears when the HSM is not available, or the file is locked,
     * thus it cannot be read.
     */
    HPSS_EIO((byte) -5),
    /**
     * (hpss_Open) The named file is a directory. Note that opening directories
     * via hpss_Open is not supported in any mode.
     */
    HPSS_EISDIR((byte) -21),
    /**
     * (hpss_Open) The client open file table is already full.
     */
    HPSS_EMFILE((byte) -24),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EMLINK((byte) -31),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EMSGSIZE((byte) -59),
    /**
     * (hpss_Open) The length of the Path string exceeds the system-imposed path
     * name limit or a path name component exceeds the system-imposed limit.
     * <p>
     * (hpss_FileGetXAttributes) The length of the Path argument exceeds the
     * system-imposed limit, or a component of the path name exceeds the
     * system-imposed limit.
     */
    HPSS_ENAMETOOLONG((byte) -86),
    /**
     * (hpss_Open) Too many files are open in the system.
     */
    HPSS_ENFILE((byte) -23),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ENODEV((byte) -19),
    /**
     * (hpss_Open) The named file does not exist and the O_CREAT flag was not
     * specified, or the Path argument points to an empty string.
     * <p>
     * (hpss_FileGetXAttributes) The named file does not exist, or the Path
     * argument points to an empty string.
     */
    HPSS_ENOENT((byte) -2),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ENOLCK((byte) -46),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ENOLINK((byte) -67),
    /**
     * Memory could not be allocated for the new path name.
     */
    HPSS_ENOMEM((byte) -12),
    /**
     * This error occurs when there is not space in the cache disk.
     */
    HPSS_ENOSPACE((byte) -28),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ENOSR((byte) -63),
    /**
     * (hpss_Open) A component of the Path prefix is not a directory.
     * <p>
     * (hpss_FileGetXAttributes) A component of the Path prefix is not a
     * directory.
     */
    HPSS_ENOTDIR((byte) -20),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ENOTEMPTY((byte) -87),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ENXIO((byte) -6),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EOPNOTSUPP((byte) -64),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EPERM((byte) -1),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EPIPE((byte) -32),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ERANGE((byte) -34),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ESTALE((byte) -52),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_ETIMEDOUT((byte) -78),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EWRPROTECT((byte) -47),
    /**
     * ????. This error is unknown when using the application, and probably it
     * never occurs.
     */
    HPSS_EXDEV((byte) -18);

    /**
     * Id of the error code.
     */
    private byte code = 0;

    /**
     * Constructor with the id of the error.
     *
     * @param errorCode
     *            Error code.
     */
    private HPSSErrorCode(final byte errorCode) {
        this.code = errorCode;
    }

    /**
     * Retrieves the same number of the HPSS's error code.
     *
     * @return The id of the error code.
     */
    public byte getCode() {
        return this.code;
    }
}
