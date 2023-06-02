import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public static class NoisySignatureVisitor extends SignatureVisitor implements Opcodes {
	public NoisySignatureVisitor() {
		super(ASM9);
	}
	
	String indent = "\t";
	private void spam(String x) { System.err.println(indent + x); }
	private void up() { indent += "\t"; }
	private void down() { indent = indent.substring(1); }
	
	@Override
	public SignatureVisitor visitClassBound() {
		spam("visitClassBound");
		up();
		return super.visitClassBound();
	}
	
	@Override
	public SignatureVisitor visitInterfaceBound() {
		spam("visitInterfaceBound");
		up();
		return super.visitInterfaceBound();
	}
	
	@Override
	public SignatureVisitor visitSuperclass() {
		spam("visitSuperclass");
		up();
		return super.visitSuperclass();
	}
	
	@Override
	public SignatureVisitor visitInterface() {
		spam("visitInterface");
		up();
		return super.visitInterface();
	}
	
	@Override
	public SignatureVisitor visitParameterType() {
		spam("visitParameterType");
		up();
		return super.visitParameterType();
	}
	
	@Override
	public SignatureVisitor visitReturnType() {
		spam("visitReturnType");
		up();
		return super.visitReturnType();
	}
	
	@Override
	public SignatureVisitor visitExceptionType() {
		spam("visitExceptionType");
		up();
		return super.visitExceptionType();
	}
	
	@Override
	public void visitFormalTypeParameter(String name) {
		spam("visitFormalTypeParameter: " + name);
		super.visitFormalTypeParameter(name);
	}
	
	@Override
	public void visitBaseType(char descriptor) {
		spam("visitBaseType: " + descriptor);
		super.visitBaseType(descriptor);
	}
	
	@Override
	public void visitTypeVariable(String name) {
		spam("visitTypeVariable: " + name);
		super.visitTypeVariable(name);
	}
	
	@Override
	public SignatureVisitor visitArrayType() {
		spam("visitArrayType");
		return super.visitArrayType();
	}
	
	@Override
	public void visitClassType(String name) {
		spam("visitClassType: " + name);
		super.visitClassType(name);
	}
	
	@Override
	public void visitInnerClassType(String name) {
		spam("visitInnerClassType: " + name);
		super.visitInnerClassType(name);
	}
	
	@Override
	public void visitTypeArgument() {
		spam("visitTypeArgument");
		super.visitTypeArgument();
	}
	
	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		spam("visitTypeArgumentWildcard: " + wildcard);
		return super.visitTypeArgument(wildcard);
	}
	
	@Override
	public void visitEnd() {
		down();
		spam("visitEnd");
		super.visitEnd();
	}
}