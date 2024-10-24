/*
 * Copyright (c) 2017, 2024, Red Hat, Inc. and/or its affiliates.
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
 *
 */

package sun.jvm.hotspot.gc.serial;

import java.io.*;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.gc.shared.*;
import sun.jvm.hotspot.memory.MemRegion;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class SerialHeap extends CollectedHeap {

  public SerialHeap(Address addr) {
    super(addr);
  }

  public CollectedHeapName kind() {
    return CollectedHeapName.SERIAL;
  }

  private static AddressField youngGenField;
  private static AddressField oldGenField;

  private static GenerationFactory genFactory;

  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static synchronized void initialize(TypeDataBase db) {
    Type type = db.lookupType("SerialHeap");

    youngGenField = type.getAddressField("_young_gen");
    oldGenField = type.getAddressField("_old_gen");

    genFactory = new GenerationFactory();
  }

  public DefNewGeneration youngGen() {
    return VMObjectFactory.newObject(DefNewGeneration.class, youngGenField.getValue(addr));
  }

  public TenuredGeneration oldGen() {
    return VMObjectFactory.newObject(TenuredGeneration.class, oldGenField.getValue(addr));
  }

  public boolean isIn(Address a) {
    return youngGen().isIn(a) || oldGen().isIn(a);
  }

  public long capacity() {
    long capacity = 0;
    capacity += youngGen().capacity();
    capacity += oldGen().capacity();
    return capacity;
  }

  public long used() {
    long used = 0;
    used += youngGen().used();
    used += oldGen().used();
    return used;
  }

  public void liveRegionsIterate(LiveRegionsClosure closure) {
    youngGen().liveRegionsIterate(closure);
    oldGen().liveRegionsIterate(closure);
  }

  public void printOn(PrintStream tty) {
    tty.println("SerialHeap:");

    tty.println("Young Generation - Invocations: " + youngGen().invocations());
    youngGen().printOn(tty);
    tty.println();

    tty.println("Old Generation - Invocations: " + oldGen().invocations());
    oldGen().printOn(tty);
    tty.println();
  }
}
