/*
Copyright (C) 2019 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
 */

package org.harctoolbox.irp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import org.harctoolbox.analyze.Analyzer;
import org.harctoolbox.ircore.IrSequence;
import org.harctoolbox.ircore.ModulatedIrSequence;
import org.harctoolbox.irp.Decoder.Decode;
import org.harctoolbox.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class ProtocolListDomFactory {

    public static Document protocolListToDom(Analyzer analyzer, List<Protocol> protocols, List<String> names, int radix, boolean fat) {
        ProtocolListDomFactory factory = new ProtocolListDomFactory(analyzer, protocols, names, radix, fat);
        return factory.getDocument();
    }

    private Map<Integer, Protocol> protocolsWithoutDefs;
    private final Document doc;
    private final List<String> names;
    private final int radix;
    private final Analyzer analyzer;
    private final List<Protocol> protocols;
    private int counter;
    private Element commandSet;

    private ProtocolListDomFactory(Analyzer analyzer, List<Protocol> protocols, List<String> names, int radix, boolean fat) {
        this.protocolsWithoutDefs = new HashMap<>(8);
        this.analyzer = analyzer;
        this.protocols = protocols;
        this.names = names;
        this.radix = radix;
        this.counter = 0;

        doc = XmlUtils.newDocument(true);
        doc.appendChild(doc.createComment(XmlUtils.GIRR_COMMENT));
        Element remotes = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "remotes");
        remotes.setAttribute(XmlUtils.SCHEMA_LOCATION_ATTRIBUTE_NAME, XmlUtils.GIRR_SCHEMA_LOCATION + " " + XmlUtils.IRP_SCHEMA_LOCATION);
        remotes.setAttribute(XmlUtils.W3C_SCHEMA_NAMESPACE_ATTRIBUTE_NAME, W3C_XML_SCHEMA_INSTANCE_NS_URI);
        remotes.setAttribute("title", "Generated by " + Version.versionString);
        remotes.setAttribute(XmlUtils.GIRR_VERSION_NAME, XmlUtils.GIRR_VERSION);
        doc.appendChild(remotes);
        if (protocols != null) {
            Element protocolsElement = mkProtocols();
            remotes.appendChild(protocolsElement);
        }
        Element remote = commandsToElement(fat);
        remotes.appendChild(remote);
    }

    public ProtocolListDomFactory(int radix) {
        this(null, null, null, radix, false);
    }

    private Element commandsToElement(boolean fat) {
        Element remote = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "remote");
        remote.setAttribute("name", "remote");
        commandSet = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "commandSet");
        commandSet.setAttribute("name", "commandSet");
        remote.appendChild(commandSet);
        if (analyzer != null) {
            if (analyzer.isSignalMode())
                commandSet.appendChild(commandToElement(protocols.get(0), (names != null && !names.isEmpty()) ? names.get(0) : null,
                        analyzer.cleanedIrSequence(0), analyzer.cleanedIrSequence(1), analyzer.cleanedIrSequence(2), fat));
            else
                for (int i = 0; i < protocols.size(); i++)
                    commandSet.appendChild(commandToElement(protocols.get(i), (names != null && names.size() > i) ? names.get(i) : null, analyzer.cleanedIrSequence(i), null, null, fat));
        }
        return remote;
    }

    private Element commandToElement(Protocol protocol, String name, IrSequence intro, IrSequence repeat, IrSequence ending, boolean fat) {
        Element command = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "command");
        @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
        String commandName = name != null ? name : ("unnamed_" + Integer.toString(counter++));
        command.setAttribute("name", commandName);
        Element parameters = parametersToElement(protocol);
        command.appendChild(parameters);
        Element raw = rawToElement(intro, repeat, ending, fat);
        command.appendChild(raw);
        command.setAttribute("master", "raw");
        return command;
    }

    private Element rawToElement(IrSequence intro, IrSequence repeat, IrSequence ending, boolean fat) {
        Element raw = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "raw");
        raw.setAttribute("frequency",
                Integer.toString(analyzer.getFrequency() != null
                        ? analyzer.getFrequency().intValue() : (int) ModulatedIrSequence.DEFAULT_FREQUENCY));
        if (intro != null)
            raw.appendChild(irSequenceToElement(intro, "intro", fat));
        if (repeat != null)
            raw.appendChild(irSequenceToElement(repeat, "repeat", fat));
        if (ending != null)
            raw.appendChild(irSequenceToElement(ending, "ending", fat));
        return raw;
    }

    private Element irSequenceToElement(IrSequence irSequence, String name, boolean fat) {
        Element element = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, name);
        if (fat) {
            for (int i = 0; i < irSequence.getLength(); i++) {
                Element el = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, ((i & 1) != 0 ? "gap" : "flash"));
                Text text = doc.createTextNode(Long.toString(Math.round(irSequence.get(i))));
                el.appendChild(text);
                element.appendChild(el);
            }
        } else {
            Text content = doc.createTextNode(irSequence.toString(true, " ", "", ""));
            element.appendChild(content);
        }
        return element;
    }

    private Element parametersToElement(Protocol protocol) {
        Element parameters = defsToElement(protocol.getDefinitions());
        Protocol withoutDefs = new Protocol(protocol.getGeneralSpec(), protocol.getBitspecIrstream(), new NameEngine(), null);

        parameters.setAttribute("protocol", formatProtocolnameFromHash(withoutDefs.hashCode()));
        return parameters;
    }

    private Element decodesToElement(Decode d) {
        Element parameters = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "parameters");
        parameters.setAttribute("protocol", d.getName());
        for (Map.Entry<String, Long> definition : d.getMap().entrySet()) {
            Element parameter = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "parameter");
            parameter.setAttribute("name", definition.getKey());
            parameter.setAttribute("value", Long.toString(definition.getValue(), radix));
            parameters.appendChild(parameter);
        }
        return parameters;
    }

    private Element defsToElement(NameEngine definitions) {
        Element parameters = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "parameters");
        for (Map.Entry<String, Expression> definition : definitions) {
            Element parameter = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "parameter");
            parameter.setAttribute("name", definition.getKey());
            parameter.setAttribute("value", definition.getValue().toIrpString(radix));
            parameters.appendChild(parameter);
        }
        return parameters;
    }

    private Element mkProtocols() {
        protocolsWithoutDefs = new HashMap<>(4);
        protocols.stream().map((protocol) -> new Protocol(protocol.getGeneralSpec(), protocol.getBitspecIrstream(), new NameEngine(), protocol.getParameterSpecs())).forEachOrdered((withoutDefs) -> {
            protocolsWithoutDefs.put(withoutDefs.hashCode(), withoutDefs);
        });
        Element protocolsElement = doc.createElementNS(XmlUtils.IRP_NAMESPACE_URI, XmlUtils.IRP_PREFIX + ":protocols");
        protocolsWithoutDefs.entrySet().forEach((proto) -> {
            Element protocolElement = doc.createElementNS(XmlUtils.IRP_NAMESPACE_URI, XmlUtils.IRP_PREFIX + ":protocol");
            protocolElement.setAttribute("name", formatProtocolnameFromHash(proto.getKey()));
            protocolsElement.appendChild(protocolElement);
            Element irp = doc.createElementNS(XmlUtils.IRP_NAMESPACE_URI, XmlUtils.IRP_PREFIX + ":irp");
            irp.appendChild(doc.createCDATASection(proto.getValue().toIrpString(radix)));
            protocolElement.appendChild(irp);
        });
        return protocolsElement;
    }

    private Document getDocument() {
        return doc;
    }

    private String formatProtocolnameFromHash(Integer key) {
        return "p_" + Integer.toUnsignedString(key, 16);
    }

    public void add(Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes, String name) {
        commandSet.appendChild(mkParametrized(decodes, name));
    }

    public void add(Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes) {
        @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
        String commandName = "unnamed_" + Integer.toString(counter++);
        add(decodes, commandName);
    }

    private Element mkParametrized(Decoder.AbstractDecodesCollection<? extends ElementaryDecode> decodes, String commandName) {
        Element command = doc.createElementNS(XmlUtils.GIRR_NAMESPACE_URI, "command");
        command.setAttribute("name", commandName);
        Decode d = decodes.first().getDecode();
        Element parameters = decodesToElement(d);
        command.appendChild(parameters);
        command.setAttribute("master", "parameters");
        return command;
    }

    public Document toDocument() {
        return doc;
    }
}
