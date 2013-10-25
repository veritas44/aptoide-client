package cm.aptoide.ptdev.parser.handlers;

import android.util.Log;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.model.ApkInfoXML;
import cm.aptoide.ptdev.utils.Configs;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 22-10-2013
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class HandlerInfoXml extends AbstractHandler {

    private final long repoId;

    public HandlerInfoXml(Database db, long repoId) {
        super(db);
        this.repoId = repoId;
    }

    @Override
    protected Apk getApk() {
        return new ApkInfoXML();
    }

    @Override
    protected void loadSpecificElements() {
        elements.put("package", new ElementHandler() {
            @Override
            public void startElement(Attributes attributes) throws SAXException {
                apk = getApk();
                ((ApkInfoXML)apk).setRepoId(repoId);
            }

            @Override
            public void endElement() throws SAXException {
                apk.databaseInsert(statements);
            }
        });

        elements.put("date", new ElementHandler() {
            public void startElement(Attributes atts) throws SAXException {

            }

            @Override
            public void endElement() throws SAXException {
                try {
                    apk.setDate(Configs.TIME_STAMP_FORMAT_INFO_XML.parse(sb.toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                    apk.setDate(new Date(0));
                }
            }
        });
    }
}
