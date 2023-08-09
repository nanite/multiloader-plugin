package dev.nanite.mlp.test;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerRemapper;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import org.objectweb.asm.commons.Remapper;

//Copy of AccessWidenerRemapper but removed class name remapping
public class CustomAccessWidenerRemapper implements AccessWidenerVisitor {
    private final AccessWidenerVisitor delegate;
    private final Remapper remapper;

    public CustomAccessWidenerRemapper(AccessWidenerVisitor delegate, Remapper remapper) {
        this.delegate = delegate;
        this.remapper = remapper;
    }

    @Override
    public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitClass(name, access, transitive);
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitMethod(
                owner,
                remapper.mapMethodName(owner, name, descriptor),
                descriptor,
                access,
                transitive
        );
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitField(
                owner,
                remapper.mapFieldName(owner, name, descriptor),
                descriptor,
                access,
                transitive
        );
    }
}
