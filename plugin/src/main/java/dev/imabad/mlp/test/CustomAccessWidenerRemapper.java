package dev.imabad.mlp.test;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerRemapper;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import org.objectweb.asm.commons.Remapper;

public class CustomAccessWidenerRemapper implements AccessWidenerVisitor {
    private final AccessWidenerVisitor delegate;
    private final String fromNamespace;
    private final String toNamespace;
    private final Remapper remapper;

    public CustomAccessWidenerRemapper(AccessWidenerVisitor delegate, Remapper remapper, String fromNamespace, String toNamespace) {
        this.delegate = delegate;
        this.fromNamespace = fromNamespace;
        this.toNamespace = toNamespace;
        this.remapper = remapper;
    }
    @Override
    public void visitHeader(String namespace) {
        if (!this.fromNamespace.equals(namespace)) {
            throw new IllegalArgumentException("Cannot remap access widener from namespace '" + namespace + "'."
                    + " Expected: '" + this.fromNamespace + "'");
        }

        delegate.visitHeader(toNamespace);
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
                remapper.mapDesc(descriptor),
                access,
                transitive
        );
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        delegate.visitField(
                owner,
                remapper.mapFieldName(owner, name, descriptor),
                remapper.mapDesc(descriptor),
                access,
                transitive
        );
    }
}
