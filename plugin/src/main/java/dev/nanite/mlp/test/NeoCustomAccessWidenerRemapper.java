package dev.nanite.mlp.test;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;

//Copy of AccessWidenerRemapper but removed class name remapping
public class NeoCustomAccessWidenerRemapper implements AccessWidenerVisitor {
    private final AccessWidenerVisitor delegate;

    public NeoCustomAccessWidenerRemapper(AccessWidenerVisitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitClass(name, access, transitive);
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitMethod(owner, name, descriptor, access, transitive);
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitField(owner, name, descriptor, access, transitive);
    }
}
