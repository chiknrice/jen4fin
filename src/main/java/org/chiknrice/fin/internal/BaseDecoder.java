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
 * Base decoder implementing common methods to fixed and var length decoders
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
abstract class BaseDecoder<T> implements Decoder<T> {

    final Encoding dataEncoding;

    BaseDecoder(Encoding dataEncoding) {
        this.dataEncoding = dataEncoding;
    }

    @Override
    public T decode(ByteBuffer buffer) {
        ByteBuffer dataBuffer = deriveDataBuffer(buffer);
        return decodeData(dataBuffer);
    }

    int determineBytesCount(int dataLength) {
        int dataByteCount;
        switch (dataEncoding) {
            case BINARY:
            case CHAR:
                dataByteCount = dataLength;
                break;
            case BCD:
            case BCD_F:
                dataByteCount = dataLength / 2 + dataLength % 2;
                break;
            case X_BCD:
            case CX_BCD:
            default:
                throw new RuntimeException(String.format("Unsupported data encoding: %s", dataEncoding));
        }
        return dataByteCount;
    }

    /**
     * Creates a new ByteBuffer with the given limit and consumes the same limit from the source ByteBuffer
     *
     * @param buffer
     * @param limit
     * @return
     */
    ByteBuffer sliceBuffer(ByteBuffer buffer, int limit) {
        ByteBuffer dataBuffer = buffer.slice();
        dataBuffer.limit(limit);
        buffer.position(buffer.position() + limit);
        return dataBuffer;
    }

    /**
     * Derive the data ByteBuffer.  The implementation assumes that the derived buffer has offset and limit set to the
     * boundaries of the data being decoded
     *
     * @param buffer
     * @return
     */
    abstract ByteBuffer deriveDataBuffer(ByteBuffer buffer);

    /**
     *
     * @param dataBuffer
     * @return
     */
    abstract T decodeData(ByteBuffer dataBuffer);

}
