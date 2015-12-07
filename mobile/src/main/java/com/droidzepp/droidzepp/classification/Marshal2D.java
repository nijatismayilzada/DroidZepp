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
public class Marshal2D implements Marshal{
    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected)
            throws IOException, XmlPullParserException {
        return parser.nextText();
    }

    public void register(SoapSerializationEnvelope cm) {
        cm.addMapping(cm.xsd, "double[][]", double[][].class, this);
    }

    public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
        double[][] myArray = (double[][]) obj;
            for (int j = 0; j < myArray.length; j++) {
                writer.startTag("", "ArrayOfDouble");
                for (int k = 0; k < myArray[j].length; k++) {
                    writer.startTag("", "double");
                    writer.text(String.valueOf(myArray[j][k]));
                    writer.endTag("", "double");
                }
                writer.endTag("", "ArrayOfDouble");
            }
    }
}
