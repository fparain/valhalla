/*
 * Copyright (c) 2019, 2021, Oracle and/or its affiliates. All rights reserved.
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

package compiler.valhalla.inlinetypes;

import compiler.lib.ir_framework.*;
import jdk.test.lib.Asserts;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static compiler.valhalla.inlinetypes.InlineTypes.IRNode.*;
import static compiler.valhalla.inlinetypes.InlineTypes.*;

/*
 * @test
 * @key randomness
 * @summary Test correct handling of nullable inline types.
 * @library /test/lib /
 * @requires (os.simpleArch == "x64" | os.simpleArch == "aarch64")
 * @run driver/timeout=300 compiler.valhalla.inlinetypes.TestNullableQTypes
 */

@ForceCompileClassInitializer
public class TestNullableQTypes {

    public static void main(String[] args) {

        Scenario[] scenarios = InlineTypes.DEFAULT_SCENARIOS;
        scenarios[3].addFlags("-XX:-MonomorphicArrayCheck", "-XX:FlatArrayElementMaxSize=-1");
        scenarios[4].addFlags("-XX:-MonomorphicArrayCheck");

        InlineTypes.getFramework()
                   .addScenarios(scenarios)
                   .addHelperClasses(MyValue1Nullable.class,
                                     MyValue2Nullable.class,
                                     MyValue2NullableInline.class)
                   .start();
    }

    static {
        try {
            Class<?> clazz = TestNullableQTypes.class;
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            MethodType test28_mt = MethodType.methodType(void.class, MyValue1Nullable.val.class);
            test28_mh1 = lookup.findStatic(clazz, "test28_target1", test28_mt);
            test28_mh2 = lookup.findStatic(clazz, "test28_target2", test28_mt);

            MethodType test29_mt = MethodType.methodType(void.class, MyValue1Nullable.val.class);
            test29_mh1 = lookup.findStatic(clazz, "test29_target1", test29_mt);
            test29_mh2 = lookup.findStatic(clazz, "test29_target2", test29_mt);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Method handle lookup failed");
        }
    }

    private static final MyValue1Nullable testValue1 = MyValue1Nullable.createWithFieldsInline(rI, rL);
    private static final MyValue1Nullable[] testValue1Array = new MyValue1Nullable[] {testValue1,
                                                                                      testValue1,
                                                                                      testValue1};
// TODO replace (MyValue1Nullable)nullField by null once javac supports that
    static final Object nullField = null;
    MyValue1Nullable nullValField = (MyValue1Nullable)nullField;
    MyValue1Nullable valueField1 = testValue1;

    MyValue1Nullable testField1;
    MyValue1Nullable testField2;
    MyValue1Nullable testField3;
    MyValue1Nullable testField4;
    static MyValue1Nullable testField5;
    static MyValue1Nullable testField6;
    static MyValue1Nullable testField7;
    static MyValue1Nullable testField8;

    // Test field loads
    @Test
    public long test1(boolean b) {
        MyValue1Nullable val1 = b ? testField3 : MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Nullable val2 = b ? testField7 : MyValue1Nullable.createWithFieldsInline(rI, rL);
        long res = 0;
        res += testField1.hash();
        res += ((Object)testField2 == null) ? 42 : testField2.hash();
        res += val1.hash();
        res += testField4.hash();

        res += testField5.hash();
        res += ((Object)testField6 == null) ? 42 : testField6.hash();
        res += val2.hash();
        res += testField8.hash();
        return res;
    }

    @Run(test = "test1")
    public void test1_verifier() {
        testField1 = testValue1;
        testField2 = nullValField;
        testField3 = testValue1;
        testField4 = testValue1;

        testField5 = testValue1;
        testField6 = nullValField;
        testField7 = testValue1;
        testField8 = testValue1;
        long res = test1(true);
        Asserts.assertEquals(res, 2*42 + 6*testValue1.hash());

        testField2 = testValue1;
        testField6 = testValue1;
        res = test1(false);
        Asserts.assertEquals(res, 8*testValue1.hash());
    }

    // Test field stores
    @Test
    public MyValue1Nullable test2(MyValue1Nullable val1) {
        Object NULL = null;
        MyValue1Nullable ret = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Nullable val2 = MyValue1Nullable.setV1(testValue1, (MyValue2Nullable)NULL);
        testField1 = testField4;
        testField2 = val1;
        testField3 = val2;

        testField5 = ret;
        testField6 = val1;
        testField7 = val2;
        testField8 = testField4;
        return ret;
    }

    @Run(test = "test2")
    public void test2_verifier() {
        Object NULL = null;
        testField4 = testValue1;
        MyValue1Nullable ret = test2((MyValue1Nullable)nullField);
        MyValue1Nullable val2 = MyValue1Nullable.setV1(testValue1, (MyValue2Nullable)NULL);
        Asserts.assertEquals(testField1, testValue1);
        Asserts.assertEquals(testField2, null);
        Asserts.assertEquals(testField3, val2);

        Asserts.assertEquals(testField5, ret);
        Asserts.assertEquals(testField6, null);
        Asserts.assertEquals(testField7, val2);
        Asserts.assertEquals(testField8, testField4);

        testField4 = (MyValue1Nullable)nullField;
        test2((MyValue1Nullable)nullField);
        Asserts.assertEquals(testField1, testField4);
        Asserts.assertEquals(testField8, testField4);
    }

    // Non-primitive Wrapper
    static class Test3Wrapper {
        MyValue1Nullable val;

        public Test3Wrapper(MyValue1Nullable val) {
            this.val = val;
        }
    }

    // Test scalarization in safepoint debug info and re-allocation on deopt
    @Test
    @IR(failOn = {ALLOC, STORE})
    public long test3(boolean deopt, boolean b1, boolean b2, Method m) {
        Object NULL = null;
        MyValue1Nullable ret = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            ret = (MyValue1Nullable)nullField;
        }
        if (b2) {
            ret = MyValue1Nullable.setV1(ret, (MyValue2Nullable)NULL);
        }
        Test3Wrapper wrapper = new Test3Wrapper(ret);
        if (deopt) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        long res = ((Object)ret != null && (Object)ret.v1 != null) ? ret.hash() : 42;
        res += ((Object)wrapper.val != null && (Object)wrapper.val.v1 != null) ? wrapper.val.hash() : 0;
        return res;
    }

    @Run(test = "test3")
    public void test3_verifier(RunInfo info) {
        Asserts.assertEquals(test3(false, false, false, info.getTest()), 2*testValue1.hash());
        Asserts.assertEquals(test3(false, true, false, info.getTest()), 42L);
        if (!info.isWarmUp()) {
            switch (rI % 4) {
            case 0:
                Asserts.assertEquals(test3(true, false, false, info.getTest()), 2*testValue1.hash());
                break;
            case 1:
                Asserts.assertEquals(test3(true, true, false, info.getTest()), 42L);
                break;
            case 2:
                Asserts.assertEquals(test3(true, false, true, info.getTest()), 42L);
                break;
            case 3:
                try {
                    Asserts.assertEquals(test3(true, true, true, info.getTest()), 42L);
                    throw new RuntimeException("NullPointerException expected");
                } catch (NullPointerException e) {
                    // Expected
                }
                break;
            }
        }
    }

    // Test scalarization in safepoint debug info and re-allocation on deopt
    @Test
    @IR(failOn = {ALLOC, STORE})
    public boolean test4(boolean deopt, boolean b, Method m) {
        Object NULL = null;
        MyValue1Nullable val = b ? (MyValue1Nullable)NULL : MyValue1Nullable.createWithFieldsInline(rI, rL);
        Test3Wrapper wrapper = new Test3Wrapper(val);
        if (deopt) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return (Object)wrapper.val == null;
    }

    @Run(test = "test4")
    public void test4_verifier(RunInfo info) {
        Asserts.assertTrue(test4(false, true, info.getTest()));
        Asserts.assertFalse(test4(false, false, info.getTest()));
        if (!info.isWarmUp()) {
            switch (rI % 2) {
                case 0:
                    Asserts.assertTrue(test4(true, true, info.getTest()));
                    break;
                case 1:
                    Asserts.assertFalse(test4(false, false, info.getTest()));
                    break;
            }
        }
    }

    static primitive class SmallNullable2 implements NullableFlattenable {
        float f1;
        double f2;
        public SmallNullable2() {
            f1 = (float)rL;
            f2 = (double)rL;
        }
    }

    static primitive class SmallNullable1 implements NullableFlattenable {
        char c;
        byte b;
        short s;
        int i;
        SmallNullable2 vt;

        public SmallNullable1(boolean useNull) {
            c = (char)rL;
            b = (byte)rL;
            s = (short)rL;
            i = (int)rL;
            Object NULL = null;
            vt = useNull ? (SmallNullable2)NULL : new SmallNullable2();
        }
    }

    @DontCompile
    public SmallNullable1 test5_interpreted(boolean b1, boolean b2) {
        Object NULL = null;
        return b1 ? (SmallNullable1)NULL : new SmallNullable1(b2);
    }

    @DontInline
    public SmallNullable1 test5_compiled(boolean b1, boolean b2) {
        Object NULL = null;
        return b1 ? (SmallNullable1)NULL : new SmallNullable1(b2);
    }

    SmallNullable1 test5_field1;
    SmallNullable1 test5_field2;

    // Test scalarization in returns
    @Test
    public SmallNullable1 test5(boolean b1, boolean b2) {
        SmallNullable1 ret = test5_interpreted(b1, b2);
        if (b1 != ((Object)ret == null)) {
            throw new RuntimeException("test5 failed");
        }
        test5_field1 = ret;
        ret = test5_compiled(b1, b2);
        if (b1 != ((Object)ret == null)) {
            throw new RuntimeException("test5 failed");
        }
        test5_field2 = ret;
        return ret;
    }

    @Run(test = "test5")
    public void test5_verifier() {
        SmallNullable1 vt = new SmallNullable1(false);
        Asserts.assertEquals(test5(true, false), null);
        Asserts.assertEquals(test5_field1, null);
        Asserts.assertEquals(test5_field2, null);
        Asserts.assertEquals(test5(false, false), vt);
        Asserts.assertEquals(test5_field1, vt);
        Asserts.assertEquals(test5_field2, vt);
        vt = new SmallNullable1(true);
        Asserts.assertEquals(test5(true, true), null);
        Asserts.assertEquals(test5_field1, null);
        Asserts.assertEquals(test5_field2, null);
        Asserts.assertEquals(test5(false, true), vt);
        Asserts.assertEquals(test5_field1, vt);
        Asserts.assertEquals(test5_field2, vt);
    }

