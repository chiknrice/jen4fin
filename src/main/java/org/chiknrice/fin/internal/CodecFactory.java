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

import static org.chiknrice.fin.internal.Xml.ATTR_BITMAP;
import static org.chiknrice.fin.internal.Xml.Tag;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class CodecFactory {

    static Codec<Number> buildMtiCodec(Tag tag) {
        return null;
    }

    static <T extends Codec<?>> T buildDataCodec(Tag tag) {
        return null;
    }

    static Codec<Number> buildLengthPrefixCodec(Tag tag) {
        return null;
    }

    static CompositeCodec buildCompositeCodec(Tag tag) {
        String bitmapAttribute = tag.getOptionalAttribute(ATTR_BITMAP);
        Bitmap.Encoding bitmapEncoding = null;
        if (bitmapAttribute != null) {
            bitmapEncoding = Bitmap.Encoding.valueOf(bitmapAttribute);
        }
        return null;
    }

}
