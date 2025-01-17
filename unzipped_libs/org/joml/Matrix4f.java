/*
 * (C) Copyright 2015 Richard Greenlees

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 */
package org.joml;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

/**
 * Contains the definition of a 4x4 Matrix of floats, and associated functions
 * to transform it. The matrix is column-major to match OpenGL's interpretation,
 * and it looks like this:
 * <p>
 * m00 m10 m20 m30</br>
 * m01 m11 m21 m31</br>
 * m02 m12 m22 m32</br>
 * m03 m13 m23 m33</br>
 *
 * @author Richard Greenlees
 * @author Kai Burjack
 */
public class Matrix4f implements Serializable, Externalizable {

	/** Returns the determinant of the supplied matrix */
	public static float determinant(Matrix4f source) {
		return (source.m00 * source.m11 - source.m01 * source.m10) * (source.m22 * source.m33 - source.m23 * source.m32)
				- (source.m00 * source.m12 - source.m02 * source.m10)
						* (source.m21 * source.m33 - source.m23 * source.m31)
				+ (source.m00 * source.m13 - source.m03 * source.m10)
						* (source.m21 * source.m32 - source.m22 * source.m31)
				+ (source.m01 * source.m12 - source.m02 * source.m11)
						* (source.m20 * source.m33 - source.m23 * source.m30)
				- (source.m01 * source.m13 - source.m03 * source.m11)
						* (source.m20 * source.m32 - source.m22 * source.m30)
				+ (source.m02 * source.m13 - source.m03 * source.m12)
						* (source.m20 * source.m31 - source.m21 * source.m30);
	}

	/**
	 * Set the upper 3x3 matrix of the given <code>dest</code> {@link Matrix4f} to
	 * the given {@link Matrix3f} and the rest to the identity.
	 * 
	 * @param mat  the 3x3 matrix
	 * @param dest the destination matrix whose upper left 3x3 submatrix will be set
	 *             to <code>mat</code>
	 */
	public static void fromMatrix3(Matrix3f mat, Matrix4f dest) {
		dest.m00 = mat.m00;
		dest.m01 = mat.m01;
		dest.m02 = mat.m02;
		dest.m03 = 0.0f;
		dest.m10 = mat.m10;
		dest.m11 = mat.m11;
		dest.m12 = mat.m12;
		dest.m13 = 0.0f;
		dest.m20 = mat.m20;
		dest.m21 = mat.m21;
		dest.m22 = mat.m22;
		dest.m23 = 0.0f;
		dest.m30 = 0.0f;
		dest.m31 = 0.0f;
		dest.m32 = 0.0f;
		dest.m33 = 1.0f;
	}

	/**
	 * Invert the supplied matrix and store the result in <code>dest</code>.
	 * 
	 * @param source the matrix to invert
	 * @param dest   the matrix to hold the result
	 */
	public static void invert(Matrix4f source, Matrix4f dest) {
		float s = source.determinant();
		if (s == 0.0f) {
			dest.set(source);
			return;
		}
		s = 1.0f / s;
		if (source != dest) {
			dest.m00 = (source.m11 * (source.m22 * source.m33 - source.m23 * source.m32)
					+ source.m12 * (source.m23 * source.m31 - source.m21 * source.m33)
					+ source.m13 * (source.m21 * source.m32 - source.m22 * source.m31)) * s;
			dest.m01 = (source.m21 * (source.m02 * source.m33 - source.m03 * source.m32)
					+ source.m22 * (source.m03 * source.m31 - source.m01 * source.m33)
					+ source.m23 * (source.m01 * source.m32 - source.m02 * source.m31)) * s;
			dest.m02 = (source.m31 * (source.m02 * source.m13 - source.m03 * source.m12)
					+ source.m32 * (source.m03 * source.m11 - source.m01 * source.m13)
					+ source.m33 * (source.m01 * source.m12 - source.m02 * source.m11)) * s;
			dest.m03 = (source.m01 * (source.m13 * source.m22 - source.m12 * source.m23)
					+ source.m02 * (source.m11 * source.m23 - source.m13 * source.m21)
					+ source.m03 * (source.m12 * source.m21 - source.m11 * source.m22)) * s;
			dest.m10 = (source.m12 * (source.m20 * source.m33 - source.m23 * source.m30)
					+ source.m13 * (source.m22 * source.m30 - source.m20 * source.m32)
					+ source.m10 * (source.m23 * source.m32 - source.m22 * source.m33)) * s;
			dest.m11 = (source.m22 * (source.m00 * source.m33 - source.m03 * source.m30)
					+ source.m23 * (source.m02 * source.m30 - source.m00 * source.m32)
					+ source.m20 * (source.m03 * source.m32 - source.m02 * source.m33)) * s;
			dest.m12 = (source.m32 * (source.m00 * source.m13 - source.m03 * source.m10)
					+ source.m33 * (source.m02 * source.m10 - source.m00 * source.m12)
					+ source.m30 * (source.m03 * source.m12 - source.m02 * source.m13)) * s;
			dest.m13 = (source.m02 * (source.m13 * source.m20 - source.m10 * source.m23)
					+ source.m03 * (source.m10 * source.m22 - source.m12 * source.m20)
					+ source.m00 * (source.m12 * source.m23 - source.m13 * source.m22)) * s;
			dest.m20 = (source.m13 * (source.m20 * source.m31 - source.m21 * source.m30)
					+ source.m10 * (source.m21 * source.m33 - source.m23 * source.m31)
					+ source.m11 * (source.m23 * source.m30 - source.m20 * source.m33)) * s;
			dest.m21 = (source.m23 * (source.m00 * source.m31 - source.m01 * source.m30)
					+ source.m20 * (source.m01 * source.m33 - source.m03 * source.m31)
					+ source.m21 * (source.m03 * source.m30 - source.m00 * source.m33)) * s;
			dest.m22 = (source.m33 * (source.m00 * source.m11 - source.m01 * source.m10)
					+ source.m30 * (source.m01 * source.m13 - source.m03 * source.m11)
					+ source.m31 * (source.m03 * source.m10 - source.m00 * source.m13)) * s;
			dest.m23 = (source.m03 * (source.m11 * source.m20 - source.m10 * source.m21)
					+ source.m00 * (source.m13 * source.m21 - source.m11 * source.m23)
					+ source.m01 * (source.m10 * source.m23 - source.m13 * source.m20)) * s;
			dest.m30 = (source.m10 * (source.m22 * source.m31 - source.m21 * source.m32)
					+ source.m11 * (source.m20 * source.m32 - source.m22 * source.m30)
					+ source.m12 * (source.m21 * source.m30 - source.m20 * source.m31)) * s;
			dest.m31 = (source.m20 * (source.m02 * source.m31 - source.m01 * source.m32)
					+ source.m21 * (source.m00 * source.m32 - source.m02 * source.m30)
					+ source.m22 * (source.m01 * source.m30 - source.m00 * source.m31)) * s;
			dest.m32 = (source.m30 * (source.m02 * source.m11 - source.m01 * source.m12)
					+ source.m31 * (source.m00 * source.m12 - source.m02 * source.m10)
					+ source.m32 * (source.m01 * source.m10 - source.m00 * source.m11)) * s;
			dest.m33 = (source.m00 * (source.m11 * source.m22 - source.m12 * source.m21)
					+ source.m01 * (source.m12 * source.m20 - source.m10 * source.m22)
					+ source.m02 * (source.m10 * source.m21 - source.m11 * source.m20)) * s;
		} else {
			dest.set(
					(source.m11 * (source.m22 * source.m33 - source.m23 * source.m32)
							+ source.m12 * (source.m23 * source.m31 - source.m21 * source.m33)
							+ source.m13 * (source.m21 * source.m32 - source.m22 * source.m31)) * s,
					(source.m21 * (source.m02 * source.m33 - source.m03 * source.m32)
							+ source.m22 * (source.m03 * source.m31 - source.m01 * source.m33)
							+ source.m23 * (source.m01 * source.m32 - source.m02 * source.m31)) * s,
					(source.m31 * (source.m02 * source.m13 - source.m03 * source.m12)
							+ source.m32 * (source.m03 * source.m11 - source.m01 * source.m13)
							+ source.m33 * (source.m01 * source.m12 - source.m02 * source.m11)) * s,
					(source.m01 * (source.m13 * source.m22 - source.m12 * source.m23)
							+ source.m02 * (source.m11 * source.m23 - source.m13 * source.m21)
							+ source.m03 * (source.m12 * source.m21 - source.m11 * source.m22)) * s,
					(source.m12 * (source.m20 * source.m33 - source.m23 * source.m30)
							+ source.m13 * (source.m22 * source.m30 - source.m20 * source.m32)
							+ source.m10 * (source.m23 * source.m32 - source.m22 * source.m33)) * s,
					(source.m22 * (source.m00 * source.m33 - source.m03 * source.m30)
							+ source.m23 * (source.m02 * source.m30 - source.m00 * source.m32)
							+ source.m20 * (source.m03 * source.m32 - source.m02 * source.m33)) * s,
					(source.m32 * (source.m00 * source.m13 - source.m03 * source.m10)
							+ source.m33 * (source.m02 * source.m10 - source.m00 * source.m12)
							+ source.m30 * (source.m03 * source.m12 - source.m02 * source.m13)) * s,
					(source.m02 * (source.m13 * source.m20 - source.m10 * source.m23)
							+ source.m03 * (source.m10 * source.m22 - source.m12 * source.m20)
							+ source.m00 * (source.m12 * source.m23 - source.m13 * source.m22)) * s,
					(source.m13 * (source.m20 * source.m31 - source.m21 * source.m30)
							+ source.m10 * (source.m21 * source.m33 - source.m23 * source.m31)
							+ source.m11 * (source.m23 * source.m30 - source.m20 * source.m33)) * s,
					(source.m23 * (source.m00 * source.m31 - source.m01 * source.m30)
							+ source.m20 * (source.m01 * source.m33 - source.m03 * source.m31)
							+ source.m21 * (source.m03 * source.m30 - source.m00 * source.m33)) * s,
					(source.m33 * (source.m00 * source.m11 - source.m01 * source.m10)
							+ source.m30 * (source.m01 * source.m13 - source.m03 * source.m11)
							+ source.m31 * (source.m03 * source.m10 - source.m00 * source.m13)) * s,
					(source.m03 * (source.m11 * source.m20 - source.m10 * source.m21)
							+ source.m00 * (source.m13 * source.m21 - source.m11 * source.m23)
							+ source.m01 * (source.m10 * source.m23 - source.m13 * source.m20)) * s,
					(source.m10 * (source.m22 * source.m31 - source.m21 * source.m32)
							+ source.m11 * (source.m20 * source.m32 - source.m22 * source.m30)
							+ source.m12 * (source.m21 * source.m30 - source.m20 * source.m31)) * s,
					(source.m20 * (source.m02 * source.m31 - source.m01 * source.m32)
							+ source.m21 * (source.m00 * source.m32 - source.m02 * source.m30)
							+ source.m22 * (source.m01 * source.m30 - source.m00 * source.m31)) * s,
					(source.m30 * (source.m02 * source.m11 - source.m01 * source.m12)
							+ source.m31 * (source.m00 * source.m12 - source.m02 * source.m10)
							+ source.m32 * (source.m01 * source.m10 - source.m00 * source.m11)) * s,
					(source.m00 * (source.m11 * source.m22 - source.m12 * source.m21)
							+ source.m01 * (source.m12 * source.m20 - source.m10 * source.m22)
							+ source.m02 * (source.m10 * source.m21 - source.m11 * source.m20)) * s);
		}
	}