// TODO enable once interpreter is fixed
//    static primitive class Empty2 implements NullableFlattenable {
    static primitive class Empty2 {

    }

// TODO enable once interpreter is fixed
//    static primitive class Empty1 implements NullableFlattenable {
    static primitive class Empty1 {
        Empty2 empty2 = Empty2.default;

    }

// TODO enable once interpreter is fixed
//    static primitive class Container implements NullableFlattenable {
    static primitive class Container {
        int x = 0;
        Empty1 empty1 = Empty1.default;
        Empty2 empty2 = Empty2.default;
    }

    @DontInline
    public static Empty1 test6_helper1(Empty1 vt) {
        return vt;
    }

    @DontInline
    public static Empty2 test6_helper2(Empty2 vt) {
        return vt;
    }

    @DontInline
    public static Container test6_helper3(Container vt) {
        return vt;
    }

    // Test scalarization in calls and returns with empty nullable inline types
    @Test
    public Empty1 test6(Empty1 vt) {
        Empty1 empty1 = test6_helper1(vt);
        test6_helper2(empty1.empty2);
        Container c = test6_helper3(new Container());
        return c.empty1;
    }

    @Run(test = "test6")
    @Warmup(10000) // Warmup to make sure helper methods are compiled as well
    public void test6_verifier() {
        Object NULL = null;
        Asserts.assertEQ(test6(Empty1.default), Empty1.default);
// TODO enable once interpreter is fixed
//        Asserts.assertEQ(test6((Empty1)NULL), null);
    }

    @DontCompile
    public void test7_helper2(boolean doit) {
        if (doit) {
            // uncommon trap
            try {
                TestFramework.deoptimize(getClass().getDeclaredMethod("test7", boolean.class, boolean.class, boolean.class));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Test deoptimization at call return with inline type returned in registers
    @DontInline
    public SmallNullable1 test7_helper1(boolean deopt, boolean b1, boolean b2) {
        test7_helper2(deopt);
        Object NULL = null;
        return b1 ? (SmallNullable1) NULL : new SmallNullable1(b2);
    }

    @Test
    public SmallNullable1 test7(boolean flag, boolean b1, boolean b2) {
        return test7_helper1(flag, b1, b2);
    }

    @Run(test = "test7")
    @Warmup(10000)
    public void test7_verifier(RunInfo info) {
        boolean b1 = ((rI % 3) == 0);
        boolean b2 = ((rI % 3) == 1);
        SmallNullable1 result = test7(!info.isWarmUp(), b1, b2);
        SmallNullable1 vt = new SmallNullable1(b2);
        Asserts.assertEQ(result, b1 ? null : vt);
    }

    // Test calling a method returning a nullable inline type as fields via reflection
    @Test
    public SmallNullable1 test8(boolean b1, boolean b2) {
        Object NULL = null;
        return b1 ? (SmallNullable1) NULL : new SmallNullable1(b2);
    }

    @Run(test = "test8")
    @Warmup(1) // Make sure we call through runtime instead of generating bytecodes for reflective call
    public void test8_verifier() throws Exception {
        Method m = getClass().getDeclaredMethod("test8", boolean.class, boolean.class);
        Asserts.assertEQ(m.invoke(this, false, true), new SmallNullable1(true));
        Asserts.assertEQ(m.invoke(this, false, false), new SmallNullable1(false));
        Asserts.assertEQ(m.invoke(this, true, false), null);
    }

    // Test .ref types as arg/return
    @Test
    public SmallNullable1.ref test9(MyValue1Nullable vt1, MyValue1Nullable.ref vt2, boolean b1, boolean b2) {
        Asserts.assertEQ(vt1, testValue1);
        if (b1) {
            Asserts.assertEQ(vt2, null);
        } else {
            Asserts.assertEQ(vt2, testValue1);
        }
        Object NULL = null;
        return b1 ? (SmallNullable1) NULL : new SmallNullable1(b2);
    }

    @Run(test = "test9")
    public void test9_verifier() throws Exception {
        Asserts.assertEQ(test9(testValue1, testValue1, false, true), new SmallNullable1(true));
        Asserts.assertEQ(test9(testValue1, testValue1, false, false), new SmallNullable1(false));
        Asserts.assertEQ(test9(testValue1, null, true, false), null);
    }

    // Test array load
    @Test
    public Object test10(boolean b, MyValue1Nullable[] array1, Object[] array2) {
        return b ? array1[0] : array2[0];
    }

    @Run(test = "test10")
    public void test10_verifier() {
        MyValue1Nullable[] array = new MyValue1Nullable[1];
        Asserts.assertEquals(test10(true, array, array), null);
        Asserts.assertEquals(test10(false, array, array), null);
        array[0] = testValue1;
        Asserts.assertEquals(test10(true, array, array), testValue1);
        Asserts.assertEquals(test10(false, array, array), testValue1);
    }

    @Test
    @IR(failOn = {ALLOC})
    public long test11(MyValue1Nullable vt) {
        long result = 0;
        try {
            result = vt.hash();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        return result;
    }

    @Run(test = "test11")
    public void test11_verifier() {
        long result = test11((MyValue1Nullable)nullField);
       // Asserts.assertEquals(result, 0L);
    }

    @Test
    @IR(failOn = {ALLOC})
    public long test12(MyValue1Nullable vt) {
        long result = 0;
        try {
            result = vt.hashInterpreted();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        return result;
    }

    @Run(test = "test12")
    public void test12_verifier() {
        long result = test12((MyValue1Nullable)nullField);
        Asserts.assertEquals(result, 0L);
    }

    @Test
    @IR(failOn = {ALLOC})
    public long test13() {
        long result = 0;
        try {
            if ((Object)nullField != null) {
                throw new RuntimeException("nullField should be null");
            }
            result = ((MyValue1Nullable)nullField).hash();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        return result;
    }

    @Run(test = "test13")
    public void test13_verifier() {
        long result = test13();
        Asserts.assertEquals(result, 0L);
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test14() {
        MyValue1Nullable vt = (MyValue1Nullable)nullField;
        Asserts.assertEquals(vt, null);
    }

    @Run(test = "test14")
    public void test14_verifier() {
        test14();
    }

    @Test
    @IR(failOn = {ALLOC})
    public MyValue1Nullable test15(MyValue1Nullable vt) {
        vt = test15_dontinline(vt);
        vt = test15_inline(vt);
        return vt;
    }

    @Run(test = "test15")
    public void test15_verifier() {
        MyValue1Nullable vt = test15((MyValue1Nullable)nullField);
        Asserts.assertEquals(vt, null);
    }

    @DontInline
    public MyValue1Nullable test15_dontinline(MyValue1Nullable vt) {
        return vt;
    }

    @ForceInline
    public MyValue1Nullable test15_inline(MyValue1Nullable vt) {
        return vt;
    }

    @Test
    @IR(failOn = {ALLOC})
    public MyValue1Nullable test16(Object obj) {
        return (MyValue1Nullable)obj;
    }

    @Run(test = "test16")
    public void test16_verifier() {
        Asserts.assertEquals(test16(null), null);
        Asserts.assertEquals(test16(testValue1), testValue1);
        try {
            test16(42);
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
    }

    @ForceInline
    public MyValue1Nullable getNullInline() {
        return (MyValue1Nullable)nullField;
    }

    @DontInline
    public MyValue1Nullable getNullDontInline() {
        return (MyValue1Nullable)nullField;
    }

    MyValue1Nullable test17Field;

    @Test
    @IR(failOn = {ALLOC})
    public void test17() {
        test17Field = getNullInline();     // Should not throw
        test17Field = getNullDontInline(); // Should not throw
    }

    @Run(test = "test17")
    public void test17_verifier() {
        test17();
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test18() {
        if ((Object)nullValField != null) {
            throw new RuntimeException("Should be null");
        }
    }

    @Run(test = "test18")
    public void test18_verifier() {
        test18();
    }

    // merge of 2 inline types, one being null
    @Test
    @IR(failOn = {ALLOC})
    public MyValue1Nullable test19(boolean flag) {
        MyValue1Nullable v;
        if (flag) {
            v = valueField1;
        } else {
            v = (MyValue1Nullable)nullField;
        }
        return v;
    }

    @Run(test = "test19")
    public void test19_verifier() {
        Asserts.assertEquals(test19(true), valueField1);
        Asserts.assertEquals(test19(false), null);
    }

    // null constant
    @Test
    @IR(failOn = {ALLOC})
    public void test20(boolean flag) {
        MyValue1Nullable val = flag ? nullValField : (MyValue1Nullable)nullField;
        nullValField = (MyValue1Nullable) val;
    }

    @Run(test = "test20")
    public void test20_verifier() {
        test20(true);
        test20(false);
        Asserts.assertEquals(nullValField, null);
    }

    // null constant
    @Test
    @IR(failOn = {ALLOC})
    public void test21(boolean flag) {
        MyValue1Nullable val = flag ? (MyValue1Nullable)nullField : nullValField;
        nullValField = (MyValue1Nullable) val;
    }

    @Run(test = "test21")
    public void test21_verifier() {
        test21(false);
        test21(true);
        Asserts.assertEquals(nullValField, null);
    }

    // null return
    int test22_cnt;

    @DontInline
    public MyValue1Nullable test22_helper() {
        test22_cnt++;
        return (MyValue1Nullable)nullField;
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test22() {
        test22_helper().hash();
    }

    @Run(test = "test22")
    public void test22_verifier() {
        try {
            test22_cnt = 0;
            test22();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (test22_cnt != 1) {
            throw new RuntimeException("call executed twice");
        }
    }

    // null return at virtual call
    class A {
        public MyValue1Nullable test23_helper() {
            return (MyValue1Nullable)nullField;
        }
    }

    class B extends A {
        public MyValue1Nullable test23_helper() {
            return (MyValue1Nullable)nullField;
        }
    }

    class C extends A {
        public MyValue1Nullable test23_helper() {
            return (MyValue1Nullable)nullField;
        }
    }

    class D extends C {
        public MyValue1Nullable test23_helper() {
            return (MyValue1Nullable)nullField;
        }
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test23(A a) {
        a.test23_helper().hash();
    }

    @Run(test = "test23")
    public void test23_verifier() {
        A a = new A();
        A b = new B();
        A c = new C();
        A d = new D();
        try {
            test23(a);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            test23(b);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            test23(c);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            test23(d);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // Test writing null to a (flattened) inline type array
    @ForceInline
    public void test24_inline(Object[] oa, Object o, int index) {
        oa[index] = o;
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test24(MyValue1Nullable[] va, int index) {
        test24_inline(va, nullField, index);
    }

    @Run(test = "test24")
    public void test24_verifier() {
        int index = Math.abs(rI) % 3;
        test24(testValue1Array, index);
        Asserts.assertEQ(testValue1Array[index], null);
        testValue1Array[index] = testValue1;
        Asserts.assertEQ(testValue1Array[index].hash(), testValue1.hash());
    }

    @DontInline
    MyValue1Nullable getNullField1() {
        return (MyValue1Nullable)nullField;
    }

    @DontInline
    MyValue1Nullable getNullField2() {
        return (MyValue1Nullable)nullField;
    }

    MyValue1Nullable test25Field;

    @Test
    @IR(failOn = {ALLOC})
    public void test25() {
        test25Field = getNullField1(); // should not throw
        try {
            getNullField1().hash();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        try {
            getNullField2().hash();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Run(test = "test25")
    public void test25_verifier() {
        test25();
    }

    @DontInline
    public boolean test26_dontinline(MyValue1Nullable vt) {
        return (Object)vt == nullField;
    }

    // Test c2c call passing null for an inline type
    @Test
    @IR(failOn = {ALLOC})
    public boolean test26(Object arg) throws Exception {
        Method test26method = getClass().getMethod("test26_dontinline", MyValue1Nullable.val.class);
        return (boolean)test26method.invoke(this, arg);
    }

    @Run(test = "test26")
    @Warmup(10000) // Warmup to make sure 'test27_dontinline' is compiled
    public void test26_verifier() throws Exception {
        boolean res = test26(null);
        Asserts.assertTrue(res);
    }

    // Test scalarization of default inline type with non-flattenable field
    final primitive class Test17Value {
        public final MyValue1Nullable valueField;

        @ForceInline
        public Test17Value(MyValue1Nullable valueField) {
            this.valueField = valueField;
        }
    }

    @Test
    @IR(failOn = {ALLOC})
    public Test17Value test27(boolean b) {
        Test17Value vt1 = Test17Value.default;
        if ((Object)vt1.valueField != null) {
            throw new RuntimeException("Should be null");
        }
        Test17Value vt2 = new Test17Value(testValue1);
        return b ? vt1 : vt2;
    }

    @Run(test = "test27")
    public void test27_verifier() {
        test27(true);
        test27(false);
    }

    static final MethodHandle test28_mh1;
    static final MethodHandle test28_mh2;

    static MyValue1Nullable nullValue;

    @DontInline
    static void test28_target1(MyValue1Nullable vt) {
        nullValue = vt;
    }

    @ForceInline
    static void test28_target2(MyValue1Nullable vt) {
        nullValue = vt;
    }

    // Test passing null for an inline type
    @Test
    @IR(failOn = {ALLOC})
    public void test28() throws Throwable {
        test28_mh1.invokeExact(nullValue);
        test28_mh2.invokeExact(nullValue);
    }

    @Run(test = "test28")
    @Warmup(11000) // Make sure lambda forms get compiled
    public void test28_verifier() {
        try {
            test28();
        } catch (Throwable t) {
            throw new RuntimeException("test28 failed", t);
        }
    }

    static MethodHandle test29_mh1;
    static MethodHandle test29_mh2;

    @DontInline
    static void test29_target1(MyValue1Nullable vt) {
        nullValue = vt;
    }

    @ForceInline
    static void test29_target2(MyValue1Nullable vt) {
        nullValue = vt;
    }

    // Same as test22 but with non-final mh
    @Test
    @IR(failOn = {ALLOC})
    public void test29() throws Throwable {
        test29_mh1.invokeExact(nullValue);
        test29_mh2.invokeExact(nullValue);
    }

    @Run(test = "test29")
    @Warmup(11000) // Make sure lambda forms get compiled
    public void test29_verifier() {
        try {
            test29();
        } catch (Throwable t) {
            throw new RuntimeException("test29 failed", t);
        }
    }

    // Same as test22/13 but with constant null
    @Test
    @IR(failOn = {ALLOC})
    public void test30(MethodHandle mh) throws Throwable {
        mh.invoke(null);
    }

    @Run(test = "test30")
    @Warmup(11000) // Make sure lambda forms get compiled
    public void test30_verifier() {
        try {
            test30(test28_mh1);
            test30(test28_mh2);
            test30(test29_mh1);
            test30(test29_mh2);
        } catch (Throwable t) {
            throw new RuntimeException("test30 failed", t);
        }
    }

    // Test writing null to a flattenable inline type field in an inline type
    final primitive class Test21Value {
        final MyValue1Nullable valueField;

        @ForceInline
        public Test21Value(MyValue1Nullable valueField) {
            this.valueField = valueField;
        }

        @ForceInline
        public Test21Value test11() {
            return new Test21Value((MyValue1Nullable)nullField);
        }

        @ForceInline
        public Test21Value test12() {
            return new Test21Value(this.valueField);
        }
    }

    @Test
    @IR(failOn = {ALLOC})
    public Test21Value test31(Test21Value vt, boolean b) {
        return b ? vt.test11() : vt.test12();
    }

    @Run(test = "test31")
    public void test31_verifier() {
        Test21Value vt = new Test21Value(testValue1);
        Asserts.assertEQ(test31(vt, true).valueField, null);
        Asserts.assertEQ(test31(vt, false).valueField, testValue1);
        vt = new Test21Value((MyValue1Nullable)nullField);
        Asserts.assertEQ(test31(vt, false).valueField, null);
    }

    @DontInline
    public MyValue1Nullable test32_helper() {
        return (MyValue1Nullable)nullField;
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test32() {
        test32_helper().hash();
    }

    @Run(test = "test32")
    public void test32_verifier() {
        try {
            test32();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    @IR(failOn = {ALLOC})
    public void test33(MyValue1Nullable[] arr, Object obj) {
        arr[0] = (MyValue1Nullable) obj;
        arr[0].hash();
    }

    @Run(test = "test33")
    public void test33_verifier() {
        MyValue1Nullable[] arr = new MyValue1Nullable[2];
        arr[0] = testValue1;
        try {
            test33(arr, null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        Asserts.assertEQ(arr[0], null);
    }

    static MyValue1Nullable nullBox;

    @Test
    @IR(failOn = {ALLOC})
    public void test34() {
        nullBox.hash();
    }

    @Run(test = "test34")
    public void test34_verifier() {
        try {
            test34();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @DontInline
    public void test35_callee(MyValue1Nullable val) { }

    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test35(boolean b, MyValue1Nullable.ref vt1, MyValue1Nullable.ref vt2) {
        vt1 = (MyValue1Nullable)vt1;
        Object obj = b ? vt1 : vt2; // We should not allocate here
        test35_callee((MyValue1Nullable) vt1);
        return ((MyValue1Nullable)obj).x;
    }

    @Run(test = "test35")
    public void test35_verifier(RunInfo info) {
        int res = test35(true, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
        res = test35(false, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
        res = test35(false, (MyValue1Nullable)nullField, testValue1);
        Asserts.assertEquals(res, testValue1.x);
        if (!info.isWarmUp()) {
            try {
                test35(true, (MyValue1Nullable)nullField, testValue1);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    // Test that chains of casts are folded and don't trigger an allocation
    @Test
    @IR(failOn = {ALLOC, STORE})
    public MyValue1Nullable test36(MyValue1Nullable vt) {
        return ((MyValue1Nullable)((Object)((MyValue1Nullable)(MyValue1Nullable)((MyValue1Nullable)((Object)vt)))));
    }

    @Run(test = "test36")
    public void test36_verifier() {
        MyValue1Nullable result = test36(testValue1);
        Asserts.assertEquals(result, testValue1);
    }

    @Test
    @IR(failOn = {ALLOC, STORE})
    public MyValue1Nullable test37(MyValue1Nullable vt) {
        return ((MyValue1Nullable)((Object)((MyValue1Nullable)(MyValue1Nullable)((MyValue1Nullable)((Object)vt)))));
    }

    @Run(test = "test37")
    public void test37_verifier() {
        MyValue1Nullable result = (MyValue1Nullable) test37(testValue1);
        Asserts.assertEquals(result, testValue1);
    }

    // Some more casting tests
    @Test
    public MyValue1Nullable test38(MyValue1Nullable vt, MyValue1Nullable vtBox, int i) {
        MyValue1Nullable result = (MyValue1Nullable)nullField;
        if (i == 0) {
            result = (MyValue1Nullable)vt;
            result = (MyValue1Nullable)nullField;
        } else if (i == 1) {
            result = (MyValue1Nullable)vt;
        } else if (i == 2) {
            result = vtBox;
        }
        return result;
    }

    @Run(test = "test38")
    public void test38_verifier() {
        MyValue1Nullable result = test38(testValue1, (MyValue1Nullable)nullField, 0);
        Asserts.assertEquals(result, null);
        result = test38(testValue1, testValue1, 1);
        Asserts.assertEquals(result, testValue1);
        result = test38(testValue1, (MyValue1Nullable)nullField, 2);
        Asserts.assertEquals(result, null);
        result = test38(testValue1, testValue1, 2);
        Asserts.assertEquals(result, testValue1);
    }

    @Test
    @IR(failOn = {ALLOC})
    public long test39(MyValue1Nullable vt, MyValue1Nullable vtBox) {
        long result = 0;
        for (int i = 0; i < 100; ++i) {
            MyValue1Nullable box;
            if (i == 0) {
                box = (MyValue1Nullable)vt;
                box = (MyValue1Nullable)nullField;
            } else if (i < 99) {
                box = (MyValue1Nullable)vt;
            } else {
                box = vtBox;
            }
            if (box != (MyValue1Nullable)nullField) {
                result += box.hash();
            }
        }
        return result;
    }

    @Run(test = "test39")
    public void test39_verifier() {
        long result = test39(testValue1, (MyValue1Nullable)nullField);
        Asserts.assertEquals(result, testValue1.hash()*98);
        result = test39(testValue1, testValue1);
        Asserts.assertEquals(result, testValue1.hash()*99);
    }

    // Test null check of inline type receiver with incremental inlining
    public long test40_callee(MyValue1Nullable vt) {
        long result = 0;
        try {
            result = vt.hashInterpreted();
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        return result;
    }

    @Test
    @IR(failOn = {ALLOC})
    public long test40() {
        return test40_callee((MyValue1Nullable)nullField);
    }

    @Run(test = "test40")
    public void test40_verifier() {
        long result = test40();
        Asserts.assertEquals(result, 0L);
    }

    // Test casting null to unloaded inline type
    final primitive class Test31Value implements NullableFlattenable {
        private final int i = 0;
    }

    @Test
    @IR(failOn = {ALLOC})
    public Test31Value test41(Object o) {
        return (Test31Value)o;
    }

    @Run(test = "test41")
    public void test41_verifier() {
        test41(null);
    }

    private static final MyValue1Nullable constNullRefField = (MyValue1Nullable)nullField;

    @Test
    @IR(failOn = {ALLOC})
    public MyValue1Nullable test42() {
        return constNullRefField;
    }

    @Run(test = "test42")
    public void test42_verifier() {
        MyValue1Nullable result = test42();
        Asserts.assertEquals(result, null);
    }

    static primitive class Test33Value1 implements NullableFlattenable {
        int x = 0;
    }

    static primitive class Test33Value2 {
        Test33Value1 vt;

        public Test33Value2() {
            vt = new Test33Value1();
        }
    }

    public static final Test33Value2 test43Val = new Test33Value2();

    @Test
    @IR(failOn = {ALLOC})
    public Test33Value2 test43() {
        return test43Val;
    }

    @Run(test = "test43")
    public void test43_verifier() {
        Test33Value2 result = test43();
        Asserts.assertEquals(result, test43Val);
    }

    // Verify that static nullable inline-type fields are not
    // treated as never-null by C2 when initialized at compile time.
    private static MyValue1Nullable test44Val;

    @Test
    public void test44(MyValue1Nullable vt) {
        if (test44Val == (MyValue1Nullable)nullField) {
            test44Val = vt;
        }
    }

    @Run(test = "test44")
    public void test44_verifier(RunInfo info) {
        test44(testValue1);
        if (!info.isWarmUp()) {
            test44Val = (MyValue1Nullable)nullField;
            test44(testValue1);
            Asserts.assertEquals(test44Val, testValue1);
        }
    }

    // Same as test27 but with non-allocated inline type at withfield
    @Test
    public Test17Value test45(boolean b) {
        Test17Value vt1 = Test17Value.default;
        if ((Object)vt1.valueField != (MyValue1Nullable)nullField) {
            throw new RuntimeException("Should be null");
        }
        MyValue1Nullable vt3 = MyValue1Nullable.createWithFieldsInline(rI, rL);
        Test17Value vt2 = new Test17Value(vt3);
        return b ? vt1 : vt2;
    }

    @Run(test = "test45")
    public void test45_verifier() {
        test45(true);
        test45(false);
    }

    // Test that when explicitly null checking an inline type, we keep
    // track of the information that the inline type can never be null.
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test47(boolean b, MyValue1Nullable vt1, MyValue1Nullable.val vt2) {
        if (vt1 == (MyValue1Nullable)nullField) {
            return 0;
        }
        // vt1 should be scalarized because it's always non-null
        Object obj = b ? vt1 : vt2; // We should not allocate vt2 here
        test35_callee(vt1);
        return ((MyValue1Nullable)obj).x;
    }

    @Run(test = "test47")
    public void test47_verifier() {
        int res = test47(true, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
        res = test47(false, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
    }

    // Test that when explicitly null checking an inline type receiver,
    // we keep track of the information that the inline type can never be null.
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test48(boolean b, MyValue1Nullable vt1, MyValue1Nullable.val vt2) {
        vt1.hash(); // Inlined - Explicit null check
        // vt1 should be scalarized because it's always non-null
        Object obj = b ? vt1 : vt2; // We should not allocate vt2 here
        test35_callee(vt1);
        return ((MyValue1Nullable)obj).x;
    }

    @Run(test = "test48")
    public void test48_verifier() {
        int res = test48(true, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
        res = test48(false, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
    }

    // Test that when implicitly null checking an inline type receiver,
    // we keep track of the information that the inline type can never be null.
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test49(boolean b, MyValue1Nullable vt1, MyValue1Nullable.val vt2) {
        vt1.hashInterpreted(); // Not inlined - Implicit null check
        // vt1 should be scalarized because it's always non-null
        Object obj = b ? vt1 : vt2; // We should not allocate vt2 here
        test35_callee(vt1);
        return ((MyValue1Nullable)obj).x;
    }

    @Run(test = "test49")
    public void test49_verifier() {
        int res = test49(true, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
        res = test49(false, testValue1, testValue1);
        Asserts.assertEquals(res, testValue1.x);
    }

    // Test casting constant null to inline type
    @Test
    @IR(failOn = {ALLOC})
    public MyValue1Nullable test50() {
        Object NULL = null;
        return (MyValue1Nullable)NULL;
    }

    @Run(test = "test50")
    public void test50_verifier() {
        test50();
    }

    MyValue1Nullable refField;
    MyValue1Nullable flatField;

    // Test scalarization
    @Test
    @IR(failOn = {ALLOC_G, STORE, TRAP})
    public int test51(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = refField;
        }
        return val.x;
    }

    @Run(test = "test51")
    public void test51_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test51(true), refField.x);
        Asserts.assertEquals(test51(false), testValue1.x);
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test51(false), testValue1.x);
                test51(true);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    // Same as test51 but with call to hash()
    @Test
    @IR(failOn = {ALLOC, STORE, TRAP})
    public long test52(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = refField;
        }
        return val.hash();
    }

    @Run(test = "test52")
    public void test52_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test52(true), refField.hash());
        Asserts.assertEquals(test52(false), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test52(false), testValue1.hash());
                test52(true);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test
    public MyValue1Nullable test53(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = refField;
        }
        return val;
    }

    @Run(test = "test53")
    public void test53_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test53(true).hash(), refField.hash());
        Asserts.assertEquals(test53(false).hash(), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            Asserts.assertEquals(test53(true), null);
        }
    }

    // Test scalarization when referenced in safepoint debug info
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test54(boolean b1, boolean b2, Method m) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = refField;
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return val.x;
    }

    @Run(test = "test54")
    public void test54_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test54(true, false, info.getTest()), refField.x);
        Asserts.assertEquals(test54(false, false, info.getTest()), testValue1.x);
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test54(false, false, info.getTest()), testValue1.x);
                test54(true, false, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
            Asserts.assertEquals(test54(true, true, info.getTest()), refField.x);
            Asserts.assertEquals(test54(false, true, info.getTest()), testValue1.x);
        }
    }

    @Test
    public MyValue1Nullable test55(boolean b1, boolean b2, Method m) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = refField;
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return val;
    }

    @Run(test = "test55")
    public void test55_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test55(true, false, info.getTest()).hash(), refField.hash());
        Asserts.assertEquals(test55(false, false, info.getTest()).hash(), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            Asserts.assertEquals(test55(true, false, info.getTest()), null);
            refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
            Asserts.assertEquals(test55(true, true, info.getTest()).hash(), refField.hash());
            Asserts.assertEquals(test55(false, true, info.getTest()).hash(), testValue1.hash());
        }
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public int test56(boolean b) {
        MyValue1Nullable val = (MyValue1Nullable)nullField;
        if (b) {
            val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        }
        return val.x;
    }

    @Run(test = "test56")
    public void test56_verifier() {
        Asserts.assertEquals(test56(true), testValue1.x);
        try {
            test56(false);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public MyValue1Nullable test57(boolean b) {
        MyValue1Nullable val = (MyValue1Nullable)nullField;
        if (b) {
            val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        }
        return val;
    }

    @Run(test = "test57")
    public void test57_verifier() {
        Asserts.assertEquals(test57(true).hash(), testValue1.hash());
        Asserts.assertEquals(test57(false), null);
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public int test58(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = (MyValue1Nullable)nullField;
        }
        return val.x;
    }

    @Run(test = "test58")
    public void test58_verifier() {
        Asserts.assertEquals(test58(false), testValue1.x);
        try {
            test58(true);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public MyValue1Nullable test59(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = (MyValue1Nullable)nullField;
        }
        return val;
    }

    @Run(test = "test59")
    public void test59_verifier() {
        Asserts.assertEquals(test59(false).hash(), testValue1.hash());
        Asserts.assertEquals(test59(true), null);
    }

    @ForceInline
    public Object test60_helper() {
        return flatField;
    }

    @Test
    @IR(failOn = {ALLOC_G, TRAP})
    public void test60(boolean b) {
        Object o = (MyValue1Nullable)nullField;
        if (b) {
            o = testValue1;
        } else {
            o = test60_helper();
        }
        flatField = (MyValue1Nullable)o;
    }

    @Run(test = "test60")
    public void test60_verifier() {
        MyValue1Nullable vt = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        flatField = vt;
        test60(false);
        Asserts.assertEquals(flatField.hash(), vt.hash());
        test60(true);
        Asserts.assertEquals(flatField.hash(), testValue1.hash());
    }

    static final primitive class MyValue1Wrapper {
        final MyValue1Nullable vt;

        @ForceInline
        public MyValue1Wrapper(MyValue1Nullable vt) {
            this.vt = vt;
        }

        @ForceInline
        public long hash() {
            return (vt != (MyValue1Nullable)nullField) ? vt.hash() : 0;
        }
    }

    MyValue1Wrapper wrapperField;

    @Test
    @IR(failOn = {ALLOC_G, STORE, TRAP})
    public long test61(boolean b) {
        MyValue1Wrapper.ref val = MyValue1Wrapper.default;
        if (b) {
            val = wrapperField;
        }
        return val.hash();
    }

    @Run(test = "test61")
    public void test61_verifier() {
        wrapperField = new MyValue1Wrapper(testValue1);
        Asserts.assertEquals(test61(true), wrapperField.hash());
        Asserts.assertEquals(test61(false), MyValue1Wrapper.default.hash());
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public boolean test62(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.default;
        if (b) {
            val = (MyValue1Nullable)nullField;
        }
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        return w.vt == (MyValue1Nullable)nullField;
    }

    @Run(test = "test62")
    public void test62_verifier() {
        Asserts.assertTrue(test62(true));
        Asserts.assertFalse(test62(false));
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public boolean test63(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = (MyValue1Nullable)nullField;
        }
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        return w.vt == (MyValue1Nullable)nullField;
    }

    @Run(test = "test63")
    public void test63_verifier() {
        Asserts.assertTrue(test63(true));
        Asserts.assertFalse(test63(false));
    }

    @Test
    @IR(failOn = {ALLOC, LOAD, STORE, TRAP})
    public long test64(boolean b1, boolean b2) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = (MyValue1Nullable)nullField;
        }
        MyValue1Wrapper.ref w = MyValue1Wrapper.default;
        if (b2) {
            w = new MyValue1Wrapper(val);
        }
        return w.hash();
    }

    @Run(test = "test64")
    public void test64_verifier() {
        MyValue1Wrapper w = new MyValue1Wrapper(MyValue1Nullable.createWithFieldsInline(rI, rL));
        Asserts.assertEquals(test64(false, false), MyValue1Wrapper.default.hash());
        Asserts.assertEquals(test64(false, true), w.hash());
        Asserts.assertEquals(test64(true, false), MyValue1Wrapper.default.hash());
        Asserts.assertEquals(test64(true, true), 0L);
    }

    @Test
    @IR(failOn = {ALLOC_G, STORE, TRAP})
    public int test65(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b) {
            w = new MyValue1Wrapper(refField);
        }
        return w.vt.x;
    }

    @Run(test = "test65")
    public void test65_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test65(true), refField.x);
        Asserts.assertEquals(test65(false), testValue1.x);
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test65(false), testValue1.x);
                test65(true);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test
    @IR(failOn = {ALLOC, STORE, TRAP})
    public long test66(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b) {
            w = new MyValue1Wrapper(refField);
        }
        return w.vt.hash();
    }

    @Run(test = "test66")
    public void test66_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test66(true), refField.hash());
        Asserts.assertEquals(test66(false), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test66(false), testValue1.hash());
                test66(true);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @Test
    public MyValue1Nullable test67(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b) {
            w = new MyValue1Wrapper(refField);
        }
        return w.vt;
    }

    @Run(test = "test67")
    public void test67_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test67(true).hash(), refField.hash());
        Asserts.assertEquals(test67(false).hash(), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            Asserts.assertEquals(test67(true), null);
        }
    }

    // Test scalarization when .ref is referenced in safepoint debug info
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test68(boolean b1, boolean b2, Method m) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b1) {
            w = new MyValue1Wrapper(refField);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return w.vt.x;
    }

    @Run(test = "test68")
    public void test68_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test68(true, false, info.getTest()), refField.x);
        Asserts.assertEquals(test68(false, false, info.getTest()), testValue1.x);
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test68(false, false, info.getTest()), testValue1.x);
                test68(true, false, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
            Asserts.assertEquals(test68(true, true, info.getTest()), refField.x);
            Asserts.assertEquals(test68(false, true, info.getTest()), testValue1.x);
        }
    }

    @Test
    public MyValue1Nullable test69(boolean b1, boolean b2, Method m) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b1) {
            w = new MyValue1Wrapper(refField);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return w.vt;
    }

    @Run(test = "test69")
    public void test69_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test69(true, false, info.getTest()).hash(), refField.hash());
        Asserts.assertEquals(test69(false, false, info.getTest()).hash(), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            Asserts.assertEquals(test69(true, false, info.getTest()), null);
            refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
            Asserts.assertEquals(test69(true, true, info.getTest()).hash(), refField.hash());
            Asserts.assertEquals(test69(false, true, info.getTest()).hash(), testValue1.hash());
        }
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public int test70(boolean b) {
        MyValue1Wrapper.ref w = new MyValue1Wrapper((MyValue1Nullable)nullField);
        if (b) {
            MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
            w = new MyValue1Wrapper(val);
        }
        return w.vt.x;
    }

    @Run(test = "test70")
    public void test70_verifier() {
        Asserts.assertEquals(test70(true), testValue1.x);
        try {
            test70(false);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public MyValue1Nullable test71(boolean b) {
        MyValue1Wrapper.ref w = new MyValue1Wrapper((MyValue1Nullable)nullField);
        if (b) {
            MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
            w = new MyValue1Wrapper(val);
        }
        return w.vt;
    }

    @Run(test = "test71")
    public void test71_verifier() {
        Asserts.assertEquals(test71(true).hash(), testValue1.hash());
        Asserts.assertEquals(test71(false), null);
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public int test72(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b) {
            w = new MyValue1Wrapper((MyValue1Nullable)nullField);
        }
        return w.vt.x;
    }

    @Run(test = "test72")
    public void test72_verifier() {
        Asserts.assertEquals(test72(false), testValue1.x);
        try {
            test72(true);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public MyValue1Nullable test73(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        MyValue1Wrapper.ref w = new MyValue1Wrapper(val);
        if (b) {
            w = new MyValue1Wrapper((MyValue1Nullable)nullField);
        }
        return w.vt;
    }

    @Run(test = "test73")
    public void test73_verifier() {
        Asserts.assertEquals(test73(false).hash(), testValue1.hash());
        Asserts.assertEquals(test73(true), null);
    }

    @ForceInline
    public MyValue1Nullable test74_helper() {
        return flatField;
    }

    @Test
    @IR(failOn = {ALLOC_G, TRAP})
    public void test74(boolean b) {
        MyValue1Wrapper.ref w = new MyValue1Wrapper((MyValue1Nullable)nullField);
        if (b) {
            w = new MyValue1Wrapper(testValue1);
        } else {
            w = new MyValue1Wrapper(test74_helper());
        }
        flatField = w.vt;
    }

    @Run(test = "test74")
    public void test74_verifier() {
        MyValue1Nullable vt = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        flatField = vt;
        test74(false);
        Asserts.assertEquals(flatField.hash(), vt.hash());
        test74(true);
        Asserts.assertEquals(flatField.hash(), testValue1.hash());
    }

    @Test
    @IR(failOn = {ALLOC_G, LOAD, STORE, TRAP})
    public long test75(boolean b) {
        MyValue1Nullable val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b) {
            val = (MyValue1Nullable)nullField;
        }
        if (val != (MyValue1Nullable)nullField) {
            return val.hashPrimitive();
        }
        return 42;
    }

    @Run(test = "test75")
    public void test75_verifier() {
        Asserts.assertEquals(test75(true), 42L);
        Asserts.assertEquals(test75(false), MyValue1Nullable.createWithFieldsInline(rI, rL).hashPrimitive());
    }

    @ForceInline
    public Object test76_helper(Object arg) {
        return arg;
    }

    // Test that arg does not block scalarization
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test76(boolean b1, boolean b2, MyValue1Nullable arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test76_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).x;
    }

    @Run(test = "test76")
    public void test76_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test76(true, false, arg, info.getTest()), arg.x);
        Asserts.assertEquals(test76(false, false, arg, info.getTest()), testValue1.x);
        if (!info.isWarmUp()) {
            try {
                Asserts.assertEquals(test76(false, false, arg, info.getTest()), testValue1.x);
                test76(true, false, (MyValue1Nullable)nullField, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            Asserts.assertEquals(test76(true, true, arg, info.getTest()), arg.x);
            Asserts.assertEquals(test76(false, true, arg, info.getTest()), testValue1.x);
        }
    }

    @DontInline
    public MyValue1Nullable test77_helper1() {
        return refField;
    }

    @ForceInline
    public Object test77_helper2() {
        return test77_helper1();
    }

    // Test that return does not block scalarization
    @Test
    @IR(failOn = {ALLOC, STORE})
    public long test77(boolean b1, boolean b2, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test77_helper2();
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test77")
    public void test77_verifier(RunInfo info) {
        refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test77(true, false, info.getTest()), refField.hash());
        Asserts.assertEquals(test77(false, false, info.getTest()), testValue1.hash());
        if (!info.isWarmUp()) {
            refField = (MyValue1Nullable)nullField;
            try {
                Asserts.assertEquals(test77(false, false, info.getTest()), testValue1.hash());
                test77(true, false, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            refField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
            Asserts.assertEquals(test77(true, true, info.getTest()), refField.hash());
            Asserts.assertEquals(test77(false, true, info.getTest()), testValue1.hash());
        }
    }

    @ForceInline
    public Object test78_helper(Object arg) {
        MyValue1Nullable tmp = (MyValue1Nullable)arg; // Result of cast is unused
        return arg;
    }

    // Test that scalarization enabled by cast is applied to parsing map
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test78(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test78_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).x;
    }

    @Run(test = "test78")
    public void test78_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test78(true, false, arg, info.getTest()), arg.x);
        Asserts.assertEquals(test78(false, false, arg, info.getTest()), testValue1.x);
        if (!info.isWarmUp()) {
            try {
                Asserts.assertEquals(test78(false, false, arg, info.getTest()), testValue1.x);
                test78(true, false, (MyValue1Nullable)nullField, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            Asserts.assertEquals(test78(true, true, arg, info.getTest()), arg.x);
            Asserts.assertEquals(test78(false, true, arg, info.getTest()), testValue1.x);
        }
    }

    @ForceInline
    public Object test79_helper(Object arg) {
        MyValue1Nullable tmp = (MyValue1Nullable)arg; // Result of cast is unused
        return arg;
    }

    // Same as test78 but with ClassCastException
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test79(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test79_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).x;
    }

    @Run(test = "test79")
    @Warmup(10000) // Make sure precise profile information is available
    public void test79_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test79(true, false, arg, info.getTest()), arg.x);
        Asserts.assertEquals(test79(false, false, arg, info.getTest()), testValue1.x);
        try {
            test79(true, false, 42, info.getTest());
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            try {
                Asserts.assertEquals(test79(false, false, arg, info.getTest()), testValue1.x);
                test79(true, false, (MyValue1Nullable)nullField, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            Asserts.assertEquals(test79(true, true, arg, info.getTest()), arg.x);
            Asserts.assertEquals(test79(false, true, arg, info.getTest()), testValue1.x);
        }
    }

    @ForceInline
    public Object test80_helper(Object arg) {
        MyValue1Nullable tmp = (MyValue1Nullable)arg; // Result of cast is unused
        return arg;
    }

    // Same as test78 but with ClassCastException and frequent NullPointerException
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test80(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test80_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).x;
    }

    @Run(test = "test80")
    @Warmup(10000) // Make sure precise profile information is available
    public void test80_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test80(true, false, arg, info.getTest()), arg.x);
        Asserts.assertEquals(test80(false, false, arg, info.getTest()), testValue1.x);
        try {
            test80(true, false, 42, info.getTest());
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
        try {
            Asserts.assertEquals(test80(false, false, arg, info.getTest()), testValue1.x);
            test80(true, false, (MyValue1Nullable)nullField, info.getTest());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test80(true, true, arg, info.getTest()), arg.x);
            Asserts.assertEquals(test80(false, true, arg, info.getTest()), testValue1.x);
        }
    }

    @ForceInline
    public Object test81_helper(Object arg) {
        MyValue1Nullable tmp = (MyValue1Nullable)arg; // Result of cast is unused
        return arg;
    }

    // Same as test78 but with cast
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test81(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test81_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).x;
    }

    @Run(test = "test81")
    public void test81_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test81(true, false, arg, info.getTest()), arg.x);
        Asserts.assertEquals(test81(false, false, arg, info.getTest()), testValue1.x);
        if (!info.isWarmUp()) {
            try {
                Asserts.assertEquals(test81(false, false, arg, info.getTest()), testValue1.x);
                test81(true, false, (MyValue1Nullable)nullField, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            Asserts.assertEquals(test81(true, true, arg, info.getTest()), arg.x);
            Asserts.assertEquals(test81(false, true, arg, info.getTest()), testValue1.x);
        }
    }

    @ForceInline
    public Object test82_helper(Object arg) {
        MyValue1Nullable tmp = (MyValue1Nullable)arg; // Result of cast is unused
        return arg;
    }

    // Same as test81 but with ClassCastException and hash() call
    @Test
    @IR(failOn = {ALLOC, STORE})
    public long test82(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test82_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test82")
    @Warmup(10000) // Make sure precise profile information is available
    public void test82_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test82(true, false, arg, info.getTest()), arg.hash());
        Asserts.assertEquals(test82(false, false, arg, info.getTest()), testValue1.hash());
        try {
            test82(true, false, 42, info.getTest());
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            try {
                Asserts.assertEquals(test82(false, false, arg, info.getTest()), testValue1.hash());
                test82(true, false, (MyValue1Nullable)nullField, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
            Asserts.assertEquals(test82(true, true, arg, info.getTest()), arg.hash());
            Asserts.assertEquals(test82(false, true, arg, info.getTest()), testValue1.hash());
        }
    }

    @ForceInline
    public Object test83_helper(Object arg) {
        MyValue1Nullable tmp = (MyValue1Nullable)arg; // Result of cast is unused
        return arg;
    }

    // Same as test81 but with ClassCastException and frequent NullPointerException
    @Test
    @IR(failOn = {ALLOC, STORE})
    public int test83(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test83_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).x;
    }

    @Run(test = "test83")
    @Warmup(10000) // Make sure precise profile information is available
    public void test83_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test83(true, false, arg, info.getTest()), arg.x);
        Asserts.assertEquals(test83(false, false, arg, info.getTest()), testValue1.x);
        try {
            test83(true, false, 42, info.getTest());
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
        try {
            Asserts.assertEquals(test83(false, false, arg, info.getTest()), testValue1.x);
            test83(true, false, (MyValue1Nullable)nullField, info.getTest());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test83(true, true, arg, info.getTest()), arg.x);
            Asserts.assertEquals(test83(false, true, arg, info.getTest()), testValue1.x);
        }
    }

    @ForceInline
    public Object test84_helper(Object arg) {
        return (MyValue1Nullable)arg;
    }

    // Same as test83 but result of cast is used and hash() is called
    @Test
    @IR(failOn = {ALLOC, STORE})
    public long test84(boolean b1, boolean b2, Object arg, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test84_helper(arg);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test84")
    @Warmup(10000) // Make sure precise profile information is available
    public void test84_verifier(RunInfo info) {
        MyValue1Nullable arg = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);
        Asserts.assertEquals(test84(true, false, arg, info.getTest()), arg.hash());
        Asserts.assertEquals(test84(false, false, arg, info.getTest()), testValue1.hash());
        try {
            test84(true, false, 42, info.getTest());
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
        try {
            Asserts.assertEquals(test84(false, false, arg, info.getTest()), testValue1.hash());
            test84(true, false, (MyValue1Nullable)nullField, info.getTest());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test84(true, true, arg, info.getTest()), arg.hash());
            Asserts.assertEquals(test84(false, true, arg, info.getTest()), testValue1.hash());
        }
    }

    // Test new merge path being added for exceptional control flow
    @Test
    @IR(failOn = {ALLOC})
    public MyValue1Nullable test85(MyValue1Nullable vt, Object obj) {
        try {
            vt = (MyValue1Nullable)obj;
            throw new RuntimeException("ClassCastException expected");
        } catch (ClassCastException e) {
            // Expected
        }
        return vt;
    }

    @Run(test = "test85")
    public void test85_verifier() {
        RuntimeException tmp = new RuntimeException("42"); // Make sure RuntimeException is loaded
        MyValue1Nullable vt = testValue1;
        MyValue1Nullable result = test85(vt, Integer.valueOf(rI));
        Asserts.assertEquals(result.hash(), vt.hash());
    }

    @ForceInline
    public Object test86_helper() {
        return constNullRefField;
    }

    // Test that constant null field does not block scalarization
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test86(boolean b1, boolean b2, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test86_helper();
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test86")
    public void test86_verifier(RunInfo info) {
        Asserts.assertEquals(test86(false, false, info.getTest()), testValue1.hash());
        try {
            test86(true, false, info.getTest());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test86(false, true, info.getTest()), testValue1.hash());
            try {
                test86(true, true, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    private static final Object constObjectValField = MyValue1Nullable.createWithFieldsInline(rI+1, rL+1);

    @ForceInline
    public Object test87_helper() {
        return constObjectValField;
    }

    // Test that constant object field with inline type content does not block scalarization
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test87(boolean b1, boolean b2, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test87_helper();
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test87")
    public void test87_verifier(RunInfo info) {
        Asserts.assertEquals(test87(true, false, info.getTest()), ((MyValue1Nullable)constObjectValField).hash());
        Asserts.assertEquals(test87(false, false, info.getTest()), testValue1.hash());
        if (!info.isWarmUp()) {
          Asserts.assertEquals(test87(true, false, info.getTest()), ((MyValue1Nullable)constObjectValField).hash());
          Asserts.assertEquals(test87(false, false, info.getTest()), testValue1.hash());
        }
    }

    @ForceInline
    public Object test88_helper() {
        return null;
    }

    // Test that constant null does not block scalarization
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test88(boolean b1, boolean b2, Method m) {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        if (b1) {
            val = test88_helper();
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test88")
    public void test88_verifier(RunInfo info) {
        Asserts.assertEquals(test88(false, false, info.getTest()), testValue1.hash());
        try {
            test88(true, false, info.getTest());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test88(false, true, info.getTest()), testValue1.hash());
            try {
                test88(true, true, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @ForceInline
    public Object test89_helper() {
        return null;
    }

    // Same as test88 but will trigger different order of PhiNode inputs
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test89(boolean b1, boolean b2, Method m) {
        Object val = test89_helper();
        if (b1) {
            val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        }
        if (b2) {
            // Uncommon trap
            TestFramework.deoptimize(m);
        }
        return ((MyValue1Nullable)val).hash();
    }

    @Run(test = "test89")
    public void test89_verifier(RunInfo info) {
        Asserts.assertEquals(test89(true, false, info.getTest()), testValue1.hash());
        try {
            test89(false, false, info.getTest());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        }
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test89(true, true, info.getTest()), testValue1.hash());
            try {
                test89(false, true, info.getTest());
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }

    @ForceInline
    public Object test90_helper(Object obj, int i) {
        if ((i % 2) == 0) {
            return MyValue1Nullable.createWithFieldsInline(i, i);
        }
        return obj;
    }

    // Test that phi nodes referencing themselves (loops) do not block scalarization
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test90() {
        Object val = MyValue1Nullable.createWithFieldsInline(rI, rL);
        for (int i = 0; i < 100; ++i) {
            val = test90_helper(val, i);
        }
        return ((MyValue1Nullable)val).hash();
    }

    private final long test90Result = test90();

    @Run(test = "test90")
    public void test90_verifier() {
        Asserts.assertEquals(test90(), test90Result);
    }

    @ForceInline
    public Object test91_helper(Object obj, int i) {
        if ((i % 2) == 0) {
            return MyValue1Nullable.createWithFieldsInline(i, i);
        }
        return obj;
    }

    // Test nested loops
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test91() {
        Object val = null;
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                for (int k = 0; k < 10; ++k) {
                    val = test91_helper(val, i + j + k);
                }
                val = test91_helper(val, i + j);
            }
            val = test91_helper(val, i);
        }
        return ((MyValue1Nullable)val).hash();
    }

    private final long test91Result = test91();

    @Run(test = "test91")
    public void test91_verifier() {
        Asserts.assertEquals(test92(), test92Result);
    }

    @ForceInline
    public Object test92_helper(Object obj, int i) {
        if ((i % 2) == 0) {
            return MyValue1Nullable.createWithFieldsInline(i, i);
        }
        return obj;
    }

    // Test loops with casts
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test92() {
        Object val = null;
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                for (int k = 0; k < 10; ++k) {
                    val = test92_helper(val, i + j + k);
                }
                if (val != null) {
                    val = test92_helper(val, i + j);
                }
            }
            val = test92_helper(val, i);
        }
        return ((MyValue1Nullable)val).hash();
    }

    private final long test92Result = test92();

    @Run(test = "test92")
    public void test92_verifier() {
        Asserts.assertEquals(test92(), test92Result);
    }

    @ForceInline
    public Object test93_helper(boolean b) {
        if (b) {
            return MyValue1Nullable.createWithFieldsInline(rI, rL);
        }
        return null;
    }

    // Test that CastPP does not block sclarization in safepoints
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test93(boolean b, Method m) {
        Object val = test93_helper(b);
        if (val != null) {
            // Uncommon trap
            TestFramework.deoptimize(m);
            return ((MyValue1Nullable)val).hash();
        }
        return 0;
    }

    @Run(test = "test93")
    public void test93_verifier(RunInfo info) {
        Asserts.assertEquals(test93(false, info.getTest()), 0L);
        if (!info.isWarmUp()) {
            Asserts.assertEquals(test93(true, info.getTest()), testValue1.hash());
        }
    }

    @ForceInline
    public Object test94_helper(Object obj, int i) {
        if ((i % 2) == 0) {
            return new MyValue1Wrapper(MyValue1Nullable.createWithFieldsInline(i, i));
        }
        return obj;
    }

    // Same as test90 but with wrapper
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test94() {
        Object val = new MyValue1Wrapper(MyValue1Nullable.createWithFieldsInline(rI, rL));
        for (int i = 0; i < 100; ++i) {
            val = test94_helper(val, i);
        }
        return ((MyValue1Wrapper.ref)val).vt.hash();
    }

    private final long test94Result = test94();

    @Run(test = "test94")
    public void test94_verifier() {
        Asserts.assertEquals(test94(), test94Result);
    }

    @ForceInline
    public Object test95_helper(Object obj, int i) {
        if ((i % 2) == 0) {
            return new MyValue1Wrapper(MyValue1Nullable.createWithFieldsInline(i, i));
        }
        return obj;
    }

    // Same as test91 but with wrapper
    @Test
    @IR(failOn = {ALLOC, LOAD, STORE})
    public long test95() {
        Object val = new MyValue1Wrapper((MyValue1Nullable)nullField);
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                for (int k = 0; k < 10; ++k) {
                    val = test95_helper(val, i + j + k);
                }
                val = test95_helper(val, i + j);
            }
            val = test95_helper(val, i);
        }
        return ((MyValue1Wrapper.ref)val).vt.hash();
    }

    private final long test95Result = test95();

    @Run(test = "test95")
    public void test95_verifier() {
        Asserts.assertEquals(test92(), test92Result);
    }

    static final class ObjectWrapper {
        public Object obj;

        @ForceInline
        public ObjectWrapper(Object obj) {
            this.obj = obj;
        }
    }

    // Test scalarization with phi referencing itself
    @Test
    @IR(applyIf = {"InlineTypePassFieldsAsArgs", "true"},
        failOn = {ALLOC, LOAD, STORE})
    @IR(applyIf = {"InlineTypePassFieldsAsArgs", "false"},
        failOn = {ALLOC, STORE})
    public long test96(MyValue1Nullable vt) {
        ObjectWrapper val = new ObjectWrapper(vt);
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                val.obj = val.obj;
            }
        }
        return ((MyValue1Nullable)val.obj).hash();
    }

    @Run(test = "test96")
    public void test96_verifier() {
        test96(testValue1);
        Asserts.assertEquals(test96(testValue1), testValue1.hash());
    }

    public static primitive class Test97C0 implements NullableFlattenable {
        int x = rI;
    }

    public static primitive class Test97C1 implements NullableFlattenable {
        Test97C0 field;
        public Test97C1(Test97C0 val) {
            field = val;
        }
    }
    
    public static primitive class Test97C2 implements NullableFlattenable {
        Test97C1 field;
        public Test97C2(Test97C1 val) {
            field = val;
        }
    }

    // Test merging .val and .ref in return
    @Test
    public Test97C1 test97(boolean b, Test97C2.val v1, Test97C2.ref v2) {
        if (b) {
            return v1.field;
        } else {
            return v2.field;
        }
    }

    @Run(test = "test97")
    public void test97_verifier(RunInfo info) {
        Object NULL = null;
        Test97C2 v = new Test97C2(new Test97C1(new Test97C0()));
        Asserts.assertEQ(test97(true, v, v), v.field);
        Asserts.assertEQ(test97(false, v, v), v.field);
        v = new Test97C2(new Test97C1((Test97C0)NULL));
        Asserts.assertEQ(test97(true, v, v), v.field);
        Asserts.assertEQ(test97(false, v, v), v.field);
        v = new Test97C2((Test97C1)NULL);
        Asserts.assertEQ(test97(true, v, v), v.field);
        Asserts.assertEQ(test97(false, v, v), v.field);
    }
}
