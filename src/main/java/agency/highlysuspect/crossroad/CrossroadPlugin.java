package agency.highlysuspect.crossroad;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolutionStrategy;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CrossroadPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getExtensions().create("crossroad", Extension.class, project);
	}
	
	public static class Extension {
		public Extension(Project project) {
			this.project = project;
		}
		
		private final Project project;
		
		@SuppressWarnings("unused") //plugin
		public Path merge(Object... deps) throws Exception {
			if(deps.length == 0) throw new IllegalArgumentException("no deps");
			
			List<Path> paths = Arrays.stream(deps)
				.map(this::realizeToPath)
				.map(Path::toAbsolutePath)
				.collect(Collectors.toList());
			
			if(paths.size() == 0) throw new IllegalArgumentException("no paths"); 
			
			//pick a filename
			MessageDigest yeah;
			try {
				yeah = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException yourMomGay) {
				throw new RuntimeException(yourMomGay);
			}
			yeah.update((byte) 3); //cache invalidation when updating the plugin
			yeah.update((byte) paths.size());
			for(Path p : paths) {
				yeah.update(p.toString().getBytes(StandardCharsets.UTF_8));
				yeah.update((byte) 0);
			}
			StringBuilder sb = new StringBuilder();
			for(byte b : yeah.digest()) {
				int hi = (b & 0xF0) >>> 4;
				sb.append((char) (hi <= 9 ? '0' + hi : 'a' + hi - 10));
				
				int lo = (b & 0x0F);
				sb.append((char) (lo <= 9 ? '0' + lo : 'a' + lo - 10));
			}
			String filename = "merge-" + sb.substring(0, 8) + ".jar";
			
			//make the file
			Path outDir = project.getGradle().getGradleUserHomeDir().toPath().resolve("caches").resolve("crossroad-cache");
			Files.createDirectories(outDir);
			Path outPath = outDir.resolve(filename);
			
			if(project.getGradle().getStartParameter().isRefreshDependencies() || project.hasProperty("crossroad.refreshDependencies")) {
				Files.deleteIfExists(outPath);
			}
			
			if(Files.notExists(outPath)) {
				project.getLogger().warn("(crossroad) merging to: " + outPath);
				
				//okay now we do the merge
				List<FileSystem> filesystemsToClose = new ArrayList<>();
				try {
					FileSystem outFs = FileSystems.newFileSystem(URI.create("jar:" + outPath.toUri()), Collections.singletonMap("create", "true"));
					filesystemsToClose.add(outFs);
					
					List<FileSystem> inFses = new ArrayList<>();
					for(Path p : paths) {
						FileSystem inFs = FileSystems.newFileSystem(URI.create("jar:" + p.toUri()), Collections.emptyMap());
						inFses.add(inFs);
						filesystemsToClose.add(inFs);
					}
					
					new JarIntersector(outFs, inFses).doIt();
				} finally {
					for(FileSystem fs : filesystemsToClose) if(fs != null) fs.close();
				}
				
				Path infoFile = outDir.resolve(filename + ".info");
				if(Files.notExists(infoFile)) {
					List<String> info = new ArrayList<>();
					info.add("Merged version of: ");
					paths.forEach(p -> info.add(p.toString()));
					info.add("");
					info.add("Part of " + project.getDisplayName());
					Files.write(infoFile, info, StandardCharsets.UTF_8);
				}
			}
			
			return outPath;
		}
		
		private Path realizeToPath(Object thing) {
			project.getLogger().info("\t-- (realizeToPath) wonder what this '{}' ({}) is? --", thing, thing.getClass().getName());
			
			if(thing instanceof Path) {
				project.getLogger().info("\t-- looks like a Path --");
				return (Path) thing;
			} else if(thing instanceof File) {
				project.getLogger().info("\t-- looks like a File --");
				return ((File) thing).toPath();
			} else if(thing instanceof Dependency) {
				project.getLogger().info("\t-- looks like a Dependency --");
				return resolveOne((Dependency) thing);
			}
			
			//just blindly assume toString makes sense (catches things like groovy GStringImpl)
			String s = thing.toString();
			
			if(s.startsWith("http:/") || s.startsWith("https:/")) {
				throw new IllegalArgumentException("URLs not implemented in crossroad");
			} else {
				project.getLogger().info("\t-- looks like a Maven coordinate (or unknown) --");
				return resolveOne(project.getDependencies().create(thing));
			}
		}
		
		private Path resolveOne(Dependency dep) {
			Configuration detatched = project.getConfigurations().detachedConfiguration(dep);
			detatched.resolutionStrategy(ResolutionStrategy::failOnNonReproducibleResolution);
			return detatched.getSingleFile().toPath();
		}
	}
}
