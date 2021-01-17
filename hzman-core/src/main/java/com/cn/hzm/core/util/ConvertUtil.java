package com.cn.hzm.core.util;

import com.thoughtworks.xstream.XStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/7/11 12:20 下午
 */
public class ConvertUtil {
    public static <T> T toBean(Class<T> clazz, String xml) {
        T xmlObject;
        XStream xstream = new XStream();

        XStream.setupDefaultSecurity(xstream);

        xstream.allowTypes(new Class[]{clazz});

        xstream.processAnnotations(clazz);
        xstream.autodetectAnnotations(true);
        xmlObject = (T) xstream.fromXML(xml);
        return xmlObject;
    }


    @SuppressWarnings("unchecked")
    public static <T> T readString(Class<T> clazz, String context) throws Exception {
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Unmarshaller u = jc.createUnmarshaller();
            return (T) u.unmarshal(new StringReader(context));
        } catch (Exception e) {
            throw e;
        }
    }
}
