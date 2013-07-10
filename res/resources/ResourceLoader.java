package resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

public class ResourceLoader {

	public static InputStreamReader getResourceISR(String resource) {

		try {
			// input stream from resource
			ClassLoader classLoader = ResourceLoader.class.getClass().getClassLoader();
			InputStream in = classLoader.getResourceAsStream(resource);

			return new InputStreamReader(in);
			
			//URL url = ResourceLoader.class.getClassLoader().getResource(resource);
			//System.out.println(url.getPath());
			//return url.getPath();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
}
