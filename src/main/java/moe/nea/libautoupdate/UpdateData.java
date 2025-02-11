package moe.nea.libautoupdate;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateData {
	String versionName;
	JsonElement versionNumber;
	String sha256;
	String download;

	public URI getDownloadAsURI() throws URISyntaxException {
		return new URI(download);
	}

	public String getRawFileName() throws URISyntaxException {
		var file = UpdateUtils.lastStringSegment(getDownloadAsURI().getRawPath(), "/");
		if (file == null || file.isEmpty())
			throw new RuntimeException("Download form url " + download + " does not contain enough of a path to guess a file name.");
		return file;
	}

	public URL getDownloadAsURL() throws MalformedURLException {
		return new URL(download);
	}
}
