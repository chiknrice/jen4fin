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
import java.nio.charset.StandardCharsets;

/**
 * Base class for variable length data element decoders
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
abstract class VarLengthDecoder<T> extends BaseDecoder<T> {

    final int lengthPrefixDigits;
    final Encoding lengthPrefixEncoding;

    VarLengthDecoder(int lengthPrefixDigits, Encoding lengthPrefixEncoding, Encoding dataEncoding) {
        super(dataEncoding);
        this.lengthPrefixDigits = lengthPrefixDigits;
        this.lengthPrefixEncoding = lengthPrefixEncoding;
    }

    /**
     * Derives the data ByteBuffer by getting decoding the length prefix first and using the result length
     *
     * @param buffer
     * @return
     */
    ByteBuffer deriveDataBuffer(ByteBuffer buffer) {
        byte[] lengthPrefixBytes;
        switch (lengthPrefixEncoding) {
            case BCD:
            case BCD_F:
                lengthPrefixBytes = new byte[lengthPrefixDigits / 2 + lengthPrefixDigits % 2];
                break;
            case CHAR:
                lengthPrefixBytes = new byte[lengthPrefixDigits];
                break;
            default:
                throw new RuntimeException(String.format("Unsupported length prefix encoding: %s", lengthPrefixEncoding));
        }
        buffer.get(lengthPrefixBytes);

        int dataLength;
        switch (lengthPrefixEncoding) {
            case BCD:
            case BCD_F:
                // TODO: decode length using these encodings
                dataLength = -1;
                break;
            case CHAR:
                dataLength = Integer.parseInt(new String(lengthPrefixBytes, StandardCharsets.ISO_8859_1));
                break;
            default:
                throw new RuntimeException(String.format("Unsupported length prefix encoding: %s", lengthPrefixEncoding));
        }

        int dataByteCount = determineBytesCount(dataLength);

        return sliceBuffer(buffer, dataByteCount);
    }

}
