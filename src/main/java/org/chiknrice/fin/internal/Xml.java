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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
class Xml {

    static final String TAG_HEADER = "header";
    static final String TAG_MESSAGE_ELEMENTS = "message-elements";
    static final String TAG_MESSAGE = "message";
    static final String TAG_VAR_COMPOSITE = "var-composite";
    static final String TAG_VAR_PREFIX = "var-";


    static final String ATTR_MTI = "mti";
    static final String ATTR_INDEX = "index";
    static final String ATTR_BITMAP = "bitmap";

    final String namespace;
    final Document document;
    final Map<String, List<Tag>> tagsMap;

    Xml(Document document) {
        this.document = document;
        this.namespace = document.getDocumentElement().getNamespaceURI();
        this.tagsMap = new ConcurrentHashMap<>();
    }

    List<Tag> getTags(String tagName) {
        List<Tag> tags = tagsMap.get(tagName);
        if (tags == null) {
            NodeList nodeList = document.getElementsByTagNameNS(namespace, tagName);
            int listSize = nodeList.getLength();
            tags = new ArrayList<>();
            for (int i = 0; i < listSize; i++) {
                Node nodeItem = nodeList.item(i);
                if (nodeItem instanceof Element) {
                    tags.add(new Tag((Element) nodeItem));
                }
            }
            tagsMap.put(tagName, tags);
        }
        return tags;
    }

    Tag getTag(String tagsName) {
        return getTags(tagsName).get(0);
    }

    static class Tag {

        final Element element;

        Tag(Element element) {
            this.element = element;
        }

        List<Tag> getChildren() {
            NodeList childNodes = element.getChildNodes();
            List<Tag> childTags = new ArrayList<>();

            int length = childNodes.getLength();

            for (int i = 0; i < length; i++) {
                Node node = childNodes.item(i);
                if (node instanceof Element && element.getNamespaceURI().equals(node.getNamespaceURI())) {
                    childTags.add(new Tag((Element) node));
                }

            }
            return childTags;
        }

        String getName() {
            return element.getTagName();
        }

        String getAttribute(String attribute) {
            String value = getOptionalAttribute(attribute);
            if (value != null) {
                return value;
            } else {
                throw new RuntimeException(String.format("Missing mandatory attribute %s in tag %s", attribute, getName()));
            }
        }

        String getOptionalAttribute(String attribute) {
            return getOptionalAttribute(attribute, null);
        }

        String getOptionalAttribute(String attribute, String defaultValue) {
            String value = element.getAttribute(attribute);
            return value == null || value.trim().length() == 0 ? defaultValue : value;
        }

        Integer getIntegerAttribute(String attribute) {
            return Integer.valueOf(getAttribute(attribute));
        }

    }

}
