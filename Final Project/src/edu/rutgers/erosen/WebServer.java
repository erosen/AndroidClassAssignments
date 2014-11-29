package edu.rutgers.erosen;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import android.os.Environment;
import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {
	
	final static String DIR = "CVBenchmark";
	final static File file = new File(Environment.getExternalStorageDirectory(),
			DIR);
	
	public WebServer(int port) throws IOException {
		super(port, file);
	}

	@Override
	public Response serveFile(String uri, Properties header, File homeDir,
			boolean allowDirectoryListing) {

		// TODO Auto-generated method stub
		return super.serveFile(uri, header, homeDir, allowDirectoryListing);

	}

	public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
	{

		return super.serve(uri, method, header, parms, files);
	}
}

