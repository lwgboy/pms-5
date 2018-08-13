/**
 * IF_ServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.pgxxgc.www.IF_Service;

public class IF_ServiceServiceLocator extends org.apache.axis.client.Service
		implements com.pgxxgc.www.IF_Service.IF_ServiceService {

	public IF_ServiceServiceLocator() {
	}

	public IF_ServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public IF_ServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName)
			throws javax.xml.rpc.ServiceException {
		super(wsdlLoc, sName);
	}

	// Use to get a proxy class for IF_Service
	private java.lang.String IF_Service_address = "http://10.1.30.45:7080/services/IF_Service";

	// Use to get a proxy class for IF_Service
	private java.lang.String IF_Service_test_address = "http://10.1.30.9:7080/services/IF_Service";

	public java.lang.String getIF_ServiceAddress() {
		return IF_Service_address;
	}

	public java.lang.String getIF_ServiceTestAddress() {
		return IF_Service_test_address;
	}

	// The WSDD service name defaults to the port name.
	private java.lang.String IF_ServiceWSDDServiceName = "IF_Service";

	public java.lang.String getIF_ServiceWSDDServiceName() {
		return IF_ServiceWSDDServiceName;
	}

	public void setIF_ServiceWSDDServiceName(java.lang.String name) {
		IF_ServiceWSDDServiceName = name;
	}

	public com.pgxxgc.www.IF_Service.IF_Service getIF_Service() throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(IF_Service_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getIF_Service(endpoint);
	}

	public com.pgxxgc.www.IF_Service.IF_Service getIF_ServiceTest() throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(IF_Service_test_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getIF_Service(endpoint);
	}

	public com.pgxxgc.www.IF_Service.IF_Service getIF_Service(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			com.pgxxgc.www.IF_Service.IF_ServiceSoapBindingStub _stub = new com.pgxxgc.www.IF_Service.IF_ServiceSoapBindingStub(
					portAddress, this);
			_stub.setPortName(getIF_ServiceWSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	public void setIF_ServiceEndpointAddress(java.lang.String address) {
		IF_Service_address = address;
	}

	/**
	 * For the given interface, get the stub implementation. If this service has no
	 * port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
		try {
			if (com.pgxxgc.www.IF_Service.IF_Service.class.isAssignableFrom(serviceEndpointInterface)) {
				com.pgxxgc.www.IF_Service.IF_ServiceSoapBindingStub _stub = new com.pgxxgc.www.IF_Service.IF_ServiceSoapBindingStub(
						new java.net.URL(IF_Service_address), this);
				_stub.setPortName(getIF_ServiceWSDDServiceName());
				return _stub;
			}
		} catch (java.lang.Throwable t) {
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
				+ (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation. If this service has no
	 * port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		if (portName == null) {
			return getPort(serviceEndpointInterface);
		}
		java.lang.String inputPortName = portName.getLocalPart();
		if ("IF_Service".equals(inputPortName)) {
			return getIF_Service();
		} else {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public javax.xml.namespace.QName getServiceName() {
		return new javax.xml.namespace.QName("http://www.pgxxgc.com/IF_Service", "IF_ServiceService");
	}

	private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet();
			ports.add(new javax.xml.namespace.QName("http://www.pgxxgc.com/IF_Service", "IF_Service"));
		}
		return ports.iterator();
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(java.lang.String portName, java.lang.String address)
			throws javax.xml.rpc.ServiceException {

		if ("IF_Service".equals(portName)) {
			setIF_ServiceEndpointAddress(address);
		} else { // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address)
			throws javax.xml.rpc.ServiceException {
		setEndpointAddress(portName.getLocalPart(), address);
	}

}
