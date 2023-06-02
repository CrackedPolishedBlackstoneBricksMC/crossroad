package agency.highlysuspect.crossroad;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JarIntersector {
	public JarIntersector(FileSystem outFs, List<FileSystem> inFses) {
		this.outFs = outFs;
		this.inFses = inFses;
	}
	
	public final FileSystem outFs;
	public final List<FileSystem> inFses;
	
	public void doIt() throws Exception {
		//walk the first filesystem
		Files.walkFileTree(inFses.get(0).getPath("/"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				String pathStr = path.toString();
				
				//only interested in classes
				if(!pathStr.endsWith(".class")) return FileVisitResult.CONTINUE;
				
				Path outPath = outFs.getPath(pathStr);
				
				//see if this path exists in all other filesystems
				//if they do, collect all paths into this "paths" array
				List<Path> allPaths = new ArrayList<>(inFses.size());
				
				for(FileSystem inFs : inFses) {
					Path p = inFs.getPath(pathStr);
					if(Files.notExists(p)) {
						//nope, it doesn't exist in all filesystems
						return FileVisitResult.CONTINUE;
					}
					allPaths.add(p);
				}
				
				//see if the files are exactly the same
				List<byte[]> hashes = new ArrayList<>();
				for(Path p : allPaths) {
					MessageDigest md = sha1();
					md.update(Files.readAllBytes(p));
					hashes.add(md.digest());
				}
				if(hashes.stream().allMatch(arr -> Arrays.equals(hashes.get(0), arr))) {
					//the files are exactly the same in all filesystems.
					//simply copy one to the destination
					if(outPath.getParent() != null) Files.createDirectories(outPath.getParent());
					Files.copy(path, outPath);
					return FileVisitResult.CONTINUE;
				}
				
				//if they're not the same, perform the class merge operation
				IntersectingClassVisitor icv = new IntersectingClassVisitor();
				for(Path p : allPaths) {
					new ClassReader(Files.readAllBytes(p)).accept(icv, ClassReader.SKIP_CODE);
				}
				
				ClassWriter write = new ClassWriter(null, 0);
				icv.accept(write);
				byte[] intersected = write.toByteArray();
				
				Path parent = outPath.getParent();
				if(parent != null) Files.createDirectories(parent);
				Files.write(outPath, intersected);
				
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	private static MessageDigest sha1() {
		try {
			return MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