	/**
	 * Multiply the supplied <code>source</code> matrix by the supplied
	 * <code>scalar</code> and store the result in <code>dest</code>.
	 * 
	 * @param source the source matrix
	 * @param scalar the scalar
	 * @param dest   willd hold the result
	 */
	public static void mul(Matrix4f source, float scalar, Matrix4f dest) {
		dest.m00 = source.m00 * scalar;
		dest.m01 = source.m01 * scalar;
		dest.m02 = source.m02 * scalar;
		dest.m03 = source.m03 * scalar;
		dest.m10 = source.m10 * scalar;
		dest.m11 = source.m11 * scalar;
		dest.m12 = source.m12 * scalar;
		dest.m13 = source.m13 * scalar;
		dest.m20 = source.m20 * scalar;
		dest.m21 = source.m21 * scalar;
		dest.m22 = source.m22 * scalar;
		dest.m23 = source.m23 * scalar;
		dest.m30 = source.m30 * scalar;
		dest.m31 = source.m31 * scalar;
		dest.m32 = source.m32 * scalar;
		dest.m33 = source.m33 * scalar;
	}

	/**
	 * Multiply the supplied left matrix by the right and store the result into
	 * dest.
	 */
	public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest) {
		if (dest == null) {
			dest = new Matrix4f();
		}

		if (left != dest && right != dest) {
			dest.m00 = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03;
			dest.m01 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03;
			dest.m02 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03;
			dest.m03 = left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03;
			dest.m10 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13;
			dest.m11 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13;
			dest.m12 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13;
			dest.m13 = left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13;
			dest.m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23;
			dest.m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23;
			dest.m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23;
			dest.m23 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23;
			dest.m30 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33;
			dest.m31 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33;
			dest.m32 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33;
			dest.m33 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33;
		} else {
			dest.set(left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03,
					left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03,
					left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03,
					left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03,
					left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13,
					left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13,
					left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13,
					left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13,
					left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23,
					left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23,
					left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23,
					left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23,
					left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33,
					left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33,
					left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33,
					left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33);
		}

		return dest;
	}

	public static void negate(Matrix4f src, Matrix4f dest) {
		dest.m00 = 1f - src.m00;
		dest.m01 = -src.m01;
		dest.m02 = -src.m02;
		dest.m03 = -src.m03;
		dest.m10 = -src.m10;
		dest.m11 = 1f - src.m11;
		dest.m12 = -src.m12;
		dest.m13 = -src.m13;
		dest.m20 = -src.m20;
		dest.m21 = -src.m21;
		dest.m22 = 1f - src.m22;
		dest.m23 = -src.m23;
		dest.m30 = -src.m30;
		dest.m31 = -src.m31;
		dest.m32 = -src.m32;
		dest.m33 = 1f - src.m33;
	}

	/**
	 * Set the destination matrix to a rotation matrix which rotates the given
	 * degrees about a given axis. The result will be stored in <code>dest</code>.
	 * 
	 * @param angle the angle in degrees
	 * @param axis  the axis to rotate about
	 * @param dest  will hold the result
	 */
	public static void rotation(float angle, Vector3f axis, Matrix4f dest) {
		dest.rotation(angle, axis);
	}

	/**
	 * Set the given matrix <code>dest</code> to be a simple scale matrix.
	 * 
	 * @param scale the scale applied to each dimension
	 * @param dest  will hold the result
	 */
	public static void scaling(Vector3f scale, Matrix4f dest) {
		dest.scaling(scale);
	}

	/**
	 * Transform/multiply the given vector by the given matrix and store the result
	 * in that vector.
	 * 
	 * @see Vector4f#mul(Matrix4f)
	 * 
	 * @param mat the matrix
	 * @param v   the vector to transform and to hold the final result
	 */
	public static Vector4f transform(Matrix4f mat, Vector4f v) {
		final Vector4f out = new Vector4f();
		Vector4f.mul(v, mat, out);
		return out;
	}

	/**
	 * Transpose the original matrix and store the results into the destination
	 * Matrix4f.
	 * 
	 * @param original the matrix to transpose
	 * @param dest     will contain the result
	 */
	public static void transpose(Matrix4f original, Matrix4f dest) {
		if (original != dest) {
			dest.m00 = original.m00;
			dest.m01 = original.m10;
			dest.m02 = original.m20;
			dest.m03 = original.m30;
			dest.m10 = original.m01;
			dest.m11 = original.m11;
			dest.m12 = original.m21;
			dest.m13 = original.m31;
			dest.m20 = original.m02;
			dest.m21 = original.m12;
			dest.m22 = original.m22;
			dest.m23 = original.m32;
			dest.m30 = original.m03;
			dest.m31 = original.m13;
			dest.m32 = original.m23;
			dest.m33 = original.m33;
		} else {
			dest.set(original.m00, original.m10, original.m20, original.m30, original.m01, original.m11, original.m21,
					original.m31, original.m02, original.m12, original.m22, original.m32, original.m03, original.m13,
					original.m23, original.m33);
		}
	}

	public float m00;
	public float m01;
	public float m02;
	public float m03;
	public float m10;
	public float m11;

	public float m12;

	public float m13;

	public float m20;

	public float m21;

	public float m22;

	public float m23;

	public float m30;

	public float m31;

	public float m32;

	public float m33;

	public Matrix4f() {
		super();
		identity();
	}

	/** Create a new 4x4 matrix using the supplied float values */
	public Matrix4f(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20,
			float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m03 = m03;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m13 = m13;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		this.m23 = m23;
		this.m30 = m30;
		this.m31 = m31;
		this.m32 = m32;
		this.m33 = m33;
	}

	public Matrix4f(FloatBuffer buffer) {
		m00 = buffer.get();
		m01 = buffer.get();
		m02 = buffer.get();
		m03 = buffer.get();
		m10 = buffer.get();
		m11 = buffer.get();
		m12 = buffer.get();
		m13 = buffer.get();
		m20 = buffer.get();
		m21 = buffer.get();
		m22 = buffer.get();
		m23 = buffer.get();
		m30 = buffer.get();
		m31 = buffer.get();
		m32 = buffer.get();
		m33 = buffer.get();
	}

	/**
	 * Create a new {@link Matrix4f} by setting its uppper left 3x3 submatrix to the
	 * values of the given {@link Matrix3f} and the rest to identity.
	 * 
	 * @param mat the {@link Matrix3f}
	 */
	public Matrix4f(Matrix3f mat) {
		this.m00 = mat.m00;
		this.m01 = mat.m01;
		this.m02 = mat.m02;
		this.m10 = mat.m10;
		this.m11 = mat.m11;
		this.m12 = mat.m12;
		this.m20 = mat.m20;
		this.m21 = mat.m21;
		this.m22 = mat.m22;
		this.m33 = 1.0f;
	}

	/** Clones this matrix from the supplied matrix */
	public Matrix4f(Matrix4f mat) {
		this.m00 = mat.m00;
		this.m01 = mat.m01;
		this.m02 = mat.m02;
		this.m03 = mat.m03;
		this.m10 = mat.m10;
		this.m11 = mat.m11;
		this.m12 = mat.m12;
		this.m13 = mat.m13;
		this.m20 = mat.m20;
		this.m21 = mat.m21;
		this.m22 = mat.m22;
		this.m23 = mat.m23;
		this.m30 = mat.m30;
		this.m31 = mat.m31;
		this.m32 = mat.m32;
		this.m33 = mat.m33;
	}

	/** Returns the determinant of this matrix */
	public float determinant() {
		return (m00 * m11 - m01 * m10) * (m22 * m33 - m23 * m32) - (m00 * m12 - m02 * m10) * (m21 * m33 - m23 * m31)
				+ (m00 * m13 - m03 * m10) * (m21 * m32 - m22 * m31) + (m01 * m12 - m02 * m11) * (m20 * m33 - m23 * m30)
				- (m01 * m13 - m03 * m11) * (m20 * m32 - m22 * m30) + (m02 * m13 - m03 * m12) * (m20 * m31 - m21 * m30);
	}

	/**
	 * Set the upper 3x3 matrix of this {@link Matrix4f} to the given
	 * {@link Matrix3f} and the rest to the identity.
	 * 
	 * @param mat  the 3x3 matrix
	 * @param dest the destination matrix whose upper left 3x3 submatrix will be set
	 *             to <code>mat</code>
	 * @return this
	 */
	public Matrix4f fromMatrix3(Matrix3f mat) {
		fromMatrix3(mat, this);
		return this;
	}

	/**
	 * Apply an arbitrary perspective projection frustum transformation to this
	 * matrix.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>F</code> the frustum
	 * matrix, then the new matrix will be <code>M * F</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * F * v</code> ,
	 * the frustum transformation will be applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://www.songho.ca/opengl/gl_projectionmatrix.html">http://www.songho.ca</a>
	 * 
	 * @param left   the distance along the x-axis to the left frustum edge
	 * @param right  the distance along the x-axis to the right frustum edge
	 * @param bottom the distance along the y-axis to the bottom frustum edge
	 * @param top    the distance along the y-axis to the top frustum edge
	 * @param zNear  the distance along the z-axis to the near clipping plane
	 * @param zFar   the distance along the z-axis to the far clipping plane
	 * @return this
	 */
	public Matrix4f frustum(float left, float right, float bottom, float top, float zNear, float zFar) {
		// calculate right matrix elements
		final float rm00 = 2.0f * zNear / (right - left);
		final float rm11 = 2.0f * zNear / (top - bottom);
		final float rm20 = (right + left) / (right - left);
		final float rm21 = (top + bottom) / (top - bottom);
		final float rm22 = -(zFar + zNear) / (zFar - zNear);
		final float rm32 = -2.0f * zFar * zNear / (zFar - zNear);

		// perform optimized matrix multiplication
		final float nm20 = m00 * rm20 + m10 * rm21 + this.m20 * rm22 - m30;
		final float nm21 = m01 * rm20 + m11 * rm21 + this.m21 * rm22 - m31;
		final float nm22 = m02 * rm20 + m12 * rm21 + this.m22 * rm22 - m32;
		final float nm23 = m03 * rm20 + m13 * rm21 + this.m23 * rm22 - m33;
		m00 = m00 * rm00;
		m01 = m01 * rm00;
		m02 = m02 * rm00;
		m03 = m03 * rm00;
		m10 = m10 * rm11;
		m11 = m11 * rm11;
		m12 = m12 * rm11;
		m13 = m13 * rm11;
		m30 = this.m20 * rm32;
		m31 = this.m21 * rm32;
		m32 = this.m22 * rm32;
		m33 = this.m23 * rm32;
		m20 = nm20;
		m21 = nm21;
		m22 = nm22;
		m23 = nm23;

		return this;
	}

	/**
	 * Store this matrix into the supplied float array.
	 * 
	 * @return this
	 */
	public Matrix4f get(float[] arr, int offset) {
		arr[offset + 0] = this.m00;
		arr[offset + 1] = this.m01;
		arr[offset + 2] = this.m02;
		arr[offset + 3] = this.m03;
		arr[offset + 4] = this.m10;
		arr[offset + 5] = this.m11;
		arr[offset + 6] = this.m12;
		arr[offset + 7] = this.m13;
		arr[offset + 8] = this.m20;
		arr[offset + 9] = this.m21;
		arr[offset + 10] = this.m22;
		arr[offset + 11] = this.m23;
		arr[offset + 12] = this.m30;
		arr[offset + 13] = this.m31;
		arr[offset + 14] = this.m32;
		arr[offset + 15] = this.m33;
		return this;
	}

	/**
	 * Store this matrix into the supplied {@link FloatBuffer} at the current buffer
	 * {@link FloatBuffer#position() position}.
	 * <p>
	 * This method will not increment the position of the given {@link FloatBuffer}.
	 * <p>
	 * If you want to specify the offset into the {@link FloatBuffer} at which the
	 * matrix is stored, you can use {@link #get(int, FloatBuffer)}, taking the
	 * absolute position as parameter.
	 * 
	 * @see #get(int, FloatBuffer)
	 * 
	 * @param buffer will receive the values of this matrix in column-major order at
	 *               its current position
	 * @return this
	 */
	public Matrix4f get(FloatBuffer buffer) {
		return get(buffer.position(), buffer);
	}

	/**
	 * Store this matrix into the supplied {@link FloatBuffer} starting at the
	 * specified buffer position/index.
	 * <p>
	 * This method will not increment the position of the given {@link FloatBuffer}.
	 * 
	 * @param index  the absolute position into the {@link FloatBuffer}
	 * @param buffer will receive the values of this matrix in column-major order
	 * @return this
	 */
	public Matrix4f get(int index, FloatBuffer buffer) {
		buffer.put(index, this.m00);
		buffer.put(index + 1, this.m01);
		buffer.put(index + 2, this.m02);
		buffer.put(index + 3, this.m03);
		buffer.put(index + 4, this.m10);
		buffer.put(index + 5, this.m11);
		buffer.put(index + 6, this.m12);
		buffer.put(index + 7, this.m13);
		buffer.put(index + 8, this.m20);
		buffer.put(index + 9, this.m21);
		buffer.put(index + 10, this.m22);
		buffer.put(index + 11, this.m23);
		buffer.put(index + 12, this.m30);
		buffer.put(index + 13, this.m31);
		buffer.put(index + 14, this.m32);
		buffer.put(index + 15, this.m33);
		return this;
	}

	/**
	 * Get the current values of <code>this</code> matrix and store them into
	 * <code>dest</code>.
	 * <p>
	 * This is the reverse method of {@link #set(Matrix4f)} and allows to obtain
	 * intermediate calculation results when chaining multiple transformations.
	 * 
	 * @param dest the destination matrix
	 * @return this
	 */
	public Matrix4f get(Matrix4f dest) {
		dest.set(this);
		return this;
	}

	public Vector3f getTranslation() {
		return new Vector3f(m30, m31, m32);
	}

	/**
	 * Reset this matrix to the identity.
	 * 
	 * @return this
	 */
	public Matrix4f identity() {
		this.m00 = 1.0f;
		this.m01 = 0.0f;
		this.m02 = 0.0f;
		this.m03 = 0.0f;
		this.m10 = 0.0f;
		this.m11 = 1.0f;
		this.m12 = 0.0f;
		this.m13 = 0.0f;
		this.m20 = 0.0f;
		this.m21 = 0.0f;
		this.m22 = 1.0f;
		this.m23 = 0.0f;
		this.m30 = 0.0f;
		this.m31 = 0.0f;
		this.m32 = 0.0f;
		this.m33 = 1.0f;
		return this;
	}

	/**
	 * Invert this matrix.
	 * 
	 * @return this
	 */
	public Matrix4f invert() {
		return invert(this);
	}

	/**
	 * Invert this matrix and write the result into <code>dest</code>.
	 * 
	 * @param dest
	 * @return this
	 */
	public Matrix4f invert(Matrix4f dest) {
		invert(this, dest);
		return this;
	}

	/**
	 * Apply a rotation transformation to this matrix to make <code>-z</code> point
	 * along <code>dir</code>.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>L</code> the
	 * lookalong rotation matrix, then the new matrix will be <code>M * L</code>. So
	 * when transforming a vector <code>v</code> with the new matrix by using
	 * <code>M * L * v</code> , the lookalong rotation transformation will be
	 * applied first!
	 * <p>
	 * This is equivalent to calling
	 * {@link #lookAt(float, float, float, float, float, float, float, float, float)
	 * lookAt} with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
	 * 
	 * @return this
	 */
	public Matrix4f lookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
		// Normalize direction
		final float dirLength = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
		dirX /= dirLength;
		dirY /= dirLength;
		dirZ /= dirLength;
		final float upLength = (float) Math.sqrt(upX * upX + upY * upY + upZ * upZ);
		upX /= upLength;
		upY /= upLength;
		upZ /= upLength;
		// right = direction x up
		float rightX, rightY, rightZ;
		rightX = dirY * upZ - dirZ * upY;
		rightY = dirZ * upX - dirX * upZ;
		rightZ = dirX * upY - dirY * upX;
		// up = right x direction
		upX = rightY * dirZ - rightZ * dirY;
		upY = rightZ * dirX - rightX * dirZ;
		upZ = rightX * dirY - rightY * dirX;

		// calculate right matrix elements
		final float rm00 = rightX;
		final float rm01 = upX;
		final float rm02 = -dirX;
		final float rm10 = rightY;
		final float rm11 = upY;
		final float rm12 = -dirY;
		final float rm20 = rightZ;
		final float rm21 = upZ;
		final float rm22 = -dirZ;

		// perform optimized matrix multiplication
		// introduce temporaries for dependent results
		final float nm00 = this.m00 * rm00 + m10 * rm01 + m20 * rm02;
		final float nm01 = this.m01 * rm00 + m11 * rm01 + m21 * rm02;
		final float nm02 = this.m02 * rm00 + m12 * rm01 + m22 * rm02;
		final float nm03 = this.m03 * rm00 + m13 * rm01 + m23 * rm02;
		final float nm10 = this.m00 * rm10 + this.m10 * rm11 + m20 * rm12;
		final float nm11 = this.m01 * rm10 + this.m11 * rm11 + m21 * rm12;
		final float nm12 = this.m02 * rm10 + this.m12 * rm11 + m22 * rm12;
		final float nm13 = this.m03 * rm10 + this.m13 * rm11 + m23 * rm12;
		m20 = this.m00 * rm20 + this.m10 * rm21 + this.m20 * rm22;
		m21 = this.m01 * rm20 + this.m11 * rm21 + this.m21 * rm22;
		m22 = this.m02 * rm20 + this.m12 * rm21 + this.m22 * rm22;
		m23 = this.m03 * rm20 + this.m13 * rm21 + this.m23 * rm22;
		// set the rest of the matrix elements
		m00 = nm00;
		m01 = nm01;
		m02 = nm02;
		m03 = nm03;
		m10 = nm10;
		m11 = nm11;
		m12 = nm12;
		m13 = nm13;

		return this;
	}

	/**
	 * Apply a rotation transformation to this matrix to make <code>-z</code> point
	 * along <code>dir</code>.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>L</code> the
	 * lookalong rotation matrix, then the new matrix will be <code>M * L</code>. So
	 * when transforming a vector <code>v</code> with the new matrix by using
	 * <code>M * L * v</code> , the lookalong rotation transformation will be
	 * applied first!
	 * <p>
	 * This is equivalent to calling {@link #lookAt(Vector3f, Vector3f, Vector3f)
	 * lookAt} with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
	 * 
	 * @param dir the direction in space to look along
	 * @param up  the direction of 'up'
	 * @return this
	 */
	public Matrix4f lookAlong(Vector3f dir, Vector3f up) {
		return lookAlong(dir.x, dir.y, dir.z, up.x, up.y, up.z);
	}

	/**
	 * Apply a "lookat" transformation to this matrix, that aligns <code>-z</code>
	 * with <code>center - eye</code>.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat
	 * matrix, then the new matrix will be <code>M * L</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * L * v</code> ,
	 * the lookat transformation will be applied first!
	 * <p>
	 * 
	 * @return this
	 */
	public Matrix4f lookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX,
			float upY, float upZ) {
		// Compute direction from position to lookAt
		float dirX, dirY, dirZ;
		dirX = centerX - eyeX;
		dirY = centerY - eyeY;
		dirZ = centerZ - eyeZ;
		// Normalize direction
		final float dirLength = (float) Math.sqrt((eyeX - centerX) * (eyeX - centerX)
				+ (eyeY - centerY) * (eyeY - centerY) + (eyeZ - centerZ) * (eyeZ - centerZ));
		dirX /= dirLength;
		dirY /= dirLength;
		dirZ /= dirLength;
		// Normalize up
		final float upLength = (float) Math.sqrt(upX * upX + upY * upY + upZ * upZ);
		upX /= upLength;
		upY /= upLength;
		upZ /= upLength;
		// right = direction x up
		float rightX, rightY, rightZ;
		rightX = dirY * upZ - dirZ * upY;
		rightY = dirZ * upX - dirX * upZ;
		rightZ = dirX * upY - dirY * upX;
		// up = right x direction
		upX = rightY * dirZ - rightZ * dirY;
		upY = rightZ * dirX - rightX * dirZ;
		upZ = rightX * dirY - rightY * dirX;

		// calculate right matrix elements
		final float rm00 = rightX;
		final float rm01 = upX;
		final float rm02 = -dirX;
		final float rm10 = rightY;
		final float rm11 = upY;
		final float rm12 = -dirY;
		final float rm20 = rightZ;
		final float rm21 = upZ;
		final float rm22 = -dirZ;
		final float rm30 = -rightX * eyeX - rightY * eyeY - rightZ * eyeZ;
		final float rm31 = -upX * eyeX - upY * eyeY - upZ * eyeZ;
		final float rm32 = dirX * eyeX + dirY * eyeY + dirZ * eyeZ;

		// perform optimized matrix multiplication
		// compute last column first, because others do not depend on it
		m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
		m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
		m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
		m33 = m03 * rm30 + m13 * rm31 + m23 * rm32 + m33;
		// introduce temporaries for dependent results
		final float nm00 = this.m00 * rm00 + m10 * rm01 + m20 * rm02;
		final float nm01 = this.m01 * rm00 + m11 * rm01 + m21 * rm02;
		final float nm02 = this.m02 * rm00 + m12 * rm01 + m22 * rm02;
		final float nm03 = this.m03 * rm00 + m13 * rm01 + m23 * rm02;
		final float nm10 = this.m00 * rm10 + this.m10 * rm11 + m20 * rm12;
		final float nm11 = this.m01 * rm10 + this.m11 * rm11 + m21 * rm12;
		final float nm12 = this.m02 * rm10 + this.m12 * rm11 + m22 * rm12;
		final float nm13 = this.m03 * rm10 + this.m13 * rm11 + m23 * rm12;
		m20 = this.m00 * rm20 + this.m10 * rm21 + this.m20 * rm22;
		m21 = this.m01 * rm20 + this.m11 * rm21 + this.m21 * rm22;
		m22 = this.m02 * rm20 + this.m12 * rm21 + this.m22 * rm22;
		m23 = this.m03 * rm20 + this.m13 * rm21 + this.m23 * rm22;
		// set the rest of the matrix elements
		m00 = nm00;
		m01 = nm01;
		m02 = nm02;
		m03 = nm03;
		m10 = nm10;
		m11 = nm11;
		m12 = nm12;
		m13 = nm13;

		return this;
	}

	/**
	 * Apply a "lookat" transformation to this matrix, that aligns <code>-z</code>
	 * with <code>center - eye</code>.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>L</code> the lookat
	 * matrix, then the new matrix will be <code>M * L</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * L * v</code> ,
	 * the lookat transformation will be applied first!
	 * 
	 * @param eye    the position of the camera
	 * @param center the point in space to look at
	 * @param up     the direction of 'up'
	 * @return this
	 */
	public Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up) {
		return lookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z);
	}

	/**
	 * Multiply this matrix by the supplied scalar value.
	 *
	 * @param scalar the scalar value to multiply each matrix element by
	 * @return this
	 */
	public Matrix4f mul(float scalar) {
		m00 *= scalar;
		m01 *= scalar;
		m02 *= scalar;
		m03 *= scalar;
		m10 *= scalar;
		m11 *= scalar;
		m12 *= scalar;
		m13 *= scalar;
		m20 *= scalar;
		m21 *= scalar;
		m22 *= scalar;
		m23 *= scalar;
		m30 *= scalar;
		m31 *= scalar;
		m32 *= scalar;
		m33 *= scalar;
		return this;
	}

	/**
	 * Multiply this matrix by the supplied <code>right</code> matrix and store the
	 * result in <code>this</code>.
	 *
	 * @param right the right matrix
	 * @return this
	 */
	public Matrix4f mul(Matrix4f right) {
		mul(this, right, this);
		return this;
	}

	/**
	 * Multiply this matrix by the supplied <code>right</code> matrix and store the
	 * result in <code>dest</code>.
	 * 
	 * @param right the right matrix
	 * @param dest  the destination matrix
	 * @return this
	 */
	public Matrix4f mul(Matrix4f right, Matrix4f dest) {
		mul(this, right, dest);
		return this;
	}

	/**
	 * Apply an orthographic projection transformation to this matrix.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>O</code> the
	 * orthographic projection matrix, then the new matrix will be
	 * <code>M * O</code>. So when transforming a vector <code>v</code> with the new
	 * matrix by using <code>M * O * v</code> , the orthographic projection
	 * transformation will be applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://www.songho.ca/opengl/gl_projectionmatrix.html">http://www.songho.ca</a>
	 * 
	 * @param left   the distance from the center to the left frustum edge
	 * @param right  the distance from the center to the right frustum edge
	 * @param bottom the distance from the center to the bottom frustum edge
	 * @param top    the distance from the center to the top frustum edge
	 * @param zNear  near clipping plane distance
	 * @param zFar   far clipping plane distance
	 * @return this
	 */
	public Matrix4f ortho(float left, float right, float bottom, float top, float zNear, float zFar) {
		// calculate right matrix elements
		final float rm00 = 2.0f / (right - left);
		final float rm11 = 2.0f / (top - bottom);
		final float rm22 = -2.0f / (zFar - zNear);
		final float rm30 = -(right + left) / (right - left);
		final float rm31 = -(top + bottom) / (top - bottom);
		final float rm32 = -(zFar + zNear) / (zFar - zNear);

		// perform optimized multiplication
		// compute the last column first, because other rows do not depend on it
		m30 = m00 * rm30 + m10 * rm31 + m20 * rm32 + m30;
		m31 = m01 * rm30 + m11 * rm31 + m21 * rm32 + m31;
		m32 = m02 * rm30 + m12 * rm31 + m22 * rm32 + m32;
		m33 = m03 * rm30 + m13 * rm31 + m23 * rm32 + m33;
		m00 = m00 * rm00;
		m01 = m01 * rm00;
		m02 = m02 * rm00;
		m03 = m03 * rm00;
		m10 = m10 * rm11;
		m11 = m11 * rm11;
		m12 = m12 * rm11;
		m13 = m13 * rm11;
		m20 = m20 * rm22;
		m21 = m21 * rm22;
		m22 = m22 * rm22;
		m23 = m23 * rm22;

		return this;
	}

	/**
	 * Apply a symmetric perspective projection frustum transformation to this
	 * matrix.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>P</code> the
	 * perspective projection matrix, then the new matrix will be
	 * <code>M * P</code>. So when transforming a vector <code>v</code> with the new
	 * matrix by using <code>M * P * v</code> , the perspective projection will be
	 * applied first!
	 * <p>
	 * 
	 * @param fovy   the vertical field of view in degrees
	 * @param aspect the aspect ratio (i.e. width / height)
	 * @param zNear  near clipping plane distance
	 * @param zFar   far clipping plane distance
	 * @return this
	 */
	public Matrix4f perspective(float fovy, float aspect, float zNear, float zFar) {
		final float h = (float) Math.tan(Math.toRadians(fovy) * 0.5f) * zNear;
		final float w = h * aspect;
		final float fl = -w;
		final float fr = +w;
		final float fb = -h;
		final float ft = +h;
		return frustum(fl, fr, fb, ft, zNear, zFar);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		m00 = in.readFloat();
		m01 = in.readFloat();
		m02 = in.readFloat();
		m03 = in.readFloat();
		m10 = in.readFloat();
		m11 = in.readFloat();
		m12 = in.readFloat();
		m13 = in.readFloat();
		m20 = in.readFloat();
		m21 = in.readFloat();
		m22 = in.readFloat();
		m23 = in.readFloat();
		m30 = in.readFloat();
		m31 = in.readFloat();
		m32 = in.readFloat();
		m33 = in.readFloat();
	}

	/**
	 * Apply the rotation transformation of the given {@link AngleAxis4f} to this
	 * matrix.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation
	 * matrix obtained from the given angle-axis, then the new matrix will be
	 * <code>M * A</code>. So when transforming a vector <code>v</code> with the new
	 * matrix by using <code>M * A * v</code> , the angle-axis rotation will be
	 * applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
	 * 
	 * @param angleAxis the {@link AngleAxis4f} (needs to be
	 *                  {@link AngleAxis4f#normalize() normalized})
	 * @return this
	 */
	public Matrix4f rotate(AngleAxis4f angleAxis) {
		return rotate(angleAxis.angle, angleAxis.x, angleAxis.y, angleAxis.z);
	}

	/**
	 * Apply rotation to this matrix by rotating the given amount of degrees about
	 * the given axis specified as x, y and z components.
	 * <p>
	 * The axis described by the three components needs to be a unit vector.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation
	 * matrix, then the new matrix will be <code>M * R</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * R * v</code> ,
	 * the rotation will be applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @param x   the x component of the axis
	 * @param y   the y component of the axis
	 * @param z   the z component of the axis
	 * @return this
	 */
	public Matrix4f rotate(float ang, float x, float y, float z) {
		final float s = (float) Math.sin(Math.toRadians(ang));
		final float c = (float) Math.cos(Math.toRadians(ang));
		final float C = 1.0f - c;

		// rotation matrix elements:
		// m30, m31, m32, m03, m13, m23 = 0
		// m33 = 1
		final float rm00 = x * x * C + c;
		final float rm01 = y * x * C + z * s;
		final float rm02 = z * x * C - y * s;
		final float rm10 = x * y * C - z * s;
		final float rm11 = y * y * C + c;
		final float rm12 = z * y * C + x * s;
		final float rm20 = x * z * C + y * s;
		final float rm21 = y * z * C - x * s;
		final float rm22 = z * z * C + c;

		// add temporaries for dependent values
		final float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
		final float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
		final float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
		final float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
		final float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
		final float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
		final float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
		final float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
		// set non-dependent values directly
		m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
		m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
		m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
		m23 = m03 * rm20 + m13 * rm21 + m23 * rm22;
		// set other values
		m00 = nm00;
		m01 = nm01;
		m02 = nm02;
		m03 = nm03;
		m10 = nm10;
		m11 = nm11;
		m12 = nm12;
		m13 = nm13;

		return this;
	}

	/**
	 * Apply the rotation transformation of the given axis and angle to this matrix.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>A</code> the rotation
	 * matrix obtained from the given angle-axis, then the new matrix will be
	 * <code>M * A</code>. So when transforming a vector <code>v</code> with the new
	 * matrix by using <code>M * A * v</code> , the angle-axis rotation will be
	 * applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
	 * 
	 * @param angle the angle in degrees
	 * @param axis  the rotation axis (needs to be {@link Vector3f#normalize()
	 *              normalized})
	 * @return this
	 */
	public Matrix4f rotate(float angle, Vector3f axis) {
		return rotate(angle, axis.x, axis.y, axis.z);
	}

	/**
	 * Apply the rotation transformation of the given {@link Quaternion} to this
	 * matrix.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>Q</code> the rotation
	 * matrix obtained from the given quaternion, then the new matrix will be
	 * <code>M * Q</code>. So when transforming a vector <code>v</code> with the new
	 * matrix by using <code>M * Q * v</code> , the quaternion rotation will be
	 * applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
	 * 
	 * @param quat the {@link Quaternion}
	 * @return this
	 */
	public Matrix4f rotate(Quaternion quat) {
		final float q00 = 2.0f * quat.x * quat.x;
		final float q11 = 2.0f * quat.y * quat.y;
		final float q22 = 2.0f * quat.z * quat.z;
		final float q01 = 2.0f * quat.x * quat.y;
		final float q02 = 2.0f * quat.x * quat.z;
		final float q03 = 2.0f * quat.x * quat.w;
		final float q12 = 2.0f * quat.y * quat.z;
		final float q13 = 2.0f * quat.y * quat.w;
		final float q23 = 2.0f * quat.z * quat.w;

		final float rm00 = 1.0f - q11 - q22;
		final float rm01 = q01 + q23;
		final float rm02 = q02 - q13;
		final float rm10 = q01 - q23;
		final float rm11 = 1.0f - q22 - q00;
		final float rm12 = q12 + q03;
		final float rm20 = q02 + q13;
		final float rm21 = q12 - q03;
		final float rm22 = 1.0f - q11 - q00;

		final float nm00 = m00 * rm00 + m10 * rm01 + m20 * rm02;
		final float nm01 = m01 * rm00 + m11 * rm01 + m21 * rm02;
		final float nm02 = m02 * rm00 + m12 * rm01 + m22 * rm02;
		final float nm03 = m03 * rm00 + m13 * rm01 + m23 * rm02;
		final float nm10 = m00 * rm10 + m10 * rm11 + m20 * rm12;
		final float nm11 = m01 * rm10 + m11 * rm11 + m21 * rm12;
		final float nm12 = m02 * rm10 + m12 * rm11 + m22 * rm12;
		final float nm13 = m03 * rm10 + m13 * rm11 + m23 * rm12;
		m20 = m00 * rm20 + m10 * rm21 + m20 * rm22;
		m21 = m01 * rm20 + m11 * rm21 + m21 * rm22;
		m22 = m02 * rm20 + m12 * rm21 + m22 * rm22;
		m23 = m03 * rm20 + m13 * rm21 + m23 * rm22;
		m00 = nm00;
		m01 = nm01;
		m02 = nm02;
		m03 = nm03;
		m10 = nm10;
		m11 = nm11;
		m12 = nm12;
		m13 = nm13;

		return this;
		
		/*float n = (float) (1.0f/Math.sqrt(quat.x*quat.x+quat.y*quat.y+quat.z*quat.z+quat.w*quat.w));
		float qx = quat.x*n;
		float qy = quat.y*n;
		float qz = quat.z*n;
		float qw = quat.w*n;
		
		Matrix4f q = new Matrix4f(1.0f - 2.0f*qy*qy - 2.0f*qz*qz, 2.0f*qx*qy - 2.0f*qz*qw, 2.0f*qx*qz + 2.0f*qy*qw, 0.0f,
			    2.0f*qx*qy + 2.0f*qz*qw, 1.0f - 2.0f*qx*qx - 2.0f*qz*qz, 2.0f*qy*qz - 2.0f*qx*qw, 0.0f,
			    2.0f*qx*qz - 2.0f*qy*qw, 2.0f*qy*qz + 2.0f*qx*qw, 1.0f - 2.0f*qx*qx - 2.0f*qy*qy, 0.0f,
			    0.0f, 0.0f, 0.0f, 1.0f);
		
		this.set(q.mul(this));
		return this;*/
	}

	public void rotate(Vector3f rotation) {
		rotateX(rotation.x);
		rotateY(rotation.y);
		rotateZ(rotation.z);
	}

	/**
	 * Apply rotation about the X axis to this matrix by rotating the given amount
	 * of degrees.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation
	 * matrix, then the new matrix will be <code>M * R</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * R * v</code> ,
	 * the rotation will be applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @return this
	 */
	public Matrix4f rotateX(float ang) {
		final float cos = (float) Math.cos(Math.toRadians(ang));
		final float sin = (float) Math.sin(Math.toRadians(ang));
		final float rm11 = cos;
		final float rm12 = sin;
		final float rm21 = -sin;
		final float rm22 = cos;

		// add temporaries for dependent values
		final float nm10 = m10 * rm11 + m20 * rm12;
		final float nm11 = m11 * rm11 + m21 * rm12;
		final float nm12 = m12 * rm11 + m22 * rm12;
		final float nm13 = m13 * rm11 + m23 * rm12;
		// set non-dependent values directly
		m20 = m10 * rm21 + m20 * rm22;
		m21 = m11 * rm21 + m21 * rm22;
		m22 = m12 * rm21 + m22 * rm22;
		m23 = m13 * rm21 + m23 * rm22;
		// set other values
		m10 = nm10;
		m11 = nm11;
		m12 = nm12;
		m13 = nm13;
		return this;
	}

	/**
	 * Apply rotation about the Y axis to this matrix by rotating the given amount
	 * of degrees.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation
	 * matrix, then the new matrix will be <code>M * R</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * R * v</code> ,
	 * the rotation will be applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @return this
	 */
	public Matrix4f rotateY(float ang) {
		final float cos = (float) Math.cos(Math.toRadians(ang));
		final float sin = (float) Math.sin(Math.toRadians(ang));
		final float rm00 = cos;
		final float rm02 = -sin;
		final float rm20 = sin;
		final float rm22 = cos;

		// add temporaries for dependent values
		final float nm00 = m00 * rm00 + m20 * rm02;
		final float nm01 = m01 * rm00 + m21 * rm02;
		final float nm02 = m02 * rm00 + m22 * rm02;
		final float nm03 = m03 * rm00 + m23 * rm02;
		// set non-dependent values directly
		m20 = m00 * rm20 + m20 * rm22;
		m21 = m01 * rm20 + m21 * rm22;
		m22 = m02 * rm20 + m22 * rm22;
		m23 = m03 * rm20 + m23 * rm22;
		// set other values
		m00 = nm00;
		m01 = nm01;
		m02 = nm02;
		m03 = nm03;
		return this;
	}

	/**
	 * Apply rotation about the Z axis to this matrix by rotating the given amount
	 * of degrees.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>R</code> the rotation
	 * matrix, then the new matrix will be <code>M * R</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * R * v</code> ,
	 * the rotation will be applied first!
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @return this
	 */
	public Matrix4f rotateZ(float ang) {
		final float cos = (float) Math.cos(Math.toRadians(ang));
		final float sin = (float) Math.sin(Math.toRadians(ang));
		final float rm00 = cos;
		final float rm01 = sin;
		final float rm10 = -sin;
		final float rm11 = cos;

		// add temporaries for dependent values
		final float nm00 = m00 * rm00 + m10 * rm01;
		final float nm01 = m01 * rm00 + m11 * rm01;
		final float nm02 = m02 * rm00 + m12 * rm01;
		final float nm03 = m03 * rm00 + m13 * rm01;
		final float nm10 = m00 * rm10 + m10 * rm11;
		final float nm11 = m01 * rm10 + m11 * rm11;
		final float nm12 = m02 * rm10 + m12 * rm11;
		final float nm13 = m03 * rm10 + m13 * rm11;
		// set other values
		m00 = nm00;
		m01 = nm01;
		m02 = nm02;
		m03 = nm03;
		m10 = nm10;
		m11 = nm11;
		m12 = nm12;
		m13 = nm13;
		return this;
	}

	/**
	 * Set this matrix to a rotation transformation rotation of the given
	 * {@link AngleAxis4f}.
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Axis_and_angle">http://en.wikipedia.org</a>
	 * 
	 * @param angleAxis the {@link AngleAxis4f} (needs to be
	 *                  {@link AngleAxis4f#normalize() normalized})
	 * @return this
	 */
	public Matrix4f rotation(AngleAxis4f angleAxis) {
		return rotation(angleAxis.angle, angleAxis.x, angleAxis.y, angleAxis.z);
	}

	/**
	 * Set this matrix to a rotation matrix which rotates the given degrees about a
	 * given axis.
	 * <p>
	 * The axis described by the three components needs to be a unit vector.
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Rotation_matrix_from_axis_and_angle">http://en.wikipedia.org</a>
	 * 
	 * @param angle the angle in degrees
	 * @param x     the x-component of the rotation axis
	 * @param y     the y-component of the rotation axis
	 * @param z     the z-component of the rotation axis
	 * @return this
	 */
	public Matrix4f rotation(float angle, float x, float y, float z) {
		final float cos = (float) Math.cos(Math.toRadians(angle));
		final float sin = (float) Math.sin(Math.toRadians(angle));
		final float C = 1.0f - cos;
		m00 = cos + x * x * C;
		m10 = x * y * C - z * sin;
		m20 = x * z * C + y * sin;
		m30 = 0.0f;
		m01 = y * x * C + z * sin;
		m11 = cos + y * y * C;
		m21 = y * z * C - x * sin;
		m31 = 0.0f;
		m02 = z * x * C - y * sin;
		m12 = z * y * C + x * sin;
		m22 = cos + z * z * C;
		m32 = 0.0f;
		m03 = 0.0f;
		m13 = 0.0f;
		m23 = 0.0f;
		m33 = 1.0f;
		return this;
	}

	/**
	 * Set this matrix to a rotation matrix which rotates the given degrees about a
	 * given axis.
	 * 
	 * @param angle the angle in degrees
	 * @param axis  the axis to rotate about (needs to be
	 *              {@link Vector3f#normalize() normalized})
	 * @return this
	 */
	public Matrix4f rotation(float angle, Vector3f axis) {
		return rotation(angle, axis.x, axis.y, axis.z);
	}

	/**
	 * Set this matrix to the rotation transformation of the given
	 * {@link Quaternion}.
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion">http://en.wikipedia.org</a>
	 * 
	 * @param quat the {@link Quaternion}
	 * @return this
	 */
	public Matrix4f rotation(Quaternion quat) {
		final float q00 = 2.0f * quat.x * quat.x;
		final float q11 = 2.0f * quat.y * quat.y;
		final float q22 = 2.0f * quat.z * quat.z;
		final float q01 = 2.0f * quat.x * quat.y;
		final float q02 = 2.0f * quat.x * quat.z;
		final float q03 = 2.0f * quat.x * quat.w;
		final float q12 = 2.0f * quat.y * quat.z;
		final float q13 = 2.0f * quat.y * quat.w;
		final float q23 = 2.0f * quat.z * quat.w;

		m00 = 1.0f - q11 - q22;
		m01 = q01 + q23;
		m02 = q02 - q13;
		m03 = 0.0f;
		m10 = q01 - q23;
		m11 = 1.0f - q22 - q00;
		m12 = q12 + q03;
		m13 = 0.0f;
		m20 = q02 + q13;
		m21 = q12 - q03;
		m22 = 1.0f - q11 - q00;
		m23 = 0.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = 0.0f;
		m33 = 1.0f;

		return this;
	}

	/**
	 * Set this matrix to a rotation transformation about the X axis.
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @return this
	 */
	public Matrix4f rotationX(float ang) {
		final float cos = (float) Math.cos(Math.toRadians(ang));
		final float sin = (float) Math.sin(Math.toRadians(ang));
		m00 = 1.0f;
		m01 = 0.0f;
		m02 = 0.0f;
		m03 = 0.0f;
		m10 = 0.0f;
		m11 = cos;
		m12 = sin;
		m13 = 0.0f;
		m20 = 0.0f;
		m21 = -sin;
		m22 = cos;
		m23 = 0.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = 0.0f;
		m33 = 1.0f;
		return this;
	}

	/**
	 * Set this matrix to a rotation transformation about the Y axis.
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @return this
	 */
	public Matrix4f rotationY(float ang) {
		final float cos = (float) Math.cos(Math.toRadians(ang));
		final float sin = (float) Math.sin(Math.toRadians(ang));
		m00 = cos;
		m01 = 0.0f;
		m02 = -sin;
		m03 = 0.0f;
		m10 = 0.0f;
		m11 = 1.0f;
		m12 = 0.0f;
		m13 = 0.0f;
		m20 = sin;
		m21 = 0.0f;
		m22 = cos;
		m23 = 0.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = 0.0f;
		m33 = 1.0f;
		return this;
	}

	/**
	 * Set this matrix to a rotation transformation about the Z axis.
	 * <p>
	 * Reference: <a href=
	 * "http://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations">http://en.wikipedia.org</a>
	 * 
	 * @param ang the angle in degrees
	 * @return this
	 */
	public Matrix4f rotationZ(float ang) {
		final float cos = (float) Math.cos(Math.toRadians(ang));
		final float sin = (float) Math.sin(Math.toRadians(ang));
		m00 = cos;
		m01 = sin;
		m02 = 0.0f;
		m03 = 0.0f;
		m10 = -sin;
		m11 = cos;
		m12 = 0.0f;
		m13 = 0.0f;
		m20 = 0.0f;
		m21 = 0.0f;
		m22 = 0.0f;
		m23 = 0.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = 0.0f;
		m33 = 1.0f;
		return this;
	}

	/**
	 * Apply scaling to this matrix by uniformly scaling all unit axes by the given
	 * <code>xyz</code> factor.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling
	 * matrix, then the new matrix will be <code>M * S</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * S * v</code> ,
	 * the scaling will be applied first!
	 * 
	 * @see #scale(float, float, float)
	 * 
	 * @param xyz the factor for all components
	 * @return this
	 */
	public Matrix4f scale(float xyz) {
		return scale(xyz, xyz, xyz);
	}

	/**
	 * Apply scaling to this matrix by scaling the unit axes by the given x, y and z
	 * factors.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>S</code> the scaling
	 * matrix, then the new matrix will be <code>M * S</code>. So when transforming
	 * a vector <code>v</code> with the new matrix by using <code>M * S * v</code> ,
	 * the scaling will be applied first!
	 * 
	 * @param x the factor of the x component
	 * @param y the factor of the y component
	 * @param z the factor of the z component
	 * @return this
	 */
	public Matrix4f scale(float x, float y, float z) {
		// scale matrix elements:
		// m00 = x, m11 = y, m22 = z
		// m33 = 1
		// all others = 0
		m00 = m00 * x;
		m01 = m01 * x;
		m02 = m02 * x;
		m03 = m03 * x;
		m10 = m10 * y;
		m11 = m11 * y;
		m12 = m12 * y;
		m13 = m13 * y;
		m20 = m20 * z;
		m21 = m21 * z;
		m22 = m22 * z;
		m23 = m23 * z;
		return this;
	}

	/**
	 * Set this matrix to be a simple scale matrix.
	 * 
	 * @param x the scale in x
	 * @param y the scale in y
	 * @param z the scale in z
	 * @return this
	 */
	public Matrix4f scaling(float x, float y, float z) {
		m00 = x;
		m01 = 0.0f;
		m02 = 0.0f;
		m03 = 0.0f;
		m10 = 0.0f;
		m11 = y;
		m12 = 0.0f;
		m13 = 0.0f;
		m20 = 0.0f;
		m21 = 0.0f;
		m22 = z;
		m23 = 0.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = 0.0f;
		m33 = 1.0f;
		return this;
	}

	/**
	 * Set this matrix to be a simple scale matrix.
	 * 
	 * @param x the scale in x
	 * @param y the scale in y
	 * @param z the scale in z
	 * @return this
	 */
	public Matrix4f scaling(float x, float y, float z, Matrix4f dest) {
		return dest.scaling(x, y, z);
	}

	/**
	 * Set this matrix to be a simple scale matrix.
	 * 
	 * @param scale the scale applied to each dimension
	 * @return this
	 */
	public Matrix4f scaling(Vector3f scale) {
		return scaling(scale.x, scale.y, scale.z);
	}

	/**
	 * Set the values in the matrix using a float array. The results will look like
	 * this:<br>
	 * <br>
	 * 
	 * 0, 4, 8, 12<br>
	 * 1, 5, 9, 13<br>
	 * 2, 6, 10, 14<br>
	 * 3, 7, 11, 15<br>
	 * 
	 * @see #set(float[], int)
	 * 
	 * @return this
	 */
	public Matrix4f set(float m[]) {
		return set(m, 0);
	}

	/**
	 * Set the values within this matrix to the supplied float values. The matrix
	 * will look like this:<br>
	 * <br>
	 *
	 * m00, m10, m20, m30<br>
	 * m01, m11, m21, m31<br>
	 * m02, m12, m22, m32<br>
	 * m03, m13, m23, m33
	 * 
	 * @return this
	 */
	public Matrix4f set(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13,
			float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m03 = m03;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m13 = m13;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		this.m23 = m23;
		this.m30 = m30;
		this.m31 = m31;
		this.m32 = m32;
		this.m33 = m33;
		return this;
	}

	/**
	 * Set the values in the matrix using a float array. The results will look like
	 * this:<br>
	 * <br>
	 * 
	 * 0, 4, 8, 12<br>
	 * 1, 5, 9, 13<br>
	 * 2, 6, 10, 14<br>
	 * 3, 7, 11, 15<br>
	 * 
	 * @return this
	 */
	public Matrix4f set(float m[], int off) {
		m00 = m[off + 0];
		m01 = m[off + 1];
		m02 = m[off + 2];
		m03 = m[off + 3];
		m10 = m[off + 4];
		m11 = m[off + 5];
		m12 = m[off + 6];
		m13 = m[off + 7];
		m20 = m[off + 8];
		m21 = m[off + 9];
		m22 = m[off + 10];
		m23 = m[off + 11];
		m30 = m[off + 12];
		m31 = m[off + 13];
		m32 = m[off + 14];
		m33 = m[off + 15];
		return this;
	}

	public Matrix4f setRow(float m[], int row) {
		switch(row) {
		case 0:
			m00 = m[0];
			m01 = m[1];
			m02 = m[2];
			m03 = m[3];
			break;
		case 1:
			m10 = m[0];
			m11 = m[1];
			m12 = m[2];
			m13 = m[3];
			break;
		case 2:
			m20 = m[0];
			m21 = m[1];
			m22 = m[2];
			m23 = m[3];
		default:
			m30 = m[0];
			m31 = m[1];
			m32 = m[2];
			m33 = m[3];
		}
		return this;
	}
	
	/**
	 * Set the values in the matrix using a FloatBuffer. The results will look like
	 * this:<br>
	 * <br>
	 * 
	 * 0, 4, 8, 12<br>
	 * 1, 5, 9, 13<br>
	 * 2, 6, 10, 14<br>
	 * 3, 7, 11, 15<br>
	 * 
	 * @return this
	 */
	public Matrix4f set(FloatBuffer buffer) {
		m00 = buffer.get();
		m01 = buffer.get();
		m02 = buffer.get();
		m03 = buffer.get();
		m10 = buffer.get();
		m11 = buffer.get();
		m12 = buffer.get();
		m13 = buffer.get();
		m20 = buffer.get();
		m21 = buffer.get();
		m22 = buffer.get();
		m23 = buffer.get();
		m30 = buffer.get();
		m31 = buffer.get();
		m32 = buffer.get();
		m33 = buffer.get();
		return this;
	}

	/**
	 * Set the upper left 3x3 submatrix of this {@link Matrix4f} to the given
	 * {@link Matrix3f} and the rest to identity.
	 * 
	 * @param mat the {@link Matrix3f}
	 * @return this
	 */
	public Matrix4f set(Matrix3f mat) {
		this.m00 = mat.m00;
		this.m01 = mat.m01;
		this.m02 = mat.m02;
		this.m03 = 0.0f;
		this.m10 = mat.m10;
		this.m11 = mat.m11;
		this.m12 = mat.m12;
		this.m13 = 0.0f;
		this.m20 = mat.m20;
		this.m21 = mat.m21;
		this.m22 = mat.m22;
		this.m23 = 0.0f;
		this.m30 = 0.0f;
		this.m31 = 0.0f;
		this.m32 = 0.0f;
		this.m33 = 1.0f;
		return this;
	}

	/**
	 * Set the values within this matrix to be the same as the supplied matrix.
	 * 
	 * @return this
	 */
	public Matrix4f set(Matrix4f m1) {
		this.m00 = m1.m00;
		this.m01 = m1.m01;
		this.m02 = m1.m02;
		this.m03 = m1.m03;
		this.m10 = m1.m10;
		this.m11 = m1.m11;
		this.m12 = m1.m12;
		this.m13 = m1.m13;
		this.m20 = m1.m20;
		this.m21 = m1.m21;
		this.m22 = m1.m22;
		this.m23 = m1.m23;
		this.m30 = m1.m30;
		this.m31 = m1.m31;
		this.m32 = m1.m32;
		this.m33 = m1.m33;
		return this;
	}

	/**
	 * Set this matrix to be an arbitrary perspective projection frustum
	 * transformation.
	 * <p>
	 * Reference: <a href=
	 * "http://www.songho.ca/opengl/gl_projectionmatrix.html">http://www.songho.ca</a>
	 * 
	 * @param left   the distance along the x-axis to the left frustum edge
	 * @param right  the distance along the x-axis to the right frustum edge
	 * @param bottom the distance along the y-axis to the bottom frustum edge
	 * @param top    the distance along the y-axis to the top frustum edge
	 * @param zNear  the distance along the z-axis to the near clipping plane
	 * @param zFar   the distance along the z-axis to the far clipping plane
	 * @return this
	 */
	public Matrix4f setFrustum(float left, float right, float bottom, float top, float zNear, float zFar) {
		m00 = 2.0f * zNear / (right - left);
		m01 = 0.0f;
		m02 = 0.0f;
		m03 = 0.0f;
		m10 = 0.0f;
		m11 = 2.0f * zNear / (top - bottom);
		m12 = 0.0f;
		m13 = 0.0f;
		m20 = (right + left) / (right - left);
		m21 = (top + bottom) / (top - bottom);
		m22 = -(zFar + zNear) / (zFar - zNear);
		m23 = -1.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = -2.0f * zFar * zNear / (zFar - zNear);
		m33 = 0.0f;
		return this;
	}

	/**
	 * Set this matrix to a rotation transformation to make <code>-z</code> point
	 * along <code>dir</code>.
	 * <p>
	 * This is equivalent to calling
	 * {@link #setLookAt(float, float, float, float, float, float, float, float, float)
	 * setLookAt} with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
	 * 
	 * @return this
	 */
	public Matrix4f setLookAlong(float dirX, float dirY, float dirZ, float upX, float upY, float upZ) {
		// Normalize direction
		final float dirLength = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
		dirX /= dirLength;
		dirY /= dirLength;
		dirZ /= dirLength;
		final float upLength = (float) Math.sqrt(upX * upX + upY * upY + upZ * upZ);
		upX /= upLength;
		upY /= upLength;
		upZ /= upLength;
		// right = direction x up
		float rightX, rightY, rightZ;
		rightX = dirY * upZ - dirZ * upY;
		rightY = dirZ * upX - dirX * upZ;
		rightZ = dirX * upY - dirY * upX;
		// up = right x direction
		upX = rightY * dirZ - rightZ * dirY;
		upY = rightZ * dirX - rightX * dirZ;
		upZ = rightX * dirY - rightY * dirX;

		m00 = rightX;
		m01 = upX;
		m02 = -dirX;
		m03 = 0.0f;
		m10 = rightY;
		m11 = upY;
		m12 = -dirY;
		m13 = 0.0f;
		m20 = rightZ;
		m21 = upZ;
		m22 = -dirZ;
		m23 = 0.0f;
		m30 = 0.0f;
		m31 = 0.0f;
		m32 = 0.0f;
		m33 = 1.0f;

		return this;
	}

	/**
	 * Set this matrix to a rotation transformation to make <code>-z</code> point
	 * along <code>dir</code>.
	 * <p>
	 * This is equivalent to calling {@link #setLookAt(Vector3f, Vector3f, Vector3f)
	 * setLookAt} with <code>eye = (0, 0, 0)</code> and <code>center = dir</code>.
	 * 
	 * @param dir the direction in space to look along
	 * @param up  the direction of 'up'
	 * @return this
	 */
	public Matrix4f setLookAlong(Vector3f dir, Vector3f up) {
		return setLookAlong(dir.x, dir.y, dir.z, up.x, up.y, up.z);
	}

	/**
	 * Set this matrix to be a "lookat" transformation, that aligns <code>-z</code>
	 * with <code>center - eye</code>.
	 * <p>
	 * 
	 * @return this
	 */
	public Matrix4f setLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ,
			float upX, float upY, float upZ) {
		// Compute direction from position to lookAt
		float dirX, dirY, dirZ;
		dirX = centerX - eyeX;
		dirY = centerY - eyeY;
		dirZ = centerZ - eyeZ;
		// Normalize direction
		final float dirLength = (float) Math.sqrt((eyeX - centerX) * (eyeX - centerX)
				+ (eyeY - centerY) * (eyeY - centerY) + (eyeZ - centerZ) * (eyeZ - centerZ));
		dirX /= dirLength;
		dirY /= dirLength;
		dirZ /= dirLength;
		// Normalize up
		final float upLength = (float) Math.sqrt(upX * upX + upY * upY + upZ * upZ);
		upX /= upLength;
		upY /= upLength;
		upZ /= upLength;
		// right = direction x up
		float rightX, rightY, rightZ;
		rightX = dirY * upZ - dirZ * upY;
		rightY = dirZ * upX - dirX * upZ;
		rightZ = dirX * upY - dirY * upX;
		// up = right x direction
		upX = rightY * dirZ - rightZ * dirY;
		upY = rightZ * dirX - rightX * dirZ;
		upZ = rightX * dirY - rightY * dirX;

		m00 = rightX;
		m01 = upX;
		m02 = -dirX;
		m03 = 0.0f;
		m10 = rightY;
		m11 = upY;
		m12 = -dirY;
		m13 = 0.0f;
		m20 = rightZ;
		m21 = upZ;
		m22 = -dirZ;
		m23 = 0.0f;
		m30 = -rightX * eyeX - rightY * eyeY - rightZ * eyeZ;
		m31 = -upX * eyeX - upY * eyeY - upZ * eyeZ;
		m32 = dirX * eyeX + dirY * eyeY + dirZ * eyeZ;
		m33 = 1.0f;

		return this;
	}

	/**
	 * Set this matrix to be a "lookat" transformation, that aligns <code>-z</code>
	 * with <code>center - eye</code>.
	 * 
	 * @param eye    the position of the camera
	 * @param center the point in space to look at
	 * @param up     the direction of 'up'
	 * @return this
	 */
	public Matrix4f setLookAt(Vector3f eye, Vector3f center, Vector3f up) {
		return setLookAt(eye.x, eye.y, eye.z, center.x, center.y, center.z, up.x, up.y, up.z);
	}

	/**
	 * Set this matrix to be an orthographic projection transformation.
	 * <p>
	 * Reference: <a href=
	 * "http://www.songho.ca/opengl/gl_projectionmatrix.html">http://www.songho.ca</a>
	 * 
	 * @param left   the distance from the center to the left frustum edge
	 * @param right  the distance from the center to the right frustum edge
	 * @param bottom the distance from the center to the bottom frustum edge
	 * @param top    the distance from the center to the top frustum edge
	 * @param zNear  near clipping plane distance
	 * @param zFar   far clipping plane distance
	 * @return this
	 */
	public Matrix4f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar) {
		m00 = 2.0f / (right - left);
		m01 = 0.0f;
		m02 = 0.0f;
		m03 = 0.0f;
		m10 = 0.0f;
		m11 = 2.0f / (top - bottom);
		m12 = 0.0f;
		m13 = 0.0f;
		m20 = 0.0f;
		m21 = 0.0f;
		m22 = -2.0f / (zFar - zNear);
		m23 = 0.0f;
		m30 = -(right + left) / (right - left);
		m31 = -(top + bottom) / (top - bottom);
		m32 = -(zFar + zNear) / (zFar - zNear);
		m33 = 1.0f;
		return this;
	}

	/**
	 * Set this matrix to be a symmetric perspective projection frustum
	 * transformation.
	 * <p>
	 * 
	 * @param fovy   the vertical field of view in degrees
	 * @param aspect the aspect ratio (i.e. width / height)
	 * @param zNear  near clipping plane distance
	 * @param zFar   far clipping plane distance
	 * @return this
	 */
	public Matrix4f setPerspective(float fovy, float aspect, float zNear, float zFar) {
		final float h = (float) Math.tan(Math.toRadians(fovy) * 0.5f) * zNear;
		final float w = h * aspect;
		final float fl = -w;
		final float fr = +w;
		final float fb = -h;
		final float ft = +h;
		return setFrustum(fl, fr, fb, ft, zNear, zFar);
	}

	/**
	 * Store this matrix in a float buffer. The matrix is stored in column major
	 * (openGL) order.
	 * 
	 * @param buf The buffer to store this matrix in
	 */
	public Matrix4f store(FloatBuffer buf) {
		buf.put(m00);
		buf.put(m01);
		buf.put(m02);
		buf.put(m03);
		buf.put(m10);
		buf.put(m11);
		buf.put(m12);
		buf.put(m13);
		buf.put(m20);
		buf.put(m21);
		buf.put(m22);
		buf.put(m23);
		buf.put(m30);
		buf.put(m31);
		buf.put(m32);
		buf.put(m33);
		return this;
	}

	public Matrix3f toMatrix3f() {
		final Matrix3f mat3 = new Matrix3f();

		mat3.m00 = m00;
		mat3.m01 = m01;
		mat3.m02 = m02;
		mat3.m10 = m10;
		mat3.m11 = m11;
		mat3.m12 = m12;
		mat3.m20 = m20;
		mat3.m21 = m21;
		mat3.m22 = m22;

		return mat3;
	}

	@Override
	public String toString() {
		final DecimalFormat formatter = new DecimalFormat("0.000E0");
		return "Matrix4f { " + formatter.format(this.m00) + ", " + formatter.format(this.m10) + ", "
				+ formatter.format(this.m20) + ", " + formatter.format(this.m30) + ",\n" + "           "
				+ formatter.format(this.m01) + ", " + formatter.format(this.m11) + ", " + formatter.format(this.m21)
				+ ", " + formatter.format(this.m31) + ",\n" + "           " + formatter.format(this.m02) + ", "
				+ formatter.format(this.m12) + ", " + formatter.format(this.m22) + ", " + formatter.format(this.m32)
				+ ",\n" + "           " + formatter.format(this.m03) + ", " + formatter.format(this.m13) + ", "
				+ formatter.format(this.m23) + ", " + formatter.format(this.m33) + " }\n";
	}

	public void transform(Vector3f v) {
		v.mul(this);
	}

	/**
	 * Transform/multiply the given vector by this matrix and store the result in
	 * that vector.
	 * 
	 * @see Vector4f#mul(Matrix4f)
	 * 
	 * @param v the vector to transform and to hold the final result
	 * @return this
	 */
	public Matrix4f transform(Vector4f v) {
		v.mul(this);
		return this;
	}

	/**
	 * Transform/multiply the given vector by this matrix and store the result in
	 * <code>dest</code>.
	 * 
	 * @see Vector4f#mul(Matrix4f, Vector4f)
	 * 
	 * @param v    the vector to transform
	 * @param dest will contain the result
	 * @return this
	 */
	public Matrix4f transform(Vector4f v, Vector4f dest) {
		v.mul(this, dest);
		return this;
	}

	/**
	 * Apply a translation to this matrix by translating by the given number of
	 * units in x, y and z.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>T</code> the
	 * translation matrix, then the new matrix will be <code>M * T</code>. So when
	 * transforming a vector <code>v</code> with the new matrix by using
	 * <code>M * T * v</code>, the translation will be applied first!
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return this
	 */
	public Matrix4f translate(float x, float y, float z) {
		final Matrix4f c = this;
		// translation matrix elements:
		// m00, m11, m22, m33 = 1
		// m30 = x, m31 = y, m32 = z
		// all others = 0
		c.m30 = c.m00 * x + c.m10 * y + c.m20 * z + c.m30;
		c.m31 = c.m01 * x + c.m11 * y + c.m21 * z + c.m31;
		c.m32 = c.m02 * x + c.m12 * y + c.m22 * z + c.m32;
		c.m33 = c.m03 * x + c.m13 * y + c.m23 * z + c.m33;
		return this;
	}

	/**
	 * Apply a translation to this matrix by translating by the given number of
	 * units in x, y and z.
	 * <p>
	 * If <code>M</code> is <code>this</code> matrix and <code>T</code> the
	 * translation matrix, then the new matrix will be <code>M * T</code>. So when
	 * transforming a vector <code>v</code> with the new matrix by using
	 * <code>M * T * v</code>, the translation will be applied first!
	 * 
	 * @param point
	 * @return this
	 */
	public Matrix4f translate(Vector3f point) {
		return translate(point.x, point.y, point.z);
	}

	/**
	 * Set this matrix to be a simple translation matrix.
	 * <p>
	 * The resulting matrix can be multiplied against another transformation matrix
	 * to obtain an additional translation.
	 * 
	 * @return this
	 */
	public Matrix4f translation(float x, float y, float z) {
		this.m00 = 1.0f;
		this.m01 = 0.0f;
		this.m02 = 0.0f;
		this.m03 = 0.0f;
		this.m10 = 0.0f;
		this.m11 = 1.0f;
		this.m12 = 0.0f;
		this.m13 = 0.0f;
		this.m20 = 0.0f;
		this.m21 = 0.0f;
		this.m22 = 0.0f;
		this.m23 = 0.0f;
		this.m30 = x;
		this.m31 = y;
		this.m32 = z;
		this.m33 = 1.0f;
		return this;
	}

	/**
	 * Set this matrix to be a simple translation matrix.
	 * <p>
	 * The resulting matrix can be multiplied against another transformation matrix
	 * to obtain an additional translation.
	 * 
	 * @return this
	 */
	public Matrix4f translation(Vector3f position) {
		return translation(position.x, position.y, position.z);
	}

	/**
	 * Transpose this matrix. Modifies the matrix directly.
	 * 
	 * @return this
	 */
	public Matrix4f transpose() {
		return transpose(this);
	}

	/**
	 * Transpose this matrix and store the result in <code>dest</code>.
	 * 
	 * @param dest
	 * @return this
	 */
	public Matrix4f transpose(Matrix4f dest) {
		transpose(this, dest);
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeFloat(m00);
		out.writeFloat(m01);
		out.writeFloat(m02);
		out.writeFloat(m03);
		out.writeFloat(m10);
		out.writeFloat(m11);
		out.writeFloat(m12);
		out.writeFloat(m13);
		out.writeFloat(m20);
		out.writeFloat(m21);
		out.writeFloat(m22);
		out.writeFloat(m23);
		out.writeFloat(m30);
		out.writeFloat(m31);
		out.writeFloat(m32);
		out.writeFloat(m33);
	}

	/**
	 * Set all the values within this matrix to <code>0</code>.
	 * 
	 * @return this
	 */
	public Matrix4f zero() {
		this.m00 = 0.0f;
		this.m01 = 0.0f;
		this.m02 = 0.0f;
		this.m03 = 0.0f;
		this.m10 = 0.0f;
		this.m11 = 0.0f;
		this.m12 = 0.0f;
		this.m13 = 0.0f;
		this.m20 = 0.0f;
		this.m21 = 0.0f;
		this.m22 = 0.0f;
		this.m23 = 0.0f;
		this.m30 = 0.0f;
		this.m31 = 0.0f;
		this.m32 = 0.0f;
		this.m33 = 0.0f;
		return this;
	}

}
