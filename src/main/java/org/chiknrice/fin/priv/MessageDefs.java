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
package org.chiknrice.fin.priv;

import org.chiknrice.fin.priv.Xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.chiknrice.fin.priv.CodecFactory.*;
import static org.chiknrice.fin.priv.Xml.*;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class MessageDefs {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDefs.class);

    public static MessageDefs build(String configXml) {
        return build(Thread.currentThread().getContextClassLoader().getResourceAsStream(configXml));
    }

    public static MessageDefs build(InputStream inputStream) {
        return new MessageDefBuilder(inputStream).build();
    }

    CompositeDef headerDefs;
    Codec<Number> mtiCodec;
    Map<Integer, CompositeDef> deDefs = new ConcurrentHashMap<>();

    public CompositeDef getHeaderDef() {
        return headerDefs;
    }

    public Codec<Number> getMtiCodec() {
        return mtiCodec;
    }

    public Map<Integer, CompositeDef> getDataElementsDef() {
        return deDefs;
    }

    private static class MessageDefBuilder {

        static final String SCHEMA_FILE = "jen4fin.xsd";

        final Xml xml;
        final MessageDefs rootDef;

        MessageDefBuilder(InputStream configXml) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setNamespaceAware(true);
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(configXml);
                doc.getDocumentElement().normalize();

                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new StreamSource(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_FILE)));

                Validator validator = schema.newValidator();
                validator.validate(new DOMSource(doc));
                xml = new Xml(doc);
                rootDef = new MessageDefs();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        MessageDefs build() {
            buildCodecs();
            buildHeaderDefs();
            buildMessageDefs();
            return rootDef;
        }

        Map<IndexExpr, Codec<?>> dataCodecs = new HashMap<>();
        Map<IndexExpr, CompositeCodec> compositeCodecs = new HashMap<>();
        Map<IndexExpr, Codec<Number>> lengthCodecs = new HashMap<>();

        void buildCodecs() {
            Tag headerTag = xml.getTag(TAG_HEADER);
            compositeCodecs.put(IndexExpr.fromTag(headerTag), buildCompositeCodec(headerTag));
            if (headerTag != null) {
                List<Tag> headerTags = headerTag.getChildren();
                for (int i = 0; i < headerTags.size(); i++) {
                    dataCodecs.put(IndexExpr.forHeader(i), buildDataCodec(headerTags.get(i)));
                }
            }
            Tag messageElementsTag = xml.getTag(TAG_MESSAGE_ELEMENTS);
            rootDef.mtiCodec = buildMtiCodec(messageElementsTag);
            compositeCodecs.put(IndexExpr.fromTag(messageElementsTag), buildCompositeCodec(messageElementsTag));

            for (Tag dataElementTag : messageElementsTag.getChildren()) {
                IndexExpr index = new IndexExpr(dataElementTag.getAttribute(ATTR_INDEX));
                if (TAG_VAR_COMPOSITE.equals(dataElementTag.getName())) {
                    compositeCodecs.put(index, buildCompositeCodec(dataElementTag));
                } else {
                    dataCodecs.put(index, buildDataCodec(dataElementTag));
                }
                if (dataElementTag.getName().startsWith(TAG_VAR_PREFIX)) {
                    lengthCodecs.put(index, buildLengthPrefixCodec(dataElementTag));
                }
            }
        }

        void buildHeaderDefs() {
            Tag headerTag = xml.getTag(TAG_HEADER);
            if (headerTag != null) {
                rootDef.headerDefs = new CompositeDef();
                List<Tag> headerComponentTags = headerTag.getChildren();
                for (int i = 0; i < headerComponentTags.size(); i++) {
                    IndexExpr indexExpr = IndexExpr.forHeader(i);
                    ComponentDef headerComponentDef = resolveComponentDef(rootDef.headerDefs, indexExpr);
                    headerComponentDef.dataCodec = dataCodecs.get(indexExpr);
                }
            }
        }

        void buildMessageDefs() {
            List<Tag> messageTags = xml.getTags(TAG_MESSAGE);
            for (Tag messageTag : messageTags) {
                Integer mti = messageTag.getIntegerAttribute(ATTR_MTI);
                CompositeDef messageDef = buildMessageDef(messageTag.getChildren());
                messageDef.compositeCodec = compositeCodecs.get(IndexExpr.fromTag(messageTag));
                rootDef.deDefs.put(mti, messageDef);
            }
        }

        CompositeDef buildMessageDef(List<Tag> deTags) {
            CompositeDef messageDef = new CompositeDef();
            for (Tag deTag : deTags) {
                IndexExpr index = IndexExpr.fromTag(deTag);
                ComponentDef componentDef = resolveComponentDef(messageDef, index);
                if (dataCodecs.containsKey(index)) {
                    componentDef.dataCodec = dataCodecs.get(index);
                }
                if (lengthCodecs.containsKey(index)) {
                    componentDef.lengthCodec = lengthCodecs.get(index);
                }
                if (compositeCodecs.containsKey(index)) {
                    if (componentDef instanceof CompositeDef) {
                        ((CompositeDef) componentDef).compositeCodec = compositeCodecs.get(index);
                    } else {
                        throw new RuntimeException(format("Expecting composite component at index %s", index));
                    }
                }
            }

            return messageDef;
        }

        void configureComponentDef(ComponentDef def, Tag tag) {

        }

        void buildComponentDef(ComponentDef def, Tag tag) {

        }

        ComponentDef resolveComponentDef(CompositeDef parentCompositeDef, IndexExpr index) {
            Map<Integer, ComponentDef> parentComponentDefs = parentCompositeDef.componentDefs;
            Integer currentIndex = index.current();
            ComponentDef componentDef = parentComponentDefs.get(currentIndex);

            if (index.isLast()) {
                if (componentDef == null) {
                    componentDef = compositeCodecs.containsKey(index) ? new CompositeDef() : new ComponentDef();
                    parentComponentDefs.put(currentIndex, componentDef);
                    componentDef.parent = parentCompositeDef;
                }
            } else {
                CompositeDef currentCompositeDef;
                if (componentDef == null) {
                    currentCompositeDef = new CompositeDef();
                    parentComponentDefs.put(currentIndex, currentCompositeDef);
                    currentCompositeDef.parent = parentCompositeDef;
                } else if (componentDef instanceof CompositeDef) {
                    currentCompositeDef = ((CompositeDef) componentDef);
                } else {
                    throw new RuntimeException(format("Exepecting composite component for index %d but got component", currentIndex));
                }
                componentDef = resolveComponentDef(currentCompositeDef, index.next());
            }
            return componentDef;
        }

    }


}
