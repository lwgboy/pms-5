package com.pgxxgc.www.IF_Service;

public class IF_ServiceProxy implements com.pgxxgc.www.IF_Service.IF_Service {
  private String _endpoint = null;
  private com.pgxxgc.www.IF_Service.IF_Service iF_Service = null;
  
  public IF_ServiceProxy() {
    _initIF_ServiceProxy();
  }
  
  public IF_ServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initIF_ServiceProxy();
  }
  
  private void _initIF_ServiceProxy() {
    try {
      iF_Service = (new com.pgxxgc.www.IF_Service.IF_ServiceServiceLocator()).getIF_Service();
      if (iF_Service != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)iF_Service)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)iF_Service)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (iF_Service != null)
      ((javax.xml.rpc.Stub)iF_Service)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.pgxxgc.www.IF_Service.IF_Service getIF_Service() {
    if (iF_Service == null)
      _initIF_ServiceProxy();
    return iF_Service;
  }
  
  public java.lang.String IFService(java.lang.String sMsgID, java.lang.String sContent) throws java.rmi.RemoteException{
    if (iF_Service == null)
      _initIF_ServiceProxy();
    return iF_Service.IFService(sMsgID, sContent);
  }
  
  
}