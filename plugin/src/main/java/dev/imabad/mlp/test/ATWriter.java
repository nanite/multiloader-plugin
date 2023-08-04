package dev.imabad.mlp.test;

import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;

public class ATWriter implements AccessWidenerVisitor {
    private final StringBuilder builder = new StringBuilder();

    @Override
    public void visitClass(String name, AccessWidenerReader.AccessType access, boolean transitive) {
        checkTransitive(transitive);
        String type = switch (access) {
            case EXTENDABLE -> "public-f";
            case ACCESSIBLE -> "public";
            case MUTABLE -> throw new IllegalArgumentException("Class cannot be mutable");
        };
        builder.append(type).append(' ').append(name.replace("/", ".")).append('\n');
    }

    @Override
    public void visitMethod(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        checkTransitive(transitive);
        String type = switch (access) {
            case EXTENDABLE -> "protected-f";
            case ACCESSIBLE -> "public";
            case MUTABLE -> throw new IllegalArgumentException("Methods cannot be mutable");
        };
        builder.append(type).append(" ").append(owner.replace("/", ".")).append(" ").append(name).append(descriptor).append("\n"); //Todo do I need;
    }

    @Override
    public void visitField(String owner, String name, String descriptor, AccessWidenerReader.AccessType access, boolean transitive) {
        checkTransitive(transitive);
        String type = switch (access) {
            case EXTENDABLE -> throw new IllegalArgumentException("Fields cannot be extendable");
            case ACCESSIBLE -> "public";
            case MUTABLE -> "public-f";
        };
        builder.append(type).append(" ").append(owner.replace("/", ".")).append(" ").append(name).append("\n");
    }

    public byte[] write() {
        String s = writeString();
        return s.getBytes(AccessWidenerReader.ENCODING);
    }

    public String writeString() {
        return builder.toString();
    }

    private void checkTransitive(boolean transitive) throws UnsupportedOperationException {
        if(transitive) {
            throw new UnsupportedOperationException("Transitive access is not supported");
        }
    }



}
