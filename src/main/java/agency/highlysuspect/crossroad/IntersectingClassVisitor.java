package agency.highlysuspect.crossroad;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IntersectingClassVisitor extends ClassVisitor implements Opcodes {
	public IntersectingClassVisitor() {
		super(ASM9);
	}
	
	int classesSeen = 0;
	
	private int version = Integer.MAX_VALUE;
	private int access = 0xFFFF_FFFF;
	private String name;
	private String signature;
	private String superName;
	private final Set<String> interfaces = new LinkedHashSet<>();
	
	//this is a map from "annotations" to "number of times i've seen this annotation"
	//n.b. this fails for repeatable annotations
	private final Map<AnnotationNode, Integer> annotations = new LinkedHashMap<>();
	
	private final Map<FieldNode, Integer> fields = new LinkedHashMap<>();
	private final Map<MethodNode, Integer> methods = new LinkedHashMap<>();
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.version = Math.min(this.version, version);
		this.access &= access;
		
		if(classesSeen == 0) {
			this.name = name;
			this.signature = signature;
			this.superName = superName;
			this.interfaces.addAll(Arrays.asList(interfaces));
		} else {
			//Classes have differing superclasses
			if(!Objects.equals(this.superName, superName)) this.superName = "java/lang/Object";
			
			//the interfaces on the class we're visiting, rendered as a set
			Set<String> visitingInterfaces = new HashSet<>(interfaces == null ? Collections.emptyList() : Arrays.asList(interfaces));
			
			//Find all interfaces we know about that do not exist on the visiting class
			Set<String> removedInterfaces = new LinkedHashSet<>();
			for(String itf : this.interfaces) if(!visitingInterfaces.contains(itf)) removedInterfaces.add(itf);
			
			//Remove those interfaces from our working set
			this.interfaces.removeAll(removedInterfaces);
			
			//Also try and strip those interfaces from the signature
			//TODO: be smarter about this (don't null the whole signature, just remove offending classes from it)
			// Also there's probably more cases where the signature must be modified?
			// Like if a non-universally-included class shows up in the signature for other reasons
			if(this.signature != null && !removedInterfaces.isEmpty()) {
				for(String removedItf : removedInterfaces) {
					if(this.signature.contains(removedItf)) {
						this.signature = null;
						break;
					}
				}
			}
		}
		
		classesSeen++;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return new AnnotationNode(ASM9, descriptor) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				incr(annotations, this);
			}
			
			@SuppressWarnings("EqualsWhichDoesntCheckParameterClass") //unnameable anon
			@Override
			public boolean equals(Object obj) {
				AnnotationNode other = (AnnotationNode) obj;
				return Objects.equals(desc, other.desc) && Objects.equals(values, other.values);
			}
			
			@SuppressWarnings("ObjectInstantiationInEqualsHashCode")
			@Override
			public int hashCode() {
				return Objects.hash(desc, values);
			}
		};
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		return new FieldNode(ASM9, access, name, descriptor, signature, value) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				incr(fields, this);
			}
			
			@SuppressWarnings("EqualsWhichDoesntCheckParameterClass") //unnameable anon
			@Override
			public boolean equals(Object obj) {
				FieldNode other = (FieldNode) obj;
				return access == other.access &&
					Objects.equals(name, other.name) &&
					Objects.equals(desc, other.desc) &&
					Objects.equals(signature, other.signature);// &&
					//Objects.equals(value, other.value);
			}
			
			@SuppressWarnings("ObjectInstantiationInEqualsHashCode")
			@Override
			public int hashCode() {
				return Objects.hash(access, name, desc, signature, value);
			}
		};
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		return new MethodNode(ASM9, access, name, descriptor, signature, exceptions) {
			@Override
			public void visitEnd() {
				super.visitEnd();
				incr(methods, this);
			}
			
			@SuppressWarnings("EqualsWhichDoesntCheckParameterClass") //unnameable anon
			@Override
			public boolean equals(Object obj) {
				MethodNode other = (MethodNode) obj;
				return access == other.access &&
					Objects.equals(name, other.name) &&
					Objects.equals(desc, other.desc) &&
					Objects.equals(signature, other.signature);
			}
			
			@SuppressWarnings("ObjectInstantiationInEqualsHashCode")
			@Override
			public int hashCode() {
				return Objects.hash(access, name, desc, signature);
			}
		};
	}
	
	public void accept(ClassVisitor out) {
		out.visit(version, access, name, signature, superName, interfaces.toArray(new String[0]));
		
		//for some reason this accept() method takes a methodvisitor instead of a cv
		//mayb that "visible" parameter which isn't actually stored on the annotation node
		//grm
		for(AnnotationNode a : filter(annotations, classesSeen)) {
			AnnotationVisitor av = out.visitAnnotation(a.desc, true);
			if(av != null) a.accept(av);
		}
		
		for(FieldNode f : filter(fields, classesSeen)) f.accept(out);
		for(MethodNode m : filter(methods, classesSeen)) m.accept(out);
		
		out.visitEnd();
	}
	
	private <K> void incr(Map<K, Integer> map, K key) {
		map.put(key, 1 + map.getOrDefault(key, 0));
	}
	
	private <K> List<K> filter(Map<K, Integer> map, int target) {
		return map.entrySet().stream()
			.filter(e -> target == e.getValue())
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
	}
}
