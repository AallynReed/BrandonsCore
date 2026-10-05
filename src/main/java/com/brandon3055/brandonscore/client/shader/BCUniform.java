package com.brandon3055.brandonscore.client.shader;

import codechicken.lib.vec.Matrix4;
import org.joml.Matrix4fc;

import java.nio.ByteBuffer;

public class BCUniform {

    private final String name;
    private final Type type;
    private final int offset;
    private final int[] values;

    BCUniform(String name, Type type, int offset) {
        this.name = name;
        this.type = type;
        this.offset = offset;
        this.values = new int[type.count];
    }

    public String getName() {
        return name;
    }

    public void glUniform1i(int i0) { glUniformI(i0); }

    public void glUniform2i(int i0, int i1) { glUniformI(i0, i1); }

    public void glUniform3i(int i0, int i1, int i2) { glUniformI(i0, i1, i2); }

    public void glUniform4i(int i0, int i1, int i2, int i3) { glUniformI(i0, i1, i2, i3); }

    public void glUniform1f(float f0) { glUniformF(f0); }

    public void glUniform2f(float f0, float f1) { glUniformF(f0, f1); }

    public void glUniform3f(float f0, float f1, float f2) { glUniformF(f0, f1, f2); }

    public void glUniform4f(float f0, float f1, float f2, float f3) { glUniformF(f0, f1, f2, f3); }

    public void glUniform1b(boolean b0) { glUniformI(b0 ? 1 : 0); }

    public void glUniformMatrix4f(Matrix4 matrix) { glUniformF(matrix.toArrayF()); }

    public void glUniformMatrix4f(Matrix4fc matrix) { glUniformF(matrix.get(new float[16])); }

    public void glUniformI(int... values) {
        checkValues(values.length, true);
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public void glUniformF(float... values) {
        checkValues(values.length, false);
        for (int i = 0; i < values.length; i++) {
            this.values[i] = Float.floatToRawIntBits(values[i]);
        }
    }

    private void checkValues(int count, boolean integer) {
        if (count != type.count || integer != type.integer) {
            throw new IllegalArgumentException("Uniform " + name + " is a " + type + ", got " + count + (integer ? " int" : " float") + " values");
        }
    }

    void write(ByteBuffer buffer) {
        for (int i = 0; i < values.length; i++) {
            buffer.putInt(offset + i * 4, values[i]);
        }
    }

    public enum Type {
        FLOAT(false, 1, 4),
        VEC2(false, 2, 8),
        VEC3(false, 3, 16),
        VEC4(false, 4, 16),
        INT(true, 1, 4),
        IVEC2(true, 2, 8),
        IVEC3(true, 3, 16),
        IVEC4(true, 4, 16),
        BOOL(true, 1, 4),
        MAT4(false, 16, 16);

        private final boolean integer;
        private final int count;
        private final int alignment;

        Type(boolean integer, int count, int alignment) {
            this.integer = integer;
            this.count = count;
            this.alignment = alignment;
        }

        int alignment() {
            return alignment;
        }

        int size() {
            return count * 4;
        }
    }
}
