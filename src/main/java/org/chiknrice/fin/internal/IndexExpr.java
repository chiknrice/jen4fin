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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.chiknrice.fin.internal.Xml.*;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class IndexExpr {

    int pos;
    String indexExpression;
    int[] indexes;

    IndexExpr(String indexExpression) {
        this.indexExpression = indexExpression;
        Pattern p = Pattern.compile("H?\\d+(\\.\\d+)*");
        Matcher m = p.matcher(indexExpression);
        if (!m.matches()) {
            throw new IllegalArgumentException(format("%s is not a valid index pattern", indexExpression));
        }
        if (indexExpression.startsWith("H")) {
            indexExpression = indexExpression.substring(1);
        }
        String[] indexes = indexExpression.split("\\.");
        this.indexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            this.indexes[i] = Integer.parseInt(indexes[i]);
        }
        pos = 0;
    }

    void reset() {
        pos = 0;
    }

    int current() {
        return indexes[pos];
    }

    IndexExpr next() {
        pos++;
        return this;
    }

    boolean isLast() {
        return pos == indexes.length - 1;
    }

    @Override
    public int hashCode() {
        return indexExpression.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() != getClass()) {
            return false;
        } else {
            IndexExpr other = (IndexExpr) o;
            return indexExpression.equals(other.indexExpression);
        }
    }

    @Override
    public String toString() {
        return indexExpression;
    }

    static IndexExpr fromTag(Tag tag) {
        IndexExpr indexExpr;
        switch (tag.getName()) {
            case TAG_HEADER:
                indexExpr = forHeader(0);
                break;
            case TAG_MESSAGE_ELEMENTS:
            case TAG_MESSAGE:
                indexExpr = new IndexExpr("0");
                break;
            default:
                indexExpr = new IndexExpr(tag.getAttribute(ATTR_INDEX));
        }
        return indexExpr;
    }

    static IndexExpr forHeader(int i) {
        return new IndexExpr(String.format("H%d", i));
    }

}
