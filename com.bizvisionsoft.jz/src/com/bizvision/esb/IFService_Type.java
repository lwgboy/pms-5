
package com.bizvision.esb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>anonymous complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sMsgID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sContent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sMsgID",
    "sContent"
})
@XmlRootElement(name = "IFService")
public class IFService_Type {

    @XmlElement(required = true)
    protected String sMsgID;
    @XmlElement(required = true)
    protected String sContent;

    /**
     * 获取sMsgID属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSMsgID() {
        return sMsgID;
    }

    /**
     * 设置sMsgID属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSMsgID(String value) {
        this.sMsgID = value;
    }

    /**
     * 获取sContent属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSContent() {
        return sContent;
    }

    /**
     * 设置sContent属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSContent(String value) {
        this.sContent = value;
    }

}
