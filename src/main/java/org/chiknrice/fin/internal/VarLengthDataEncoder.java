/*
 * Copyright (c) 2015 Ian Bondoc
 *
 * This file is part of Jen4Fin
 *
 * Jen4Fin is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * Jen4Fin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package org.chiknrice.fin.internal;

import java.nio.ByteBuffer;

/**
 * Base class for variable length data element encoders.  This defines the standard flow of encoding the data and the
 * length prefix.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
abstract class VarLengthDataEncoder<T> implements Encoder<T> {

    final int varLengthPrefixDigits;
    final Encoding varLengthPrefixEncoding;

    VarLengthDataEncoder(int varLengthPrefixDigits, Encoding varLengthPrefixEncoding) {
        this.varLengthPrefixDigits = varLengthPrefixDigits;
        this.varLengthPrefixEncoding = varLengthPrefixEncoding;
    }

    @Override
    public void encode(ByteBuffer buf, T value) {
        int lengthPrefixBytes = determineLengthBytes();
        ByteBuffer dataBuffer = buf.slice();
        dataBuffer.position(lengthPrefixBytes);
        int lengthPrefixValue = encodeData(dataBuffer, value);
        int dataBytes = dataBuffer.position() - lengthPrefixBytes;
        encodeLength(buf, lengthPrefixValue);
        buf.position(buf.position() + dataBytes);
    }

    /**
     * Encodes the length prefix of a var length data element
     *
     * @param buf
     * @param dataLength
     */
    void encodeLength(ByteBuffer buf, int dataLength) {
        // TODO: implement encoding of length prefix
        switch (varLengthPrefixEncoding) {
            case BCD:

                break;
            case BCD_F:

                break;
            case CHAR:

                break;
            default:
                throw new RuntimeException(
                        String.format("Unsupported length prefix encoding: %s", varLengthPrefixEncoding));
        }
    }

    /**
     * Determines the length bytes for a particular encoding of the length prefix
     *
     * @return
     */
    int determineLengthBytes() {
        int lengthPrefixBytes;
        switch (varLengthPrefixEncoding) {
            case BCD:
            case BCD_F:
                lengthPrefixBytes = varLengthPrefixDigits / 2 + varLengthPrefixDigits % 2;
                break;
            case CHAR:
                lengthPrefixBytes = varLengthPrefixDigits;
                break;
            default:
                throw new RuntimeException(
                        String.format("Unsupported length prefix encoding: %s", varLengthPrefixEncoding));
        }
        return lengthPrefixBytes;
    }

    /**
     * Encodes the data and returns the value to be encoded as the length prefix
     *
     * @param dataBuffer
     * @param value
     * @return the length to be encoded in the length prefix
     */
    abstract int encodeData(ByteBuffer dataBuffer, T value);

}
