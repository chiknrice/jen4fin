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
 * Base contract for an encoder.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public interface Encoder<T> {

    /**
     * Encodes the value T to the ByteBuffer provided. The method assumes the value would be encoded from ByteBuffer's
     * current position.
     *
     * @param buf
     * @param value
     */
    void encode(ByteBuffer buf, T value);

}
