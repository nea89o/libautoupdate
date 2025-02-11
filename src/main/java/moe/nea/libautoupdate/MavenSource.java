package moe.nea.libautoupdate;

import com.google.gson.JsonPrimitive;
import lombok.*;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Download source for a maven artifact. Pulls from the maven-metadata versioning info and does not support SNAPSHOT
 * versions.
 * <p>The stream {@code full} is dedicated to only full releases (as indicated by the {@code release} tag).</p>
 * <p>The stream {@code latest} is dedicated to all releases (as indicated by the {@code latest} tag).</p>
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
@NonNull
public class MavenSource implements UpdateSource {
	String repoUrl;
	String module;
	String artifact;
	String classifier;
	String extension;

	protected URI getMavenBaseUrl() {
		var uri = URI.create(repoUrl);
		for (String modulePart : UpdateUtils.stringSplitIterator(module, "/")) {
			uri = uri.resolve(UpdateUtils.urlEncode(modulePart));
		}
		uri = uri.resolve(UpdateUtils.urlEncode(artifact));
		return uri;
	}

	protected URI getMavenMetadataUrl() {
		return getMavenBaseUrl().resolve("maven-metadata.xml");
	}

	protected String getFileName(String version) {
		return String.format("%s-%s%s%s.%s", artifact, version, classifier.isEmpty() ? "" : "-", classifier, extension);
	}

	protected URI getMavenArtifactUrl(String version) {
		return getMavenBaseUrl().resolve(UpdateUtils.urlEncode(version))
		                        .resolve(UpdateUtils.urlEncode(getFileName(version)));
	}

	@SneakyThrows
	@Override
	public CompletableFuture<UpdateData> checkUpdate(String updateStream) {
		var dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		var db = dbf.newDocumentBuilder();
		return CompletableFuture.supplyAsync(() -> {
			try (val is = UpdateUtils.openUrlConnection(getMavenMetadataUrl().toURL())) {
				var document = db.parse(new InputSource(is));
				var metadata = (Element) document.getDocumentElement();
				var versioning = (Element) metadata.getElementsByTagName("versioning").item(0);
				var latest = (Element) versioning.getElementsByTagName("latest").item(0);
				var release = (Element) versioning.getElementsByTagName("release").item(0);
				if (release != null && ("full".equals(updateStream) || latest == null)) {
					latest = release;
				}
				var latestVersion = latest.getTextContent();
				return new UpdateData(
						latestVersion,
						new JsonPrimitive(latestVersion),
						null,
						getMavenArtifactUrl(latestVersion).toString()
				);
			} catch (Exception e) {
				throw new CompletionException(e);
			}
		});
	}
}
