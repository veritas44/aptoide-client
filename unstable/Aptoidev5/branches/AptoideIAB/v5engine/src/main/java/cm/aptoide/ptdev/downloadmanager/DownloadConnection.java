package cm.aptoide.ptdev.downloadmanager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 02-07-2013
 * Time: 11:50
 * To change this template use File | Settings | File Templates.
 */
public abstract class DownloadConnection {



    protected URL mURL;


    protected DownloadConnection(URL url)
    {
        this.mURL = url;
    }

    public String getFileName()
    {
        String fileName = this.mURL.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    public URL getURL()
    {
        return this.mURL;
    }

    public abstract void connect(long downloaded) throws IOException, CompletedDownloadException, NotFoundException, IPBlackListedException, ContentTypeNotApkException;



    public abstract void close();


    public abstract BufferedInputStream getStream() ;
    public abstract long getShallowSize() throws IOException;
}


