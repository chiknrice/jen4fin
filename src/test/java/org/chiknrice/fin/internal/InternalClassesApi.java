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

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class InternalClassesApi {

    static String printEnum(Class<? extends Enum<?>> enumType) {
        try {
            Object[] values = ((Object[]) enumType.getMethod("values", new Class[0]).invoke(enumType, new Object[0]));
            StringBuilder sb = new StringBuilder();
            for (Object value : values) {
                if (sb.length() == 0) {
                    sb.append("[");
                } else {
                    sb.append(",");
                }
                sb.append(value);
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String getBitmapEncodings() {
        return printEnum(Bitmap.Encoding.class);
    }

    public static String getEncodings() {
        return printEnum(Encoding.class);
    }

}
