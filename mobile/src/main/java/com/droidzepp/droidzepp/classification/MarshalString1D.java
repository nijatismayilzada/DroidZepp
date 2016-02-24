package com.droidzepp.droidzepp.classification;

import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class MarshalString1D implements Marshal{
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected)
            throws IOException, XmlPullParserException {
        return parser.nextText();
    }

    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "String[]", String[].class, this);
    }

    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
        String[] myArray = (String[]) obj;
            for (int k = 0; k < myArray.length; k++) {
                writer.startTag("", "string");
                writer.text(myArray[k]);
                writer.endTag("", "string");
            }
    }
}
