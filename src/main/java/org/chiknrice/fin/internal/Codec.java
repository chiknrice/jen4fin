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
 * The main contract for a codec used across encoding and decoding of message components. Each field/component of the
 * ISO message would have its own instance of codec which contains the definition of how the value should be
 * encoded/decoded. The codec should be designed to be thread safe as the instance would live throughout the life of the
 * IsoMessageDef. Any issues encountered during encoding/decoding should be throwing a CodecException. Any issues
 * encountered during codec configuration/construction the constructor should throw a ConfigException. A ConfigException
 * should generally happen during startup while CodecException happens when the Codec is being used.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
interface Codec<T> {

    /**
     * The implementation should define how the value T should be decoded from the ByteBuffer provided. The
     * implementation could either decode the value from a certain number of bytes or consume the whole ByteBuffer.
     *
     * @param buf
     * @return the decoded value
     */
    T decode(ByteBuffer buf);

    /**
     * The implementation should define how the value T should be encoded to the ByteBuffer provided. The ByteBuffer
     * assumes the value would be encoded from the current position.
     *
     * @param buf
     * @param value the value to be encoded
     */
    void encode(ByteBuffer buf, T value);

    /**
     * Defines how the value should be encoded/decoded.
     *
     * @return the encoding defined for the value.
     */
    Encoding getEncoding();

}
