/*
 * Copyright (c) 2020, 2020, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_OOPS_VIRTUALFIELDINFO_HPP
#define SHARE_OOPS_VIRTUALFIELDINFO_HPP

#include "memory/metaspaceClosure.hpp"
#include "oops/access.hpp"
#include "oops/metadata.hpp"
#include "utilities/ostream.hpp"
#include "utilities/sizes.hpp"

class VirtualFieldInfo : public MetaspaceObj {
  friend class VMStructs;
  friend class JVMCIVMStructs;
  friend class InstanceKlass;
protected:
  Klass* _holder;
  int _local_index;
  jint _offset;
  BasicType _type;
  Klass* _type_klass;

 public:
  VirtualFieldInfo():
    _holder(NULL),
    _local_index(-1),
    _offset(-1),
    _type(T_ILLEGAL),
    _type_klass(NULL) { }

  Klass* holder() const { return _holder; }
  void set_holder(Klass* k) { _holder = k; }

  int local_index() const { return _local_index; }
  void set_local_index(int i) { _local_index = i; }

  int offset() const { return _offset; }
  void set_offset(int o) { _offset = o; }

  BasicType basic_type() const { return _type; }
  void set_basic_type(BasicType bt) { _type = bt; }

  Klass* type_klass() const { return _type_klass; }
  void set_type_klass(Klass* k) { _type_klass = k; }

  static ByteSize offset_offset() { return in_ByteSize(offset_of(VirtualFieldInfo, _offset)); }

  virtual void print_value_on(outputStream* st) const;
  static void print_all(Array<VirtualFieldInfo>* arr, outputStream* st);

  void metaspace_pointers_do(MetaspaceClosure* it);
  static int size() { return sizeof(VirtualFieldInfo) / wordSize; }
  MetaspaceObj::Type type() const { return VirtualFieldInfoType; }
  const char* internal_name() const { return "{VirtualFieldInfo}"; }
};

#endif // SHARE_OOPS_VIRTUALFIELDINFO_HPP