/*
 * Copyright (c) 2017, 2021, Oracle and/or its affiliates. All rights reserved.
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

import compiler.lib.ir_framework.DontInline;
import compiler.lib.ir_framework.ForceInline;

final primitive class MyValue2NullableInline implements NullableFlattenable {
    final double d;
    final long l;

    @ForceInline
    public MyValue2NullableInline(double d, long l) {
        this.d = d;
        this.l = l;
    }

    @ForceInline
    static MyValue2NullableInline setD(MyValue2NullableInline v, double d) {
        return new MyValue2NullableInline(d, v.l);
    }

    @ForceInline
    static MyValue2NullableInline setL(MyValue2NullableInline v, long l) {
        return new MyValue2NullableInline(v.d, l);
    }

    @ForceInline
    public static MyValue2NullableInline createDefault() {
        return MyValue2NullableInline.default;
    }

    @ForceInline
    public static MyValue2NullableInline createWithFieldsInline(double d, long l) {
        MyValue2NullableInline v = MyValue2NullableInline.createDefault();
        v = MyValue2NullableInline.setD(v, d);
        v = MyValue2NullableInline.setL(v, l);
        return v;
    }
}

public final primitive class MyValue2Nullable extends MyAbstractNullable implements NullableFlattenable {
    final int x;
    final byte y;
    final MyValue2NullableInline v;

    @ForceInline
    public MyValue2Nullable(int x, byte y, MyValue2NullableInline v) {
        this.x = x;
        this.y = y;
        this.v = v;
    }

    @ForceInline
    public static MyValue2Nullable createDefaultInline() {
        return MyValue2Nullable.default;
    }

    @ForceInline
    public static MyValue2Nullable createWithFieldsInline(int x, long y, double d) {
        MyValue2Nullable v = createDefaultInline();
        v = setX(v, x);
        v = setY(v, (byte)x);
        v = setV(v, MyValue2NullableInline.createWithFieldsInline(d, y));
        return v;
    }

    @ForceInline
    public static MyValue2Nullable createWithFieldsInline(int x, double d) {
        MyValue2Nullable v = createDefaultInline();
        v = setX(v, x);
        v = setY(v, (byte)x);
        v = setV(v, MyValue2NullableInline.createWithFieldsInline(d, InlineTypes.rL));
        return v;
    }

    @DontInline
    public static MyValue2Nullable createWithFieldsDontInline(int x, double d) {
        MyValue2Nullable v = createDefaultInline();
        v = setX(v, x);
        v = setY(v, (byte)x);
        v = setV(v, MyValue2NullableInline.createWithFieldsInline(d, InlineTypes.rL));
        return v;
    }

    @ForceInline
    public long hash() {
        return x + y + (long)v.d + v.l;
    }

    @DontInline
    public long hashInterpreted() {
        return x + y + (long)v.d + v.l;
    }

    @ForceInline
    public void print() {
        System.out.print("x=" + x + ", y=" + y + ", d=" + v.d + ", l=" + v.l);
    }

    @ForceInline
    static MyValue2Nullable setX(MyValue2Nullable v, int x) {
        return new MyValue2Nullable(x, v.y, v.v);
    }

    @ForceInline
    static MyValue2Nullable setY(MyValue2Nullable v, byte y) {
        return new MyValue2Nullable(v.x, y, v.v);
    }

    @ForceInline
    static MyValue2Nullable setV(MyValue2Nullable v, MyValue2NullableInline vi) {
        return new MyValue2Nullable(v.x, v.y, vi);
    }
}
