package com.testsoapdemo;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MyMessageNamespaceMapper implements SOAPHandler<SOAPMessageContext> {
	

  @Override
  public Set<QName> getHeaders() {
    return null;
  }

  @Override
  public boolean handleMessage(SOAPMessageContext context) {
    final Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    // only process outbound messages
    if (outbound) {
      try {
        final SOAPMessage soapMessage = context.getMessage();
        final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
        final SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        final SOAPBody soapBody = soapMessage.getSOAPBody();

        // STEP 1: add new prefix/namespace entries
        soapEnvelope.addNamespaceDeclaration("S1", "http://schemas.xmlsoap.org/soap/envelope/");
        soapEnvelope.addNamespaceDeclaration("FOO-1", "http://testsoapdemo.com/");

        // STEP 2: set desired namespace prefixes
        // set desired namespace prefix for the envelope, header and body
        soapEnvelope.setPrefix("S1");
        soapHeader.setPrefix("S1");
        soapBody.setPrefix("S1");
        addDesiredBodyNamespaceEntries(soapBody.getChildElements());

        // STEP 3: remove prefix/namespace entries entries added by JAX-WS
        soapEnvelope.removeNamespaceDeclaration("S");
        soapEnvelope.removeNamespaceDeclaration("SOAP-ENV");
        removeUndesiredBodyNamespaceEntries(soapBody.getChildElements());

        // IMPORTANT! "Save" the changes
        soapMessage.saveChanges();
      }
      catch (SOAPException e) {
        // handle the error
      }
    }

    return true;
  }

  private void addDesiredBodyNamespaceEntries(Iterator childElements) {
    while (childElements.hasNext()) {
      final Object childElementNode = childElements.next();
      if (childElementNode instanceof SOAPElement) {
        SOAPElement soapElement = (SOAPElement) childElementNode;

        // set desired namespace body element prefix
        soapElement.setPrefix("FOO-1");

        // recursively set desired namespace prefix entries in child elements
        addDesiredBodyNamespaceEntries(soapElement.getChildElements());
      }
    }
  }

  private void removeUndesiredBodyNamespaceEntries(Iterator childElements) {
    while (childElements.hasNext()) {
      final Object childElementNode = childElements.next();
      if (childElementNode instanceof SOAPElement) {
        SOAPElement soapElement = (SOAPElement) childElementNode;

        // we remove any prefix/namespace entries added by JAX-WS in the body element that is not the one we want
        for (String prefix : getNamespacePrefixList(soapElement.getNamespacePrefixes())) {
          if (prefix != null && ! "FOO-1".equals(prefix)) {
            soapElement.removeNamespaceDeclaration(prefix);
          }
        }

        // recursively remove prefix/namespace entries in child elements
        removeUndesiredBodyNamespaceEntries(soapElement.getChildElements());
      }
    }
  }

  private Set<String> getNamespacePrefixList(Iterator namespacePrefixIter) {
    Set<String> namespacePrefixesSet = new HashSet<>();
    while (namespacePrefixIter.hasNext()) {
      namespacePrefixesSet.add((String) namespacePrefixIter.next());
    }
    return namespacePrefixesSet;
  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    return true;
  }

  @Override
  public void close(MessageContext context) {
  }
}