/*
 * Copyright (c) 2021, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package runtime.valhalla.inlinetypes;


import jdk.test.lib.Asserts;

/*
 * @test
 * @summary Nullable Flattenable semantic test
 * @library /test/lib
 * @compile -XDallowWithFieldOperator NullableInvalidDefaultTest.java
 * @run main/othervm -Xint -XX:InlineFieldMaxFlatSize=64 -XX:FlatArrayElementMaxSize=16 runtime.valhalla.inlinetypes.NullableInvalidDefaultTest
 */

public class NullableInvalidDefaultTest {
  static primitive class NF implements NullableWithInvalidDefault {
    int i;
    int j;

    public NF() {
        i = 0; j = 0;
    }

    public static NF setI(NF nf, int i) {
      nf = __WithField(nf.i, i);
      return nf;
    }

    public static NF setJ(NF nf, int j) {
      nf = __WithField(nf.j, j);
      return nf;
    }

    public NF(int i) {
        // By setting one field to a value and the other to
        // this value plus one, we have the guarantee that at least
        // one field is non zero
        this.i = i;
        this.j = i + 1;
    }
  }

  static primitive class BigNF implements NullableWithInvalidDefault {
    long j0, j1, j2, j3, j4, j5, j6, j7, j8, j9;
    long j10, j11, j12, j13, j14, j15, j16, j17, j18, j19;

    public BigNF() {
        j0 = 0; j1 = 0; j2 = 0; j3 = 0; j4 = 0; j5 = 0; j6 = 0; j7 = 0; j8 = 0; j9 = 0;
        j10 = 0; j11 = 0; j12 = 0; j13 = 0; j14 = 0; j15 = 0; j16 = 0; j17 = 0; j18 = 0; j19 = 0;
    }

    public BigNF(long v) {
        j0 = v; j1 = v; j2 = v; j3 = v; j4 = v; j5 = v; j6 = v; j7 = v; j8 = v; j9 = v;
        j10 = v + 1; j11 = v + 1; j12 = v + 1; j13 = v + 1; j14 = v + 1;
        j15 = v + 1; j16 = v + 1; j17 = v + 1; j18 = v + 1; j19 = v + 1;
    }

  }


  static NF snf;
  NF nf, nf2;;
  BigNF bigNF;

  public static void main(String[] args) {
    Object o = null;
    NF[] array = new NF[10];
    BigNF[] array2 = new BigNF[10];

    // Checkcast
    testCheckCast();

    // Withfield
    testWithfield();

    // Arrays
    testUninitializedArrayElements(array, array2);
    testWritingNonNullValueToArrays(array, array2);
    testWritingNullValueToArrays(array, array2);
    testArrayStoreException(array, new BigNF(1));
    testArrayStoreException(array2, new NF(1));

    // Static fields
    testStaticUninitializedFields();
    testWrittingNonNullValueToStaticField();
    testWrittingNullValueToStaticField();

    // Non-static fields
    // running test twice to test quickened bytecodes
    test(new NullableInvalidDefaultTest());
    test(new NullableInvalidDefaultTest());
  }

  static void testCheckCast() {
    Object o = null;
    NF nf = (NF)o;
    Asserts.assertNull(nf, "Checkcast must let null pass for nullable flattenable types");
  }

  static void testWithfield() {
    NF nf = new NF(1);
    nf = NF.setI(nf, 0);
    Asserts.assertNotNull(nf, "Should not be null, still one non zero field");
    nf = NF.setJ(nf, 0);
    Asserts.assertNull(nf, "Should be null once all fields are zeroed");
    nf = NF.setJ(nf, 1);
    Asserts.assertNotNull(nf, "Should not be null, after a field is set to non zero");
  }

  static void testUninitializedArrayElements(NF[] array, BigNF[] array2) {
    System.out.println("Reading uninitialized value element");
    Asserts.assertNull(array[1], "Uninitialized elements of arrays of nullable flattenable types must be null");
    Asserts.assertNull(array2[2], "Uninitialized elements of arrays of nullable flattenable types must be null");
  }

  static void testWritingNonNullValueToArrays(NF[] array, BigNF[] array2) {
    System.out.println("Writting non null value to array");
    array[1] = new NF(1);
    Asserts.assertNotNull(array[1], "Failed to write a non-null value to a null nullable flattenable element");
    array2[2] = new BigNF(1);
    Asserts.assertNotNull(array2[2], "Failed to write a non-null value to a null nullable flattenable element");
  }

  static void testWritingNullValueToArrays(NF[] array, BigNF[] array2) {
    System.out.println("Writting null value to array");
    Object o = null;
    array[1] = (NF)o;
    Asserts.assertNull(array[1], "Failed to write a null value to a nullable flattenable element");
    array2[2] = (BigNF)o;
    Asserts.assertNull(array2[2], "Failed to write a null value to a nullable flattenable element");
  }

  static void testArrayStoreException(Object[] array, Object o) {
    boolean excpt = false;
    try {
      array[0] = o;
    } catch(ArrayStoreException e) {
      excpt = true;
    }
    Asserts.assertTrue(excpt, "ArrayStoreException should have been thrown");
  }

  static void test(NullableInvalidDefaultTest nft) {
    nft.testUninitializedFields();
    nft.testWritingNonNullValue();
    nft.testWritingNullValue();
  }

  void testUninitializedFields() {
    System.out.println("Testing read uninitialized fields");
    Asserts.assertNull(nf, "Uninitialized nullable flattenable fields must be null");
    Asserts.assertNull(bigNF, "Uninitialized nullable flattenable fields must be null");
  }

  void testWritingNonNullValue() {
    System.out.println("Testing writing non null value");
    nf = new NF(1);
    Asserts.assertNotNull(nf, "Writing non-null value to null field failed");
    bigNF = new BigNF(1);
    Asserts.assertNotNull(bigNF, "Writing non-null value to null field failed");
  }

  void testWritingNullValue() {
    System.out.println("Testing writing null value");
    Object o = null;
    NF n = (NF)o;
    Asserts.assertNull(n, "Just checking");
    nf = n;
    Asserts.assertNull(nf, "Failed to write null to a nullable flattenable field");
    BigNF nb = (BigNF)o;
    Asserts.assertNull(nb, "Just checking");
    bigNF = nb;
    Asserts.assertNull(bigNF, "Failed to write null to a nullable flattenable field");
  }

  static void testStaticUninitializedFields() {
    System.out.println("Testing read uninitialized static fields");
    Asserts.assertNull(NullableInvalidDefaultTest.snf, "Uninitialized nullable flattenable static fields must be null");
  }

  static void testWrittingNonNullValueToStaticField() {
    System.out.println("Testing writting non null value to static field");
    NF n = new NF(1);
    NullableInvalidDefaultTest.snf = n;
    Asserts.assertNotNull(NullableInvalidDefaultTest.snf, "Failed to write over null nullable flattenable field");
  }

  static void testWrittingNullValueToStaticField() {
    System.out.println("Testing writting null value to static field");
    Object o = null;
    NF n = (NF)o;
    Asserts.assertNull(n, "Just checking");
    NullableInvalidDefaultTest.snf = n;
    Asserts.assertNull(NullableInvalidDefaultTest.snf, "Failed to write null to a static nullable flattenable field");
  }
}
