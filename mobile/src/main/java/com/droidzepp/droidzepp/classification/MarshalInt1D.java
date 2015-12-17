package com.droidzepp.droidzepp.classification;

import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by nijat on 06/12/15.
 */
public class MarshalInt1D implements Marshal{
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected)
            throws IOException, XmlPullParserException {
        return parser.nextText();
    }

    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "int[]", int[].class, this);
    }

    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
        int[] myArray = (int[]) obj;
            for (int k = 0; k < myArray.length; k++) {
                writer.startTag("", "int");
                writer.text(String.valueOf(myArray[k]));
                writer.endTag("", "int");
            }
    }
}
